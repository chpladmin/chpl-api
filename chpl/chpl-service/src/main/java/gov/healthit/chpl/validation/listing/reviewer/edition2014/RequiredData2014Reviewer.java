package gov.healthit.chpl.validation.listing.reviewer.edition2014;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.RequiredDataReviewer;

@Component("requiredData2014Reviewer")
public class RequiredData2014Reviewer extends RequiredDataReviewer {

    private static final String[] G3_COMPLEMENTARY_CERTS = {
            "170.314 (a)(1)", "170.314 (a)(2)", "170.314 (a)(6)", "170.314 (a)(7)", "170.314 (a)(8)", "170.314 (a)(16)",
            "170.314 (a)(18)", "170.314 (a)(19)", "170.314 (a)(20)", "170.314 (b)(3)", "170.314 (b)(4)",
            "170.314 (b)(9)"
    };

    private static final String[] CQM_REQUIRED_CERTS = {
            "170.314 (c)(1)", "170.314 (c)(2)", "170.314 (c)(3)"
    };

    @Autowired
    public RequiredData2014Reviewer(CertificationResultRules certRules, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions) {
        super(certRules, msgUtil, resourcePermissions);
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        super.review(listing);
        if (listing.getPracticeType() == null || listing.getPracticeType().get("id") == null) {
            listing.getErrorMessages().add("Practice setting is required but was not found.");
        }
        if (listing.getClassificationType() == null || listing.getClassificationType().get("id") == null) {
            listing.getErrorMessages().add("Product classification is required but was not found.");
        }
        if (StringUtils.isEmpty(listing.getReportFileLocation())) {
            listing.getErrorMessages().add("Test Report URL is required but was not found.");
        }

        boolean isCqmRequired = false;
        for (CertificationResult cert : listing.getCertificationResults()) {
            for (int i = 0; i < CQM_REQUIRED_CERTS.length; i++) {
                if (cert.getNumber().equals(CQM_REQUIRED_CERTS[i]) && cert.isSuccess()) {
                    isCqmRequired = true;
                }
            }
            if (cert.isSuccess() != null && cert.isSuccess()) {
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.SED)) {
                    if (cert.isSed() == null) {
                        listing.getErrorMessages().add(
                                msgUtil.getMessage("listing.criteria.SEDRequired", cert.getNumber()));
                    } else if (cert.isSed() != null && cert.isSed().booleanValue()
                            && !certHasUcdProcess(cert, listing.getSed().getUcdProcesses())) {
                        if (listing.getIcs() != null && listing.getIcs().getInherits() != null
                                && listing.getIcs().getInherits().booleanValue()) {
                            listing.getWarningMessages().add(
                                    msgUtil.getMessage("listing.criteria.missingUcdProccesses", cert.getNumber()));
                        } else {
                            listing.getErrorMessages().add(
                                    msgUtil.getMessage("listing.criteria.missingUcdProccesses", cert.getNumber()));
                        }
                    } else if (cert.isSed() != null && !cert.isSed().booleanValue()
                            && certHasUcdProcess(cert, listing.getSed().getUcdProcesses())) {
                        listing.getWarningMessages().add(
                                msgUtil.getMessage("listing.criteria.sedUcdMismatch", cert.getNumber()));
                    }
                }
            }
        }
        if (isCqmRequired) {
            boolean hasOneCqmWithVersion = false;
            for (CQMResultDetails cqm : listing.getCqmResults()) {
                if (cqm.isSuccess() && cqm.getSuccessVersions() != null && cqm.getSuccessVersions().size() > 0) {
                    hasOneCqmWithVersion = true;
                }
            }
            if (!hasOneCqmWithVersion) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingCQM"));
            }
        }

        // g4 check
        boolean hasG4 = false;
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.getNumber().equals("170.314 (g)(4)") && cert.isSuccess()) {
                hasG4 = true;
            }
        }
        if (!hasG4) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingG4"));
        }

        // g3 check
        boolean hasG3 = false;
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.getNumber().equals("170.314 (g)(3)") && cert.isSuccess()) {
                hasG3 = true;
            }
        }
        boolean hasG3Complement = false;
        for (CertificationResult cert : listing.getCertificationResults()) {
            for (int i = 0; i < G3_COMPLEMENTARY_CERTS.length; i++) {
                if (cert.getNumber().equals(G3_COMPLEMENTARY_CERTS[i]) && cert.isSuccess()) {
                    hasG3Complement = true;
                }
            }
        }

        if (hasG3 && !hasG3Complement) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingG3complement"));
        }
        if (hasG3Complement && !hasG3) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingComplementG3"));
        }
    }

    private boolean certHasUcdProcess(final CertificationResult cert, final List<UcdProcess> ucdProcesses) {
        boolean hasUcd = false;
        for (UcdProcess ucdProcess : ucdProcesses) {
            for (CertificationCriterion ucdCriteria : ucdProcess.getCriteria()) {
                if (ucdCriteria.getNumber().equalsIgnoreCase(cert.getNumber())) {
                    hasUcd = true;
                }
            }
        }
        return hasUcd;
    }
}
