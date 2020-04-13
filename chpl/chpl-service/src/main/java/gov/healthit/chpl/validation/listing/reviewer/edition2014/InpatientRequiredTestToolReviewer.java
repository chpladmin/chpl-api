package gov.healthit.chpl.validation.listing.reviewer.edition2014;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("inpatientRequiredTestToolReviewer")
public class InpatientRequiredTestToolReviewer implements Reviewer {
    private static final String[] TEST_TOOL_CHECK_CERTS = {
            "170.314 (g)(1)", "170.314 (g)(2)"
    };

    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private CertificationResultRules certRules;

    @Autowired
    public InpatientRequiredTestToolReviewer(ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil, CertificationResultRules certRules) {
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
        this.certRules = certRules;
    }

    @Override
    public void review(final CertifiedProductSearchDetails listing) {
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.isSuccess() != null && cert.isSuccess()) {
                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP)
                        && cert.isGap()) {
                    gapEligibleAndTrue = true;
                }

                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED)
                        && !validationUtils.containsCert(cert, TEST_TOOL_CHECK_CERTS)
                        && (cert.getTestToolsUsed() == null || cert.getTestToolsUsed().size() == 0)) {
                    if (listing.getIcs() != null && listing.getIcs().getInherits() != null
                            && listing.getIcs().getInherits().booleanValue()) {
                        listing.getWarningMessages()
                        .add(msgUtil.getMessage("listing.criteria.missingTestTool", cert.getNumber()));
                    } else {
                        listing.getErrorMessages()
                        .add(msgUtil.getMessage("listing.criteria.missingTestTool", cert.getNumber()));
                    }
                }
            }
        }
    }
}
