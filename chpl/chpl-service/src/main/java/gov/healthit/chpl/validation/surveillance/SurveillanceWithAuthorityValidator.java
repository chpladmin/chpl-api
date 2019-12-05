package gov.healthit.chpl.validation.surveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.surveillance.reviewer.AuthorityReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.RemovedCriteriaComparisonReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceDetailsReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceNonconformityReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceRequirementReviewer;

@Component("surveillanceWithAuthorityValidator")
public class SurveillanceWithAuthorityValidator extends SurveillanceValidator {

    @Autowired
    public SurveillanceWithAuthorityValidator(SurveillanceDetailsReviewer survDetailsReviewer,
            SurveillanceRequirementReviewer survReqReviewer,
            SurveillanceNonconformityReviewer survNcReviewer,
            AuthorityReviewer authorityReviewer,
            RemovedCriteriaComparisonReviewer removedCriteriaReviewer) {
        super(survDetailsReviewer, survReqReviewer, survNcReviewer, removedCriteriaReviewer);
        getReviewers().add(authorityReviewer);
    }
}
