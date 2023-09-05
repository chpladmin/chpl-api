package gov.healthit.chpl.questionableactivity.listing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.Util;

@Component
public class DeletedCertificationsActivity implements ListingActivity {

    private CertificationCriterionService criteriaService;
    private Map<CertificationCriterion, CertificationCriterion> originalToCuresCriteriaMap;

    @Autowired
    public DeletedCertificationsActivity(CertificationCriterionService criteriaService) {
        this.criteriaService = criteriaService;

        originalToCuresCriteriaMap = criteriaService.getOriginalToCuresCriteriaMap();
    }

    @Override
     public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        List<QuestionableActivityListing> certRemovedActivities = new ArrayList<QuestionableActivityListing>();
        if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0
                && newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            // all cert results are in the details so find the same one in the orig and new objects
            // based on number and compare the success boolean to see if one was removed
            for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                    if (origCertResult.getCriterion().getId().equals(newCertResult.getCriterion().getId())) {
                        if (origCertResult.isSuccess() && !newCertResult.isSuccess()
                                && !wasCuresCriteriaSwappedForOriginal(newCertResult.getCriterion(), origListing, newListing)) {
                            // orig did have this cert result but new does not so it was removed
                            QuestionableActivityListing activity = new QuestionableActivityListing();
                            activity.setBefore(CertificationCriterionService.formatCriteriaNumber(origCertResult.getCriterion()));
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

    private boolean wasCuresCriteriaSwappedForOriginal(CertificationCriterion removedCriterion,
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        removedCriterion = criteriaService.get(removedCriterion.getId());
        return !Util.isCures(removedCriterion)
                && wasCuresCounterpartAdded(removedCriterion, origListing, newListing);
    }

    private boolean wasCuresCounterpartAdded(CertificationCriterion nonCuresCriterion,
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        CertificationCriterion curesCounterpart = originalToCuresCriteriaMap.get(nonCuresCriterion);
        if (curesCounterpart == null) {
            return false;
        }
        Optional<CertificationResult> origCertResultOpt = origListing.getCertificationResults().stream()
            .filter(certResult -> certResult.getCriterion().getId().equals(curesCounterpart.getId()))
            .findAny();
        Optional<CertificationResult> newCertResultOpt = newListing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion().getId().equals(curesCounterpart.getId()))
                .findAny();
        return origCertResultOpt.isPresent() && newCertResultOpt.isPresent()
                && !origCertResultOpt.get().isSuccess() && newCertResultOpt.get().isSuccess();
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.CRITERIA_REMOVED;
    }

}
