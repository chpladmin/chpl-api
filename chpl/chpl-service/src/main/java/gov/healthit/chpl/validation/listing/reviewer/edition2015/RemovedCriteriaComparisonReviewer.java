package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalityTested.CertificationResultTestFunctionality;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

/**
 * This reviewer confirms that an ACB user does not attempt to add a 'removed' criteria
 * to a listing or edit an existing 'removed' criteria in a listing.
 * @author kekey
 *
 */
@Component("removedCriteriaComparisonReviewer")
public class RemovedCriteriaComparisonReviewer implements ComparisonReviewer {
    private CertificationResultRules certResultRules;
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public RemovedCriteriaComparisonReviewer(CertificationResultRules certResultRules,
            ResourcePermissions resourcePermissions, ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        //checking for the addition of a removed criteria
        //this is only disallowed if the user is not ADMIN/ONC, so first check the permissions
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        }

        for (CertificationResult updatedCert : updatedListing.getCertificationResults()) {
            for (CertificationResult existingCert : existingListing.getCertificationResults()) {
                //find matching criteria in existing/updated listings
                if (updatedCert.getCriterion().getId() != null && existingCert.getCriterion().getId() != null
                        && updatedCert.getCriterion().getId().equals(existingCert.getCriterion().getId())) {
                    if (isRemovedCertAdded(existingCert, updatedCert)) {
                        updatedListing.getErrorMessages().add(
                                msgUtil.getMessage("listing.removedCriteriaAddNotAllowed",
                                        Util.formatCriteriaNumber(updatedCert.getCriterion())));
                    } else if (isRemovedCertEdited(existingCert, updatedCert)) {
                        addErrorsForCertEdits(updatedListing, existingCert, updatedCert);
                    }
                }
            }
        }
    }

    private boolean isRemovedCertAdded(CertificationResult existingCert, CertificationResult updatedCert) {
        return (existingCert.isSuccess() == null || !existingCert.isSuccess())
                && (updatedCert.isSuccess() != null && updatedCert.isSuccess()
                && updatedCert.getCriterion().getRemoved() != null
                && updatedCert.getCriterion().getRemoved().booleanValue());
    }

    private boolean isRemovedCertEdited(CertificationResult existingCert, CertificationResult updatedCert) {
        return existingCert.isSuccess() != null && existingCert.isSuccess()
                && updatedCert.isSuccess() != null && updatedCert.isSuccess()
                && updatedCert.getCriterion().getRemoved() != null
                && updatedCert.getCriterion().getRemoved().booleanValue();
    }

    private void addErrorsForCertEdits(CertifiedProductSearchDetails updatedListing,
            CertificationResult existingCert, CertificationResult updatedCert) {
        if (isGapChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()), "Gap"));
        }
        if (isG1SuccessChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()), "G1 Success"));
        }
        if (isG2SuccessChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()), "G2 Success"));
        }
        if (isAdditionalSoftwareChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()), "Additional Software"));
        }
        if (isFunctionalityTestedChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()), "Functionality Tested"));
        }
        if (isTestStandardsChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()), "Test Standards"));
        }
        if (isTestDataChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()), "Test Data"));
        }
        if (isTestProceduresChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()), "Test Procedures"));
        }
        if (isTestToolsChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()), "Test Tools"));
        }
        if (isApiDocumentationChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()), "API Documentation"));
        }
        if (isPrivacySecurityFrameworkChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()), "Privacy and Security Framework"));
        }
        if (isSedChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()), "SED"));
        }
    }

    private boolean isGapChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getNumber(), CertificationResultRules.GAP)) {
            return !ObjectUtils.equals(existingCert.isGap(), updatedCert.isGap());
        }
        return false;
    }

    private boolean isG1SuccessChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getNumber(), CertificationResultRules.G1_SUCCESS)) {
            return !ObjectUtils.equals(existingCert.isG1Success(), updatedCert.isG1Success());
        }
        return false;
    }

    private boolean isG2SuccessChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getNumber(), CertificationResultRules.G2_SUCCESS)) {
            return !ObjectUtils.equals(existingCert.isG2Success(), updatedCert.isG2Success());
        }
        return false;
    }

    private boolean isAdditionalSoftwareChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getNumber(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
            for (CertificationResultAdditionalSoftware updatedAdditionalSoftware : updatedCert.getAdditionalSoftware()) {
                boolean isInExistingCert =
                        existingCert.getAdditionalSoftware().stream()
                        .anyMatch(existingAdditionalSoftware -> existingAdditionalSoftware.matches(updatedAdditionalSoftware));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultAdditionalSoftware existingAdditionalSoftware : existingCert.getAdditionalSoftware()) {
                boolean isInUpdatedCert =
                        updatedCert.getAdditionalSoftware().stream()
                        .anyMatch(updatedAdditionalSoftware -> updatedAdditionalSoftware.matches(existingAdditionalSoftware));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFunctionalityTestedChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
            for (CertificationResultTestFunctionality updatedTestFunctionality : updatedCert.getFunctionalitiesTested()) {
                boolean isInExistingCert =
                        existingCert.getFunctionalitiesTested().stream()
                        .anyMatch(existingTestFunctionality -> existingTestFunctionality.matches(updatedTestFunctionality));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultTestFunctionality existingTestFunctionality : existingCert.getFunctionalitiesTested()) {
                boolean isInUpdatedCert =
                        updatedCert.getFunctionalitiesTested().stream()
                        .anyMatch(updatedTestFunctionality -> updatedTestFunctionality.matches(existingTestFunctionality));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTestStandardsChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getNumber(), CertificationResultRules.STANDARDS_TESTED)) {
            for (CertificationResultTestStandard updatedTestStandard : updatedCert.getTestStandards()) {
                boolean isInExistingCert =
                        existingCert.getTestStandards().stream()
                        .anyMatch(existingTestStandard -> existingTestStandard.matches(updatedTestStandard));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultTestStandard existingTestStandard : existingCert.getTestStandards()) {
                boolean isInUpdatedCert =
                        updatedCert.getTestStandards().stream()
                        .anyMatch(updatedTestStandard -> updatedTestStandard.matches(existingTestStandard));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTestDataChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getNumber(), CertificationResultRules.TEST_DATA)) {
            for (CertificationResultTestData updatedTestData : updatedCert.getTestDataUsed()) {
                boolean isInExistingCert =
                        existingCert.getTestDataUsed().stream()
                        .anyMatch(existingTestData -> existingTestData.matches(updatedTestData));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultTestData existingTestData : existingCert.getTestDataUsed()) {
                boolean isInUpdatedCert =
                        updatedCert.getTestDataUsed().stream()
                        .anyMatch(updatedTestData -> updatedTestData.matches(existingTestData));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTestProceduresChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getNumber(), CertificationResultRules.TEST_PROCEDURE)) {
            for (CertificationResultTestProcedure updatedTestProcedure : updatedCert.getTestProcedures()) {
                boolean isInExistingCert =
                        existingCert.getTestProcedures().stream()
                        .anyMatch(existingTestProcedure -> existingTestProcedure.matches(updatedTestProcedure));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultTestProcedure existingTestProcedure : existingCert.getTestProcedures()) {
                boolean isInUpdatedCert =
                        updatedCert.getTestProcedures().stream()
                        .anyMatch(updatedTestProcedure -> updatedTestProcedure.matches(existingTestProcedure));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTestToolsChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getNumber(), CertificationResultRules.TEST_TOOLS_USED)) {
            for (CertificationResultTestTool updatedTestTool : updatedCert.getTestToolsUsed()) {
                boolean isInExistingCert =
                        existingCert.getTestToolsUsed().stream()
                        .anyMatch(existingTestTool -> existingTestTool.matches(updatedTestTool));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultTestTool existingTestTool : existingCert.getTestToolsUsed()) {
                boolean isInUpdatedCert =
                        updatedCert.getTestToolsUsed().stream()
                        .anyMatch(updatedTestTool -> updatedTestTool.matches(existingTestTool));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isApiDocumentationChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getNumber(), CertificationResultRules.API_DOCUMENTATION)) {
            return !ObjectUtils.equals(updatedCert.getApiDocumentation(), existingCert.getApiDocumentation());
        }
        return false;
    }

    private boolean isPrivacySecurityFrameworkChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getNumber(), CertificationResultRules.PRIVACY_SECURITY)) {
            return !ObjectUtils.equals(updatedCert.getPrivacySecurityFramework(), existingCert.getPrivacySecurityFramework());
        }
        return false;
    }

    private boolean isSedChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getNumber(), CertificationResultRules.SED)) {
            return !ObjectUtils.equals(updatedCert.isSed(), existingCert.isSed());
        }
        return false;
    }
}
