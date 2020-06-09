package gov.healthit.chpl.listener;

import java.util.Date;

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
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@Aspect
public class QuestionableActivityListener implements EnvironmentAware {
    private Environment env;
    private CertifiedProductDAO listingDao;
    private QuestionableActivityManager questionableActivityManager;

    private long listingActivityThresholdMillis = -1;
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

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

    public void checkQuestionableActivity(ActivityConcept concept, Long objectId, String activityDescription,
            Object originalData, Object newData) {
        checkQuestionableActivity(concept, objectId, activityDescription, originalData, newData, null);
    }

    public void checkQuestionableActivity(ActivityConcept concept, Long objectId, String activityDescription,
            Object originalData, Object newData, String reason) {

        if (newData instanceof CertifiedProductSearchDetails) {
            checkQuestionableActivityForListingEdit(concept, objectId, activityDescription,
                    (CertifiedProductSearchDetails) originalData, (CertifiedProductSearchDetails) newData, reason);
        } else if (areAllValuesNonNull(originalData, newData) && originalData instanceof DeveloperDTO
                && newData instanceof DeveloperDTO) {
            checkQuestionableActivityForDeveloper(concept, objectId, activityDescription, (DeveloperDTO) originalData,
                    (DeveloperDTO) newData);
        } else if (areAllValuesNonNull(originalData, newData) && originalData instanceof ProductDTO
                && newData instanceof ProductDTO) {
            checkQuestionableActivityForProduct(concept, objectId, activityDescription, (ProductDTO) originalData,
                    (ProductDTO) newData);
        } else if (areAllValuesNonNull(originalData, newData) && originalData instanceof ProductVersionDTO
                && newData instanceof ProductVersionDTO) {
            checkQuestionableActivityForVersion(concept, objectId, activityDescription, (ProductVersionDTO) originalData,
                    (ProductVersionDTO) newData);
        }
    }

    private void checkQuestionableActivityForListingEdit(ActivityConcept concept, Long objectId, String activityDescription,
            CertifiedProductSearchDetails originalData, CertifiedProductSearchDetails newData, String reason) {

        if (originalData == null || newData == null || AuthUtil.getCurrentUser() == null) {
            return;
        }

        // all questionable activity from this action should have the exact same date and user id
        Date activityDate = new Date();
        Long activityUser = AuthUtil.getAuditId();

        // look for any of the listing questionable activity
        questionableActivityManager.checkListingQuestionableActivityOnEdit(
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

    private void checkQuestionableActivityForDeveloper(ActivityConcept concept, Long objectId, String activityDescription,
            DeveloperDTO originalDeveloper, DeveloperDTO newDeveloper) {

        if (originalDeveloper == null || newDeveloper == null || AuthUtil.getCurrentUser() == null) {
            return;
        }

        // all questionable activity from this action should have the exact same date and user id
        Date activityDate = new Date();
        Long activityUser = AuthUtil.getAuditId();

        questionableActivityManager.checkDeveloperQuestionableActivity(originalDeveloper, newDeveloper, activityDate,
                activityUser);
    }

    private void checkQuestionableActivityForProduct(ActivityConcept concept, Long objectId, String activityDescription,
            ProductDTO originalProduct, ProductDTO newProduct) {

        if (originalProduct == null || newProduct == null || AuthUtil.getCurrentUser() == null) {
            return;
        }

        // all questionable activity from this action should have the exact same date and user id
        Date activityDate = new Date();
        Long activityUser = AuthUtil.getAuditId();

        questionableActivityManager.checkProductQuestionableActivity(originalProduct, newProduct, activityDate, activityUser);
    }

    private void checkQuestionableActivityForVersion(ActivityConcept concept, Long objectId, String activityDescription,
            ProductVersionDTO originalVersion, ProductVersionDTO newVersion) {

        if (originalVersion == null || newVersion == null || AuthUtil.getCurrentUser() == null) {
            return;
        }

        // all questionable activity from this action should have the exact same date and user id
        Date activityDate = new Date();
        Long activityUser = AuthUtil.getAuditId();

        questionableActivityManager.checkVersionQuestionableActivity(originalVersion, newVersion, activityDate, activityUser);
    }

    private boolean areAllValuesNonNull(Object... values) {
        for (Object value : values) {
            if (value == null) {
                return false;
            }
        }
        return true;
    }

}
