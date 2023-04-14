package gov.healthit.chpl.questionableactivity.listing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.Util;

@Component
public class AddedCertificationsActivity implements ListingActivity {

    private CertificationCriterionService criteriaService;
    private Map<CertificationCriterion, CertificationCriterion> curesToOriginalCriteriaMap;

    @Autowired
    public AddedCertificationsActivity(CertificationCriterionService criteriaService) {
        this.criteriaService = criteriaService;

        Map<CertificationCriterion, CertificationCriterion> originalToCuresCriteriaMap
            = criteriaService.getOriginalToCuresCriteriaMap();

        curesToOriginalCriteriaMap = originalToCuresCriteriaMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        List<QuestionableActivityListingDTO> certAddedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0
                && newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            // all cert results are in the details so find the same one in the orig and new objects
            // based on id and compare the success boolean to see if one was added
            for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                    if (origCertResult.getCriterion().getId().equals(newCertResult.getCriterion().getId())) {
                        if (!origCertResult.isSuccess() && newCertResult.isSuccess()
                                && !wasCuresCriteriaSwappedForOriginal(newCertResult.getCriterion(), origListing, newListing)) {

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

    private boolean wasCuresCriteriaSwappedForOriginal(CertificationCriterion addedCriterion,
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        addedCriterion = criteriaService.get(addedCriterion.getId());
        return Util.isCures(addedCriterion)
                && wasNonCuresCounterpartRemoved(addedCriterion, origListing, newListing);
    }

    private boolean wasNonCuresCounterpartRemoved(CertificationCriterion curesCriterion,
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        CertificationCriterion nonCuresCounterpart = curesToOriginalCriteriaMap.get(curesCriterion);
        if (nonCuresCounterpart == null) {
            return false;
        }
        Optional<CertificationResult> origCertResultOpt = origListing.getCertificationResults().stream()
            .filter(certResult -> certResult.getCriterion().getId().equals(nonCuresCounterpart.getId()))
            .findAny();
        Optional<CertificationResult> newCertResultOpt = newListing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion().getId().equals(nonCuresCounterpart.getId()))
                .findAny();
        return origCertResultOpt.isPresent() && newCertResultOpt.isPresent()
                && origCertResultOpt.get().isSuccess() && !newCertResultOpt.get().isSuccess();
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.CRITERIA_ADDED;
    }
}
