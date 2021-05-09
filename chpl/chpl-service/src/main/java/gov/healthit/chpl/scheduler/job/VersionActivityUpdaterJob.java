package gov.healthit.chpl.scheduler.job;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.activity.ActivityMetadataBuilder;
import gov.healthit.chpl.activity.ActivityMetadataBuilderFactory;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.VersionActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ActivityEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.util.JSONUtils;
import gov.healthit.chpl.util.UserMapper;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "versionActivityUpdaterJobLogger")
public class VersionActivityUpdaterJob implements Job {

    @Autowired
    private ProductVersionManager versionManager;

    @Autowired
    private ActivityMetadataBuilderFactory metadataBuilderFactory;

    @Autowired
    private UpdateableActivityDao activityUpdateDao;

    private ObjectMapper jsonMapper;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Version Activity Updater job. *********");
        jsonMapper = new ObjectMapper();

        List<ActivityDTO> allVersionActivity = activityUpdateDao.getAllVersionActivityMetadata();
        LOGGER.info("Found " + allVersionActivity.size() + " version activity database entries.");
        allVersionActivity.stream()
            .map(activityDto -> mapToVersionActivityMetadata(activityDto))
            .filter(activityMeta -> isMissingProductOrDeveloper(activityMeta))
            .map(activityMeta -> fillInMissingInformation(activityMeta))
            .filter(activityDto -> activityDto != null)
            .forEach(activityDto -> saveActivityUpdates(activityDto));

        LOGGER.info("********* Completed the Version Activity Updater job. *********");

    }

    private VersionActivityMetadata mapToVersionActivityMetadata(ActivityDTO activity) {
        ActivityMetadataBuilder builder = metadataBuilderFactory.getBuilder(activity);
        return (VersionActivityMetadata) builder.build(activity);
    }

    private boolean isMissingProductOrDeveloper(VersionActivityMetadata activity) {
        return StringUtils.isEmpty(activity.getProductName()) || StringUtils.isEmpty(activity.getDeveloperName());
    }

    private ActivityDTO fillInMissingInformation(VersionActivityMetadata activity) {
        LOGGER.info("Activity " + activity.getId() + " is missing product or developer information.");
        ProductVersionDTO versionDto = null;
        try {
            versionDto = versionManager.getById(activity.getObjectId(), true);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not get version with ID " + activity.getObjectId());
        }

        //query for the activity dto by id
        ActivityDTO activityDto = null;
        try {
            activityDto = activityUpdateDao.getById(activity.getId());
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not get the activity with ID " + activity.getId());
        }

        if (versionDto != null && activityDto != null) {
            if (!StringUtils.isEmpty(activityDto.getOriginalData())) {
                ProductVersionDTO originalActivityData = parseJsonAsProductVersionDto(activityDto.getId(), activityDto.getOriginalData());
                LOGGER.info("Updating activity original data for " + activity.getId());
                updateActivityData(originalActivityData, versionDto);
                try {
                    activityDto.setOriginalData(JSONUtils.toJSON(originalActivityData));
                } catch (JsonProcessingException ex) {
                    LOGGER.error("Could not convert updated activity original data to JSON", ex);
                }
            }
            if (!StringUtils.isEmpty(activityDto.getNewData())) {
                ProductVersionDTO newActivityData = parseJsonAsProductVersionDto(activityDto.getId(), activityDto.getNewData());
                LOGGER.info("Updating activity new data for " + activity.getId());
                updateActivityData(newActivityData, versionDto);
                try {
                    activityDto.setNewData(JSONUtils.toJSON(newActivityData));
                } catch (JsonProcessingException ex) {
                    LOGGER.error("Could not convert updated activity new data to JSON", ex);
                }
            }
        }
        return activityDto;
    }

    private void updateActivityData(ProductVersionDTO savedActivityData, ProductVersionDTO completeVersionDto) {
        if (savedActivityData != null) {
            if (savedActivityData.getProductId() == null) {
                LOGGER.info("\tSetting missing product id to " + completeVersionDto.getProductId());
                savedActivityData.setProductId(completeVersionDto.getProductId());
            }
            if (StringUtils.isEmpty(savedActivityData.getProductName())) {
                LOGGER.info("\tSetting missing product name to " + completeVersionDto.getProductName());
                savedActivityData.setProductName(completeVersionDto.getProductName());
            }
            //TODO: for developer data - we could try to find the product's owner at the
            //time of this activity and save that developer
            if (savedActivityData.getDeveloperId() == null) {
                LOGGER.info("\tSetting missing developer id to " + completeVersionDto.getDeveloperId());
                savedActivityData.setDeveloperId(completeVersionDto.getDeveloperId());
            }
            if (StringUtils.isEmpty(savedActivityData.getDeveloperName())) {
                LOGGER.info("\tSetting missing developer name to " + completeVersionDto.getDeveloperName());
                savedActivityData.setDeveloperName(completeVersionDto.getDeveloperName());
            }
        }
    }

    private ProductVersionDTO parseJsonAsProductVersionDto(Long activityId, String activityJson) {
        ProductVersionDTO version = null;
        try {
            version =
                jsonMapper.readValue(activityJson, ProductVersionDTO.class);
        } catch (Exception ex) {
            LOGGER.warn("Unable to parse '" + activityJson + "' as a ProductVersionDTO object.");
            //TODO: this could also be a List of versions if a merge or split was done and that isn't being
            //handled by this class. Not sure how many activities might be like this?
        }
        return version;
    }

    private void saveActivityUpdates(ActivityDTO activity) {
        activityUpdateDao.update(activity);
    }

    @Component
    private final class UpdateableActivityDao extends ActivityDAO {
        private UserMapper userMapper;

        @Autowired
        private UpdateableActivityDao(UserMapper userMapper) {
            super(userMapper);
            this.userMapper = userMapper;
        }

        @Transactional
        public void update(ActivityDTO dto) {
            ActivityEntity entity = getEntityManager().find(ActivityEntity.class, dto.getId());
            if (entity != null) {
                entity.setNewData(dto.getNewData());
                entity.setOriginalData(dto.getOriginalData());
            }
            update(entity);
        }

        @Transactional
        public List<ActivityDTO> getAllVersionActivityMetadata() {
            String hql = "SELECT a "
                    + "FROM ActivityEntity a "
                    + "JOIN FETCH a.concept concept "
                    + "LEFT JOIN FETCH a.user u "
                    + "LEFT JOIN FETCH u.permission "
                    + "LEFT JOIN FETCH u.contact "
                    + "WHERE concept.concept = :conceptName "
                    + "AND a.deleted = false";
            Query query = entityManager.createQuery(hql, ActivityEntity.class);
            query.setParameter("conceptName", ActivityConcept.VERSION);
            return ((List<ActivityEntity>) query.getResultList()).stream()
                    .map(entity -> mapEntityToDto(entity))
                    .collect(Collectors.toList());
        }

        private ActivityDTO mapEntityToDto(ActivityEntity entity) {
            ActivityDTO activity = new ActivityDTO(entity);
            if (entity.getUser() != null) {
                activity.setUser(userMapper.from(entity.getUser()));
            }
            return activity;
        }
    }
}
