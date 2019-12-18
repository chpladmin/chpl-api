package gov.healthit.chpl.manager;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;

import gov.healthit.chpl.activity.ActivityMetadataBuilder;
import gov.healthit.chpl.activity.ActivityMetadataBuilderFactory;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ActivityMetadataPage;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Service("activityPagedMetadataManager")
public class ActivityPagedMetadataManager extends SecuredManager {
    private static final Logger LOGGER = LogManager.getLogger(ActivityPagedMetadataManager.class);
    private static final int DEFAULT_MAX_ACTIVITY_PAGE_SIZE = 100;

    private ActivityDAO activityDao;
    private ActivityMetadataBuilderFactory metadataBuilderFactory;
    private ErrorMessageUtil msgUtil;

    @Value("${maxActivityPageSize}")
    private Integer maxActivityPageSize;

    @Autowired
    public ActivityPagedMetadataManager(ActivityDAO activityDao, ActivityMetadataBuilderFactory metadataBuilderFactory,
            ErrorMessageUtil msgUtil) {
        this.activityDao = activityDao;
        this.metadataBuilderFactory = metadataBuilderFactory;
        this.msgUtil = msgUtil;

        if (maxActivityPageSize == null) {
            maxActivityPageSize = DEFAULT_MAX_ACTIVITY_PAGE_SIZE;
        }
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
        return getActivityMetadataPageByConceptWithoutSecurity(concept, startMillis, endMillis, pageNum, pageSize);
    }

    @Transactional
    public List<ActivityMetadata> getActivityMetadataByObject(final Long objectId, final ActivityConcept concept,
            final Date startDate, final Date endDate) throws JsonParseException, IOException {

        return getActivityMetadataByObjectWithoutSecurity(objectId, concept, startDate, endDate);
    }

    private ActivityMetadataPage getActivityMetadataPageByConceptWithoutSecurity(ActivityConcept concept,
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
        Integer pageSize = maxActivityPageSize;
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

    private List<ActivityMetadata> getActivityMetadataByObjectWithoutSecurity(final Long objectId,
            final ActivityConcept concept, final Date startDate, final Date endDate)
            throws JsonParseException, IOException {

        LOGGER.info("Getting " + concept.name() + " activity for id " + objectId + " from " + startDate + " through "
                + endDate);
        // get the activity
        List<ActivityDTO> activityDtos = activityDao.findByObjectId(objectId, concept, startDate, endDate);
        List<ActivityMetadata> activityMetas = new ArrayList<ActivityMetadata>();
        ActivityMetadataBuilder builder = null;
        if (activityDtos != null && activityDtos.size() > 0) {
            // excpect all dtos to have the same
            // since we've searched based on activity concept
            builder = metadataBuilderFactory.getBuilder(activityDtos.get(0));
            // convert to domain object
            for (ActivityDTO dto : activityDtos) {
                ActivityMetadata activityMeta = builder.build(dto);
                activityMetas.add(activityMeta);
            }
        }
        return activityMetas;
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
