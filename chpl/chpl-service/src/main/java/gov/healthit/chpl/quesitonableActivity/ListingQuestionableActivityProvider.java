package gov.healthit.chpl.quesitonableActivity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;

@Component
public class ListingQuestionableActivityProvider {
    
    public QuestionableActivityListingDTO check2011EditionUpdated(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        
        QuestionableActivityListingDTO activity = null;
        if (origListing.getCertificationEdition().get("name").equals("2011")) {
              activity = new QuestionableActivityListingDTO();
              activity.setBefore(null);
              activity.setAfter(null);
        }
        
        return activity;
    }
    
    public QuestionableActivityListingDTO checkCertificationStatusUpdated(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        
        QuestionableActivityListingDTO activity = null;
        if (!origListing.getCertificationStatus().get("id")
                .equals(newListing.getCertificationStatus().get("id"))) {
              activity = new QuestionableActivityListingDTO();
              activity.setBefore(origListing.getCertificationStatus().get("name").toString());
              activity.setAfter(newListing.getCertificationStatus().get("name").toString());
        }
        
        return activity;
    }
    
    public List<QuestionableActivityListingDTO> checkCqmsAdded(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        
        List<QuestionableActivityListingDTO> cqmAddedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCqmResults() != null && origListing.getCqmResults().size() > 0 && 
                newListing.getCqmResults() != null && newListing.getCqmResults().size() > 0) {
            //all cqms are in the details so find the same one in the orig and new objects
            //based on cms id and compare the success boolean to see if one was added
            for (CQMResultDetails origCqm : origListing.getCqmResults()) {
                for (CQMResultDetails newCqm : newListing.getCqmResults()) {
                    if (StringUtils.isEmpty(newCqm.getCmsId())
                            && StringUtils.isEmpty(origCqm.getCmsId())
                            && !StringUtils.isEmpty(newCqm.getNqfNumber())
                            && !StringUtils.isEmpty(origCqm.getNqfNumber())
                            && !newCqm.getNqfNumber().equals("N/A") && !origCqm.getNqfNumber().equals("N/A")
                            && newCqm.getNqfNumber().equals(origCqm.getNqfNumber())) {
                        // NQF is the same if the NQF numbers are equal
                        if (origCqm.isSuccess() == Boolean.FALSE && newCqm.isSuccess() == Boolean.TRUE) {
                            //orig did not have this cqm but new does so it was added
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(null);
                            activity.setAfter(newCqm.getCmsId() != null ? newCqm.getCmsId() : newCqm.getNqfNumber());
                            cqmAddedActivities.add(activity);
                        }
                        break;
                    } else if (newCqm.getCmsId() != null && origCqm.getCmsId() != null
                            && newCqm.getCmsId().equals(origCqm.getCmsId())) {
                        // CMS is the same if the CMS ID and version is equal
                        if (origCqm.isSuccess() == Boolean.FALSE && newCqm.isSuccess() == Boolean.TRUE) {
                            //orig did not have this cqm but new does so it was added
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(null);
                            activity.setAfter(newCqm.getCmsId() != null ? newCqm.getCmsId() : newCqm.getNqfNumber());
                            cqmAddedActivities.add(activity);
                        }
                        break;
                    }
                }
            }
        }
        
        return cqmAddedActivities;
    }
    
    public List<QuestionableActivityListingDTO> checkCqmsRemoved(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        
        List<QuestionableActivityListingDTO> cqmRemovedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCqmResults() != null && origListing.getCqmResults().size() > 0 && 
                newListing.getCqmResults() != null && newListing.getCqmResults().size() > 0) {
            //all cqms are in the details so find the same one in the orig and new objects
            //based on cms id and compare the success boolean to see if one was removed
            for (CQMResultDetails origCqm : origListing.getCqmResults()) {
                for (CQMResultDetails newCqm : newListing.getCqmResults()) {
                    if (StringUtils.isEmpty(newCqm.getCmsId())
                            && StringUtils.isEmpty(origCqm.getCmsId())
                            && !StringUtils.isEmpty(newCqm.getNqfNumber())
                            && !StringUtils.isEmpty(origCqm.getNqfNumber())
                            && !newCqm.getNqfNumber().equals("N/A") && !origCqm.getNqfNumber().equals("N/A")
                            && newCqm.getNqfNumber().equals(origCqm.getNqfNumber())) {
                        // NQF is the same if the NQF numbers are equal
                        if (origCqm.isSuccess() == Boolean.TRUE && newCqm.isSuccess() == Boolean.FALSE) {
                            //orig did have this cqm but new does not so it was removed
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(origCqm.getCmsId() != null ? origCqm.getCmsId() : origCqm.getNqfNumber());
                            activity.setAfter(null);
                            cqmRemovedActivities.add(activity);
                        }
                        break;
                    } else if (newCqm.getCmsId() != null && origCqm.getCmsId() != null
                            && newCqm.getCmsId().equals(origCqm.getCmsId())) {
                        // CMS is the same if the CMS ID and version is equal
                        if (origCqm.isSuccess() == Boolean.TRUE && newCqm.isSuccess() == Boolean.FALSE) {
                            //orig did not have this cqm but new does so it was added
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(origCqm.getCmsId() != null ? origCqm.getCmsId() : origCqm.getNqfNumber());
                            activity.setAfter(null);
                            cqmRemovedActivities.add(activity);
                        }
                        break;
                    }
                }
            }
        }
        
        return cqmRemovedActivities;
    }
    
    public List<QuestionableActivityListingDTO> checkCertificationsAdded(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        
        List<QuestionableActivityListingDTO> certAddedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0 && 
                newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            //all cert results are in the details so find the same one in the orig and new objects
            //based on number and compare the success boolean to see if one was added
            for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                    if (origCertResult.getNumber().equals(newCertResult.getNumber())) {
                        if (origCertResult.isSuccess() == Boolean.FALSE && newCertResult.isSuccess() == Boolean.TRUE) {
                            //orig did not have this cert result but new does so it was added
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(null);
                            activity.setAfter(newCertResult.getNumber());
                            certAddedActivities.add(activity);
                        }
                        break;
                    }
                }
            }
        }

        return certAddedActivities;
    }
    
    public List<QuestionableActivityListingDTO> checkCertificationsRemoved(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        
        List<QuestionableActivityListingDTO> certRemovedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0 && 
                newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            //all cert results are in the details so find the same one in the orig and new objects
            //based on number and compare the success boolean to see if one was removed
            for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                    if (origCertResult.getNumber().equals(newCertResult.getNumber())) {
                        if (origCertResult.isSuccess() == Boolean.TRUE && newCertResult.isSuccess() == Boolean.FALSE) {
                            //orig did have this cert result but new does not so it was removed
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(origCertResult.getNumber());
                            activity.setAfter(null);
                            certRemovedActivities.add(activity);
                        }
                        break;
                    }
                }
            }
        }
        
        return certRemovedActivities;
    }
    
    public QuestionableActivityListingDTO checkSurveillanceDeleted(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        
        QuestionableActivityListingDTO activity = null;
        if(origListing.getSurveillance() != null && origListing.getSurveillance().size() > 0 && 
                (newListing.getSurveillance() == null || 
                 newListing.getSurveillance().size() < origListing.getSurveillance().size())) {
              
              activity = new QuestionableActivityListingDTO();
              //TODO: should we find the specific surveillance here? maybe put a count of the number deleted?
              activity.setBefore(null);
              activity.setAfter(null);
        }
        
        return activity;
    }
}
