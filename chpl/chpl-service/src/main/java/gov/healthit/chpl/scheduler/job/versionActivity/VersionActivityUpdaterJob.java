package gov.healthit.chpl.scheduler.job.versionActivity;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.activity.ActivityMetadataBuilder;
import gov.healthit.chpl.activity.ActivityMetadataBuilderFactory;
import gov.healthit.chpl.domain.activity.VersionActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.util.JSONUtils;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "versionActivityUpdaterJobLogger")
public class VersionActivityUpdaterJob implements Job {

    @Autowired
    private ProductVersionManager versionManager;

    @Autowired
    private ProductManager productManager;

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
                updateActivityData(activityDto, originalActivityData, versionDto);
                try {
                    activityDto.setOriginalData(JSONUtils.toJSON(originalActivityData));
                } catch (JsonProcessingException ex) {
                    LOGGER.error("Could not convert updated activity original data to JSON", ex);
                }
            }
            if (!StringUtils.isEmpty(activityDto.getNewData())) {
                ProductVersionDTO newActivityData = parseJsonAsProductVersionDto(activityDto.getId(), activityDto.getNewData());
                LOGGER.info("Updating activity new data for " + activity.getId());
                updateActivityData(activityDto, newActivityData, versionDto);
                try {
                    activityDto.setNewData(JSONUtils.toJSON(newActivityData));
                } catch (JsonProcessingException ex) {
                    LOGGER.error("Could not convert updated activity new data to JSON", ex);
                }
            }
        }
        return activityDto;
    }

    private void updateActivityData(ActivityDTO activityDto, ProductVersionDTO savedActivityData, ProductVersionDTO completeVersionDto) {
        if (savedActivityData != null) {
            if (savedActivityData.getProductId() == null || StringUtils.isEmpty(savedActivityData.getProductName())) {
                ProductDTO product = findProductAssociatedWithVersionOnDate(completeVersionDto, activityDto.getActivityDate());
                if (savedActivityData.getProductId() == null) {
                    LOGGER.info("\tSetting missing product id to " + product.getId());
                    savedActivityData.setProductId(product.getId());
                }
                if (StringUtils.isEmpty(savedActivityData.getProductName())) {
                    LOGGER.info("\tSetting missing product name to " + product.getName());
                    savedActivityData.setProductName(product.getName());
                }
            }

            if (savedActivityData.getDeveloperId() == null || StringUtils.isEmpty(savedActivityData.getDeveloperName())) {
                ProductDTO product = ProductDTO.builder()
                        .id(savedActivityData.getProductId() != null ? savedActivityData.getProductId() : completeVersionDto.getProductId())
                        .name(savedActivityData.getProductName() != null ? savedActivityData.getProductName() : completeVersionDto.getProductName())
                        .build();
                //try to find the product's owner at the time of this activity and save that developer
                DeveloperDTO developer = findDeveloperAssociatedWithProductOnDate(product.getId(), activityDto.getActivityDate());
                if (savedActivityData.getDeveloperId() == null) {
                    Long developerIdToSave = developer.getId() != null ? developer.getId() : completeVersionDto.getDeveloperId();
                    LOGGER.info("\tSetting missing developer id to " + developerIdToSave);
                    savedActivityData.setDeveloperId(developerIdToSave);
                }
                if (StringUtils.isEmpty(savedActivityData.getDeveloperName())) {
                    String developerNameToSave = !StringUtils.isEmpty(developer.getName()) ? developer.getName() : completeVersionDto.getDeveloperName();
                    LOGGER.info("\tSetting missing developer name to " + developerNameToSave);
                    savedActivityData.setDeveloperName(developerNameToSave);
                }
            }
        }
    }

    private ProductDTO findProductAssociatedWithVersionOnDate(ProductVersionDTO version, Date date) {
        //TODO: need some better logic here to narrow down by date...
        //1. If products were merged then this version may have previously been under a different product ID
        //2. IF products were split then this version may have previously been under a different product ID
        ProductDTO product = null;
        try {
            product = productManager.getById(version.getProductId());
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Cannot find product with ID " + version.getProductId());
        }
        return product;
    }

    private DeveloperDTO findDeveloperAssociatedWithProductOnDate(Long productId, Date date) {
        ProductDTO product = null;
        try {
            product = productManager.getById(productId);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Cannot find product with ID " + product.getId());
        }
        if (product == null) {
            return null;
        }

        ProductOwnerDTO productOwnerOnActivityDate = product.getOwnerOnDate(date);
        return DeveloperDTO.builder()
                .id(productOwnerOnActivityDate.getDeveloper().getId())
                .name(productOwnerOnActivityDate.getDeveloper().getName())
                .build();
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
}
