package gov.healthit.chpl.questionableactivity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.MacraMeasure;
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

    /**
     * Check for QA re: addition to G1 MACRA measures.
     * @param origCertResult original certification criteria result
     * @param newCertResult new certification criteria result
     * @return list of added measures
     */
    public List<QuestionableActivityCertificationResultDTO> checkG1MacraMeasuresAdded(
            final CertificationResult origCertResult, final CertificationResult newCertResult) {

        List<QuestionableActivityCertificationResultDTO> addedMacras
        = new ArrayList<QuestionableActivityCertificationResultDTO>();
        if ((origCertResult.getG1MacraMeasures() == null || origCertResult.getG1MacraMeasures().size() == 0)
                && newCertResult.getG1MacraMeasures() != null && newCertResult.getG1MacraMeasures().size() > 0) {
            //all the newcert g1 macras are "added"
            for (MacraMeasure newMacra : newCertResult.getG1MacraMeasures()) {
                QuestionableActivityCertificationResultDTO activity = new QuestionableActivityCertificationResultDTO();
                activity.setBefore(null);
                activity.setAfter(newMacra.getAbbreviation());
                addedMacras.add(activity);
            }
        } else if (origCertResult.getG1MacraMeasures() != null && origCertResult.getG1MacraMeasures().size() > 0
                && newCertResult.getG1MacraMeasures() != null && newCertResult.getG1MacraMeasures().size() > 0) {
            for (MacraMeasure newMacra : newCertResult.getG1MacraMeasures()) {
                boolean hasMatch = false;
                for (MacraMeasure origMacra : origCertResult.getG1MacraMeasures()) {
                    if (newMacra.getId().longValue() == origMacra.getId().longValue()) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    QuestionableActivityCertificationResultDTO activity
                    = new QuestionableActivityCertificationResultDTO();
                    activity.setBefore(null);
                    activity.setAfter(newMacra.getAbbreviation());
                    addedMacras.add(activity);
                }
            }
        }
        return addedMacras;
    }

    /**
     * Check for QA re: removal of G1 MACRA measures.
     * @param origCertResult original certification criteria result
     * @param newCertResult new certification criteria result
     * @return list of removed measures
     */
     public List<QuestionableActivityCertificationResultDTO> checkG1MacraMeasuresRemoved(
            final CertificationResult origCertResult, final CertificationResult newCertResult) {

        List<QuestionableActivityCertificationResultDTO> removedMacras
        = new ArrayList<QuestionableActivityCertificationResultDTO>();
        if ((newCertResult.getG1MacraMeasures() == null || newCertResult.getG1MacraMeasures().size() == 0)
                 && origCertResult.getG1MacraMeasures() != null && origCertResult.getG1MacraMeasures().size() > 0) {
            //all the origCert g1 macras are "removed"
            for (MacraMeasure origMacra : origCertResult.getG1MacraMeasures()) {
                QuestionableActivityCertificationResultDTO activity = new QuestionableActivityCertificationResultDTO();
                activity.setBefore(origMacra.getAbbreviation());
                activity.setAfter(null);
                removedMacras.add(activity);
            }
        } else if (origCertResult.getG1MacraMeasures() != null && origCertResult.getG1MacraMeasures().size() > 0
                 && newCertResult.getG1MacraMeasures() != null && newCertResult.getG1MacraMeasures().size() > 0) {
            for (MacraMeasure origMacra : origCertResult.getG1MacraMeasures()) {
                boolean hasMatch = false;
                for (MacraMeasure newMacra : newCertResult.getG1MacraMeasures()) {
                    if (newMacra.getId().longValue() == origMacra.getId().longValue()) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    QuestionableActivityCertificationResultDTO activity
                    = new QuestionableActivityCertificationResultDTO();
                    activity.setBefore(origMacra.getAbbreviation());
                    activity.setAfter(null);
                    removedMacras.add(activity);
                }
            }
        }
        return removedMacras;
    }

     /**
      * Check for QA re: addition to G2 MACRA measures.
      * @param origCertResult original certification criteria result
      * @param newCertResult new certification criteria result
      * @return list of added measures
      */
      public List<QuestionableActivityCertificationResultDTO> checkG2MacraMeasuresAdded(
            final CertificationResult origCertResult, final CertificationResult newCertResult) {

        List<QuestionableActivityCertificationResultDTO> addedMacras
        = new ArrayList<QuestionableActivityCertificationResultDTO>();
        if ((origCertResult.getG2MacraMeasures() == null || origCertResult.getG2MacraMeasures().size() == 0)
                 && newCertResult.getG2MacraMeasures() != null && newCertResult.getG2MacraMeasures().size() > 0) {
            //all the newCert G2 macras are "added"
            for (MacraMeasure newMacra : newCertResult.getG2MacraMeasures()) {
                QuestionableActivityCertificationResultDTO activity = new QuestionableActivityCertificationResultDTO();
                activity.setBefore(null);
                activity.setAfter(newMacra.getAbbreviation());
                addedMacras.add(activity);
            }
        } else if (origCertResult.getG2MacraMeasures() != null && origCertResult.getG2MacraMeasures().size() > 0
                 && newCertResult.getG2MacraMeasures() != null && newCertResult.getG2MacraMeasures().size() > 0) {
            for (MacraMeasure newMacra : newCertResult.getG2MacraMeasures()) {
                boolean hasMatch = false;
                for (MacraMeasure origMacra : origCertResult.getG2MacraMeasures()) {
                    if (newMacra.getId().longValue() == origMacra.getId().longValue()) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    QuestionableActivityCertificationResultDTO activity
                    = new QuestionableActivityCertificationResultDTO();
                    activity.setBefore(null);
                    activity.setAfter(newMacra.getAbbreviation());
                    addedMacras.add(activity);
                }
            }
        }
        return addedMacras;
    }

      /**
       * Check for QA re: removal of G2 MACRA measures.
       * @param origCertResult original certification criteria result
       * @param newCertResult new certification criteria result
       * @return list of removed measures
       */
       public List<QuestionableActivityCertificationResultDTO> checkG2MacraMeasuresRemoved(
            final CertificationResult origCertResult, final CertificationResult newCertResult) {

        List<QuestionableActivityCertificationResultDTO> removedMacras
        = new ArrayList<QuestionableActivityCertificationResultDTO>();
        if ((newCertResult.getG2MacraMeasures() == null || newCertResult.getG2MacraMeasures().size() == 0)
                 && origCertResult.getG2MacraMeasures() != null && origCertResult.getG2MacraMeasures().size() > 0) {
            //all the origCert G2 macras are "removed"
            for (MacraMeasure origMacra : origCertResult.getG2MacraMeasures()) {
                QuestionableActivityCertificationResultDTO activity = new QuestionableActivityCertificationResultDTO();
                activity.setBefore(origMacra.getAbbreviation());
                activity.setAfter(null);
                removedMacras.add(activity);
            }
        } else if (origCertResult.getG2MacraMeasures() != null && origCertResult.getG2MacraMeasures().size() > 0
                 && newCertResult.getG2MacraMeasures() != null && newCertResult.getG2MacraMeasures().size() > 0) {
            for (MacraMeasure origMacra : origCertResult.getG2MacraMeasures()) {
                boolean hasMatch = false;
                for (MacraMeasure newMacra : newCertResult.getG2MacraMeasures()) {
                    if (newMacra.getId().longValue() == origMacra.getId().longValue()) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    QuestionableActivityCertificationResultDTO activity
                    = new QuestionableActivityCertificationResultDTO();
                    activity.setBefore(origMacra.getAbbreviation());
                    activity.setAfter(null);
                    removedMacras.add(activity);
                }
            }
        }
        return removedMacras;
    }
}
