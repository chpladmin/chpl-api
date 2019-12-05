package gov.healthit.chpl.validation.surveillance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.validation.surveillance.reviewer.ComparisonReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.RemovedCriteriaComparisonReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.Reviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceDetailsReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceNonconformityReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceRequirementReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.UnsupportedCharacterReviewer;

@Component("surveillanceUpdateValidator")
public class SurveillanceUpdateValidator {

    private List<Reviewer> reviewers;
    private List<ComparisonReviewer> comparisonReviewers;

    @Autowired
    public SurveillanceUpdateValidator(SurveillanceDetailsReviewer survDetailsReviewer,
            SurveillanceRequirementReviewer survReqReviewer,
            SurveillanceNonconformityReviewer survNcReviewer,
            @Qualifier("surveillanceUnsupportedCharacterReviewer") UnsupportedCharacterReviewer charReviewer,
            RemovedCriteriaComparisonReviewer removedCriteriaReviewer) {
        reviewers = new ArrayList<Reviewer>();
        reviewers.add(survDetailsReviewer);
        reviewers.add(survReqReviewer);
        reviewers.add(survNcReviewer);
        reviewers.add(charReviewer);

        comparisonReviewers = new ArrayList<ComparisonReviewer>();
        comparisonReviewers.add(removedCriteriaReviewer);
    }

    public void validate(Surveillance existingSurv, Surveillance updatedSurv) {
        for (Reviewer reviewer : reviewers) {
            reviewer.review(updatedSurv);
        }
        if (existingSurv != null) {
            for (ComparisonReviewer reviewer : comparisonReviewers) {
                reviewer.review(existingSurv, updatedSurv);
            }
        }
    }

    public List<Reviewer> getReviewers() {
        return reviewers;
    }

    public List<ComparisonReviewer> getComparisonReviewers() {
        return comparisonReviewers;
    }
}
