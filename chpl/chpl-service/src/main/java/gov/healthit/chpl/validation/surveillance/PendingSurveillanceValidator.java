package gov.healthit.chpl.validation.surveillance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.validation.surveillance.reviewer.PendingSurveillanceRemovedCriteriaReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.Reviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceDetailsReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceNonconformityReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceRequirementReviewer;

@Component("pendingSurveillanceValidator")
public class PendingSurveillanceValidator {

    private List<Reviewer> reviewers;

    @Autowired
    public PendingSurveillanceValidator(SurveillanceDetailsReviewer survDetailsReviewer,
            SurveillanceRequirementReviewer survReqReviewer,
            SurveillanceNonconformityReviewer survNcReviewer,
            PendingSurveillanceRemovedCriteriaReviewer removedCriteriaReviewer) {
        reviewers = new ArrayList<Reviewer>();
        reviewers.add(survDetailsReviewer);
        reviewers.add(survReqReviewer);
        reviewers.add(survNcReviewer);
        reviewers.add(removedCriteriaReviewer);
    }

    public void validate(Surveillance updatedSurv) {
        for (Reviewer reviewer : reviewers) {
            reviewer.review(updatedSurv);
        }
    }
}
