package gov.healthit.chpl.validation.surveillance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.validation.surveillance.reviewer.ReadReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceDetailsReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceNonconformityReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceRequirementReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.UnsupportedCharacterReviewer;

@Component("surveillanceReadValidator")
public class SurveillanceReadValidator {

    private List<ReadReviewer> reviewers;

    @Autowired
    public SurveillanceReadValidator(SurveillanceDetailsReviewer survDetailsReviewer,
            SurveillanceRequirementReviewer survReqReviewer,
            SurveillanceNonconformityReviewer survNcReviewer,
            @Qualifier("surveillanceUnsupportedCharacterReviewer") UnsupportedCharacterReviewer charReviewer) {
        reviewers = new ArrayList<ReadReviewer>();
        reviewers.add(survDetailsReviewer);
        reviewers.add(survReqReviewer);
        reviewers.add(survNcReviewer);
        reviewers.add(charReviewer);
    }

    public void validate(Surveillance updatedSurv) {
        for (ReadReviewer reviewer : reviewers) {
            reviewer.review(updatedSurv);
        }
    }
}
