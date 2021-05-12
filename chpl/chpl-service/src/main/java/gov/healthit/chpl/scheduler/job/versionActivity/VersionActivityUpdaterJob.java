package gov.healthit.chpl.scheduler.job.versionActivity;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
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
    private ActivityManager activityManager;

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
            .map(activityDto -> mapToProductVersionDTOs(activityDto))
            .filter(pvActivityDto -> isMissingProductOrDeveloper(pvActivityDto))
            .map(pvActivityDto -> fillInMissingInformation(pvActivityDto))
            .filter(pvActivityDto -> pvActivityDto != null)
            .forEach(pvActivityDto -> saveActivityUpdates(pvActivityDto));

        LOGGER.info("********* Completed the Version Activity Updater job. *********");

    }

    private ProductVersionActivityDTO mapToProductVersionDTOs(ActivityDTO activityDto) {
        ProductVersionActivityDTO pvActivity = new ProductVersionActivityDTO();
        pvActivity.setActivityDate(activityDto.getActivityDate());
        pvActivity.setActivityObjectId(activityDto.getActivityObjectId());
        pvActivity.setConcept(activityDto.getConcept());
        pvActivity.setCreationDate(activityDto.getCreationDate());
        pvActivity.setDeleted(activityDto.getDeleted());
        pvActivity.setDescription(activityDto.getDescription());
        pvActivity.setId(activityDto.getId());
        pvActivity.setLastModifiedDate(activityDto.getLastModifiedDate());
        pvActivity.setLastModifiedUser(activityDto.getLastModifiedUser());
        pvActivity.setNewData(activityDto.getNewData());
        pvActivity.setOriginalData(activityDto.getOriginalData());
        pvActivity.setUser(activityDto.getUser());

        if (!StringUtils.isEmpty(activityDto.getOriginalData())) {
            ProductVersionDTO originalActivityData = parseJsonAsProductVersionDto(activityDto.getOriginalData());
            pvActivity.setOriginalVersion(originalActivityData);
        }
        if (!StringUtils.isEmpty(activityDto.getNewData())) {
            ProductVersionDTO newActivityData = parseJsonAsProductVersionDto(activityDto.getNewData());
            pvActivity.setUpdatedVersion(newActivityData);
        }
        return pvActivity;
    }

    private boolean isMissingProductOrDeveloper(ProductVersionActivityDTO activity) {
        return (activity.getOriginalVersion() != null
                && (activity.getOriginalVersion().getProductId() == null || activity.getOriginalVersion().getDeveloperId() == null
                    || StringUtils.isEmpty(activity.getOriginalVersion().getProductName()) || StringUtils.isEmpty(activity.getOriginalVersion().getDeveloperName())))
                ||
                (activity.getUpdatedVersion() != null
                 && (activity.getUpdatedVersion().getProductId() == null || activity.getUpdatedVersion().getDeveloperId() == null
                     || StringUtils.isEmpty(activity.getUpdatedVersion().getProductName()) || StringUtils.isEmpty(activity.getUpdatedVersion().getDeveloperName())));
    }

    private ActivityDTO fillInMissingInformation(ProductVersionActivityDTO activityDto) {
        LOGGER.info("Activity " + activityDto.getId() + " is missing product or developer information.");
        ProductVersionDTO versionDto = null;
        try {
            versionDto = versionManager.getById(activityDto.getActivityObjectId(), true);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not get version with ID " + activityDto.getActivityObjectId());
        }

        if (versionDto != null && activityDto != null) {
            if (activityDto.getOriginalVersion() != null) {
                LOGGER.info("Updating activity original data for " + activityDto.getId());
                updateActivityData(activityDto, activityDto.getOriginalVersion(), versionDto);
                try {
                    activityDto.setOriginalData(JSONUtils.toJSON(activityDto.getOriginalVersion()));
                } catch (JsonProcessingException ex) {
                    LOGGER.error("Could not convert updated activity original data to JSON", ex);
                }
            }
            if (activityDto.getUpdatedVersion() != null) {
                LOGGER.info("Updating activity new data for " + activityDto.getId());
                updateActivityData(activityDto, activityDto.getUpdatedVersion(), versionDto);
                try {
                    activityDto.setNewData(JSONUtils.toJSON(activityDto.getUpdatedVersion()));
                } catch (JsonProcessingException ex) {
                    LOGGER.error("Could not convert updated activity new data to JSON", ex);
                }
            }
        }
        return activityDto;
    }

    private void updateActivityData(ActivityDTO activityDto, ProductVersionDTO existingActivityData, ProductVersionDTO systemVersionDto) {
        if (existingActivityData != null) {
            ProductDTO productAssociatedWithVersionActivityRecord = ProductDTO.builder()
                    .id(existingActivityData.getProductId())
                    .name(existingActivityData.getProductName())
                    .build();
            if (existingActivityData.getProductId() == null || StringUtils.isEmpty(existingActivityData.getProductName())) {
                productAssociatedWithVersionActivityRecord = findProductAssociatedWithVersionOnDate(systemVersionDto, activityDto.getActivityDate());
                if (productAssociatedWithVersionActivityRecord != null && existingActivityData.getProductId() == null) {
                    LOGGER.info("\tSetting missing product id to " + productAssociatedWithVersionActivityRecord.getId());
                    existingActivityData.setProductId(productAssociatedWithVersionActivityRecord.getId());
                }
                if (productAssociatedWithVersionActivityRecord != null && StringUtils.isEmpty(existingActivityData.getProductName())) {
                    LOGGER.info("\tSetting missing product name to " + productAssociatedWithVersionActivityRecord.getName());
                    existingActivityData.setProductName(productAssociatedWithVersionActivityRecord.getName());
                }
            }

            if (productAssociatedWithVersionActivityRecord != null && productAssociatedWithVersionActivityRecord.getId() != null
                    && (existingActivityData.getDeveloperId() == null || StringUtils.isEmpty(existingActivityData.getDeveloperName()))) {
                //try to find the product's owner at the time of this activity and save that developer
                DeveloperDTO developer = findDeveloperAssociatedWithProductOnDate(productAssociatedWithVersionActivityRecord.getId(), activityDto.getActivityDate());
                if (developer != null && existingActivityData.getDeveloperId() == null) {
                    LOGGER.info("\tSetting missing developer id to " + developer.getId());
                    existingActivityData.setDeveloperId(developer.getId());
                }
                if (developer != null && StringUtils.isEmpty(existingActivityData.getDeveloperName())) {
                    LOGGER.info("\tSetting missing developer name to " + developer.getName());
                    existingActivityData.setDeveloperName(developer.getName());
                }
            }
        }
    }

    private ProductDTO findProductAssociatedWithVersionOnDate(ProductVersionDTO version, Date date) {
        ProductDTO product = null;
        try {
            product = productManager.getById(version.getProductId(), true);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Cannot find product with ID " + version.getProductId());
        }
        //The above code gets the product currently associated with the version.
        //If the product has been updated, merged, or split since the date of the version activity
        //then it is possible the retrieved product information does not match what it looked like
        //at the time of the version activity.
        //So - query for any update (potentially a different name), split, or merge activity for
        //this product that has occurred since the activity.
        //If it exists, I do not think we can continue with this product with confidence.
        // Up to the team/PO if we need to try to:
        //   1) Trace through the activity to see what product the version was under at the time of activity
        //      a) Can we even do that? Can QA or anyone even validate that it's right?
        //   2) Proceed with this product, or
        //   3) Leave the product and developer blank
        //After reviewing the logs, this affects 770 version activity records.
        if (product != null) {
            List<ActivityDTO> productActivitySinceDate = getProductActivitySinceDate(product.getId(), date);
            if (productActivitySinceDate != null && productActivitySinceDate.size() > 0) {
                LOGGER.info("Product " + product.getId() + ": " + product.getName() + " has had activity since " + date.toString());
                ProductDTO productOnActivityDate = getProductOnActivityDate(productActivitySinceDate, date);
                if (productOnActivityDate == null) {
                    LOGGER.warn("Could not determine product data on " + date.toString() + " and will not proceed with this activity.");
                }
                product = productOnActivityDate;
            }
        }
        return product;
    }

    private List<ActivityDTO> getProductActivitySinceDate(Long productId, Date activityDate) {
        Date currentDate = new Date();
        List<ActivityDTO> activityForProduct = activityUpdateDao.findByObjectId(productId, ActivityConcept.PRODUCT, activityDate, currentDate);

        if (activityForProduct != null && activityForProduct.size() > 0) {
            //if there are any activities where new + original data are both present
            //then that means the product was updated in some way
            //(new data is null when product is deleted, original data is null when product is created)
            return activityForProduct.stream()
                .filter(activity -> activity.getNewData() != null && activity.getOriginalData() != null)
                .collect(Collectors.toList());
        }
        return null;
    }

    private ProductDTO getProductOnActivityDate(List<ActivityDTO> productActivities, Date date) {
        ActivityDTO productActivity = null;
        productActivities.sort(new Comparator<ActivityDTO>() {
            @Override
            public int compare(ActivityDTO o1, ActivityDTO o2) {
                return o1.getActivityDate().compareTo(o2.getActivityDate());
            }
        });

        for (ActivityDTO currActivity : productActivities) {
            if (productActivity == null && currActivity.getActivityDate().after(date)) {
                productActivity = currActivity;
            }
        }

        if (productActivity == null) {
            return null;
        }
        return parseJsonAsProductDto(productActivity.getOriginalData());
    }

    private DeveloperDTO findDeveloperAssociatedWithProductOnDate(Long productId, Date date) {
        ProductDTO product = null;
        try {
            product = productManager.getById(productId, true);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Cannot find product with ID " + productId);
        }
        if (product == null) {
            return null;
        }

        ProductOwnerDTO productOwnerOnActivityDate = product.getOwnerOnDate(date);
        //The above code gets the developer that was associated with the product at the date specified.
        //If the developer has been updated, merged, or split since the date of the version activity
        //then it is possible the retrieved developer information does not match what it looked like
        //at the time of the version activity.
        //So - query for any update (potentially a different name), split, or merge activity for
        //this developer that has occurred since the version activity.
        //If it exists, I do not think we can continue with this developer with confidence.
        // Up to the team/PO if we need to try to:
        //   1) Trace through the activity to see what developer the product was under at the time of activity
        //      a) Can we even do that? Can QA or anyone even validate that it's right?
        //   2) Proceed with this developer, or
        //   3) Leave the developer blank
        //After reviewing the logs, this affects 770 version activity records.
        DeveloperDTO developer = DeveloperDTO.builder()
                .id(productOwnerOnActivityDate.getDeveloper().getId())
                .name(productOwnerOnActivityDate.getDeveloper().getName())
                .build();
        List<ActivityDTO> developerActivitySinceDate = getDeveloperActivitySinceDate(developer.getId(), date);
        if (developerActivitySinceDate != null && developerActivitySinceDate.size() > 0) {
            LOGGER.info("Developer " + developer.getId() + ": " + developer.getName() + " has had activity since " + date.toString());
            DeveloperDTO developerOnActivityDate = getDeveloperOnActivityDate(developerActivitySinceDate, date);
            if (developerOnActivityDate == null) {
                LOGGER.warn("Could not determine developer data on " + date.toString() + " and will not proceed with this activity.");
            }
            developer = developerOnActivityDate;
        }
        return developer;
    }

    private List<ActivityDTO> getDeveloperActivitySinceDate(Long developerId, Date activityDate) {
        Date currentDate = new Date();
        List<ActivityDTO> activityForDeveloper = activityUpdateDao.findByObjectId(developerId, ActivityConcept.DEVELOPER, activityDate, currentDate);
        if (activityForDeveloper != null && activityForDeveloper.size() > 0) {
            //if there are any activities where new + original data are both present
            //then that means the developer was updated in some way
            //(new data is null when developer is deleted, original data is null when developer is created)
            return activityForDeveloper.stream()
                .filter(activity -> activity.getNewData() != null && activity.getOriginalData() != null)
                .collect(Collectors.toList());
        }
        return null;
    }

    private DeveloperDTO getDeveloperOnActivityDate(List<ActivityDTO> developerActivities, Date date) {
        ActivityDTO developerActivity = null;
        developerActivities.sort(new Comparator<ActivityDTO>() {
            @Override
            public int compare(ActivityDTO o1, ActivityDTO o2) {
                return o1.getActivityDate().compareTo(o2.getActivityDate());
            }
        });

        for (ActivityDTO currActivity : developerActivities) {
            if (developerActivity == null && currActivity.getActivityDate().after(date)) {
                developerActivity = currActivity;
            }
        }

        if (developerActivity == null) {
            return null;
        }
        return parseJsonAsDeveloperDto(developerActivity.getOriginalData());
    }

    private ProductVersionDTO parseJsonAsProductVersionDto(String activityJson) {
        ProductVersionDTO version = null;
        try {
            version =
                jsonMapper.readValue(activityJson, ProductVersionDTO.class);
        } catch (Exception ex) {
            LOGGER.warn("Unable to parse '" + activityJson + "' as a ProductVersionDTO object.");
            //This could also be a List of versions if a merge or split was done and that isn't being
            //handled by this job. By looking at the job log, it appears that there are 0
            //activities of this type with missing developer data.
        }
        return version;
    }

    private ProductDTO parseJsonAsProductDto(String activityJson) {
        ProductDTO product = null;
        try {
            product =
                jsonMapper.readValue(activityJson, ProductDTO.class);
        } catch (Exception ex) {
            LOGGER.info("Unable to parse '" + activityJson + "' as a ProductDTO object.");
            //This could also be a List of products if a merge or split was done and that isn't being
            //handled by this job.
        }
        return product;
    }

    private DeveloperDTO parseJsonAsDeveloperDto(String activityJson) {
        DeveloperDTO developer = null;
        try {
            developer =
                jsonMapper.readValue(activityJson, DeveloperDTO.class);
        } catch (Exception ex) {
            LOGGER.info("Unable to parse '" + activityJson + "' as a DeveloperDTO object.");
            //This could also be a List of developers if a merge or split was done and that isn't being
            //handled by this job.
        }
        return developer;
    }

    private void saveActivityUpdates(ActivityDTO activity) {
        activityUpdateDao.update(activity);
    }
}
