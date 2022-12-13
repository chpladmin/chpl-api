package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
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
import gov.healthit.chpl.validation.listing.reviewer.RequiredDataReviewer;

@Component("requiredData2015Reviewer")
public class RequiredData2015Reviewer extends RequiredDataReviewer {
    private static final String[] UCD_RELATED_CERTS = {
            "170.315 (a)(1)", "170.315 (a)(2)", "170.315 (a)(3)", "170.315 (a)(4)", "170.315 (a)(5)", "170.315 (a)(6)",
            "170.315 (a)(7)", "170.315 (a)(8)", "170.315 (a)(9)", "170.315 (a)(14)", "170.315 (b)(2)", "170.315 (b)(3)"
    };

    private static final String G1_CRITERIA_NUMBER = "170.315 (g)(1)";
    private static final String G2_CRITERIA_NUMBER = "170.315 (g)(2)";
    private static final int MINIMUM_TEST_PARTICIPANT_COUNT = 10;

    private TestDataDAO testDataDao;
    private ValidationUtils validationUtils;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public RequiredData2015Reviewer(CertificationResultRules certRules, ErrorMessageUtil msgUtil,
            TestDataDAO testDataDao,
            ValidationUtils validationUtils, ResourcePermissions resourcePermissions) {
        super(certRules, msgUtil, resourcePermissions);
        this.testDataDao = testDataDao;
        this.validationUtils = validationUtils;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        super.review(listing);

        if (listing.getIcs() == null || listing.getIcs().getInherits() == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingIcs"));
        }

        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);

