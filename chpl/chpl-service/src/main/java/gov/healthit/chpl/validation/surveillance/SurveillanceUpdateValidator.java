package gov.healthit.chpl.validation.surveillance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.validation.surveillance.reviewer.ComparisonReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.ReadReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceDetailsReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceNonconformityReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceRequirementReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.UnsupportedCharacterReviewer;

@Component("surveillanceUpdateValidator")
public class SurveillanceUpdateValidator {

    private List<ReadReviewer> reviewers;
    private List<ComparisonReviewer> comparisonReviewers;

    @Autowired
    public SurveillanceUpdateValidator(SurveillanceDetailsReviewer survDetailsReviewer,
            SurveillanceRequirementReviewer survReqReviewer,
            SurveillanceNonconformityReviewer survNcReviewer,
            @Qualifier("surveillanceUnsupportedCharacterReviewer") UnsupportedCharacterReviewer charReviewer) {
        reviewers = new ArrayList<ReadReviewer>();
        reviewers.add(survDetailsReviewer);
        reviewers.add(survReqReviewer);
        reviewers.add(survNcReviewer);
        reviewers.add(charReviewer);

        comparisonReviewers = new ArrayList<ComparisonReviewer>();
    }

    public void validate(Surveillance existingSurv, Surveillance updatedSurv) {
        for (ReadReviewer reviewer : reviewers) {
            reviewer.review(updatedSurv);
        }
        if (existingSurv != null) {
            for (ComparisonReviewer reviewer : comparisonReviewers) {
                reviewer.review(existingSurv, updatedSurv);
            }
        }
    }

    public List<ReadReviewer> getReviewers() {
        return reviewers;
    }

    public List<ComparisonReviewer> getComparisonReviewers() {
        return comparisonReviewers;
    }
}
