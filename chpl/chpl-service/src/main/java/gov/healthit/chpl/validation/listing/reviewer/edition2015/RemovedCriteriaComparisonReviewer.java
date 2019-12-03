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
                boolean hasMatch = false;
                for (CertificationResultAdditionalSoftware existingAdditionalSoftware : existingCert.getAdditionalSoftware()) {
                    if (updatedAdditionalSoftware.matches(existingAdditionalSoftware)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }

            for (CertificationResultAdditionalSoftware existingAdditionalSoftware : existingCert.getAdditionalSoftware()) {
                boolean hasMatch = false;
                for (CertificationResultAdditionalSoftware updatedAdditionalSoftware : updatedCert.getAdditionalSoftware()) {
                    if (existingAdditionalSoftware.matches(updatedAdditionalSoftware)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFunctionalityTestedChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
            for (CertificationResultTestFunctionality updatedTestFunctionality : updatedCert.getTestFunctionality()) {
                boolean hasMatch = false;
                for (CertificationResultTestFunctionality existingTestFunctionality : existingCert.getTestFunctionality()) {
                    if (updatedTestFunctionality.matches(existingTestFunctionality)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }

            for (CertificationResultTestFunctionality existingTestFunctionality : existingCert.getTestFunctionality()) {
                boolean hasMatch = false;
                for (CertificationResultTestFunctionality updatedTestFunctionality : updatedCert.getTestFunctionality()) {
                    if (existingTestFunctionality.matches(updatedTestFunctionality)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isG1MacraMeasuresChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.G1_MACRA)) {
            for (MacraMeasure updatedMacra : updatedCert.getG1MacraMeasures()) {
                boolean hasMatch = false;
                for (MacraMeasure existingMacra : existingCert.getG1MacraMeasures()) {
                    if (updatedMacra.matches(existingMacra)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }

            for (MacraMeasure existingMacra : existingCert.getG1MacraMeasures()) {
                boolean hasMatch = false;
                for (MacraMeasure updatedMacra : updatedCert.getG1MacraMeasures()) {
                    if (existingMacra.matches(updatedMacra)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isG2MacraMeasuresChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.G2_MACRA)) {
            for (MacraMeasure updatedMacra : updatedCert.getG2MacraMeasures()) {
                boolean hasMatch = false;
                for (MacraMeasure existingMacra : existingCert.getG2MacraMeasures()) {
                    if (updatedMacra.matches(existingMacra)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }

            for (MacraMeasure existingMacra : existingCert.getG2MacraMeasures()) {
                boolean hasMatch = false;
                for (MacraMeasure updatedMacra : updatedCert.getG2MacraMeasures()) {
                    if (existingMacra.matches(updatedMacra)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTestStandardsChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.STANDARDS_TESTED)) {
            for (CertificationResultTestStandard updatedTestStandard : updatedCert.getTestStandards()) {
                boolean hasMatch = false;
                for (CertificationResultTestStandard existingTestStandard : existingCert.getTestStandards()) {
                    if (updatedTestStandard.matches(existingTestStandard)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }

            for (CertificationResultTestStandard existingTestStandard : existingCert.getTestStandards()) {
                boolean hasMatch = false;
                for (CertificationResultTestStandard updatedTestStandard : updatedCert.getTestStandards()) {
                    if (existingTestStandard.matches(updatedTestStandard)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTestDataChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.TEST_DATA)) {
            for (CertificationResultTestData updatedTestData : updatedCert.getTestDataUsed()) {
                boolean hasMatch = false;
                for (CertificationResultTestData existingTestData : existingCert.getTestDataUsed()) {
                    if (updatedTestData.matches(existingTestData)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }

            for (CertificationResultTestData existingTestData : existingCert.getTestDataUsed()) {
                boolean hasMatch = false;
                for (CertificationResultTestData updatedTestData : updatedCert.getTestDataUsed()) {
                    if (existingTestData.matches(updatedTestData)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTestProceduresChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.TEST_PROCEDURE)) {
            for (CertificationResultTestProcedure updatedTestProcedure : updatedCert.getTestProcedures()) {
                boolean hasMatch = false;
                for (CertificationResultTestProcedure existingTestProcedure : existingCert.getTestProcedures()) {
                    if (updatedTestProcedure.matches(existingTestProcedure)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }

            for (CertificationResultTestProcedure existingTestProcedure : existingCert.getTestProcedures()) {
                boolean hasMatch = false;
                for (CertificationResultTestProcedure updatedTestProcedure : updatedCert.getTestProcedures()) {
                    if (existingTestProcedure.matches(updatedTestProcedure)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTestToolsChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getNumber(), CertificationResultRules.TEST_TOOLS_USED)) {
            for (CertificationResultTestTool updatedTestTool : updatedCert.getTestToolsUsed()) {
                boolean hasMatch = false;
                for (CertificationResultTestTool existingTestTool : existingCert.getTestToolsUsed()) {
                    if (updatedTestTool.matches(existingTestTool)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
                    return true;
                }
            }

            for (CertificationResultTestTool existingTestTool : existingCert.getTestToolsUsed()) {
                boolean hasMatch = false;
                for (CertificationResultTestTool updatedTestTool : updatedCert.getTestToolsUsed()) {
                    if (existingTestTool.matches(updatedTestTool)) {
                        hasMatch = true;
                    }
                }
                if (!hasMatch) {
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
