package gov.healthit.chpl.listener;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityManager;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
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

    public void checkQuestionableActivity(ActivityDTO activity, Object originalData, Object newData) {
        checkQuestionableActivity(activity, originalData, newData, null);
    }

    public void checkQuestionableActivity(ActivityDTO activity, Object originalData, Object newData, String reason) {

        if (newData instanceof CertifiedProductSearchDetails) {
            checkQuestionableActivityForListingEdit(activity, (CertifiedProductSearchDetails) originalData, (CertifiedProductSearchDetails) newData, reason);
        } else if (areAllValuesNonNull(originalData, newData) && originalData instanceof Developer
                && newData instanceof Developer) {
            checkQuestionableActivityForDeveloper(activity, (Developer) originalData, (Developer) newData);
        } else if (areAllValuesNonNull(originalData, newData) && originalData instanceof Product
                && newData instanceof Product) {
            checkQuestionableActivityForProduct(activity, (Product) originalData, (Product) newData);
        } else if (areAllValuesNonNull(originalData, newData) && originalData instanceof ProductVersionDTO
                && newData instanceof ProductVersionDTO) {
            checkQuestionableActivityForVersion(activity, (ProductVersionDTO) originalData, (ProductVersionDTO) newData);
        }
    }

    private void checkQuestionableActivityForListingEdit(ActivityDTO activity,
            CertifiedProductSearchDetails originalData, CertifiedProductSearchDetails newData, String reason) {

        if (newData == null || AuthUtil.getCurrentUser() == null) {
            return;
        }

        // look for any of the listing questionable activity
        questionableActivityManager.checkListingQuestionableActivity(originalData, newData, activity, reason);

        // check for cert result questionable activity
        // outside of the acceptable activity threshold
        // get confirm date of the listing to check against the threshold
        Date confirmDate = listingDao.getConfirmDate(newData.getId());
        if (originalData != null &&
                confirmDate != null && activity.getActivityDate() != null
                && (activity.getActivityDate().getTime() - confirmDate.getTime() > listingActivityThresholdMillis)) {

            // look for certification result questionable activity
            if (originalData.getCertificationResults() != null && originalData.getCertificationResults().size() > 0
                    && newData.getCertificationResults() != null
                    && newData.getCertificationResults().size() > 0) {

                // all cert results are in the details so find matches based on the
                // original and new criteria number fields
                for (CertificationResult origCertResult : originalData.getCertificationResults()) {
                    for (CertificationResult newCertResult : newData.getCertificationResults()) {
                        if (origCertResult.getCriterion().getId().equals(newCertResult.getCriterion().getId())) {
                            questionableActivityManager.checkCertificationResultQuestionableActivity(origCertResult,
                                    newCertResult, activity, reason);
                        }
                    }
                }
            }
        }
    }

    private void checkQuestionableActivityForDeveloper(ActivityDTO activity, Developer originalDeveloper, Developer newDeveloper) {
        if (originalDeveloper == null || newDeveloper == null || AuthUtil.getCurrentUser() == null) {
            return;
        }
        questionableActivityManager.checkDeveloperQuestionableActivity(originalDeveloper, newDeveloper, activity);
    }

    private void checkQuestionableActivityForProduct(ActivityDTO activity, Product originalProduct, Product newProduct) {
        if (originalProduct == null || newProduct == null || AuthUtil.getCurrentUser() == null) {
            return;
        }
        questionableActivityManager.checkProductQuestionableActivity(originalProduct, newProduct, activity);
    }

    private void checkQuestionableActivityForVersion(ActivityDTO activity, ProductVersionDTO originalVersion, ProductVersionDTO newVersion) {
        if (originalVersion == null || newVersion == null || AuthUtil.getCurrentUser() == null) {
            return;
        }
        questionableActivityManager.checkVersionQuestionableActivity(originalVersion, newVersion, activity);
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
