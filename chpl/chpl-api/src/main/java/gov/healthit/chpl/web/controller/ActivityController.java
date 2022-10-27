package gov.healthit.chpl.web.controller;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;

import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityDetails;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ActivityMetadataPage;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.ActivityMetadataManager;
import gov.healthit.chpl.manager.ActivityPagedMetadataManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "activity", description = "Find historical activity about objects in the CHPL.")
@RestController
@Log4j2
@RequestMapping("/activity")
public class ActivityController {
    public static final int DEFAULT_MAX_ACTIVITY_RANGE_DAYS = 30;

    private ActivityManager activityManager;
    private ActivityMetadataManager activityMetadataManager;
    private ActivityPagedMetadataManager pagedMetadataManager;
    private CertifiedProductManager cpManager;
    private DeveloperManager developerManager;
    private ProductManager productManager;
    private ProductVersionManager versionManager;
    private ChplProductNumberUtil chplProductNumberUtil;
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    private ErrorMessageUtil msgUtil;

    @Value("${maxActivityRangeInDays}")
    private Integer maxActivityRangeInDays;

    @SuppressWarnings({
            "checkstyle:parameternumber"
    })
    @Autowired
    public ActivityController(ActivityManager activityManager, ActivityMetadataManager activityMetadataManager,
            ActivityPagedMetadataManager pagedMetadataManager, CertifiedProductManager cpManager,
            DeveloperManager developerManager, ProductManager productManager, ProductVersionManager versionManager,
            ChplProductNumberUtil chplProductNumberUtil, CertifiedProductSearchResultDAO certifiedProductSearchResultDAO,
            ErrorMessageUtil msgUtil) {
        this.activityManager = activityManager;
        this.activityMetadataManager = activityMetadataManager;
        this.pagedMetadataManager = pagedMetadataManager;
        this.cpManager = cpManager;
        this.developerManager = developerManager;
        this.productManager = productManager;
        this.versionManager = versionManager;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.certifiedProductSearchResultDAO = certifiedProductSearchResultDAO;
        this.msgUtil = msgUtil;
        if (maxActivityRangeInDays == null) {
            maxActivityRangeInDays = DEFAULT_MAX_ACTIVITY_RANGE_DAYS;
        }
    }

    @Operation(summary = "Get detailed audit data for a specific activity event.",
            description = "Security Restrictions: ROLE_ADMIN and ROLE_ONC may view any activity event. "
                    + "Other users may be restricted in what they can see.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{id:^-?\\d+$}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ActivityDetails activityById(@PathVariable("id") final Long id)
            throws EntityRetrievalException, JsonParseException, IOException, ValidationException {
        ActivityDetails details = activityManager.getActivityById(id);
        return details;
    }

    @Operation(summary = "Get metadata about auditable records in the system for listings.",
            description = "All parameters are optional and will default to the first page of listing activity "
                    + "with a page size of the maximum allowed. Page number is 0-based. Activities will be returned "
                    + "with the most recent activity first.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/metadata/listings", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public ActivityMetadataPage metadataForListings(@RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end, @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) throws JsonParseException, IOException, ValidationException {
        return pagedMetadataManager.getActivityMetadataByConcept(
                ActivityConcept.CERTIFIED_PRODUCT, start, end, pageNum, pageSize);
    }

    @Operation(summary = "Get metadata about auditable records in the system for a specific listing.",
            description = "A start and end date may optionally be provided to limit activity results.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/metadata/listings/{id:^-?\\d+$}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForListingById(@PathVariable("id") Long id,
            @RequestParam(required = false) Long start, @RequestParam(required = false) Long end)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        cpManager.getById(id); // throws 404 if bad id

        return getActivityForListing(start, end, id);
    }

    @SuppressWarnings({
            "checkstyle:parameternumber"
    })
    @Operation(summary = "Get metadata about auditable records in the system for a specific listing given its "
            + "new-style CHPL product number.",
            description = "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode} "
                    + "represents a valid CHPL Product Number.  A valid call to this service would look like "
                    + "activity/certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD. "
                    + "A start and end date may optionally be provided to limit activity results.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/metadata/listings/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode}",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForListingByChplProductNumber(
            @PathVariable("year") final String year,
            @PathVariable("testingLab") final String testingLab,
            @PathVariable("certBody") final String certBody,
            @PathVariable("vendorCode") final String vendorCode,
            @PathVariable("productCode") final String productCode,
            @PathVariable("versionCode") final String versionCode,
            @PathVariable("icsCode") final String icsCode,
            @PathVariable("addlSoftwareCode") final String addlSoftwareCode,
            @PathVariable("certDateCode") final String certDateCode,
            @RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                versionCode, icsCode, addlSoftwareCode, certDateCode);

        List<CertifiedProductDetailsDTO> dtos = certifiedProductSearchResultDAO.getByChplProductNumber(chplProductNumber);
        if (dtos.size() == 0) {
            throw new EntityRetrievalException("Could not retrieve CertifiedProductSearchDetails.");
        }

        return getActivityForListing(start, end, dtos.get(0).getId());
    }

    @Operation(summary = "Get metadata about auditable records in the system for a specific listing given its "
            + "legacy CHPL Number.",
            description = "{chplPrefix}-{identifier} represents a valid CHPL Product Number.  "
                    + "A valid call to this service "
                    + "would look like activity/certified_products/CHP-999999. "
                    + "A start and end date may optionally be provided to limit activity results.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/metadata/listings/{chplPrefix}-{identifier}",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForListingByChplProductNumber(
            @PathVariable("chplPrefix") final String chplPrefix,
            @PathVariable("identifier") final String identifier,
            @RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);

        List<CertifiedProductDetailsDTO> dtos = certifiedProductSearchResultDAO.getByChplProductNumber(chplProductNumber);
        if (dtos.size() == 0) {
            throw new EntityRetrievalException("Could not retrieve CertifiedProductSearchDetails.");
        }
        return getActivityForListing(start, end, dtos.get(0).getId());
    }

    private List<ActivityMetadata> getActivityForListing(Long start, Long end, Long certifiedProductId)
        throws IOException {
        // if one of start of end is provided then the other must also be
        // provided.
        // if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingStartHasEnd"));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingEndHasStart"));
        }

        return activityMetadataManager.getActivityMetadataByObject(
                certifiedProductId, ActivityConcept.CERTIFIED_PRODUCT, startDate, endDate);
    }

