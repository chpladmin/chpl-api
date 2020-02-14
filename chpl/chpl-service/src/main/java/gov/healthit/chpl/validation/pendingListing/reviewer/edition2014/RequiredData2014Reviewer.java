package gov.healthit.chpl.validation.pendingListing.reviewer.edition2014;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCqmCriterionDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.RequiredDataReviewer;

@Component("pendingRequiredData2014Reviewer")
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
    public RequiredData2014Reviewer(ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions,
            CertificationResultRules certRules) {
        super(msgUtil, resourcePermissions, certRules);
    }

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
        if (listing.getHasQms() != null && listing.getHasQms()
                && (listing.getQmsStandards() == null || listing.getQmsStandards().isEmpty())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingQMSStandards"));
        }
        if (listing.getHasQms() != null && !listing.getHasQms() && !listing.getQmsStandards().isEmpty()) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingQMSBoolean"));
        }

        boolean isCqmRequired = false;
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria()) {
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.G1_SUCCESS)
                        && cert.getG1Success() == null) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.criteria.missingG1Success", cert.getCriterion().getNumber()));
                }
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.G2_SUCCESS)
                        && cert.getG2Success() == null) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.criteria.missingG2Success", cert.getCriterion().getNumber()));
                }
                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.GAP)
                        && cert.getGap() != null && cert.getGap()) {
                    gapEligibleAndTrue = true;
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.SED)) {
                    if (cert.getSed() == null) {
                        listing.getErrorMessages().add(
                                msgUtil.getMessage("listing.criteria.SEDRequired", cert.getCriterion().getNumber()));
                    } else if (cert.getSed() != null && cert.getSed().booleanValue()
                            && (cert.getUcdProcesses() == null || cert.getUcdProcesses().size() == 0)) {
                        if (listing.getIcs() != null && listing.getIcs().booleanValue()) {
                            listing.getWarningMessages().add(
                                    msgUtil.getMessage("listing.criteria.missingUcdProccesses", cert.getCriterion().getNumber()));
                        } else {
                            listing.getErrorMessages().add(
                                    msgUtil.getMessage("listing.criteria.missingUcdProccesses", cert.getCriterion().getNumber()));
                        }
                    } else if (cert.getSed() != null && !cert.getSed().booleanValue()
                            && cert.getUcdProcesses() != null && cert.getUcdProcesses().size() > 0) {
                        listing.getWarningMessages().add(
                                msgUtil.getMessage("listing.criteria.sedUcdMismatch", cert.getCriterion().getNumber()));
                    }
                }

                if (!gapEligibleAndTrue && certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.TEST_DATA)
                        && (cert.getTestData() == null || cert.getTestData().size() == 0)) {
                    listing.getErrorMessages().add("Test Data is required for certification " + cert.getCriterion().getNumber() + ".");
                }
                for (int i = 0; i < CQM_REQUIRED_CERTS.length; i++) {
                    if (cert.getCriterion().getNumber().equals(CQM_REQUIRED_CERTS[i])) {
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
            if (cert.getCriterion().getNumber().equals("170.314 (g)(4)") && cert.getMeetsCriteria()) {
                hasG4 = true;
            }
            if (cert.getCriterion().getNumber().equals("170.314 (g)(3)") && cert.getMeetsCriteria()) {
                hasG3 = true;
            }
            for (int i = 0; i < G3_COMPLEMENTARY_CERTS.length; i++) {
                if (cert.getCriterion().getNumber().equals(G3_COMPLEMENTARY_CERTS[i]) && cert.getMeetsCriteria()) {
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
    }
}