        for (int i = 0; i < UCD_RELATED_CERTS.length; i++) {
            if (validationUtils.hasCert(UCD_RELATED_CERTS[i], attestedCriteria)) {
                // check for full set of UCD data
                for (CertificationResult cert : listing.getCertificationResults()) {
                    if (cert.isSuccess() != null && cert.isSuccess().equals(Boolean.TRUE)
                            && cert.getCriterion().getNumber().equals(UCD_RELATED_CERTS[i])) {
                        if (cert.isSed()) {
                            if (listing.getSed() == null || listing.getSed().getTestTasks() == null
                                    || listing.getSed().getTestTasks().size() == 0) {
                                addCriterionError(listing, cert, "listing.criteria.missingTestTask",
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
                                    addCriterionError(listing, cert, "listing.criteria.missingTestTask",
                                            Util.formatCriteriaNumber(cert.getCriterion()));
                                }
                            }
                        }

                        if (listing.getSed() != null && listing.getSed().getTestTasks() != null) {
                            for (TestTask task : listing.getSed().getTestTasks()) {
                                String description = StringUtils.isEmpty(task.getDescription()) ? "unknown"
                                        : task.getDescription();
                                if (task.getTestParticipants() == null || task.getTestParticipants().size() < MINIMUM_TEST_PARTICIPANT_COUNT) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.sed.badTestTaskParticipantsSize", description));
                                }
                                if (StringUtils.isEmpty(task.getDescription())) {
                                    listing.getErrorMessages()
                                    .add(msgUtil.getMessage("listing.sed.badTestDescription", description));
                                }
                                if (task.getTaskSuccessAverage() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.sed.badTestTaskSuccessAverage", description));
                                }
                                if (task.getTaskSuccessStddev() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.sed.badTestTaskSuccessStddev", description));
                                }
                                if (task.getTaskPathDeviationObserved() == null) {
                                    listing.getErrorMessages().add(msgUtil
                                            .getMessage("listing.sed.badTestTaskPathDeviationObserved", description));
                                }
                                if (task.getTaskPathDeviationOptimal() == null) {
                                    listing.getErrorMessages().add(msgUtil
                                            .getMessage("listing.sed.badTestTaskPathDeviationOptimal", description));
                                }
                                if (task.getTaskTimeAvg() == null) {
                                    listing.getErrorMessages()
                                    .add(msgUtil.getMessage("listing.sed.badTestTaskTimeAvg", description));
                                }
                                if (task.getTaskTimeStddev() == null) {
                                    listing.getErrorMessages()
                                    .add(msgUtil.getMessage("listing.sed.badTestTaskTimeStddev", description));
                                }
                                if (task.getTaskTimeDeviationObservedAvg() == null) {
                                    listing.getErrorMessages().add(msgUtil.getMessage(
                                            "listing.sed.badTestTaskTimeDeviationObservedAvg", description));
                                }
                                if (task.getTaskTimeDeviationOptimalAvg() == null) {
                                    listing.getErrorMessages().add(msgUtil
                                            .getMessage("listing.sed.badTestTaskTimeDeviationOptimalAvg", description));
                                }
                                if (task.getTaskErrors() == null) {
                                    listing.getErrorMessages()
                                    .add(msgUtil.getMessage("listing.sed.badTestTaskErrors", description));
                                }
                                if (task.getTaskErrorsStddev() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.sed.badTestTaskErrorsStddev", description));
                                }
                                if (StringUtils.isEmpty(task.getTaskRatingScale())) {
                                    listing.getErrorMessages()
                                    .add(msgUtil.getMessage("listing.sed.badTestTaskRatingScale", description));
                                }
                                if (task.getTaskRating() == null) {
                                    listing.getErrorMessages()
                                    .add(msgUtil.getMessage("listing.sed.badTestTaskRating", description));
                                }
                                if (task.getTaskRatingStddev() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.sed.badTestTaskRatingStddev", description));
                                }
                                for (TestParticipant part : task.getTestParticipants()) {
                                    if (part.getEducationTypeId() == null) {
                                        listing.getErrorMessages().add(msgUtil
                                                .getMessage("listing.sed.badParticipantEducationLevel", description));
                                    }
                                    if (part.getAgeRangeId() == null) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.sed.badParticipantAgeRange", description));
                                    }
                                    if (StringUtils.isEmpty(part.getGender())) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.sed.badParticipantGender", description));
                                    }
                                    if (StringUtils.isEmpty(part.getOccupation())) {
                                        listing.getErrorMessages().add(msgUtil
                                                .getMessage("listing.sed.badParticipantOccupation", description));
                                    }
                                    if (StringUtils.isEmpty(part.getAssistiveTechnologyNeeds())) {
                                        listing.getErrorMessages().add(msgUtil.getMessage(
                                                "listing.sed.badParticipantAssistiveTechnologyNeeds", description));
                                    }
                                    if (part.getProfessionalExperienceMonths() == null) {
                                        listing.getErrorMessages().add(msgUtil.getMessage(
                                                "listing.sed.badParticipantProfessionalExperienceMonths", description));
                                    }
                                    if (part.getProductExperienceMonths() == null) {
                                        listing.getErrorMessages().add(msgUtil.getMessage(
                                                "listing.sed.badParticipantProductExperienceMonths", description));
                                    }
                                    if (part.getComputerExperienceMonths() == null) {
                                        listing.getErrorMessages().add(msgUtil.getMessage(
                                                "listing.sed.badParticipantComputerExperienceMonths", description));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (listing.getQmsStandards() == null || listing.getQmsStandards().size() == 0) {
            listing.getErrorMessages().add("QMS Standards are required.");
        } else {
            for (CertifiedProductQmsStandard qms : listing.getQmsStandards()) {
                if (StringUtils.isEmpty(qms.getApplicableCriteria())) {
                    listing.getErrorMessages().add("Applicable criteria is required for each QMS Standard listed.");
                }
            }
        }

        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.isSuccess() != null && cert.isSuccess()) {
                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.GAP)
                        && cert.isGap() != null && cert.isGap()) {
                    gapEligibleAndTrue = true;
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.ATTESTATION_ANSWER)
                        && cert.getAttestationAnswer() == null) {
                    addCriterionError(listing, cert,
                            "listing.criteria.missingAttestationAnswer", Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.PRIVACY_SECURITY)
                        && StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    addCriterionError(listing, cert,
                            "listing.criteria.missingPrivacySecurityFramework", Util.formatCriteriaNumber(cert.getCriterion()));
                }
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.API_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getApiDocumentation())) {
                    addCriterionError(listing, cert, "listing.criteria.missingApiDocumentation",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.EXPORT_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getExportDocumentation())) {
                    addCriterionError(listing, cert, "listing.criteria.missingExportDocumentation",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.USE_CASES)
                        && StringUtils.isEmpty(cert.getUseCases())
                        && cert.getAttestationAnswer() != null && cert.getAttestationAnswer().equals(Boolean.TRUE)) {
                    addCriterionError(listing, cert, "listing.criteria.missingUseCases",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                } else if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.USE_CASES)
                        && !StringUtils.isEmpty(cert.getUseCases())
                        && (cert.getAttestationAnswer() == null || cert.getAttestationAnswer().equals(Boolean.FALSE))) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.useCasesWithoutAttestation",
                                    Util.formatCriteriaNumber(cert.getCriterion())));
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.SERVICE_BASE_URL_LIST)
                        && StringUtils.isEmpty(cert.getServiceBaseUrlList())) {
                    addCriterionError(listing, cert, "listing.criteria.missingServiceBaseUrlList",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                // require at least one test procedure where gap does not exist
                // or is false, and criteria cannot have Conformance Methods
                if (!gapEligibleAndTrue
                        && (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.TEST_PROCEDURE)
                                && !certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.CONFORMANCE_METHOD))
                        && (cert.getTestProcedures() == null || cert.getTestProcedures().size() == 0)) {
                    addCriterionError(listing, cert, "listing.criteria.missingTestProcedure",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.CONFORMANCE_METHOD)
                        && (cert.getConformanceMethods() == null || cert.getConformanceMethods().size() == 0)) {
                    addCriterionError(listing, cert, "listing.criteria.conformanceMethod.missingConformanceMethod",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.TEST_DATA)
                        && cert.getTestDataUsed() != null && cert.getTestDataUsed().size() > 0) {
                    for (CertificationResultTestData crTestData : cert.getTestDataUsed()) {
                        if (crTestData.getTestData() == null
                                || (crTestData.getTestData() != null && crTestData.getTestData().getId() == null
                                        && StringUtils.isEmpty(crTestData.getTestData().getName()))) {
                            listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.missingTestDataNameReplaced",
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
                                listing.getWarningMessages()
                                .add(msgUtil.getMessage("listing.criteria.badTestDataName",
                                        crTestData.getTestData().getName(),  Util.formatCriteriaNumber(cert.getCriterion()),
                                        TestDataDTO.DEFAULT_TEST_DATA));
                                foundTestData = testDataDao.getByCriterionAndValue(cert.getCriterion().getId(),
                                        TestDataDTO.DEFAULT_TEST_DATA);
                                crTestData.getTestData().setId(foundTestData.getId());
                            } else {
                                crTestData.getTestData().setId(foundTestData.getId());
                            }
                        }  else if (crTestData.getTestData() != null && crTestData.getTestData().getId() != null) {
                            List<TestDataDTO> criterionTestData = testDataDao.getByCriterionId(cert.getCriterion().getId());
                            boolean hasMatchingTestDatum = criterionTestData.stream()
                                    .filter(testDatum -> testDatum.getId().equals(crTestData.getTestData().getId()))
                                    .findAny().isPresent();
                            if (!hasMatchingTestDatum) {
                                String testDataName = crTestData.getTestData().getName();
                                listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.invalidTestDataId", crTestData.getTestData().getId(), Util.formatCriteriaNumber(cert.getCriterion())));
                            }
                        }

                        if (crTestData.getTestData() != null && !StringUtils.isEmpty(crTestData.getTestData().getName())
                                && StringUtils.isEmpty(crTestData.getVersion())) {
                            addCriterionError(listing, cert,
                                    "listing.criteria.missingTestDataVersion",  Util.formatCriteriaNumber(cert.getCriterion()));
                        }
                    }
                }

                if (!gapEligibleAndTrue
                        && (cert.getCriterion().getNumber().equals(G1_CRITERIA_NUMBER) || cert.getCriterion().getNumber().equals(G2_CRITERIA_NUMBER))
                        && (cert.getTestDataUsed() == null || cert.getTestDataUsed().size() == 0)) {
                    listing.getErrorMessages().add("Test Data is required for certification "
                        +  Util.formatCriteriaNumber(cert.getCriterion()) + ".");
                }
            }
        }
    }
}
