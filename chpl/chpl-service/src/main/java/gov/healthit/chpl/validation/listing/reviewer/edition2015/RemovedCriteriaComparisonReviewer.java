package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import org.apache.commons.lang.ObjectUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
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
    private FF4j ff4j;

    @Autowired
    public RemovedCriteriaComparisonReviewer(CertificationResultRules certResultRules,
            ResourcePermissions resourcePermissions,
            ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.certResultRules = certResultRules;
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            return;
        }

        //checking for the addition of a removed criteria
        //this is only disallowed if the user is not ADMIN/ONC, so first check the permissions
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        }

        for (CertificationResult updatedCert : updatedListing.getCertificationResults()) {
            for (CertificationResult existingCert : existingListing.getCertificationResults()) {
                //find matching criteria in existing/updated listings
                if (!StringUtils.isEmpty(updatedCert.getNumber()) && !StringUtils.isEmpty(existingCert.getNumber())
                        && updatedCert.getNumber().equals(existingCert.getNumber())) {
                    if (isRemovedCertAdded(existingCert, updatedCert)) {
                        updatedListing.getErrorMessages().add(
                                msgUtil.getMessage("listing.removedCriteriaAddNotAllowed", updatedCert.getNumber()));
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
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(), "Gap"));
        }
        if (isG1SuccessChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(), "G1 Success"));
        }
        if (isG2SuccessChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(), "G2 Success"));
        }
        if (isAdditionalSoftwareChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(), "Additional Software"));
        }
        if (isFunctionalityTestedChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(), "Functionality Tested"));
        }
        if (isG1MacraMeasuresChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(), "G1 Macra Measures"));
        }
        if (isG2MacraMeasuresChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(), "G2 Macra Measures"));
        }
        if (isTestStandardsChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(), "Test Standards"));
        }
        if (isTestDataChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(), "Test Data"));
        }
        if (isTestProceduresChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(), "Test Procedures"));
        }
        if (isTestToolsChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(), "Test Tools"));
        }
        if (isApiDocumentationChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(), "API Documentation"));
        }
        if (isPrivacySecurityFrameworkChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(),
                            "Privacy and Security Framework"));
        }
        if (isSedChanged(existingCert, updatedCert)) {
            updatedListing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedCriteriaEditNotAllowed", updatedCert.getNumber(), "SED"));
        }
    }

    private boolean isGapChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.GAP)) {
            return !ObjectUtils.equals(existingCert.isGap(), updatedCert.isGap());
        }
        return false;
    }

    private boolean isG1SuccessChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.G1_SUCCESS)) {
            return !ObjectUtils.equals(existingCert.isG1Success(), updatedCert.isG1Success());
        }
        return false;
    }

    private boolean isG2SuccessChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.G2_SUCCESS)) {
            return !ObjectUtils.equals(existingCert.isG2Success(), updatedCert.isG2Success());
        }
        return false;
    }

    private boolean isAdditionalSoftwareChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
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
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
            for (CertificationResultTestFunctionality updatedTestFunctionality : updatedCert.getTestFunctionality()) {
                boolean isInExistingCert =
                        existingCert.getTestFunctionality().stream()
                        .anyMatch(existingTestFunctionality -> existingTestFunctionality.matches(updatedTestFunctionality));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultTestFunctionality existingTestFunctionality : existingCert.getTestFunctionality()) {
                boolean isInUpdatedCert =
                        updatedCert.getTestFunctionality().stream()
                        .anyMatch(updatedTestFunctionality -> updatedTestFunctionality.matches(existingTestFunctionality));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isG1MacraMeasuresChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.G1_MACRA)) {
            for (MacraMeasure updatedMacra : updatedCert.getG1MacraMeasures()) {
                boolean isInExistingCert =
                        existingCert.getG1MacraMeasures().stream()
                        .anyMatch(existingMacra -> existingMacra.matches(updatedMacra));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (MacraMeasure existingMacra : existingCert.getG1MacraMeasures()) {
                boolean isInUpdatedCert =
                        updatedCert.getG1MacraMeasures().stream()
                        .anyMatch(updatedMacra -> updatedMacra.matches(existingMacra));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isG2MacraMeasuresChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.G2_MACRA)) {
            for (MacraMeasure updatedMacra : updatedCert.getG2MacraMeasures()) {
                boolean isInExistingCert =
                        existingCert.getG2MacraMeasures().stream()
                        .anyMatch(existingMacra -> existingMacra.matches(updatedMacra));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (MacraMeasure existingMacra : existingCert.getG2MacraMeasures()) {
                boolean isInUpdatedCert =
                        updatedCert.getG2MacraMeasures().stream()
                        .anyMatch(updatedMacra -> updatedMacra.matches(existingMacra));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTestStandardsChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.STANDARDS_TESTED)) {
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
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.TEST_DATA)) {
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
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.TEST_PROCEDURE)) {
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
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.TEST_TOOLS_USED)) {
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
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.API_DOCUMENTATION)) {
            return !ObjectUtils.equals(updatedCert.getApiDocumentation(), existingCert.getApiDocumentation());
        }
        return false;
    }

    private boolean isPrivacySecurityFrameworkChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.PRIVACY_SECURITY)) {
            return !ObjectUtils.equals(updatedCert.getPrivacySecurityFramework(), existingCert.getPrivacySecurityFramework());
        }
        return false;
    }

    private boolean isSedChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.SED)) {
            return !ObjectUtils.equals(updatedCert.isSed(), existingCert.isSed());
        }
        return false;
    }
}
