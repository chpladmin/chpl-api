package gov.healthit.chpl.validation.pendingListing.reviewer.edition2014;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingInpatientRequiredTestToolReviewer")
public class InpatientRequiredTestToolReviewer implements Reviewer {
    private static final String[] TEST_TOOL_CHECK_CERTS = {
            "170.314 (g)(1)", "170.314 (g)(2)"
    };

    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private CertificationResultRules certRules;

    @Autowired
    public InpatientRequiredTestToolReviewer(ValidationUtils validationUtils, ErrorMessageUtil msgUtil,
            CertificationResultRules certRules) {
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
        this.certRules = certRules;
    }

    @Override
    public void review(final PendingCertifiedProductDTO listing) {
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria()) {
                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.GAP)
                        && cert.getGap()) {
                    gapEligibleAndTrue = true;
                }

                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.TEST_TOOLS_USED)
                        && !validationUtils.containsCert(cert, TEST_TOOL_CHECK_CERTS)
                        && (cert.getTestTools() == null || cert.getTestTools().size() == 0)) {
                    if (listing.getIcs() != null && listing.getIcs().booleanValue()) {
                        listing.getWarningMessages()
                        .add(msgUtil.getMessage("listing.criteria.missingTestTool", cert.getCriterion().getNumber()));
                    } else {
                        listing.getErrorMessages()
                        .add(msgUtil.getMessage("listing.criteria.missingTestTool", cert.getCriterion().getNumber()));
                    }
                }
            }
        }
    }
}
