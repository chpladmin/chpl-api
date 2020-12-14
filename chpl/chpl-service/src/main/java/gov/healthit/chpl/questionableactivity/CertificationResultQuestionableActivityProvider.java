package gov.healthit.chpl.questionableactivity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;

/**
 * Checker for certification criteria questionable activity.
 */
@Component
public class CertificationResultQuestionableActivityProvider {

    /**
     * Check for QA re: change to G1 success value.
     * @param origCertResult original certification criteria result
     * @param newCertResult new certification criteria result
     * @return DTO iff there was questionable activity
     */
    public QuestionableActivityCertificationResultDTO checkG1SuccessUpdated(
            final CertificationResult origCertResult, final CertificationResult newCertResult) {

        QuestionableActivityCertificationResultDTO activity = null;
        if (origCertResult.isG1Success() != null || newCertResult.isG1Success() != null) {
            if ((origCertResult.isG1Success() == null && newCertResult.isG1Success() != null)
                    || (!origCertResult.isG1Success() && newCertResult.isG1Success())) {
                //g1 success changed to true
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setBefore(Boolean.FALSE.toString());
                activity.setAfter(Boolean.TRUE.toString());
            } else if ((origCertResult.isG1Success() != null && newCertResult.isG1Success() == null)
                    || (origCertResult.isG1Success() && !newCertResult.isG1Success())) {
                //g1 success changed to false
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setBefore(Boolean.TRUE.toString());
                activity.setAfter(Boolean.FALSE.toString());
            }
        }
        return activity;
    }

    /**
     * Check for QA re: change to G2 success value.
     * @param origCertResult original certification criteria result
     * @param newCertResult new certification criteria result
     * @return DTO iff there was questionable activity
     */
    public QuestionableActivityCertificationResultDTO checkG2SuccessUpdated(
            final CertificationResult origCertResult, final CertificationResult newCertResult) {

        QuestionableActivityCertificationResultDTO activity = null;
        if (origCertResult.isG2Success() != null || newCertResult.isG2Success() != null) {
            if ((origCertResult.isG2Success() == null && newCertResult.isG2Success() != null)
                    || (!origCertResult.isG2Success() && newCertResult.isG2Success())) {
                //g2 success changed to true
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setBefore(Boolean.FALSE.toString());
                activity.setAfter(Boolean.TRUE.toString());
            } else if ((origCertResult.isG2Success() != null && newCertResult.isG2Success() == null)
                    || (origCertResult.isG2Success() && !newCertResult.isG2Success())) {
                //g2 success changed to false
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setBefore(Boolean.TRUE.toString());
                activity.setAfter(Boolean.FALSE.toString());
            }
        }

        return activity;
    }

    /**
     * Check for QA re: change to gap value.
     * @param origCertResult original certification criteria result
     * @param newCertResult new certification criteria result
     * @return DTO iff there was questionable activity
     */
    public QuestionableActivityCertificationResultDTO checkGapUpdated(
            final CertificationResult origCertResult, final CertificationResult newCertResult) {

        QuestionableActivityCertificationResultDTO activity = null;
        if (origCertResult.isGap() != null || newCertResult.isGap() != null) {
            if ((origCertResult.isGap() == null && newCertResult.isGap() != null)
                    ||  (!origCertResult.isGap() && newCertResult.isGap())) {
                //gap changed to true
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setBefore(Boolean.FALSE.toString());
                activity.setAfter(Boolean.TRUE.toString());
            } else if ((origCertResult.isGap() != null && newCertResult.isGap() == null)
                    || (origCertResult.isGap() && !newCertResult.isGap())) {
                //gap changed to false
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setBefore(Boolean.TRUE.toString());
                activity.setAfter(Boolean.FALSE.toString());
            }
        }
        return activity;
    }
}
