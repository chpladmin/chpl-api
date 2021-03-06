package gov.healthit.chpl.validation.listing.reviewer.edition2014;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("inpatientCompleteRequiredCriteriaReviewer")
public class InpatientCompleteRequiredCriteriaReviewer implements Reviewer {
    private static final String[] REQUIRED_CRITERIA = {
            "170.314 (a)(2)", "170.314 (a)(3)", "170.314 (a)(4)", "170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)",
            "170.314 (a)(8)", "170.314 (a)(9)", "170.314 (a)(10)", "170.314 (a)(11)", "170.314 (a)(12)",
            "170.314 (a)(13)", "170.314 (a)(14)", "170.314 (a)(15)", "170.314 (a)(16)", "170.314 (a)(17)",
            "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (b)(5)(B)", "170.314 (b)(6)", "170.314 (b)(7)",
            "170.314 (c)(1)", "170.314 (c)(2)", "170.314 (c)(3)", "170.314 (d)(1)", "170.314 (d)(2)", "170.314 (d)(3)",
            "170.314 (d)(4)", "170.314 (d)(5)", "170.314 (d)(6)", "170.314 (d)(7)", "170.314 (d)(8)", "170.314 (e)(1)",
            "170.314 (f)(1)", "170.314 (f)(2)", "170.314 (f)(3)", "170.314 (g)(2)", "170.314 (g)(3)", "170.314 (g)(4)"
    };

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        checkA1OrA18A19A20(listing);
        checkB1B2B8H1(listing);

        List<CertificationResult> certificationCriterion = listing.getCertificationResults();
        for (int i = 0; i < REQUIRED_CRITERIA.length; i++) {
            boolean hasCert = false;
            for (CertificationResult certCriteria : certificationCriterion) {
                if (certCriteria.getNumber().equals(REQUIRED_CRITERIA[i]) && certCriteria.isSuccess()) {
                    hasCert = true;
                }
            }
            if (!hasCert) {
                listing.getErrorMessages()
                        .add("Required certification criteria " + REQUIRED_CRITERIA[i] + " was not found.");
            }
        }
    }

    private boolean checkA1OrA18A19A20(final CertifiedProductSearchDetails listing) {
        boolean hasA1 = false;
        boolean hasA18 = false;
        boolean hasA19 = false;
        boolean hasA20 = false;
        for (CertificationResult certCriteria : listing.getCertificationResults()) {
            if (certCriteria.getNumber().equals("170.314 (a)(1)") && certCriteria.isSuccess()) {
                hasA1 = true;
            }
            if (certCriteria.getNumber().equals("170.314 (a)(18)") && certCriteria.isSuccess()) {
                hasA18 = true;
            }
            if (certCriteria.getNumber().equals("170.314 (a)(19)") && certCriteria.isSuccess()) {
                hasA19 = true;
            }
            if (certCriteria.getNumber().equals("170.314 (a)(20)") && certCriteria.isSuccess()) {
                hasA20 = true;
            }
        } if (!hasA1) {
            if (!hasA18 || !hasA19 || !hasA20) {
                listing.getErrorMessages()
                .add("Neither (a)(1) nor the combination of (a)(18), (a)(19), and (a)(20) were found.");
                return false;
            }
        }
        return true;
    }

    private boolean checkB1B2B8H1(final CertifiedProductSearchDetails listing) {
        // (b)(1), (b)(2)**
        // (in replacement for (b)(1) and (b)(2) -
        // (b)(1) and (b)(8), OR
        // (b)(8) and (h)(1), OR
        // (b)(1) and (b)(2) and (b)(8), OR
        // (b)(1) and (b)(2) and (h)(1), OR
        // (b)(1) and (b)(2) and (b)(8) and (h)(1)"
        boolean hasB1 = false;
        boolean hasB2 = false;
        boolean hasB8 = false;
        boolean hasH1 = false;
        for (CertificationResult certCriteria : listing.getCertificationResults()) {
            if (certCriteria.getNumber().equals("170.314 (b)(1)") && certCriteria.isSuccess()) {
                hasB1 = true;
            }
            if (certCriteria.getNumber().equals("170.314 (b)(2)") && certCriteria.isSuccess()) {
                hasB2 = true;
            }
            if (certCriteria.getNumber().equals("170.314 (b)(8)") && certCriteria.isSuccess()) {
                hasB8 = true;
            }
            if (certCriteria.getNumber().equals("170.314 (h)(1)") && certCriteria.isSuccess()) {
                hasH1 = true;
            }
        }
        if (!hasB1 && !hasB2) {
            if (!hasB1 && !hasB8) {
                if (!hasB8 && !hasH1) {
                    listing.getErrorMessages()
                    .add("An allowed combination of (b)(1), (b)(2), (b)(8), and (h)(1) was not found.");
                    return false;
                }
            }
        }
        return true;
    }
}
