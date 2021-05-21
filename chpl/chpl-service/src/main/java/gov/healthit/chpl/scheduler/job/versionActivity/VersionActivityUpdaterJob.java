package gov.healthit.chpl.scheduler.job.versionActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.util.JSONUtils;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "versionActivityUpdaterJobLogger")
public class VersionActivityUpdaterJob implements Job {
    private static final String PRODUCT_OWNER_HISTORY_TABLE_EXISTS_DATE = "11/15/2016";

    @Autowired
    private ProductVersionManager versionManager;

    @Autowired
    private ProductManager productManager;

    @Autowired
    private DeveloperManager developerManager;

    @Autowired
    private UpdateableActivityDao activityUpdateDao;

    @Autowired
    private ProductOwnerHistoryDao productOwnerHistoryDao;

    private Date productOwnerHistoryTableExists = null;
    private ObjectMapper jsonMapper;

    public VersionActivityUpdaterJob() {
        jsonMapper = new ObjectMapper();

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(PRODUCT_OWNER_HISTORY_TABLE_EXISTS_DATE));
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        productOwnerHistoryTableExists = cal.getTime();
        } catch (ParseException ex) {
            LOGGER.fatal("Could not parse " + PRODUCT_OWNER_HISTORY_TABLE_EXISTS_DATE + " as a date.");
        }
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Version Activity Updater job. *********");

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
                if (existingActivityData.getProductId() != null && StringUtils.isEmpty(existingActivityData.getProductName())) {
                    //sometimes we've only filled in a product id, so lookup product with that ID to get the name
                    productAssociatedWithVersionActivityRecord = findProductWithIdOnDate(systemVersionDto.getId(), existingActivityData.getProductId(), activityDto.getActivityDate());
                } else {
                    productAssociatedWithVersionActivityRecord = findProductWithIdOnDate(systemVersionDto.getId(), systemVersionDto.getProductId(), activityDto.getActivityDate());
                }
                //fill in missing data if we can
                if (productAssociatedWithVersionActivityRecord != null && productAssociatedWithVersionActivityRecord.getId() != null
                        && existingActivityData.getProductId() == null) {
                    LOGGER.info("\tSetting missing product id to " + productAssociatedWithVersionActivityRecord.getId());
                    existingActivityData.setProductId(productAssociatedWithVersionActivityRecord.getId());
                }
                if (productAssociatedWithVersionActivityRecord != null && !StringUtils.isEmpty(productAssociatedWithVersionActivityRecord.getName())
                        && StringUtils.isEmpty(existingActivityData.getProductName())) {
                    LOGGER.info("\tSetting missing product name to " + productAssociatedWithVersionActivityRecord.getName());
                    existingActivityData.setProductName(productAssociatedWithVersionActivityRecord.getName());
                }
            }

            if (productAssociatedWithVersionActivityRecord != null && productAssociatedWithVersionActivityRecord.getId() != null
                    && (existingActivityData.getDeveloperId() == null || StringUtils.isEmpty(existingActivityData.getDeveloperName()))) {
                DeveloperDTO developerAssociatedWithVersionActivityRecord = null;
                if (existingActivityData.getDeveloperId() != null && StringUtils.isEmpty(existingActivityData.getDeveloperName())) {
                    //sometimes we've only filled in a developer id, so look up developer with that ID to get the name
                    developerAssociatedWithVersionActivityRecord = findDeveloperWithIdOnDate(existingActivityData.getDeveloperId(), activityDto.getActivityDate());
                } else {
                    //try to find the product's owner at the time of this activity and save that developer
                    developerAssociatedWithVersionActivityRecord = findDeveloperAssociatedWithProductOnDate(productAssociatedWithVersionActivityRecord.getId(), activityDto.getActivityDate());
                }
                //fill in missing data if we can
                if (developerAssociatedWithVersionActivityRecord != null && developerAssociatedWithVersionActivityRecord.getId() != null && existingActivityData.getDeveloperId() == null) {
                    LOGGER.info("\tSetting missing developer id to " + developerAssociatedWithVersionActivityRecord.getId());
                    existingActivityData.setDeveloperId(developerAssociatedWithVersionActivityRecord.getId());
                }
                if (developerAssociatedWithVersionActivityRecord != null && !StringUtils.isEmpty(developerAssociatedWithVersionActivityRecord.getName()) && StringUtils.isEmpty(existingActivityData.getDeveloperName())) {
                    LOGGER.info("\tSetting missing developer name to " + developerAssociatedWithVersionActivityRecord.getName());
                    existingActivityData.setDeveloperName(developerAssociatedWithVersionActivityRecord.getName());
                }
            }
        }
    }

    private ProductDTO findProductWithIdOnDate(Long versionId, Long productId, Date date) {
        //First look to see if there is any activity for a product with this ID that has occurred
        //since the given date. If there is, that means that whatever result we get when we
        //query for this product today is potentially different from what the product data was
        //on the date of the version activity.
        //If there is such product activity, we will try to figure out the name of the product
        //as it was in the system at that time.
        //If there is no such product activity, we can just query for the product by ID
        //and use it the way it looks today.
        ProductDTO product = null;
        List<ActivityDTO> productActivitySinceDate = getProductActivitySinceDate(productId, date);
        if (productActivitySinceDate != null && productActivitySinceDate.size() > 0) {
            LOGGER.info("Product " + productId + " has had activity since " + date.toString());
            ProductDTO productOnActivityDate = getProductOnActivityDate(versionId, productActivitySinceDate, date);
            if (productOnActivityDate == null) {
                LOGGER.warn("Could not determine product data on " + date.toString() + " and will not proceed with this activity.");
            }
            product = productOnActivityDate;
        } else {
            try {
                product = productManager.getById(productId, true);
            } catch (EntityRetrievalException ex) {
                LOGGER.error("Cannot find product with ID " + productId);
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

    private ProductDTO getProductOnActivityDate(Long versionId, List<ActivityDTO> productActivities, Date date) {
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
        ProductDTO productFromOriginalData = parseJsonAsProductDto(productActivity.getOriginalData());
        if (productFromOriginalData == null) {
            List<ProductDTO> productsFromOriginalData = parseJsonAsProductDtoList(productActivity.getOriginalData());
            //this action was a merge - sometimes all the original things had the same name
            //so if they did we can return a product object with the name filled in
            if (productsFromOriginalData != null) {
                ProductDTO productForVersion = getProductForVersion(productsFromOriginalData, versionId);
                if (productForVersion != null) {
                    productFromOriginalData = productForVersion;
                } else if (productsFromOriginalData.stream().map(prod -> prod.getName()).distinct().count() == 1) {
                    productFromOriginalData = ProductDTO.builder()
                            .name(productsFromOriginalData.get(0).getName())
                            .build();
                }
            } else {
                LOGGER.warn("Could not handle product activity JSON: " + productActivity.getOriginalData());
            }
        }
        return productFromOriginalData;
    }

    private ProductDTO getProductForVersion(List<ProductDTO> products, Long versionId) {
        return products.stream()
            .filter(product -> doesProductHaveVersionId(product, versionId))
            .findAny().get();
    }

    private boolean doesProductHaveVersionId(ProductDTO product, Long versionId) {
        if (product.getProductVersions() == null || product.getProductVersions().size() == 0) {
            return false;
        }
        return product.getProductVersions().stream()
            .filter(pv -> pv.getId().equals(versionId))
            .count() == 1;
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
        } else if (product.getLastModifiedDate().getTime() <= date.getTime()) {
            //if the product's last modified date hasn't changed since the activity date
            //then we can use the developer ID associated with it in the DB
            return product.getOwner();
        } else if (date.before(productOwnerHistoryTableExists)) {
            LOGGER.warn("Cannot determine relevant developer/product owner on " + date
                    + " since it is before " + PRODUCT_OWNER_HISTORY_TABLE_EXISTS_DATE + " when the "
                    + "product_owner_history_map table existed.");
            return null;
        } else {
            List<ProductOwnerDTO> productOwnerHistory = productOwnerHistoryDao.getProductOwnerHistoryAsOfDate(product.getId(), date);
            product.setOwnerHistory(productOwnerHistory);
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
            DeveloperDTO developer = null;
            if (productOwnerOnActivityDate != null) {
                Long developerId = productOwnerOnActivityDate.getDeveloper().getId();
                developer = findDeveloperWithIdOnDate(developerId, date);
            }
            return developer;
        }
    }

    private DeveloperDTO findDeveloperWithIdOnDate(Long developerId, Date date) {
        DeveloperDTO developer = null;
        List<ActivityDTO> developerActivitySinceDate = getDeveloperActivitySinceDate(developerId, date);
        if (developerActivitySinceDate != null && developerActivitySinceDate.size() > 0) {
            LOGGER.info("Developer " + developerId + " has had activity since " + date.toString());
            DeveloperDTO developerOnActivityDate = getDeveloperOnActivityDate(developerId, developerActivitySinceDate, date);
            if (developerOnActivityDate == null) {
                LOGGER.warn("Could not determine developer data on " + date.toString() + " and will not proceed with this activity.");
            }
            developer = developerOnActivityDate;
        } else {
            try {
                developer = developerManager.getById(developerId, true);
            } catch (EntityRetrievalException ex) {
                LOGGER.error("Could not look up developer by id " + developerId);
            }
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

    private DeveloperDTO getDeveloperOnActivityDate(Long developerId, List<ActivityDTO> developerActivities, Date date) {
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
        DeveloperDTO developerFromOriginalData = parseJsonAsDeveloperDto(developerActivity.getOriginalData());
        if (developerFromOriginalData == null) {
            List<DeveloperDTO> developersFromOriginalData = parseJsonAsDeveloperDtoList(developerActivity.getOriginalData());
            //this action was a merge - sometimes all the original things had the same name
            //so if they did we can return a developer object with the name filled in
            if (developersFromOriginalData != null && developersFromOriginalData.stream().filter(dev -> dev.getId().equals(developerId)).count() == 1) {
                developerFromOriginalData = developersFromOriginalData.stream()
                        .filter(dev -> dev.getId().equals(developerId))
                        .findAny().get();
            } else if (developersFromOriginalData != null && developersFromOriginalData.stream().map(dev -> dev.getName()).distinct().count() == 1) {
                developerFromOriginalData = DeveloperDTO.builder()
                        .name(developersFromOriginalData.get(0).getName())
                        .build();
            } else {
                LOGGER.warn("Could not handle developer activity JSON: " + developerActivity.getOriginalData());
            }
        }
        return developerFromOriginalData;
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
            LOGGER.debug("Unable to parse '" + activityJson + "' as a ProductDTO object.");
            //This could also be a List of products if a merge or split was done and that isn't being
            //handled by this job.
        }
        return product;
    }

    private List<ProductDTO> parseJsonAsProductDtoList(String activityJson) {
        List<ProductDTO> products = null;
        try {
            products = jsonMapper.readValue(activityJson,
                    jsonMapper.getTypeFactory().constructCollectionType(List.class, ProductDTO.class));
        } catch (Exception ex) {
            LOGGER.debug("Unable to parse '" + activityJson + "' as a List of DeveloperDTO objects.");
            //This could also be a List of developers if a merge or split was done and that isn't being
            //handled by this job.
        }
        return products;
    }

    private DeveloperDTO parseJsonAsDeveloperDto(String activityJson) {
        DeveloperDTO developer = null;
        try {
            developer =
                jsonMapper.readValue(activityJson, DeveloperDTO.class);
        } catch (Exception ex) {
            LOGGER.debug("Unable to parse '" + activityJson + "' as a DeveloperDTO object.");
            //This could also be a List of developers if a merge or split was done and that isn't being
            //handled by this job.
        }
        return developer;
    }

    private List<DeveloperDTO> parseJsonAsDeveloperDtoList(String activityJson) {
        List<DeveloperDTO> developers = null;
        try {
            developers = jsonMapper.readValue(activityJson,
                    jsonMapper.getTypeFactory().constructCollectionType(List.class, DeveloperDTO.class));
        } catch (Exception ex) {
            LOGGER.debug("Unable to parse '" + activityJson + "' as a List of DeveloperDTO objects.");
            //This could also be a List of developers if a merge or split was done and that isn't being
            //handled by this job.
        }
        return developers;
    }

    private void saveActivityUpdates(ActivityDTO activity) {
        activityUpdateDao.update(activity);
    }
}
