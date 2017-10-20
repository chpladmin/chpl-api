package gov.healthit.chpl.quesitonableActivity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;

@Component
public class ListingQuestionableActivityProvider {
    
    public QuestionableActivityListingDTO check2011EditionUpdated(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        
        QuestionableActivityListingDTO activity = null;
        if (origListing.getCertificationEdition().get("name").equals("2011")) {
              activity = new QuestionableActivityListingDTO();
              activity.setMessage("TRUE");
        }
        
        return activity;
    }
    
    public QuestionableActivityListingDTO checkCertificationStatusUpdated(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        
        QuestionableActivityListingDTO activity = null;
        if (!origListing.getCertificationStatus().get("id")
                .equals(newListing.getCertificationStatus().get("id"))) {
              activity = new QuestionableActivityListingDTO();
              activity.setMessage("From " + origListing.getCertificationStatus().get("name").toString() + 
                      " to " + newListing.getCertificationStatus().get("name").toString());
        }
        
        return activity;
    }
    
    public List<QuestionableActivityListingDTO> checkCqmsAdded(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        
        List<QuestionableActivityListingDTO> cqmAddedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if ((origListing.getCqmResults() == null || origListing.getCqmResults().size() == 0) && 
                newListing.getCqmResults() != null && newListing.getCqmResults().size() > 0) {
            //all the newListing cqms are "added"
            for(CQMResultDetails newCqm : newListing.getCqmResults()) {
                QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                activity.setMessage(newCqm.getCmsId());
                cqmAddedActivities.add(activity);
            }
        } else if (origListing.getCqmResults() != null && origListing.getCqmResults().size() > 0 && 
                newListing.getCqmResults() != null && newListing.getCqmResults().size() > 0) {
            //all cqms are in the details so find the same one in the orig and new objects
            //based on cms id and compare the success boolean to see if one was added
            for (CQMResultDetails origCqm : origListing.getCqmResults()) {
                for (CQMResultDetails newCqm : newListing.getCqmResults()) {
                    if (origCqm.getCmsId().equals(newCqm.getCmsId())) {
                        if (origCqm.isSuccess() == Boolean.FALSE && newCqm.isSuccess() == Boolean.TRUE) {
                            //orig did not have this cqm but new does so it was added
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setMessage(newCqm.getCmsId());
                            cqmAddedActivities.add(activity);
                        }
                    }
                }
            }
        }
        
        return cqmAddedActivities;
    }
    
    public List<QuestionableActivityListingDTO> checkCqmsRemoved(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        
        List<QuestionableActivityListingDTO> cqmRemovedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if ((newListing.getCqmResults() == null || newListing.getCqmResults().size() == 0) && 
                origListing.getCqmResults() != null && origListing.getCqmResults().size() > 0) {
            //all the origListing cqms are "removed"
            for(CQMResultDetails origCqm : origListing.getCqmResults()) {
                QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                activity.setMessage(origCqm.getCmsId());
                cqmRemovedActivities.add(activity);
            }
        } else if (origListing.getCqmResults() != null && origListing.getCqmResults().size() > 0 && 
                newListing.getCqmResults() != null && newListing.getCqmResults().size() > 0) {
            //all cqms are in the details so find the same one in the orig and new objects
            //based on cms id and compare the success boolean to see if one was removed
            for (CQMResultDetails origCqm : origListing.getCqmResults()) {
                for (CQMResultDetails newCqm : newListing.getCqmResults()) {
                    if (origCqm.getCmsId().equals(newCqm.getCmsId())) {
                        if (origCqm.isSuccess() == Boolean.TRUE && newCqm.isSuccess() == Boolean.FALSE) {
                            //orig did have this cqm but new does not so it was removed
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setMessage(origCqm.getCmsId());
                            cqmRemovedActivities.add(activity);
                        }
                    }
                }
            }
        }
        
        return cqmRemovedActivities;
    }
    
    public List<QuestionableActivityListingDTO> checkCertificationsAdded(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        
        List<QuestionableActivityListingDTO> certAddedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if ((origListing.getCertificationResults() == null || origListing.getCertificationResults().size() == 0) && 
                newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            //all the newListing cert results are "added"
            for(CertificationResult newCertResult : newListing.getCertificationResults()) {
                QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                activity.setMessage(newCertResult.getNumber());
                certAddedActivities.add(activity);
            }
        } else if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0 && 
                newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            //all cert results are in the details so find the same one in the orig and new objects
            //based on number and compare the success boolean to see if one was added
            for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                    if (origCertResult.getNumber().equals(newCertResult.getNumber())) {
                        if (origCertResult.isSuccess() == Boolean.FALSE && newCertResult.isSuccess() == Boolean.TRUE) {
                            //orig did not have this cert result but new does so it was added
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setMessage(newCertResult.getNumber());
                            certAddedActivities.add(activity);
                        }
                    }
                }
            }
        }

        return certAddedActivities;
    }
    
    public List<QuestionableActivityListingDTO> checkCertificationsRemoved(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        
        List<QuestionableActivityListingDTO> certRemovedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if ((newListing.getCertificationResults() == null || newListing.getCertificationResults().size() == 0) && 
                origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0) {
            //all the origListing cert results are "removed"
            for(CertificationResult origCertResult : origListing.getCertificationResults()) {
                QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                activity.setMessage(origCertResult.getNumber());
                certRemovedActivities.add(activity);
            }
        } else if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0 && 
                newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            //all cert results are in the details so find the same one in the orig and new objects
            //based on number and compare the success boolean to see if one was removed
            for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                    if (origCertResult.getNumber().equals(newCertResult.getNumber())) {
                        if (origCertResult.isSuccess() == Boolean.TRUE && newCertResult.isSuccess() == Boolean.FALSE) {
                            //orig did have this cert result but new does not so it was removed
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setMessage(origCertResult.getNumber());
                            certRemovedActivities.add(activity);
                        }
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
              activity.setMessage("TRUE");
        }
        
        return activity;
    }
}
