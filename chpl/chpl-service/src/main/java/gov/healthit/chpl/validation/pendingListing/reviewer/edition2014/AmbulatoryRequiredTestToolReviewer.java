package gov.healthit.chpl.validation.pendingListing.reviewer.edition2014;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingAmbulatoryRequiredTestToolReviewer")
public class AmbulatoryRequiredTestToolReviewer implements Reviewer {
    private static final String[] TEST_TOOL_CHECK_CERTS = {
            "170.314 (g)(1)", "170.314 (g)(2)", "170.314 (f)(3)"
    };

    private ErrorMessageUtil msgUtil;
    private CertificationResultRules certRules;

    @Autowired
    public AmbulatoryRequiredTestToolReviewer(ErrorMessageUtil msgUtil, CertificationResultRules certRules) {
        this.msgUtil = msgUtil;
        this.certRules = certRules;
    }

    @Override
    public void review(final PendingCertifiedProductDTO listing) {
        //check for test tools
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria()) {
                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP)
                        && cert.getGap() != null &&  cert.getGap()) {
                    gapEligibleAndTrue = true;
                }

                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED)
                        && !ValidationUtils.containsCert(cert, TEST_TOOL_CHECK_CERTS)
                        && (cert.getTestTools() == null || cert.getTestTools().size() == 0)) {
                    listing.getErrorMessages()
                    .add(msgUtil.getMessage("listing.criteria.missingTestTool", cert.getNumber()));
                }
            }
        }
    }
}
