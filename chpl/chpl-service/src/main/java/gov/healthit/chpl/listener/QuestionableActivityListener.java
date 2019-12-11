package gov.healthit.chpl.listener;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.manager.QuestionableActivityManager;
import gov.healthit.chpl.util.AuthUtil;

/**
 * Checks for and adds Questionable Activity when appropriate.
 * 
 * @author TYoung
 *
 */
@Component
@Aspect
public class QuestionableActivityListener implements EnvironmentAware {
    private static final Logger LOGGER = LogManager.getLogger(QuestionableActivityListener.class);
    private Environment env;
    private CertifiedProductDAO listingDao;
    private QuestionableActivityManager questionableActivityManager;

    private long listingActivityThresholdMillis = -1;
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    /**
     * Autowired constructor for dependency injection.
     * 
     * @param env
     *            - Environment
     * @param listingDao
     *            - CertifiedProductDAO
     * @param questionableActivityManager
     *            - QuestionableActivityManager
     */
    @Autowired
    public QuestionableActivityListener(final Environment env, final CertifiedProductDAO listingDao,
            final QuestionableActivityManager questionableActivityManager) {
        this.env = env;
        this.listingDao = listingDao;
        this.questionableActivityManager = questionableActivityManager;
    }

    @Override
    public void setEnvironment(final Environment e) {
        this.env = e;
        String activityThresholdDaysStr = env.getProperty("questionableActivityThresholdDays");
        int activityThresholdDays = Integer.parseInt(activityThresholdDaysStr);
        listingActivityThresholdMillis = activityThresholdDays * MILLIS_PER_DAY;
    }

    // By "typing" the originalData and newData parameters in the method signature, the AOP advice will
    // only intercept when the parameters are of type 'CertifiedProductSearchDetails'.
    @After("execution(* gov.healthit.chpl.manager.impl.ActivityManagerImpl.addActivity(..)) && "
            + "args(concept,objectId,activityDescription,originalData,newData,reason,..)")
    public void checkQuestionableActivityForListingEdit(ActivityConcept concept, Long objectId, String activityDescription,
            CertifiedProductSearchDetails originalData, CertifiedProductSearchDetails newData, String reason) {

        if (originalData == null || newData == null
                || !originalData.getClass().equals(newData.getClass())
                || AuthUtil.getCurrentUser() == null) {
            return;
        }

        // all questionable activity from this action should have the exact same date and user id
        Date activityDate = new Date();
        Long activityUser = AuthUtil.getAuditId();

        // look for any of the listing questionable activity
        questionableActivityManager.checkListingQuestionableActivity(
                originalData, newData, activityDate, activityUser, reason);

        // check for cert result questionable activity
        // outside of the acceptable activity threshold
        // get confirm date of the listing to check against the threshold
        Date confirmDate = listingDao.getConfirmDate(originalData.getId());
        if (confirmDate != null && newData.getLastModifiedDate() != null
                && (newData.getLastModifiedDate().longValue()
                        - confirmDate.getTime() > listingActivityThresholdMillis)) {

            // look for certification result questionable activity
            if (originalData.getCertificationResults() != null && originalData.getCertificationResults().size() > 0
                    && newData.getCertificationResults() != null
                    && newData.getCertificationResults().size() > 0) {

                // all cert results are in the details so find matches based on the
                // original and new criteria number fields
                for (CertificationResult origCertResult : originalData.getCertificationResults()) {
                    for (CertificationResult newCertResult : newData.getCertificationResults()) {
                        if (origCertResult.getNumber().equals(newCertResult.getNumber())) {
                            questionableActivityManager.checkCertificationResultQuestionableActivity(origCertResult,
                                    newCertResult,
                                    activityDate, activityUser, reason);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds Questionable Activity for Developer, Product, or Version when appropriate
     * 
     * @param concept
     *            - ActivityConcept
     * @param objectId
     *            - Long
     * @param activityDescription
     *            - String
     * @param originalData
     *            - Object (DeveloperDTO, ProductDTO, or ProductVersionDTO)
     * @param newData
     *            - Object (DeveloperDTO, ProductDTO, or ProductVersionDTO)
     */
    @After("execution(* gov.healthit.chpl.manager.impl.ActivityManagerImpl.addActivity(..)) && "
            + "args(concept,objectId,activityDescription,originalData,newData,..)")
    public void checkQuestionableActivity(final ActivityConcept concept,
            final Long objectId, final String activityDescription, final Object originalData, final Object newData) {
        if (originalData == null || newData == null
                || !originalData.getClass().equals(newData.getClass())
                || AuthUtil.getCurrentUser() == null) {
            return;
        }

        // all questionable activity from this action should have the exact same date and user id
        Date activityDate = new Date();
        Long activityUser = AuthUtil.getAuditId();

        if (originalData instanceof DeveloperDTO && newData instanceof DeveloperDTO) {
            DeveloperDTO origDeveloper = (DeveloperDTO) originalData;
            DeveloperDTO newDeveloper = (DeveloperDTO) newData;
            questionableActivityManager.checkDeveloperQuestionableActivity(origDeveloper, newDeveloper, activityDate,
                    activityUser);
        } else if (originalData instanceof ProductDTO && newData instanceof ProductDTO) {
            ProductDTO origProduct = (ProductDTO) originalData;
            ProductDTO newProduct = (ProductDTO) newData;
            questionableActivityManager.checkProductQuestionableActivity(origProduct, newProduct, activityDate,
                    activityUser);
        } else if (originalData instanceof ProductVersionDTO && newData instanceof ProductVersionDTO) {
            ProductVersionDTO origVersion = (ProductVersionDTO) originalData;
            ProductVersionDTO newVersion = (ProductVersionDTO) newData;
            questionableActivityManager.checkVersionQuestionableActivity(origVersion, newVersion, activityDate,
                    activityUser);
        }
    }

}
