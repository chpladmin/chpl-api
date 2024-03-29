package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.testtool.CertificationResultTestTool;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

@Component("unavailableCriteriaComparisonReviewer")
public class UnavailableCriteriaComparisonReviewer implements ComparisonReviewer {
    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public UnavailableCriteriaComparisonReviewer(CertificationResultRules certResultRules,
            ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        //added criteria are the ones present in updatedListing but not in existingListing
        List<CertificationResult> addedCertificationResults = getAddedCertificationResults(existingListing, updatedListing);
        addedCertificationResults.stream()
            .forEach(addedCertResult -> reviewCriterionAvailableAndAddable(updatedListing, addedCertResult));

        //check possibly updated criteria to make sure if there was a change then the criteria was editable
        for (CertificationResult updatedCert : updatedListing.getCertificationResults()) {
            for (CertificationResult existingCert : existingListing.getCertificationResults()) {
                // find matching criteria in existing/updated listings
                if (updatedCert.getCriterion().getId() != null && existingCert.getCriterion().getId() != null
                        && updatedCert.getCriterion().getId().equals(existingCert.getCriterion().getId())
                        && isCriteriaEdited(updatedCert, existingCert)) {
                        reviewCriterionAvailableAndEditable(updatedListing, existingCert, updatedCert);
                }
            }
        }
    }

    private List<CertificationResult> getAddedCertificationResults(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        return updatedListing.getCertificationResults().stream()
            .filter(updatedCertResult -> !isCriterionAttested(existingListing, updatedCertResult.getCriterion().getId()))
            .collect(Collectors.toList());
    }

