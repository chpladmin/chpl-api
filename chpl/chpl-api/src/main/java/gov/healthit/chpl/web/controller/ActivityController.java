package gov.healthit.chpl.web.controller;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.AnnouncementManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.manager.TestingLabManager;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "activity")
@RestController
@RequestMapping("/activity")
public class ActivityController {
    private static final Logger LOGGER = LogManager.getLogger(ActivityController.class);
    public static final int DEFAULT_MAX_ACTIVITY_RANGE_DAYS = 30;
    
    @Autowired 
    Environment env;
    @Autowired
    private ActivityManager activityManager;
    @Autowired
    private CertificationBodyManager acbManager;
    @Autowired
    private AnnouncementManager announcementManager;
    @Autowired
    private TestingLabManager atlManager;
    @Autowired
    private CertifiedProductManager cpManager;
    @Autowired
    private CertificationIdManager certificationIdManager;
    @Autowired
    private PendingCertifiedProductManager pcpManager;
    @Autowired
    private DeveloperManager developerManager;
    @Autowired
    private ProductManager productManager;
    @Autowired
    private ProductVersionManager versionManager;
    @Autowired
    private UserManager userManager;
    @Autowired
    private ChplProductNumberUtil chplProductNumberUtil;
    @Autowired
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;

    @Autowired private MessageSource messageSource;

    @ApiOperation(value = "Get auditable data for certification bodies.",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results. "
                    + "Only users calling this API with ROLE_ADMIN may set the 'showDeleted' flag to true. "
                    + "Those users are allowed to see activity for all certification bodies including that have been deleted. "
                    + "The default behavior is to show activity for non-deleted ACBs.")
    @RequestMapping(value = "/acbs", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForACBs(@RequestParam(required = true) Long start, 
            @RequestParam(required = true) Long end,
            @RequestParam(value = "showDeleted", required = false, defaultValue = "false") boolean showDeleted)
            throws JsonParseException, IOException, ValidationException {

        if(!Util.isUserRoleAdmin() && showDeleted) {
            LOGGER.warn("Non-admin user " + Util.getUsername() + " tried to see activity for deleted ACBs");
            throw new AccessDeniedException("Only Admins can see deleted ACB's");
        } else {
            if(start == null || end == null) {
                throw new IllegalArgumentException(messageSource.getMessage(
                        new DefaultMessageSourceResolvable("activity.missingStartEnd"),
                        LocaleContextHolder.getLocale()));
            }
            Date startDate = new Date(start);
            Date endDate = new Date(end);
            validateActivityDates(start, end);
            return activityManager.getAcbActivity(showDeleted, startDate, endDate);
        }
    }

    @ApiOperation(value = "Get auditable data for a specific certification body.",
            notes = "Only users calling this API with ROLE_ADMIN may set the 'showDeleted' flag to true and should "
                    + "do so if the certification body specified in the path has been deleted. ")
    @RequestMapping(value = "/acbs/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForACBById(@PathVariable("id") Long id,
            @RequestParam(value = "showDeleted", required = false, defaultValue = "false") boolean showDeleted)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        acbManager.getById(id, showDeleted); // throws 404 if ACB doesn't exist

        if (!Util.isUserRoleAdmin() && showDeleted) {
            LOGGER.warn("Non-admin user " + Util.getUsername() + " tried to see activity for deleted ACB " + id);
            throw new AccessDeniedException("Only Admins can see deleted ACB's");
        } else {
            return activityManager.getAcbActivity(showDeleted, id, new Date(0), new Date());
        }
    }

    @ApiOperation(value = "Get auditable data for all announcements",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date "
                    + "range of the results. Anonymous users will only receive activity for public "
                    + "announcements.")
    @RequestMapping(value = "/announcements", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForAnnoucements(@RequestParam Long start,
            @RequestParam Long end) throws JsonParseException, IOException, ValidationException {
        if(start == null || end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartEnd"),
                    LocaleContextHolder.getLocale()));        
        }
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDates(start, end);
        return getActivityEventsForAnnouncements(startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data for a specific announcement",
            notes = "Anonymous users are only allowed to see activity for public announcements.")
    @RequestMapping(value = "/announcements/{id}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForAnnouncementById(@PathVariable("id") Long id)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        announcementManager.getById(id, true); // throws 404 if bad id
        return getActivityEventsForAnnouncement(id, new Date(0), new Date());
    }