    @Operation(summary = "Get metadata about auditable records in the system for developers.",
            description = "All parameters are optional and will default to the first page of listing activity "
                    + "with a page size of the maximum allowed. Page number is 0-based. Activities will be returned "
                    + "with the most recent activity first.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/metadata/developers", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public ActivityMetadataPage metadataForDevelopers(@RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end, @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) throws JsonParseException, IOException, ValidationException {
        return pagedMetadataManager.getActivityMetadataByConcept(
                ActivityConcept.DEVELOPER, start, end, pageNum, pageSize);
    }

    @Operation(summary = "Get metadata about auditable records in the system for a specific developer.",
            description = "A start or end date may optionally be provided to limit activity results.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/metadata/developers/{id:^-?\\d+$}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForDeveloperById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        developerManager.getById(id, true); // allows getting activity for
                                            // deleted developer

        // if one of start of end is provided then the other must also be
        // provided.
        // if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start != null) {
            startDate = new Date(start);
        } else if (end != null) {
            endDate = new Date(end);
        }

        return activityMetadataManager.getActivityMetadataByObject(
                id, ActivityConcept.DEVELOPER, startDate, endDate);
    }

    @Operation(summary = "Get metadata about auditable records in the system for products.",
            description = "All parameters are optional and will default to the first page of product activity "
                    + "with a page size of the maximum allowed. Page number is 0-based. Activities will be returned "
                    + "with the most recent activity first.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/metadata/products", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public ActivityMetadataPage metadataForProducts(@RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end, @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) throws JsonParseException, IOException, ValidationException {
        return pagedMetadataManager.getActivityMetadataByConcept(
                ActivityConcept.PRODUCT, start, end, pageNum, pageSize);
    }

    @Operation(summary = "Get metadata about auditable records in the system for a specific product.",
            description = "A start or end date may optionally be provided to limit activity results.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/metadata/products/{id:^-?\\d+$}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForProductById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        productManager.getById(id, true); // allows getting activity for deleted
                                          // product

        // if one of start of end is provided then the other must also be
        // provided.
        // if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start != null) {
            startDate = new Date(start);
        } else if (end != null) {
            endDate = new Date(end);
        }

        return activityMetadataManager.getActivityMetadataByObject(
                id, ActivityConcept.PRODUCT, startDate, endDate);
    }

