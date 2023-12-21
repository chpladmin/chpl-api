package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("requiredData2015Reviewer")
public class RequiredData2015Reviewer extends PermissionBasedReviewer {
    private static final String[] UCD_RELATED_CERTS = {
            "170.315 (a)(1)", "170.315 (a)(2)", "170.315 (a)(3)", "170.315 (a)(4)", "170.315 (a)(5)", "170.315 (a)(6)",
            "170.315 (a)(7)", "170.315 (a)(8)", "170.315 (a)(9)", "170.315 (a)(14)", "170.315 (b)(2)", "170.315 (b)(3)"
    };

    private static final String G1_CRITERIA_NUMBER = "170.315 (g)(1)";
    private static final String G2_CRITERIA_NUMBER = "170.315 (g)(2)";
    private static final int MINIMUM_TEST_PARTICIPANT_COUNT = 10;

    private CertificationResultRules certRules;
    private TestDataDAO testDataDao;
    private ValidationUtils validationUtils;
    private FF4j ff4j;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public RequiredData2015Reviewer(CertificationResultRules certRules, ErrorMessageUtil msgUtil, TestDataDAO testDataDao,
            ValidationUtils validationUtils, ResourcePermissions resourcePermissions, FF4j ff4j) {
        super(msgUtil, resourcePermissions);
        this.certRules = certRules;
        this.testDataDao = testDataDao;
        this.validationUtils = validationUtils;
        this.ff4j = ff4j;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        reviewRequiredFieldsCommonToAllListings(listing);

        if (listing.getIcs() == null || listing.getIcs().getInherits() == null) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.missingIcs"));
        }

        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);

        for (int i = 0; i < UCD_RELATED_CERTS.length; i++) {
            if (validationUtils.hasCert(UCD_RELATED_CERTS[i], attestedCriteria)) {
                // check for full set of UCD data
                for (CertificationResult cert : listing.getCertificationResults()) {
                    if (cert.getSuccess() != null && cert.getSuccess().equals(Boolean.TRUE)
                            && cert.getCriterion().getNumber().equals(UCD_RELATED_CERTS[i])) {
                        if (cert.getSed()) {
                            if (listing.getSed() == null || listing.getSed().getTestTasks() == null
                                    || listing.getSed().getTestTasks().size() == 0) {
                                addBusinessCriterionError(listing, cert, "listing.criteria.missingTestTask",
                                        Util.formatCriteriaNumber(cert.getCriterion()));
                            } else {

                                boolean foundCriteria = false;
                                for (TestTask tt : listing.getSed().getTestTasks()) {
                                    for (CertificationCriterion criteria : tt.getCriteria()) {
                                        if (criteria.getId().equals(cert.getCriterion().getId())) {
                                            foundCriteria = true;
                                        }
                                    }
                                }
                                if (!foundCriteria) {
                                    addBusinessCriterionError(listing, cert, "listing.criteria.missingTestTask",
                                            Util.formatCriteriaNumber(cert.getCriterion()));
                                }
                            }
                        }

                        if (listing.getSed() != null && listing.getSed().getTestTasks() != null) {
                            for (TestTask task : listing.getSed().getTestTasks()) {
                                String description = StringUtils.isEmpty(task.getDescription()) ? "unknown"
                                        : task.getDescription();
                                if (task.getTestParticipants() == null) {
                                    listing.addDataErrorMessage(
                                            msgUtil.getMessage("listing.sed.badTestTaskParticipantsSize", description));
                                }
                                if (task.getTestParticipants().size() < MINIMUM_TEST_PARTICIPANT_COUNT) {
                                    listing.addBusinessErrorMessage(
                                            msgUtil.getMessage("listing.sed.badTestTaskParticipantsSize", description));
                                }
                                if (StringUtils.isEmpty(task.getDescription())) {
                                    listing.addDataErrorMessage(msgUtil.getMessage("listing.sed.badTestDescription", description));
                                }
                                if (task.getTaskSuccessAverage() == null) {
                                    listing.addDataErrorMessage(
                                            msgUtil.getMessage("listing.sed.badTestTaskSuccessAverage", description));
                                }
                                if (task.getTaskSuccessStddev() == null) {
                                    listing.addDataErrorMessage(
                                            msgUtil.getMessage("listing.sed.badTestTaskSuccessStddev", description));
                                }
                                if (task.getTaskPathDeviationObserved() == null) {
                                    listing.addDataErrorMessage(msgUtil
                                            .getMessage("listing.sed.badTestTaskPathDeviationObserved", description));
                                }
                                if (task.getTaskPathDeviationOptimal() == null) {
                                    listing.addDataErrorMessage(msgUtil
                                            .getMessage("listing.sed.badTestTaskPathDeviationOptimal", description));
                                }
                                if (task.getTaskTimeAvg() == null) {
                                    listing.addDataErrorMessage(
                                            msgUtil.getMessage("listing.sed.badTestTaskTimeAvg", description));
                                }
                                if (task.getTaskTimeStddev() == null) {
                                    listing.addDataErrorMessage(
                                            msgUtil.getMessage("listing.sed.badTestTaskTimeStddev", description));
                                }
                                if (task.getTaskTimeDeviationObservedAvg() == null) {
                                    listing.addDataErrorMessage(msgUtil.getMessage(
                                            "listing.sed.badTestTaskTimeDeviationObservedAvg", description));
                                }
                                if (task.getTaskTimeDeviationOptimalAvg() == null) {
                                    listing.addDataErrorMessage(msgUtil
                                            .getMessage("listing.sed.badTestTaskTimeDeviationOptimalAvg", description));
                                }
                                if (task.getTaskErrors() == null) {
                                    listing.addDataErrorMessage(
                                            msgUtil.getMessage("listing.sed.badTestTaskErrors", description));
                                }
                                if (task.getTaskErrorsStddev() == null) {
                                    listing.addDataErrorMessage(
                                            msgUtil.getMessage("listing.sed.badTestTaskErrorsStddev", description));
                                }
                                if (StringUtils.isEmpty(task.getTaskRatingScale())) {
                                    listing.addDataErrorMessage(
                                            msgUtil.getMessage("listing.sed.badTestTaskRatingScale", description));
                                }
                                if (task.getTaskRating() == null) {
                                    listing.addDataErrorMessage(
                                            msgUtil.getMessage("listing.sed.badTestTaskRating", description));
                                }
                                if (task.getTaskRatingStddev() == null) {
                                    listing.addDataErrorMessage(
                                            msgUtil.getMessage("listing.sed.badTestTaskRatingStddev", description));
                                }
                                for (TestParticipant part : task.getTestParticipants()) {
                                    if (part.getEducationTypeId() == null) {
                                        listing.addDataErrorMessage(msgUtil
                                                .getMessage("listing.sed.badParticipantEducationLevel", description));
                                    }
                                    if (part.getAgeRangeId() == null) {
                                        listing.addDataErrorMessage(
                                                msgUtil.getMessage("listing.sed.badParticipantAgeRange", description));
                                    }
                                    if (StringUtils.isEmpty(part.getGender())) {
                                        listing.addDataErrorMessage(
                                                msgUtil.getMessage("listing.sed.badParticipantGender", description));
                                    }
                                    if (StringUtils.isEmpty(part.getOccupation())) {
                                        listing.addDataErrorMessage(msgUtil
                                                .getMessage("listing.sed.badParticipantOccupation", description));
                                    }
                                    if (StringUtils.isEmpty(part.getAssistiveTechnologyNeeds())) {
                                        listing.addDataErrorMessage(msgUtil.getMessage(
                                                "listing.sed.badParticipantAssistiveTechnologyNeeds", description));
                                    }
                                    if (part.getProfessionalExperienceMonths() == null) {
                                        listing.addDataErrorMessage(msgUtil.getMessage(
                                                "listing.sed.badParticipantProfessionalExperienceMonths", description));
                                    }
                                    if (part.getProductExperienceMonths() == null) {
                                        listing.addDataErrorMessage(msgUtil.getMessage(
                                                "listing.sed.badParticipantProductExperienceMonths", description));
                                    }
                                    if (part.getComputerExperienceMonths() == null) {
                                        listing.addDataErrorMessage(msgUtil.getMessage(
                                                "listing.sed.badParticipantComputerExperienceMonths", description));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.getSuccess() != null && cert.getSuccess()) {
                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.GAP)
                        && cert.getGap() != null && cert.getGap()) {
                    gapEligibleAndTrue = true;
                }

                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.ATTESTATION_ANSWER)
                        && cert.getAttestationAnswer() == null) {
                    addBusinessCriterionError(listing, cert,
                            "listing.criteria.missingAttestationAnswer", Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.PRIVACY_SECURITY)
                        && StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    addBusinessCriterionError(listing, cert,
                            "listing.criteria.missingPrivacySecurityFramework", Util.formatCriteriaNumber(cert.getCriterion()));
                }
                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.API_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getApiDocumentation())) {
                    addDataCriterionError(listing, cert, "listing.criteria.missingApiDocumentation",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }
                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.EXPORT_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getExportDocumentation())) {
                    addBusinessCriterionError(listing, cert, "listing.criteria.missingExportDocumentation",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.USE_CASES)
                        && StringUtils.isEmpty(cert.getUseCases())
                        && cert.getAttestationAnswer() != null && cert.getAttestationAnswer().equals(Boolean.TRUE)) {
                    addBusinessCriterionError(listing, cert, "listing.criteria.missingUseCases",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                } else if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.USE_CASES)
                        && !StringUtils.isEmpty(cert.getUseCases())
                        && (cert.getAttestationAnswer() == null || cert.getAttestationAnswer().equals(Boolean.FALSE))) {
                    listing.addWarningMessage(
                            msgUtil.getMessage("listing.criteria.useCasesWithoutAttestation",
                                    Util.formatCriteriaNumber(cert.getCriterion())));
                }

                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.SERVICE_BASE_URL_LIST)
                        && StringUtils.isEmpty(cert.getServiceBaseUrlList())) {
                    addDataCriterionError(listing, cert, "listing.criteria.missingServiceBaseUrlList",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.RISK_MANAGEMENT_SUMMARY_INFORMATION)
                        && StringUtils.isEmpty(cert.getRiskManagementSummaryInformation())) {
                    addBusinessCriterionError(listing, cert, "listing.criteria.missingRiskManagementSummaryInformation",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                // require at least one test procedure where gap does not exist
                // or is false, and criteria cannot have Conformance Methods
                if (!gapEligibleAndTrue
                        && (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.TEST_PROCEDURE)
                                && !certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.CONFORMANCE_METHOD))
                        && (cert.getTestProcedures() == null || cert.getTestProcedures().size() == 0)) {
                    addBusinessCriterionError(listing, cert, "listing.criteria.missingTestProcedure",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.CONFORMANCE_METHOD)
                        && (cert.getConformanceMethods() == null || cert.getConformanceMethods().size() == 0)) {
                    addBusinessCriterionError(listing, cert, "listing.criteria.conformanceMethod.missingConformanceMethod",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.TEST_DATA)
                        && cert.getTestDataUsed() != null && cert.getTestDataUsed().size() > 0) {
                    for (CertificationResultTestData crTestData : cert.getTestDataUsed()) {
                        if (crTestData.getTestData() == null
                                || (crTestData.getTestData() != null && crTestData.getTestData().getId() == null
                                        && StringUtils.isEmpty(crTestData.getTestData().getName()))) {
                            listing.addWarningMessage(msgUtil.getMessage("listing.criteria.missingTestDataNameReplaced",
                                    Util.formatCriteriaNumber(cert.getCriterion()), TestDataDTO.DEFAULT_TEST_DATA));
                            TestDataDTO foundTestData = testDataDao.getByCriterionAndValue(cert.getCriterion().getId(),
                                    TestDataDTO.DEFAULT_TEST_DATA);
                            TestData foundTestDataDomain = new TestData(foundTestData.getId(), foundTestData.getName());
                            crTestData.setTestData(foundTestDataDomain);
                        } else if (crTestData.getTestData() != null && crTestData.getTestData().getId() == null
                                && !StringUtils.isEmpty(crTestData.getTestData().getName())) {
                            TestDataDTO foundTestData = testDataDao.getByCriterionAndValue(cert.getCriterion().getId(),
                                    crTestData.getTestData().getName());
                            if (foundTestData == null || foundTestData.getId() == null) {
                                listing.addWarningMessage(msgUtil.getMessage("listing.criteria.badTestDataName",
                                                crTestData.getTestData().getName(), Util.formatCriteriaNumber(cert.getCriterion()),
                                                TestDataDTO.DEFAULT_TEST_DATA));
                                foundTestData = testDataDao.getByCriterionAndValue(cert.getCriterion().getId(),
                                        TestDataDTO.DEFAULT_TEST_DATA);
                                crTestData.getTestData().setId(foundTestData.getId());
                            } else {
                                crTestData.getTestData().setId(foundTestData.getId());
                            }
                        } else if (crTestData.getTestData() != null && crTestData.getTestData().getId() != null) {
                            List<TestDataDTO> criterionTestData = testDataDao.getByCriterionId(cert.getCriterion().getId());
                            boolean hasMatchingTestDatum = criterionTestData.stream()
                                    .filter(testDatum -> testDatum.getId().equals(crTestData.getTestData().getId()))
                                    .findAny().isPresent();
                            if (!hasMatchingTestDatum) {
                                String testDataName = crTestData.getTestData().getName();
                                listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.invalidTestDataId", crTestData.getTestData().getId(), Util.formatCriteriaNumber(cert.getCriterion())));
                            }
                        }

                        if (crTestData.getTestData() != null && !StringUtils.isEmpty(crTestData.getTestData().getName())
                                && StringUtils.isEmpty(crTestData.getVersion())) {
                            addDataCriterionError(listing, cert,
                                    "listing.criteria.missingTestDataVersion", Util.formatCriteriaNumber(cert.getCriterion()));
                        }
                    }
                }

                if (!gapEligibleAndTrue
                        && (cert.getCriterion().getNumber().equals(G1_CRITERIA_NUMBER) || cert.getCriterion().getNumber().equals(G2_CRITERIA_NUMBER))
                        && (cert.getTestDataUsed() == null || cert.getTestDataUsed().size() == 0)) {
                    listing.addBusinessErrorMessage("Test Data is required for certification "
                            + Util.formatCriteriaNumber(cert.getCriterion()) + ".");
                }
            }
        }
    }

    private void reviewRequiredFieldsCommonToAllListings(CertifiedProductSearchDetails listing) {
        if (!ff4j.check(FeatureList.EDITIONLESS)) {
            if (listing.getEdition() == null
                    || listing.getEdition().getId() == null) {
                listing.addBusinessErrorMessage("Certification edition is required but was not found.");
            }
        }

        if (StringUtils.isEmpty(listing.getAcbCertificationId())) {
            listing.addWarningMessage("CHPL certification ID was not found.");
        }
        if (listing.getCertificationDate() == null) {
            listing.addBusinessErrorMessage("Certification date was not found.");
        }
        if (listing.getDeveloper() == null) {
            listing.addBusinessErrorMessage("A developer is required.");
        }
        if (listing.getProduct() == null || StringUtils.isEmpty(listing.getProduct().getName())) {
            listing.addBusinessErrorMessage("A product name is required.");
        }
        if (listing.getVersion() == null || StringUtils.isEmpty(listing.getVersion().getVersion())) {
            listing.addBusinessErrorMessage("A product version is required.");
        }
        if (listing.getOldestStatus() == null) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.noStatusProvided"));
        }

        for (CertificationResult cert : listing.getCertificationResults()) {
            if (BooleanUtils.isTrue(cert.getSuccess())
                    && certRules.hasCertOption(cert.getCriterion().getId(), CertificationResultRules.GAP)
                    && cert.getGap() == null) {
                addBusinessCriterionError(listing, cert, "listing.criteria.missingGap",
                        Util.formatCriteriaNumber(cert.getCriterion()));
            }
        }
    }
}
