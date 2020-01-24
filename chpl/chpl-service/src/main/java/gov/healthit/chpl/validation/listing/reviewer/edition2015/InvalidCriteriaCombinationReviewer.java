package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.Optional;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component
public class InvalidCriteriaCombinationReviewer implements Reviewer {
    private static final String B6 = "170.315 (b)(6)";
    private static final String B10 = "170.315 (b)(10)";

    private static final String G8 = "170.315 (g)(8)";
    private static final String G10 = "170.315 (g)(10)";

    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    @Autowired
    public InvalidCriteriaCombinationReviewer(ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            checkForInvalidCriteriaCombination(listing, B6, B10);
            checkForInvalidCriteriaCombination(listing, G8, G10);
        }
    }

    private void checkForInvalidCriteriaCombination(CertifiedProductSearchDetails listing, String certificationNumberA,
            String certificationNumberB) {

        Optional<CertificationResult> certResultA = findCerificationResult(listing, certificationNumberA);
        Optional<CertificationResult> certResultB = findCerificationResult(listing, certificationNumberB);

        if (certResultA.isPresent() && certResultB.isPresent()) {
            listing.getErrorMessages().add("NEED TO UPDATE MESSAGE");
        }
    }

    private Optional<CertificationResult> findCerificationResult(CertifiedProductSearchDetails listing,
            String certificationNumber) {
        return listing.getCertificationResults().stream()
                .filter(cr -> cr.getNumber().equals(certificationNumber) && cr.isSuccess())
                .findFirst();
    }
}