    @Operation(summary = "Get metadata about auditable records in the system for versions.",
            description = "All parameters are optional and will default to the first page of version activity "
                    + "with a page size of the maximum allowed. Page number is 0-based. Activities will be returned "
                    + "with the most recent activity first.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/metadata/versions", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public ActivityMetadataPage metadataForVersions(@RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end, @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) throws JsonParseException, IOException, ValidationException {
        return pagedMetadataManager.getActivityMetadataByConcept(
                ActivityConcept.VERSION, start, end, pageNum, pageSize);
    }

    @Operation(summary = "Get metadata about auditable records in the system for a specific version.",
            description = "A start or end date may optionally be provided to limit activity results.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/metadata/versions/{id:^-?\\d+$}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForVersionById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        versionManager.getById(id, true); // allows getting activity for deleted
                                          // version

        // if one of start of end is provided then the other must also be
        // provided.
        // if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start != null) {
            startDate = new Date(start);
        } else if (end != null) {
            endDate = new Date(end);
        }

        return activityMetadataManager.getActivityMetadataByObject(
                id, ActivityConcept.VERSION, startDate, endDate);
    }

    @Operation(summary = "Get metadata about auditable records in the system for certification bodies.",
            description = "All parameters are optional and will default to the first page of ONC-ACB activity "
                    + "with a page size of the maximum allowed. Page number is 0-based. Activities will be returned "
                    + "with the most recent activity first.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/metadata/acbs", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public ActivityMetadataPage metadataForAcbs(@RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end, @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) throws JsonParseException, IOException, ValidationException {
        return pagedMetadataManager.getCertificationBodyActivityMetadata(start, end, pageNum, pageSize);
    }

    @Operation(summary = "Get metadata about auditable records in the system for a specific certification body.",
            description = "A start and end date may optionally be provided to limit activity results.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/metadata/acbs/{id:^-?\\d+$}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForAcbById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        // if one of start of end is provided then the other must also be
        // provided.
        // if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingStartHasEnd"));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingEndHasStart"));
        }

        return activityMetadataManager.getCertificationBodyActivityMetadata(
                id, startDate, endDate);
    }

    @Operation(summary = "Get metadata about auditable records in the system for testing labs.",
            description = "All parameters are optional and will default to the first page of ONC-ATL activity "
                    + "with a page size of the maximum allowed. Page number is 0-based. Activities will be returned "
                    + "with the most recent activity first.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/metadata/atls", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public ActivityMetadataPage metadataForAtls(@RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end, @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) throws JsonParseException, IOException, ValidationException {
        return pagedMetadataManager.getTestingLabActivityMetadata(start, end, pageNum, pageSize);
    }

    @Operation(summary = "Get metadata about auditable records in the system for a specific testing lab.",
            description = "A start and end date may optionally be provided to limit activity results.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/metadata/atls/{id:^-?\\d+$}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForAtlById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
            throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        // if one of start of end is provided then the other must also be
        // provided.
        // if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingStartHasEnd"));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingEndHasStart"));
        }

        return activityMetadataManager.getTestingLabActivityMetadata(
                id, startDate, endDate);
    }

