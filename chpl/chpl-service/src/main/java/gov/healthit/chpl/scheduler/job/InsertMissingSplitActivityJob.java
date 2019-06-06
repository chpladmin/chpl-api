package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.util.JSONUtils;

/**
 * There are a few product and developer splits that happened in the system before
 * we were correctly adding activity for that action. This job is being temporarily
 * added so we can run it and it will add the missing activity. It is written such that
 * it may be run multiple times without adding duplicate activities.
 *
 * TODO: When removing this class also remember to remove the @Transational annotation in
 * the create method of ActivityDAOImpl.
 * @author kekey
 *
 */
@DisallowConcurrentExecution
public class InsertMissingSplitActivityJob extends QuartzJob implements InterruptableJob {
    private static final Logger LOGGER = LogManager.getLogger("insertMissingSplitActivityJobLogger");
    private boolean interrupted;

    @Autowired
    private DeveloperManager devManager;

    @Autowired
    private ProductManager productManager;

    @Autowired
    private ActivityDAO activityDao;

    /**
     * Default constructor.
     */
    public InsertMissingSplitActivityJob() {
        interrupted = false;
    }

    @Override
    @Transactional
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

       //Developer Split
       //3/20/19: Orion Health (code 2113 and database id 1114) split into Interoperability Bidco (code 3043 and database id 2044)
       //     - User was dconiglio
        try {
            Calendar activityDate = Calendar.getInstance();
            activityDate.set(2019, 2, 20, 18, 50, 27);
            activityDate.set(Calendar.MILLISECOND, 0);
            Long origDeveloperId = 1114L;
            Long newDeveloperId = 2044L;
            Long activityUserId = 14L;
            DeveloperDTO origDeveloper = devManager.getById(origDeveloperId);
            DeveloperDTO afterDeveloper = devManager.getById(newDeveloperId);
            List<DeveloperDTO> splitDevelopers = new ArrayList<DeveloperDTO>();
            splitDevelopers.add(origDeveloper);
            splitDevelopers.add(afterDeveloper);
            String activityDescription = "Split developer " + origDeveloper.getName() + " into "
                    + origDeveloper.getName() + " and " + afterDeveloper.getName();

            insertSplitActivityIfNotExists(activityDate.getTime(), newDeveloperId,
                    origDeveloper, splitDevelopers, activityDescription, activityUserId, ActivityConcept.DEVELOPER);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Exception getting developer info to add to split activity.", ex);
        }

        if (interrupted) {
            return;
        }

        //Product Split
        //11/26/18 MEDITECH 6.1 Emergency Department Management (database id 1503) was split to become MEDITECH 6.1 Emergency Department Management (database id 2991)
        //- User was jodigonzalez
        //- Yes the product names are identical.
        try {
            Calendar activityDate = Calendar.getInstance();
            activityDate.set(2018, 10, 26, 17, 39, 24);
            activityDate.set(Calendar.MILLISECOND, 0);
            Long origProductId = 1503L;
            Long newProductId = 2991L;
            Long activityUserId = 5L;
            ProductDTO origProduct = productManager.getById(origProductId);
            ProductDTO afterProduct = productManager.getById(newProductId);
            List<ProductDTO> splitProducts = new ArrayList<ProductDTO>();
            splitProducts.add(origProduct);
            splitProducts.add(afterProduct);
            String activityDescription = "Split product " + origProduct.getName() + " into "
                    + origProduct.getName() + " and " + afterProduct.getName();

            insertSplitActivityIfNotExists(activityDate.getTime(), newProductId,
                    origProduct, splitProducts, activityDescription, activityUserId, ActivityConcept.PRODUCT);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Exception getting product info to add to split activity.", ex);
        }

        if (interrupted) {
            return;
        }

        //11/26/18 MEDITECH 6.1 Ambulatory  Electronic Health Record (database ID 2791) was split to become MEDITECH Expanse (6.16) Ambulatory (database ID 2990)
        //- User was jodigonzalez
        try {
            Calendar activityDate = Calendar.getInstance();
            activityDate.set(2018, 10, 26, 15, 9, 57);
            activityDate.set(Calendar.MILLISECOND, 0);
            Long origProductId = 2791L;
            Long newProductId = 2990L;
            Long activityUserId = 5L;
            ProductDTO origProduct = productManager.getById(origProductId);
            ProductDTO afterProduct = productManager.getById(newProductId);
            List<ProductDTO> splitProducts = new ArrayList<ProductDTO>();
            splitProducts.add(origProduct);
            splitProducts.add(afterProduct);
            String activityDescription = "Split product " + origProduct.getName() + " into "
                    + origProduct.getName() + " and " + afterProduct.getName();

            insertSplitActivityIfNotExists(activityDate.getTime(), newProductId,
                    origProduct, splitProducts, activityDescription, activityUserId, ActivityConcept.PRODUCT);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Exception getting product info to add to split activity.", ex);
        }

