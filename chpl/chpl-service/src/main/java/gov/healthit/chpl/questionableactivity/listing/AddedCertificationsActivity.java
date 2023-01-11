package gov.healthit.chpl.questionableactivity.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;
import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class AddedCertificationsActivity implements ListingActivity {

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        List<QuestionableActivityListingDTO> certAddedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0
                && newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            // all cert results are in the details so find the same one in the orig and new objects
            // based on number and compare the success boolean to see if one was added
            for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                    if (origCertResult.getCriterion().getId().equals(newCertResult.getCriterion().getId())) {
                        if (!origCertResult.isSuccess() && newCertResult.isSuccess()) {
                            // orig did not have this cert result but new does so it was added
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(null);
                            activity.setAfter(CertificationCriterionService.formatCriteriaNumber(newCertResult.getCriterion()));
                            certAddedActivities.add(activity);
                        }
                        break;
                    }
                }
            }
        }
        return certAddedActivities;
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.CRITERIA_ADDED;
    }
}