    @ApiOperation(value = "Get auditable data for testing labs.",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results. "
                    + "Only users calling this API with ROLE_ADMIN may set the 'showDeleted' flag to true. "
                    + "Those users are allowed to see activity for all testing labs including that have been deleted. "
                    + "The default behavior is to show activity for non-deleted ATLs.")
    @RequestMapping(value = "/atls", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityforATLs(@RequestParam Long start, @RequestParam Long end,
            @RequestParam(value = "showDeleted", required = false, defaultValue = "false") boolean showDeleted)
            throws JsonParseException, IOException, ValidationException {

        if (!Util.isUserRoleAdmin() && showDeleted) {
            LOGGER.warn("Non-admin user " + Util.getUsername() + " tried to see activity for deleted ATLs");
            throw new AccessDeniedException("Only Admins can see deleted ATL's");
        } else {
            if(start == null || end == null) {
                throw new IllegalArgumentException(messageSource.getMessage(
                        new DefaultMessageSourceResolvable("activity.missingStartEnd"),
                        LocaleContextHolder.getLocale()));
            }
            Date startDate = new Date(start);
            Date endDate = new Date(end);
            validateActivityDates(start, end);
            return activityManager.getAtlActivity(showDeleted, startDate, endDate);
        }
    }

    @ApiOperation(value = "Get auditable data for a specific testing lab.",
            notes = "Only users calling this API with ROLE_ADMIN may set the 'showDeleted' flag to true and should "
                    + "do so if the testing lab specified in the path has been deleted. ")
    @RequestMapping(value = "/atls/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForATLById(@PathVariable("id") Long id,
            @RequestParam(value = "showDeleted", required = false, defaultValue = "false") boolean showDeleted)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        atlManager.getById(id, showDeleted); // throws 404 if bad id

        if (!Util.isUserRoleAdmin() && showDeleted) {
            LOGGER.warn("Non-admin user " + Util.getUsername() + " tried to see activity for deleted ATL " + id);
            throw new AccessDeniedException("Only Admins can see deleted ATL's");
        } else {
            return activityManager.getAtlActivity(showDeleted, id, new Date(0), new Date());
        }
    }

