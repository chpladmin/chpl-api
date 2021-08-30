package gov.healthit.chpl.validation.surveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.surveillance.reviewer.AuthorityReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.DeprecatedFieldReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceRemovedDataComparisonReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceDetailsReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceNonconformityReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceRequirementReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.UnsupportedCharacterReviewer;

@Component("surveillanceUpdateWithAuthorityValidator")
public class SurveillanceUpdateWithAuthorityValidator extends SurveillanceUpdateValidator {

    @Autowired
    public SurveillanceUpdateWithAuthorityValidator(SurveillanceDetailsReviewer survDetailsReviewer,
            SurveillanceRequirementReviewer survReqReviewer,
            SurveillanceNonconformityReviewer survNcReviewer,
            @Qualifier("surveillanceUnsupportedCharacterReviewer") UnsupportedCharacterReviewer charReviewer,
            AuthorityReviewer authorityReviewer,
            @Qualifier("surveillanceRemovedDataComparisonReviewer") SurveillanceRemovedDataComparisonReviewer removedDataReviewer,
            @Qualifier("surveillanceDeprecatedFieldReviewer") DeprecatedFieldReviewer deprecatedFieldReviewer) {
        super(survDetailsReviewer, survReqReviewer, survNcReviewer, charReviewer, removedDataReviewer, deprecatedFieldReviewer);
        getReviewers().add(authorityReviewer);
    }
}
