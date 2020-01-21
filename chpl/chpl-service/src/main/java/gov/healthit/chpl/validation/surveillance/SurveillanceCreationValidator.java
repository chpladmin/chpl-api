package gov.healthit.chpl.validation.surveillance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.validation.surveillance.reviewer.NewSurveillanceEditionReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.NewSurveillanceRemovedCriteriaReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.Reviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceDetailsReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceNonconformityReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceRequirementReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.UnsupportedCharacterReviewer;

@Component("surveillanceCreationValidator")
public class SurveillanceCreationValidator {

    private List<Reviewer> reviewers;

    @Autowired
    public SurveillanceCreationValidator(SurveillanceDetailsReviewer survDetailsReviewer,
            SurveillanceRequirementReviewer survReqReviewer,
            SurveillanceNonconformityReviewer survNcReviewer,
            @Qualifier("surveillanceUnsupportedCharacterReviewer") UnsupportedCharacterReviewer charReviewer,
            NewSurveillanceRemovedCriteriaReviewer removedCriteriaReviewer,
            NewSurveillanceEditionReviewer editionReviewer) {
        reviewers = new ArrayList<Reviewer>();
        reviewers.add(survDetailsReviewer);
        reviewers.add(survReqReviewer);
        reviewers.add(survNcReviewer);
        reviewers.add(charReviewer);
        reviewers.add(removedCriteriaReviewer);
        reviewers.add(editionReviewer);
    }

    public void validate(Surveillance updatedSurv) {
        for (Reviewer reviewer : reviewers) {
            reviewer.review(updatedSurv);
        }
    }
}