    private boolean isCriterionAttested(CertifiedProductSearchDetails listing, Long criterionId) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion().getId().equals(criterionId))
                .findAny().isPresent();
    }


    private void reviewCriterionAvailableAndAddable(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!doCriterionDatesOverlapCertificationDay(listing, certResult)) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.unavailableCriteriaAddNotAllowed",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (!certResult.getCriterion().isEditable()) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.unavailableCriteriaRemovedTooLongAgo",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewCriterionAvailableAndEditable(CertifiedProductSearchDetails updatedListing, CertificationResult existingCert,
            CertificationResult updatedCert) {
        if (!doCriterionDatesOverlapCertificationDay(updatedListing, updatedCert)
                || !updatedCert.getCriterion().isEditable()) {
            addErrorsForCertEdits(updatedListing, existingCert, updatedCert);
        }
    }

    private boolean doCriterionDatesOverlapCertificationDay(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        LocalDate listingEndDay = listing.getDecertificationDay() != null ? listing.getDecertificationDay() : LocalDate.now();
        return certResult.getCriterion() != null
                && DateUtil.datesOverlap(Pair.of(listing.getCertificationDay(), listingEndDay),
                        Pair.of(certResult.getCriterion().getStartDay(), certResult.getCriterion().getEndDay()));
    }

    private boolean isCriteriaEdited(CertificationResult updatedCert, CertificationResult existingCert) {
        return existingCert.getSuccess() != null && existingCert.getSuccess()
                && updatedCert.getSuccess() != null && updatedCert.getSuccess();
    }

    private void addErrorsForCertEdits(CertifiedProductSearchDetails updatedListing,
            CertificationResult existingCert, CertificationResult updatedCert) {
        if (isGapChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "Gap"));
        }
        if (isG1SuccessChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "G1 Success"));
        }
        if (isG2SuccessChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "G2 Success"));
        }
        if (isAdditionalSoftwareChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "Additional Software"));
        }
        if (isConformanceMethodsChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "Conformance Methods"));
        }
        if (isFunctionalityTestedChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "Functionality Tested"));
        }
        if (isOptionalStandardsChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "Optional Standards"));
        }
        if (isSvapsChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "SVAPs"));
        }
        if (isTestStandardsChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "Test Standards"));
        }
        if (isTestDataChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "Test Data"));
        }
        if (isTestProceduresChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "Test Procedures"));
        }
        if (isTestToolsChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "Test Tools"));
        }
        if (isApiDocumentationChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "API Documentation"));
        }
        if (isPrivacySecurityFrameworkChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "Privacy and Security Framework"));
        }
        if (isSedChanged(existingCert, updatedCert)) {
            updatedListing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.unavailableCriteriaEditNotAllowed",
                            Util.formatCriteriaNumber(updatedCert.getCriterion()),
                            "SED"));
        }
    }

    private boolean isGapChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.GAP)) {
            return !Objects.equals(existingCert.getGap(), updatedCert.getGap());
        }
        return false;
    }

    private boolean isG1SuccessChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.G1_SUCCESS)) {
            return !Objects.equals(existingCert.getG1Success(), updatedCert.getG1Success());
        }
        return false;
    }

    private boolean isG2SuccessChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.G2_SUCCESS)) {
            return !Objects.equals(existingCert.getG2Success(), updatedCert.getG2Success());
        }
        return false;
    }

    private boolean isAdditionalSoftwareChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
            for (CertificationResultAdditionalSoftware updatedAdditionalSoftware : updatedCert.getAdditionalSoftware()) {
                boolean isInExistingCert = existingCert.getAdditionalSoftware().stream()
                        .anyMatch(existingAdditionalSoftware -> existingAdditionalSoftware.matches(updatedAdditionalSoftware));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultAdditionalSoftware existingAdditionalSoftware : existingCert.getAdditionalSoftware()) {
                boolean isInUpdatedCert = updatedCert.getAdditionalSoftware().stream()
                        .anyMatch(updatedAdditionalSoftware -> updatedAdditionalSoftware.matches(existingAdditionalSoftware));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFunctionalityTestedChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
            for (CertificationResultFunctionalityTested updatedFunctionalityTested : updatedCert.getFunctionalitiesTested()) {
                boolean isInExistingCert = existingCert.getFunctionalitiesTested().stream()
                        .anyMatch(existingFunctionalityTested -> existingFunctionalityTested.matches(updatedFunctionalityTested));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultFunctionalityTested existingFunctionalityTested : existingCert.getFunctionalitiesTested()) {
                boolean isInUpdatedCert = updatedCert.getFunctionalitiesTested().stream()
                        .anyMatch(updatedFunctionalityTested -> updatedFunctionalityTested.matches(existingFunctionalityTested));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isConformanceMethodsChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.CONFORMANCE_METHOD)) {
            for (CertificationResultConformanceMethod updatedConformanceMethod : updatedCert.getConformanceMethods()) {
                boolean isInExistingCert = existingCert.getConformanceMethods().stream()
                        .anyMatch(existingConformanceMethod -> existingConformanceMethod.matches(updatedConformanceMethod));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultConformanceMethod existingConformanceMethod : existingCert.getConformanceMethods()) {
                boolean isInUpdatedCert = updatedCert.getConformanceMethods().stream()
                        .anyMatch(updatedConformanceMethod -> updatedConformanceMethod.matches(existingConformanceMethod));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTestStandardsChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.STANDARDS_TESTED)) {
            for (CertificationResultTestStandard updatedTestStandard : updatedCert.getTestStandards()) {
                boolean isInExistingCert = existingCert.getTestStandards().stream()
                        .anyMatch(existingTestStandard -> existingTestStandard.matches(updatedTestStandard));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultTestStandard existingTestStandard : existingCert.getTestStandards()) {
                boolean isInUpdatedCert = updatedCert.getTestStandards().stream()
                        .anyMatch(updatedTestStandard -> updatedTestStandard.matches(existingTestStandard));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOptionalStandardsChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.OPTIONAL_STANDARD)) {
            for (CertificationResultOptionalStandard updatedOptionalStandard : updatedCert.getOptionalStandards()) {
                boolean isInExistingCert = existingCert.getOptionalStandards().stream()
                        .anyMatch(existingOptionalStandard -> existingOptionalStandard.matches(updatedOptionalStandard));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultOptionalStandard existingOptionalStandard : existingCert.getOptionalStandards()) {
                boolean isInUpdatedCert = updatedCert.getOptionalStandards().stream()
                        .anyMatch(updatedOptionalStandard -> updatedOptionalStandard.matches(existingOptionalStandard));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTestDataChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.TEST_DATA)) {
            for (CertificationResultTestData updatedTestData : updatedCert.getTestDataUsed()) {
                boolean isInExistingCert = existingCert.getTestDataUsed().stream()
                        .anyMatch(existingTestData -> existingTestData.matches(updatedTestData));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultTestData existingTestData : existingCert.getTestDataUsed()) {
                boolean isInUpdatedCert = updatedCert.getTestDataUsed().stream()
                        .anyMatch(updatedTestData -> updatedTestData.matches(existingTestData));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTestProceduresChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.TEST_PROCEDURE)) {
            for (CertificationResultTestProcedure updatedTestProcedure : updatedCert.getTestProcedures()) {
                boolean isInExistingCert = existingCert.getTestProcedures().stream()
                        .anyMatch(existingTestProcedure -> existingTestProcedure.matches(updatedTestProcedure));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultTestProcedure existingTestProcedure : existingCert.getTestProcedures()) {
                boolean isInUpdatedCert = updatedCert.getTestProcedures().stream()
                        .anyMatch(updatedTestProcedure -> updatedTestProcedure.matches(existingTestProcedure));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTestToolsChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.TEST_TOOLS_USED)) {
            for (CertificationResultTestTool updatedTestTool : updatedCert.getTestToolsUsed()) {
                boolean isInExistingCert = existingCert.getTestToolsUsed().stream()
                        .anyMatch(existingTestTool -> existingTestTool.matches(updatedTestTool));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultTestTool existingTestTool : existingCert.getTestToolsUsed()) {
                boolean isInUpdatedCert = updatedCert.getTestToolsUsed().stream()
                        .anyMatch(updatedTestTool -> updatedTestTool.matches(existingTestTool));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSvapsChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.SVAP)) {
            for (CertificationResultSvap updatedSvap : updatedCert.getSvaps()) {
                boolean isInExistingCert = existingCert.getSvaps().stream()
                        .anyMatch(existingSvap -> existingSvap.matches(updatedSvap));
                if (!isInExistingCert) {
                    return true;
                }
            }

            for (CertificationResultSvap existingSvap : existingCert.getSvaps()) {
                boolean isInUpdatedCert = updatedCert.getSvaps().stream()
                        .anyMatch(updatedSvap -> updatedSvap.matches(existingSvap));
                if (!isInUpdatedCert) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isApiDocumentationChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.API_DOCUMENTATION)) {
            return !Objects.equals(updatedCert.getApiDocumentation(), existingCert.getApiDocumentation());
        }
        return false;
    }

    private boolean isPrivacySecurityFrameworkChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.PRIVACY_SECURITY)) {
            return !Objects.equals(updatedCert.getPrivacySecurityFramework(), existingCert.getPrivacySecurityFramework());
        }
        return false;
    }

    private boolean isSedChanged(CertificationResult existingCert, CertificationResult updatedCert) {
        if (certResultRules.hasCertOption(updatedCert.getCriterion().getId(), CertificationResultRules.SED)) {
            return !Objects.equals(updatedCert.getSed(), existingCert.getSed());
        }
        return false;
    }
}
