package gov.healthit.chpl.validation.pendingListing.review.edition2014;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.review.RequiredDataReviewer;

@Component
public class RequiredData2014Reviewer extends RequiredDataReviewer {
    @Autowired private ErrorMessageUtil msgUtil;
    @Autowired private CertificationResultRules certRules;
    
    private static final String[] G1_COMPLEMENTARY_CERTS = {
            "170.314 (a)(1)", "170.314 (a)(3)", "170.314 (a)(4)", "170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)",
            "170.314 (a)(9)", "170.314 (a)(11)", "170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(14)",
            "170.314 (a)(15)", "170.314 (a)(18)", "170.314 (a)(19)", "170.314 (a)(20)", "170.314 (b)(2)",
            "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (e)(1)"
    };

    private static final String[] G2_COMPLEMENTARY_CERTS = {
            "170.314 (a)(1)", "170.314 (a)(3)", "170.314 (a)(4)", "170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)",
            "170.314 (a)(9)", "170.314 (a)(11)", "170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(14)",
            "170.314 (a)(15)", "170.314 (a)(18)", "170.314 (a)(19)", "170.314 (a)(20)", "170.314 (b)(2)",
            "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (e)(1)"
    };
    
    private static final String[] G3_COMPLEMENTARY_CERTS = {
            "170.314 (a)(1)", "170.314 (a)(2)", "170.314 (a)(6)", "170.314 (a)(7)", "170.314 (a)(8)", "170.314 (a)(16)",
            "170.314 (a)(18)", "170.314 (a)(19)", "170.314 (a)(20)", "170.314 (b)(3)", "170.314 (b)(4)",
            "170.314 (b)(9)"
    };

    private static final String[] CQM_REQUIRED_CERTS = {
            "170.314 (c)(1)", "170.314 (c)(2)", "170.314 (c)(3)"
    };
    
    @Override
    public void review(final PendingCertifiedProductDTO listing) {
        super.review(listing);
        if (listing.getPracticeTypeId() == null) {
            listing.getErrorMessages().add("Practice setting is required but was not found.");
        } 
//        else {
//            listing.getErrorMessages().addAll(
//                    certifiedtProductTestFunctionalityValidator.getTestFunctionalityValidationErrors(product));
//        }
        if (listing.getProductClassificationId() == null) {
            listing.getErrorMessages().add("Product classification is required but was not found.");
        }
        if (StringUtils.isEmpty(listing.getReportFileLocation())) {
            listing.getErrorMessages().add("Test Report URL is required but was not found.");
        }
        boolean isCqmRequired = false;
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria()) {
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G1_SUCCESS)
                        && cert.getG1Success() == null) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.criteria.missingG1Success", cert.getNumber()));
                }
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G2_SUCCESS)
                        && cert.getG2Success() == null) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.criteria.missingG2Success", cert.getNumber()));
                }
                for (int i = 0; i < CQM_REQUIRED_CERTS.length; i++) {
                    if (cert.getNumber().equals(CQM_REQUIRED_CERTS[i])) {
                        isCqmRequired = true;
                    }
                }
            }
        }
        if (isCqmRequired) {
            boolean hasOneCqmWithVersion = false;
            for (PendingCqmCriterionDTO cqm : listing.getCqmCriterion()) {
                if (cqm.isMeetsCriteria() && !StringUtils.isEmpty(cqm.getVersion())) {
                    hasOneCqmWithVersion = true;
                }
            }
            if (!hasOneCqmWithVersion) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingCQM"));
            }
        }

        // g4, g3 and g3 complement check
        boolean hasG4 = false;
        boolean hasG3 = false;
        boolean hasG3Complement = false;
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getNumber().equals("170.314 (g)(4)") && cert.getMeetsCriteria()) {
                hasG4 = true;
            }
            if (cert.getNumber().equals("170.314 (g)(3)") && cert.getMeetsCriteria()) {
                hasG3 = true;
            }
            for (int i = 0; i < G3_COMPLEMENTARY_CERTS.length; i++) {
                if (cert.getNumber().equals(G3_COMPLEMENTARY_CERTS[i]) && cert.getMeetsCriteria()) {
                    hasG3Complement = true;
                }
            }
        }
        if (!hasG4) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missing170314g4"));
        }
        if (hasG3 && !hasG3Complement) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingG3complement"));
        }
        if (hasG3Complement && !hasG3) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingComplementG3"));
        }

        // check (g)(1)
        boolean hasG1Cert = false;
        for (PendingCertificationResultDTO certCriteria : listing.getCertificationCriterion()) {
            if (certCriteria.getNumber().equals("170.314 (g)(1)") && certCriteria.getMeetsCriteria()) {
                hasG1Cert = true;
            }
        }
        if (hasG1Cert) {
            boolean hasG1Complement = false;
            for (int i = 0; i < G1_COMPLEMENTARY_CERTS.length && !hasG1Complement; i++) {
                for (PendingCertificationResultDTO certCriteria : listing.getCertificationCriterion()) {
                    if (certCriteria.getNumber().equals(G1_COMPLEMENTARY_CERTS[i]) && certCriteria.getMeetsCriteria()) {
                        hasG1Complement = true;
                    }
                }
            }

            if (!hasG1Complement) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.missingG1Related"));
            }
        }

        // check (g)(2)
        boolean hasG2Cert = false;
        for (PendingCertificationResultDTO certCriteria : listing.getCertificationCriterion()) {
            if (certCriteria.getNumber().equals("170.314 (g)(2)") && certCriteria.getMeetsCriteria()) {
                hasG2Cert = true;
            }
        }
        if (hasG2Cert) {
            boolean hasG2Complement = false;
            for (int i = 0; i < G2_COMPLEMENTARY_CERTS.length && !hasG2Complement; i++) {
                for (PendingCertificationResultDTO certCriteria : listing.getCertificationCriterion()) {
                    if (certCriteria.getNumber().equals(G2_COMPLEMENTARY_CERTS[i]) && certCriteria.getMeetsCriteria()) {
                        hasG2Complement = true;
                    }
                }
            }

            if (!hasG2Complement) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.missingG2Related"));
            }
        }

        if (hasG1Cert && hasG2Cert) {
            listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.G1G2Found"));
        }
    }
}