        if (interrupted) {
            return;
        }

        //11/26/18 MEDITECH 6.1 Electronic Health  Record Core HCIS (database ID 2838) was split to become MEDITECH Expanse (6.16) Core HCIS (database ID 2986)
        //- User was jodigonzalez
        try {
            Calendar activityDate = Calendar.getInstance();
            activityDate.set(2018, 10, 26, 14, 38, 14);
            activityDate.set(Calendar.MILLISECOND, 0);
            Long origProductId = 2838L;
            Long newProductId = 2986L;
            Long activityUserId = 5L;
            ProductDTO origProduct = productManager.getById(origProductId);
            ProductDTO afterProduct = productManager.getById(newProductId);
            List<ProductDTO> splitProducts = new ArrayList<ProductDTO>();
            splitProducts.add(origProduct);
            splitProducts.add(afterProduct);
            String activityDescription = "Split product " + origProduct.getName() + " into "
                    + origProduct.getName() + " and " + afterProduct.getName();

            insertSplitActivityIfNotExists(activityDate.getTime(), newProductId,
                    origProduct, splitProducts, activityDescription, activityUserId, ActivityConcept.PRODUCT);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Exception getting product info to add to split activity.", ex);
        }

        if (interrupted) {
            return;
        }

        //11/16/18 EpicCare Ambulatory EHR Suite (database ID 2916) was split to become EpicCare Ambulatory Base (database ID 2980)
        //- User was jodigonzalez
        try {
            Calendar activityDate = Calendar.getInstance();
            activityDate.set(2018, 10, 16, 13, 19, 18);
            activityDate.set(Calendar.MILLISECOND, 0);
            Long origProductId = 2916L;
            Long newProductId = 2980L;
            Long activityUserId = 5L;
            ProductDTO origProduct = productManager.getById(origProductId);
            ProductDTO afterProduct = productManager.getById(newProductId);
            List<ProductDTO> splitProducts = new ArrayList<ProductDTO>();
            splitProducts.add(origProduct);
            splitProducts.add(afterProduct);
            String activityDescription = "Split product " + origProduct.getName() + " into "
                    + origProduct.getName() + " and " + afterProduct.getName();

            insertSplitActivityIfNotExists(activityDate.getTime(), newProductId,
                    origProduct, splitProducts, activityDescription, activityUserId, ActivityConcept.PRODUCT);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Exception getting product info to add to split activity.", ex);
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        this.interrupted = true;
    }

    /**
     * Check first if there is an activity for the object at the specific time.
     * If there is not then insert the split activity.
     * @param activityDate
     * @param activityObjectId
     * @param origData
     * @param newData
     * @param activityDescription
     * @param activityUserId
     * @param concept
     */
    private void insertSplitActivityIfNotExists(final Date activityDate, final Long activityObjectId,
            final Object origData, final Object newData, final String activityDescription,
            final Long activityUserId, final ActivityConcept concept) {
      //check if activity already exists
        List<ActivityDTO> existingActivity =
                activityDao.findByObjectId(activityObjectId, concept, activityDate, activityDate);
        if (existingActivity != null && existingActivity.size() > 0) {
            LOGGER.info(existingActivity.size() + " activity(ies) found for " + concept + " with id "
                    + activityObjectId + " at " + activityDate);
        } else {
            LOGGER.info("No existing activity found for " + concept + " with id "
                    + activityObjectId + " at " + activityDate + ". Inserting new split activity.");
            try {
                String originalDataStr = JSONUtils.toJSON(origData);
                String newDataStr = JSONUtils.toJSON(newData);
                ActivityDTO dto = new ActivityDTO();
                dto.setConcept(concept);
                dto.setId(null);
                dto.setDescription(activityDescription);
                dto.setOriginalData(originalDataStr);
                dto.setNewData(newDataStr);
                dto.setActivityDate(activityDate);
                dto.setActivityObjectId(activityObjectId);
                dto.setCreationDate(new Date());
                dto.setLastModifiedDate(new Date());
                dto.setLastModifiedUser(activityUserId);
                dto.setDeleted(false);
                activityDao.create(dto);
                LOGGER.info("Inserted split activity for " + concept + " " + activityObjectId);
            } catch (JsonProcessingException jsonEx) {
                LOGGER.error("Could not process original or new data as json", jsonEx);
            } catch (EntityCreationException createEx) {
                LOGGER.error("Could not create activity record.", createEx);
            } catch (Exception unknown) {
                LOGGER.error("Caught unexpected exception: " + unknown.getMessage(), unknown);
            }
        }
    }
}
