package gov.healthit.chpl.aspect;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.manager.QuestionableActivityManager;

/**
 * @author TYoung
 * Checks for and adds Questionable Activity when appropriate.
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
     * @param env - Environment
     * @param listingDao - CertifiedProductDAO
     * @param questionableActivityManager - QuestionableActivityManager
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

    /**
     * Any activity added with a reason would be handled here.
     * @param concept - ActivityConcept
     * @param objectId - Long
     * @param activityDescription - String
     * @param originalData - Object
     * @param newData - Object
     */
    @After("execution(* gov.healthit.chpl.manager.impl.ActivityManagerImpl.addActivity(..)) && "
            + "args(concept,objectId,activityDescription,originalData,newData,..)")
    public void checkQuestionableActivity(final ActivityConcept concept, final Long objectId,
            final String activityDescription, final Object originalData, final Object newData) {
        LOGGER.info("Called QuestionableActivityAspect2.checkQuestionableActivity()");
        if (originalData == null || newData == null
                || !originalData.getClass().equals(newData.getClass())
                || Util.getCurrentUser() == null) {
            return;
        }

        //all questionable activity from this action should have the exact same date and user id
        Date activityDate = new Date();
        Long activityUser = Util.getCurrentUser().getId();

        if (originalData instanceof DeveloperDTO && newData instanceof DeveloperDTO) {
            DeveloperDTO origDeveloper = (DeveloperDTO) originalData;
            DeveloperDTO newDeveloper = (DeveloperDTO) newData;
            questionableActivityManager.checkDeveloperQuestionableActivity(
                    origDeveloper, newDeveloper, activityDate, activityUser);
        } else if (originalData instanceof ProductDTO && newData instanceof ProductDTO) {
            ProductDTO origProduct = (ProductDTO) originalData;
            ProductDTO newProduct = (ProductDTO) newData;
            questionableActivityManager.checkProductQuestionableActivity(
                    origProduct, newProduct, activityDate, activityUser);
        } else if (originalData instanceof ProductVersionDTO && newData instanceof ProductVersionDTO) {
            ProductVersionDTO origVersion = (ProductVersionDTO) originalData;
            ProductVersionDTO newVersion = (ProductVersionDTO) newData;
            questionableActivityManager.checkVersionQuestionableActivity(
                    origVersion, newVersion, activityDate, activityUser);
        }
    }

}