    @ApiOperation(value = "Get auditable data for all API keys",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/api_keys", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForApiKeys(@RequestParam Long start,
            @RequestParam Long end) throws JsonParseException, IOException, ValidationException {
        if(start == null || end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartEnd"),
                    LocaleContextHolder.getLocale()));
        }
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDates(start, end);
        return activityManager.getApiKeyActivity(startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data for certified products",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/certified_products", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForCertifiedProducts(@RequestParam Long start,
            @RequestParam Long end) throws JsonParseException, IOException, ValidationException {
        if(start == null || end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartEnd"),
                    LocaleContextHolder.getLocale()));
        }
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDates(start, end);
        return getActivityEventsForCertifiedProducts(startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data for corrective action plans",
            notes = "Deprecated. Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/corrective_action_plans", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @Deprecated
    public List<ActivityEvent> activityForCorrectiveActionPlans(@RequestParam Long start,
            @RequestParam Long end) throws JsonParseException, IOException, ValidationException {
        if(start == null || end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartEnd"),
                    LocaleContextHolder.getLocale()));
        }
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDates(start, end);
        return getActivityEventsForCorrectiveActionPlans(startDate, endDate);
    }
    
    @ApiOperation(value = "Get auditable data for a specific certified product")
    @RequestMapping(value = "/certified_products/{id:^-?\\d+$}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForCertifiedProductById(@PathVariable("id") Long id)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        cpManager.getById(id); // throws 404 if bad id
        return getActivityEventsForCertifiedProductsByIdAndDateRange(id, new Date(0), new Date());
    }

    @ApiOperation(value = "Get auditable data for a specific certified product based on CHPL Product Number.",
            notes = "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode} " 
                    + "represents a valid CHPL Product Number.  A valid call to this service would look like "
                    + "activity/certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD.")
    @RequestMapping(value = "/certified_products/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode}", 
                    method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForCertifiedProductByChplProductNumber(
            @PathVariable("year") final String year,
            @PathVariable("testingLab") final String testingLab,
            @PathVariable("certBody") final String certBody,
            @PathVariable("vendorCode") final String vendorCode,
            @PathVariable("productCode") final String productCode,
            @PathVariable("versionCode") final String versionCode,
            @PathVariable("icsCode") final String icsCode,
            @PathVariable("addlSoftwareCode") final String addlSoftwareCode,
            @PathVariable("certDateCode") final String certDateCode) 
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        
        String chplProductNumber = 
                chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode, 
                        versionCode, icsCode, addlSoftwareCode, certDateCode);
        
        List<CertifiedProductDetailsDTO> dtos =
                certifiedProductSearchResultDAO.getByChplProductNumber(chplProductNumber);
        if (dtos.size() == 0) {
            throw new EntityRetrievalException("Could not retrieve CertifiedProductSearchDetails.");
        }
        return getActivityEventsForCertifiedProductsByIdAndDateRange(dtos.get(0).getId(), new Date(0), new Date());
    }

    @ApiOperation(value = "Get auditable data for a specific certified product based on a legacy CHPL Product Number.",
            notes = "{chplPrefix}-{identifier} represents a valid CHPL Product Number.  "
                    + "A valid call to this service "
                    + "would look like activity/certified_products/CHP-999999.")
    @RequestMapping(value = "/certified_products/{chplPrefix}-{identifier}", 
                    method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForCertifiedProductByChplProductNumber(
            @PathVariable("chplPrefix") final String chplPrefix,
            @PathVariable("identifier") final String identifier) 
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);

        List<CertifiedProductDetailsDTO> dtos =
                certifiedProductSearchResultDAO.getByChplProductNumber(chplProductNumber);
        if (dtos.size() == 0) {
            throw new EntityRetrievalException("Could not retrieve CertifiedProductSearchDetails.");
        }
        return getActivityEventsForCertifiedProductsByIdAndDateRange(dtos.get(0).getId(), new Date(0), new Date());
    }

    @ApiOperation(value = "Get auditable data for all certifications",
            notes = "Deprecated. Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/certifications", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @Deprecated
    public List<ActivityEvent> activityForCertifications(@RequestParam Long start,
            @RequestParam Long end) throws JsonParseException, IOException, ValidationException {
        if(start == null || end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartEnd"),
                    LocaleContextHolder.getLocale()));
        }
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDates(start, end);
        return getActivityEventsForCertifications(startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data for a specific certification",
            notes = "Deprecated.")
    @RequestMapping(value = "/certifications/{id}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @Deprecated
    public List<ActivityEvent> activityForCertificationById(@PathVariable("id") Long id)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        certificationIdManager.getById(id); // throws 404 if bad id
        return getActivityEventsForCertifications(id, new Date(0), new Date());
    }

    @ApiOperation(value = "Get auditable data for all pending certified products",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/pending_certified_products", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForPendingCertifiedProducts(@RequestParam Long start,
            @RequestParam Long end) throws JsonParseException, IOException, ValidationException {
        if(start == null || end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartEnd"),
                    LocaleContextHolder.getLocale()));
        }
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDates(start, end);
        return activityManager.getPendingListingActivity(startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data for a specific pending certified product")
    @RequestMapping(value = "/pending_certified_products/{id}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForPendingCertifiedProductById(@PathVariable("id") Long id)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        List<CertificationBodyDTO> acbs = acbManager.getAllForUser(false);
        pcpManager.getById(acbs, id); // returns 404 if bad id
        return activityManager.getPendingListingActivity(id, new Date(0), new Date());
    }

    @ApiOperation(value = "Get auditable data for all products",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/products", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForProducts(@RequestParam Long start,
            @RequestParam Long end) throws JsonParseException, IOException, ValidationException {
        if(start == null || end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartEnd"),
                    LocaleContextHolder.getLocale()));
        }
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDates(start, end);
        return getActivityEventsForProducts(startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data for a specific product")
    @RequestMapping(value = "/products/{id}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForProducts(@PathVariable("id") Long id)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        productManager.getById(id); // returns 404 if bad id
        return getActivityEventsForProducts(id, new Date(0), new Date());
    }

    @ApiOperation(value = "Get auditable data for all versions",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/versions", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForVersions(@RequestParam Long start,
            @RequestParam Long end) throws JsonParseException, IOException, ValidationException {
        if(start == null || end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartEnd"),
                    LocaleContextHolder.getLocale()));
        }
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDates(start, end);
        return getActivityEventsForVersions(startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data for a specific version")
    @RequestMapping(value = "/versions/{id}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForVersions(@PathVariable("id") Long id)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        versionManager.getById(id); // returns 404 if bad id
        return getActivityEventsForVersions(id, new Date(0), new Date());
    }

    @ApiOperation(value = "Get auditable data about all CHPL user accounts",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/users", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForUsers(@RequestParam Long start,
            @RequestParam Long end) throws JsonParseException, IOException, ValidationException {
        if(start == null || end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartEnd"),
                    LocaleContextHolder.getLocale()));
        }
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDates(start, end);
        return activityManager.getUserActivity(startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data about a specific CHPL user account.")
    @RequestMapping(value = "/users/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForUsers(@PathVariable("id") Long id)
            throws JsonParseException, IOException, UserRetrievalException, ValidationException {
        userManager.getById(id); // throws 404 if bad id
        return activityManager.getUserActivity(id, new Date(0), new Date());
    }

    @ApiOperation(value = "Get auditable data about all developers",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/developers", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForDevelopers(@RequestParam Long start,
            @RequestParam Long end) throws JsonParseException, IOException, ValidationException {
        if(start == null || end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartEnd"),
                    LocaleContextHolder.getLocale()));
        }
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDates(start, end);
        return getActivityEventsForDevelopers(startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data for a specific developer.")
    @RequestMapping(value = "/developers/{id}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityForDeveloperById(@PathVariable("id") Long id)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        developerManager.getById(id); // returns 404 if bad id
        return getActivityEventsForDevelopers(id, new Date(0), new Date());
    }

    @ApiOperation(value = "Track the actions of all users in the system",
            notes = "The authenticated user calling this method must have ROLE_ADMIN or ROLE_ONC_STAFF. "
                    + "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/user_activities", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<UserActivity> activityByUser(@RequestParam Long start,
            @RequestParam Long end)
            throws JsonParseException, IOException, UserRetrievalException, ValidationException {
        if(start == null || end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartEnd"),
                    LocaleContextHolder.getLocale()));
        }
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDates(start, end);
        return activityManager.getActivityByUserInDateRange(startDate, endDate);
    }

    @ApiOperation(value = "Track the actions of a specific user in the system",
            notes = "The authenticated user calling this method must have ROLE_ADMIN or ROLE_ONC_STAFF.")
    @RequestMapping(value = "/user_activities/{id}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityEvent> activityByUser(@PathVariable("id") Long id)
            throws JsonParseException, IOException, UserRetrievalException, ValidationException {
        userManager.getById(id); // throws 404 if bad id
        return activityManager.getActivityForUserInDateRange(id, new Date(0), new Date());
    }

    private List<ActivityEvent> getActivityEventsForCertifications(Long id, Date startDate, Date endDate)
            throws JsonParseException, IOException {

        List<ActivityEvent> events = null;
        ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
        events = getActivityEventsForObject(concept, id, startDate, endDate);
        return events;
    }

    private List<ActivityEvent> getActivityEventsForCertifiedProductsByIdAndDateRange(Long id, Date startDate, Date endDate)
            throws JsonParseException, IOException {

        List<ActivityEvent> events = null;
        ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
        events = getActivityEventsForObject(concept, id, startDate, endDate);
        return events;
    }

    private List<ActivityEvent> getActivityEventsForProducts(Long id, Date startDate, Date endDate)
            throws JsonParseException, IOException {

        List<ActivityEvent> events = null;
        ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
        events = getActivityEventsForObject(concept, id, startDate, endDate);
        return events;
    }

    private List<ActivityEvent> getActivityEventsForDevelopers(Long id, Date startDate, Date endDate)
            throws JsonParseException, IOException {

        List<ActivityEvent> events = null;
        ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER;
        events = getActivityEventsForObject(concept, id, startDate, endDate);
        return events;
    }

    private List<ActivityEvent> getActivityEventsForVersions(Long id, Date startDate, Date endDate)
            throws JsonParseException, IOException {

        List<ActivityEvent> events = null;
        ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
        events = getActivityEventsForObject(concept, id, startDate, endDate);
        return events;

    }

    private List<ActivityEvent> getActivityEventsForCertifications(Date startDate, Date endDate)
            throws JsonParseException, IOException {
        LOGGER.info("User " + Util.getUsername() + " requested certification activity between " + startDate + " and "
                + endDate);

        List<ActivityEvent> events = null;
        ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
        events = getActivityEventsForConcept(concept, startDate, endDate);

        return events;
    }

    private List<ActivityEvent> getActivityEventsForCertifiedProducts(Date startDate, Date endDate)
            throws JsonParseException, IOException {
        LOGGER.info("User " + Util.getUsername() + " requested certified product activity between " + startDate
                + " and " + endDate);

        List<ActivityEvent> events = null;
        ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
        events = getActivityEventsForConcept(concept, startDate, endDate);

        return events;
    }

    private List<ActivityEvent> getActivityEventsForCorrectiveActionPlans(Date startDate, Date endDate)
            throws JsonParseException, IOException {
        LOGGER.info("User " + Util.getUsername() + " requested corrective action plan activity between " + startDate
                + " and " + endDate);

        List<ActivityEvent> events = null;
        events = getActivityEventsForConcept(ActivityConcept.ACTIVITY_CONCEPT_CORRECTIVE_ACTION_PLAN, startDate, endDate);

        return events;
    }

    private List<ActivityEvent> getActivityEventsForProducts(Date startDate, Date endDate)
            throws JsonParseException, IOException {
        LOGGER.info(
                "User " + Util.getUsername() + " requested product activity between " + startDate + " and " + endDate);

        List<ActivityEvent> events = null;
        ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
        events = getActivityEventsForConcept(concept, startDate, endDate);

        return events;
    }

    private List<ActivityEvent> getActivityEventsForDevelopers(Date startDate, Date endDate)
            throws JsonParseException, IOException {
        LOGGER.info("User " + Util.getUsername() + " requested developer activity between " + startDate + " and "
                + endDate);

        List<ActivityEvent> events = null;
        ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER;
        events = getActivityEventsForConcept(concept, startDate, endDate);

        return events;
    }

    private List<ActivityEvent> getActivityEventsForVersions(Date startDate, Date endDate)
            throws JsonParseException, IOException {
        LOGGER.info(
                "User " + Util.getUsername() + " requested version activity between " + startDate + " and " + endDate);

        List<ActivityEvent> events = null;
        ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
        events = getActivityEventsForConcept(concept, startDate, endDate);
        return events;

    }

    private List<ActivityEvent> getActivityEventsForAnnouncements(Date startDate, Date endDate)
            throws JsonParseException, IOException {
        if(Util.getCurrentUser() == null) {
            LOGGER.info("Anonymous user requested public announcement activity between " + startDate + " and "
                    + endDate);
            return activityManager.getPublicAnnouncementActivity(startDate, endDate);
        }

        LOGGER.info("User " + Util.getUsername() + " requested all announcement activity between " + startDate + " and "
                + endDate);
        List<ActivityEvent> events = null;
        ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT;
        events = getActivityEventsForConcept(concept, startDate, endDate);

        return events;
    }

    private List<ActivityEvent> getActivityEventsForAnnouncement(Long id, Date startDate, Date endDate)
            throws JsonParseException, IOException {
        if(Util.getCurrentUser() == null) {
            LOGGER.info("Anonymous user requested public announcement activity "
                    + "for announcement id " + id + " between " + startDate + " and "
                    + endDate);
            return activityManager.getPublicAnnouncementActivity(id, startDate, endDate);
        }

        LOGGER.info("User " + Util.getUsername() + " requested all announcement activity "
                + "for announcement id " + id + " between " + startDate + " and "
                + endDate);

        List<ActivityEvent> events = null;
        ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT;
        events = getActivityEventsForObject(concept, id, startDate, endDate);

        return events;
    }

    private List<ActivityEvent> getActivityEventsForConcept(ActivityConcept concept,
            Date startDate, Date endDate) throws JsonParseException, IOException {
        return activityManager.getActivityForConcept(concept, startDate, endDate);
    }
    
    private List<ActivityEvent> getActivityEventsForObject(ActivityConcept concept, Long objectId,
            Date startDate, Date endDate) throws JsonParseException, IOException {
        return activityManager.getActivityForObject(concept, objectId, startDate, endDate);
    }

    private void validateActivityDates(Long startDate, Long endDate) throws IllegalArgumentException {
        LocalDate startDateUtc = 
                Instant.ofEpochMilli(startDate).atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate endDateUtc = 
                Instant.ofEpochMilli(endDate).atZone(ZoneId.of("UTC")).toLocalDate();

        if(startDateUtc.isAfter(endDateUtc)) {
            throw new IllegalArgumentException("Cannot search for activity with the start date after the end date");
        }

        Integer maxActivityRangeInDays = Integer.getInteger(
                env.getProperty("maxActivityRangeInDays"), DEFAULT_MAX_ACTIVITY_RANGE_DAYS);
        endDateUtc = endDateUtc.minusDays(maxActivityRangeInDays);
        if(startDateUtc.isBefore(endDateUtc)) {
            throw new IllegalArgumentException(
                    "Cannot search for activity with a date range more than " + maxActivityRangeInDays + " days.");
        }
    }

    
}