    @Operation(summary = "Get metadata about auditable records in the system for users.",
            description = "All parameters are optional and will default to the first page of user activity "
                    + "with a page size of the maximum allowed. Page number is 0-based. Activities will be returned "
                    + "with the most recent activity first.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/metadata/users", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ActivityMetadataPage metadataForUsers(@RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end, @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) throws JsonParseException, IOException, ValidationException {
        return pagedMetadataManager.getUserMaintenanceActivityMetadata(start, end, pageNum, pageSize);
    }

    @Operation(summary = "Get metadata about auditable records in the system for announcements.",
            description = "All parameters are optional and will default to the first page of announcement activity "
                    + "with a page size of the maximum allowed. Page number is 0-based. Activities will be returned "
                    + "with the most recent activity first. "
                    + "Security Restrictions: Anonymous users are only allowed to see activity for public "
                    + "announcements. All other roles can see private and public announcements.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/metadata/announcements", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public ActivityMetadataPage metadataForAnnouncements(@RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end, @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) throws JsonParseException, IOException, ValidationException {
        return pagedMetadataManager.getAnnouncementActivityMetadata(start, end, pageNum, pageSize);
    }

    @Operation(summary = "Get metadata about auditable records in the system for complaints.",
            description = "Users must specify 'start' and 'end' parameters to restrict the date range of the results."
                    + "Security Restrictions: ROLE_ADMIN and ROLE_ONC may see activity for all complaints.  "
                    + "ROLE_ACB can see activity for their own ACBs.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/metadata/complaints", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForComplaints(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return activityMetadataManager.getComplaintActivityMetadata(startDate, endDate);
    }

    @Operation(summary = "Get metadata about auditable records in the system for quarterly reports.",
            description = "Users must specify 'start' and 'end' parameters to restrict the date range of the results."
                    + "Security Restrictions: ROLE_ADMIN and ROLE_ONC may see activity for all quarterly reports.  "
                    + "ROLE_ACB can see activity for their own ACBs.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/metadata/quarterly-reports", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForQuarterlyReports(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {

        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        List<ActivityMetadata> results = new ArrayList<ActivityMetadata>();
        List<ActivityMetadata> reportMetadata = activityMetadataManager.getQuarterlyReportActivityMetadata(startDate, endDate);
        if (reportMetadata != null && reportMetadata.size() > 0) {
            results.addAll(reportMetadata);
        }
        List<ActivityMetadata> relevantListingMetadata = activityMetadataManager.getQuarterlyReportListingActivityMetadata(startDate, endDate);
        if (relevantListingMetadata != null && relevantListingMetadata.size() > 0) {
            results.addAll(relevantListingMetadata);
        }
        return results;
    }

    @Operation(summary = "Get metadata about auditable records in the system for annual reports.",
            description = "Users must specify 'start' and 'end' parameters to restrict the date range of the results."
                    + "Security Restrictions: ROLE_ADMIN and ROLE_ONC may see activity for all annual reports.  "
                    + "ROLE_ACB can see activity for their own ACBs.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/metadata/annual-reports", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForAnnualReports(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return activityMetadataManager.getAnnualReportActivityMetadata(startDate, endDate);
    }

    @Operation(summary = "Get metadata about auditable records in the system for corrective action plans.",
            description = "All parameters are optional and will default to the first page of corrective action plan activity "
                    + "with a page size of the maximum allowed. Page number is 0-based. Activities will be returned "
                    + "with the most recent activity first.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/metadata/corrective-action-plans", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ActivityMetadataPage metadataForCorrectiveActionPlans(@RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end, @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) throws JsonParseException, IOException, ValidationException {
        return pagedMetadataManager.getActivityMetadataByConcept(
                ActivityConcept.CORRECTIVE_ACTION_PLAN, start, end, pageNum, pageSize);
    }

    @Operation(summary = "Get metadata about auditable records in the system for pending surveillances.",
            description = "All parameters are optional and will default to the first page of pending surveillance activity "
                    + "with a page size of the maximum allowed. Page number is 0-based. Activities will be returned "
                    + "with the most recent activity first.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/metadata/pending-surveillances", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public ActivityMetadataPage metadataForPendingSurveillances(@RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end, @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) throws JsonParseException, IOException, ValidationException {
        return pagedMetadataManager.getPendingSurveillanceActivityMetadata(start, end, pageNum, pageSize);
    }

    @Operation(summary = "Get metadata about auditable records in the system for change requests.",
            description = "Users must specify 'start' and 'end' parameters to restrict the date range of the results."
                    + "Security Restrictions: ROLE_ADMIN and ROLE_ONC may see activity for all chan ge requests.  "
                    + "ROLE_ACB can see activity for change requests they are associated with.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/metadata/change-requests", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForChangeRequests(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return activityMetadataManager.getChangeRequestActivityMetadata(startDate, endDate);
    }

    @Operation(summary = "Get metadata about auditable records in the system for API Keys.",
            description = "All parameters are optional and will default to the first page of API Key activity "
                    + "with a page size of the maximum allowed. Page number is 0-based. Activities will be returned "
                    + "with the most recent activity first.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/metadata/api-keys", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public ActivityMetadataPage metadataForApiKeys(@RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end, @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) throws JsonParseException, IOException, ValidationException {
        return pagedMetadataManager.getApiKeyManagementMetadata(start, end, pageNum, pageSize);
    }

    private void validateActivityDates(final Long startDate, final Long endDate) throws IllegalArgumentException {
        LocalDate startDateUtc = Instant.ofEpochMilli(startDate).atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate endDateUtc = Instant.ofEpochMilli(endDate).atZone(ZoneId.of("UTC")).toLocalDate();

        if (startDateUtc.isAfter(endDateUtc)) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.startDateAfterEndDate"));
        }
    }

    private void validateActivityDatesAndDateRange(final Long startDate, final Long endDate) throws IllegalArgumentException {
        LocalDate startDateUtc = Instant.ofEpochMilli(startDate).atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate endDateUtc = Instant.ofEpochMilli(endDate).atZone(ZoneId.of("UTC")).toLocalDate();

        if (startDateUtc.isAfter(endDateUtc)) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.startDateAfterEndDate"));
        }

        endDateUtc = endDateUtc.minusDays(maxActivityRangeInDays);
        if (startDateUtc.isBefore(endDateUtc)) {
            throw new IllegalArgumentException(
                    "Cannot search for activity with a date range more than " + maxActivityRangeInDays + " days.");
        }
    }
}
