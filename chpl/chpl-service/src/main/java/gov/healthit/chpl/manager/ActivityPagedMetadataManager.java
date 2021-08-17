package gov.healthit.chpl.manager;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;

import gov.healthit.chpl.activity.ActivityMetadataBuilder;
import gov.healthit.chpl.activity.ActivityMetadataBuilderFactory;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.AnnouncementDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ActivityMetadataPage;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Service("activityPagedMetadataManager")
@Log4j2
public class ActivityPagedMetadataManager extends SecuredManager {
    private ActivityDAO activityDao;
    private AnnouncementDAO announcementDao;
    private ActivityMetadataBuilderFactory metadataBuilderFactory;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;

    @Value("${maxActivityPageSize}")
    private Integer maxActivityPageSize;

    @Value("${defaultActivityPageSize}")
    private Integer defaultActivityPageSize;

    @Autowired
    public ActivityPagedMetadataManager(ActivityDAO activityDao, AnnouncementDAO announcementDao,
            ActivityMetadataBuilderFactory metadataBuilderFactory, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions) {
        this.activityDao = activityDao;
        this.announcementDao = announcementDao;
        this.metadataBuilderFactory = metadataBuilderFactory;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
    }


    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_ACTIVITY_METADATA_BY_CONCEPT, #concept)")
    public ActivityMetadataPage getActivityMetadataByConcept(ActivityConcept concept, Long startMillis, Long endMillis,
            Integer pageNum, Integer pageSize) throws ValidationException, JsonParseException, IOException {
        Set<String> errors = new HashSet<String>();
        errors.addAll(validateActivityDates(startMillis, endMillis));
        errors.addAll(validatePagingParameters(pageNum, pageSize));
        if (errors.size() > 0) {
            throw new ValidationException(errors);
        }
        return getActivityMetadataPageByConcept(concept, startMillis, endMillis, pageNum, pageSize);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_ACB_METADATA)")
    @Transactional
    public ActivityMetadataPage getCertificationBodyActivityMetadata(Long startMillis, Long endMillis,
            Integer pageNum, Integer pageSize) throws ValidationException, JsonParseException, IOException {
        Set<String> errors = new HashSet<String>();
        errors.addAll(validateActivityDates(startMillis, endMillis));
        errors.addAll(validatePagingParameters(pageNum, pageSize));
        if (errors.size() > 0) {
            throw new ValidationException(errors);
        }
        return getActivityMetadataPageByConcept(ActivityConcept.CERTIFICATION_BODY,
                startMillis, endMillis, pageNum, pageSize);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_ATL_METADATA)")
    @Transactional
    public ActivityMetadataPage getTestingLabActivityMetadata(Long startMillis, Long endMillis,
            Integer pageNum, Integer pageSize) throws ValidationException, JsonParseException, IOException {
        Set<String> errors = new HashSet<String>();
        errors.addAll(validateActivityDates(startMillis, endMillis));
        errors.addAll(validatePagingParameters(pageNum, pageSize));
        if (errors.size() > 0) {
            throw new ValidationException(errors);
        }
        return getActivityMetadataPageByConcept(ActivityConcept.TESTING_LAB,
                startMillis, endMillis, pageNum, pageSize);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_API_KEY_MANAGEMENT_METADATA)")
    public ActivityMetadataPage getApiKeyManagementMetadata(Long startMillis, Long endMillis,
        Integer pageNum, Integer pageSize) throws ValidationException, JsonParseException, IOException {
        Set<String> errors = new HashSet<String>();
        errors.addAll(validateActivityDates(startMillis, endMillis));
        errors.addAll(validatePagingParameters(pageNum, pageSize));
        if (errors.size() > 0) {
            throw new ValidationException(errors);
        }
        return getActivityMetadataPageByConcept(ActivityConcept.API_KEY, startMillis, endMillis, pageNum, pageSize);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_USER_MAINTENANCE_METADATA)")
    public ActivityMetadataPage getUserMaintenanceActivityMetadata(Long startMillis, Long endMillis,
            Integer pageNum, Integer pageSize) throws ValidationException, JsonParseException, IOException {
        Set<String> errors = new HashSet<String>();
        errors.addAll(validateActivityDates(startMillis, endMillis));
        errors.addAll(validatePagingParameters(pageNum, pageSize));
        if (errors.size() > 0) {
            throw new ValidationException(errors);
        }
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc() || resourcePermissions.isUserRoleOncStaff()) {
            return getActivityMetadataPageByConcept(ActivityConcept.USER, startMillis, endMillis, pageNum, pageSize);
        } else {
            List<UserDTO> allowedUsers = resourcePermissions.getAllUsersForCurrentUser();
            List<Long> allowedUserIds = allowedUsers.stream()
                .map(allowedUser -> allowedUser.getId())
                .collect(Collectors.toList());
            return getActivityMetadataPageByConceptAndObject(
                    ActivityConcept.USER, allowedUserIds, startMillis, endMillis, pageNum, pageSize);
        }
    }

    @Transactional
    public ActivityMetadataPage getAnnouncementActivityMetadata(Long startMillis, Long endMillis,
            Integer pageNum, Integer pageSize) throws ValidationException, JsonParseException, IOException {
        Set<String> errors = new HashSet<String>();
        errors.addAll(validateActivityDates(startMillis, endMillis));
        errors.addAll(validatePagingParameters(pageNum, pageSize));
        if (errors.size() > 0) {
            throw new ValidationException(errors);
        }
        if (resourcePermissions.isUserAnonymous()) {
            List<AnnouncementDTO> publicAnnouncements = announcementDao.findAll(true, false);
            List<Long> publicAnnouncementIds = publicAnnouncements.stream()
                .map(publicAnnouncement -> publicAnnouncement.getId())
                .collect(Collectors.toList());
            return getActivityMetadataPageByConceptAndObject(
                    ActivityConcept.ANNOUNCEMENT, publicAnnouncementIds, startMillis, endMillis, pageNum, pageSize);
        } else {
            return getActivityMetadataPageByConcept(ActivityConcept.ANNOUNCEMENT, startMillis, endMillis, pageNum, pageSize);
        }
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_PENDING_SURVEILLANCE_METADATA)")
    @Transactional
    public ActivityMetadataPage getPendingSurveillanceActivityMetadata(Long startMillis, Long endMillis,
            Integer pageNum, Integer pageSize) throws ValidationException, JsonParseException, IOException {
        return getActivityMetadataByConcept(ActivityConcept.PENDING_SURVEILLANCE, startMillis, endMillis, pageNum, pageSize);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_PENDING_LISTING_METADATA)")
    @Transactional
    public ActivityMetadataPage getPendingListingActivityMetadata(Long startMillis, Long endMillis,
            Integer pageNum, Integer pageSize) throws ValidationException, JsonParseException, IOException {
        return getActivityMetadataByConcept(ActivityConcept.PENDING_CERTIFIED_PRODUCT,
                startMillis, endMillis, pageNum, pageSize);
    }

    private ActivityMetadataPage getActivityMetadataPageByConcept(ActivityConcept concept,
            Long startMillis, Long endMillis, Integer pageNumParam, Integer pageSizeParam)
                    throws JsonParseException, IOException {
        Date startDate = new Date(0);
        if (startMillis != null) {
            startDate = new Date(startMillis);
        }
        Date endDate = new Date();
        if (endMillis != null) {
            endDate = new Date(endMillis);
        }
        Integer pageNum = 0;
        if (pageNumParam != null) {
            pageNum = pageNumParam;
        }
        Integer pageSize = defaultActivityPageSize;
        if (pageSizeParam != null) {
            pageSize = pageSizeParam;
        }
        LOGGER.info("Getting " + concept.name() + " activity: page " + pageNum
                + ", " + pageSize + " records, from " + startDate + " through " + endDate);

        ActivityMetadataPage page = new ActivityMetadataPage();
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        page.setResultSetSize(activityDao.findResultSetSizeByConcept(concept, startDate, endDate));
        List<ActivityDTO> activityDtos = activityDao.findPageByConcept(concept, startDate, endDate, pageNum, pageSize);
        Set<ActivityMetadata> activityMetas = new LinkedHashSet<ActivityMetadata>();
        ActivityMetadataBuilder builder = null;
        if (activityDtos != null && activityDtos.size() > 0) {
            LOGGER.info("Found " + activityDtos.size() + " activity events");
            builder = metadataBuilderFactory.getBuilder(activityDtos.get(0));
            for (ActivityDTO dto : activityDtos) {
                ActivityMetadata activityMeta = builder.build(dto);
                activityMetas.add(activityMeta);
            }
        } else {
            LOGGER.info("Found no activity events");
        }
        page.setActivities(activityMetas);
        return page;
    }

    private ActivityMetadataPage getActivityMetadataPageByConceptAndObject(ActivityConcept concept, List<Long> objectIds,
            Long startMillis, Long endMillis, Integer pageNumParam, Integer pageSizeParam)
                    throws JsonParseException, IOException {
        Date startDate = new Date(0);
        if (startMillis != null) {
            startDate = new Date(startMillis);
        }
        Date endDate = new Date();
        if (endMillis != null) {
            endDate = new Date(endMillis);
        }
        Integer pageNum = 0;
        if (pageNumParam != null) {
            pageNum = pageNumParam;
        }
        Integer pageSize = defaultActivityPageSize;
        if (pageSizeParam != null) {
            pageSize = pageSizeParam;
        }
        LOGGER.info("Getting " + concept.name() + " activity: page " + pageNum
                + ", " + pageSize + " records, from " + startDate + " through " + endDate);

        ActivityMetadataPage page = new ActivityMetadataPage();
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        page.setResultSetSize(activityDao.findResultSetSizeByConceptAndObject(concept, objectIds, startDate, endDate));
        List<ActivityDTO> activityDtos = activityDao.findPageByConceptAndObject(
                concept, objectIds, startDate, endDate, pageNum, pageSize);
        Set<ActivityMetadata> activityMetas = new LinkedHashSet<ActivityMetadata>();
        ActivityMetadataBuilder builder = null;
        if (activityDtos != null && activityDtos.size() > 0) {
            LOGGER.info("Found " + activityDtos.size() + " activity events");
            builder = metadataBuilderFactory.getBuilder(activityDtos.get(0));
            for (ActivityDTO dto : activityDtos) {
                ActivityMetadata activityMeta = builder.build(dto);
                activityMetas.add(activityMeta);
            }
        } else {
            LOGGER.info("Found no activity events");
        }
        page.setActivities(activityMetas);
        return page;
    }

    private Set<String> validateActivityDates(Long startMillis, Long endMillis) {
        Set<String> errors = new HashSet<String>();
        if (startMillis == null || endMillis == null) {
            return errors;
        }

        LocalDate startDateUtc =
                Instant.ofEpochMilli(startMillis).atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate endDateUtc =
                Instant.ofEpochMilli(endMillis).atZone(ZoneId.of("UTC")).toLocalDate();

        if (startDateUtc.isAfter(endDateUtc)) {
            errors.add(msgUtil.getMessage("activity.startDateAfterEndDate"));
        }
        return errors;
    }

    private Set<String> validatePagingParameters(Integer pageNum, Integer pageSize) throws IllegalArgumentException {
        Set<String> errors = new HashSet<String>();

        if (pageNum != null && pageNum < 0) {
            errors.add(msgUtil.getMessage("activity.negativePageNum"));
        }
        if (pageSize != null && pageSize <= 0) {
            errors.add(msgUtil.getMessage("activity.negativePageSize"));
        } else if (pageSize != null && pageSize > maxActivityPageSize) {
            errors.add(msgUtil.getMessage("activity.pageSizeOverMax", maxActivityPageSize));
        }
        return errors;
    }
}
