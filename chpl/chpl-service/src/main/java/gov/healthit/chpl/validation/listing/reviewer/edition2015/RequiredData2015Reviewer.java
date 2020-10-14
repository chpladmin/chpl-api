package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.RequiredDataReviewer;

@Component("requiredData2015Reviewer")
public class RequiredData2015Reviewer extends RequiredDataReviewer {
    private static final String[] A_RELATED_CERTS = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)", "170.315 (d)(5)", "170.315 (d)(6)",
            "170.315 (d)(7)"
    };

    private static final String[] A_CERT_EXCEPTIONS = {
            "170.315 (a)(4)", "170.315 (a)(9)", "170.315 (a)(10)", "170.315 (a)(13)"
    };

    private static final String[] A_RELATED_CERTS_EXCEPTION = {
            "170.315 (d)(4)"
    };

    private static final String[] B_RELATED_CERTS = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)", "170.315 (d)(5)", "170.315 (d)(6)", "170.315 (d)(7)",
            "170.315 (d)(8)"
    };

    private static final String[] C_RELATED_CERTS = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)", "170.315 (d)(5)"
    };

    private static final String[] E1_RELATED_CERTS = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)", "170.315 (d)(5)", "170.315 (d)(7)", "170.315 (d)(9)"
    };

    private static final String[] E2E3_RELATED_CERTS = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)", "170.315 (d)(5)", "170.315 (d)(9)"
    };

    private static final String[] F_RELATED_CERTS = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)", "170.315 (d)(7)"
    };

    private static final String[] G7G8G9_RELATED_CERTS = {
            "170.315 (d)(1)", "170.315 (d)(9)"
    };

    private static final String[] H_RELATED_CERTS = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)"
    };

    private static final String[] UCD_RELATED_CERTS = {
            "170.315 (a)(1)", "170.315 (a)(2)", "170.315 (a)(3)", "170.315 (a)(4)", "170.315 (a)(5)", "170.315 (a)(6)",
            "170.315 (a)(7)", "170.315 (a)(8)", "170.315 (a)(9)", "170.315 (a)(14)", "170.315 (b)(2)", "170.315 (b)(3)"
    };

    private static final String[] CERTS_REQUIRING_G6 = {
            "170.315 (b)(1)", "170.315 (b)(2)", "170.315 (b)(4)", "170.315 (b)(6)", "170.315 (b)(9)", "170.315 (e)(1)",
            "170.315 (g)(9)"
    };

    private static final String B1_CRITERIA_NUMBER = "170.315 (b)(1)";
    private static final String G1_CRITERIA_NUMBER = "170.315 (g)(1)";
    private static final String G2_CRITERIA_NUMBER = "170.315 (g)(2)";
    private static final String G3_CRITERIA_NUMBER = "170.315 (g)(3)";
    private static final String G6_CRITERIA_NUMBER = "170.315 (g)(6)";
    private static final String H1_CRITERIA_NUMBER = "170.315 (h)(1)";

    private List<String> e2e3Criterion = new ArrayList<String>();
    private List<String> g7g8g9Criterion = new ArrayList<String>();
    private List<String> d2d10Criterion = new ArrayList<String>();

    private static final int MINIMUM_TEST_PARTICIPANT_COUNT = 10;

    private MacraMeasureDAO macraDao;
    private TestFunctionalityDAO testFuncDao;
    private TestProcedureDAO testProcDao;
    private TestDataDAO testDataDao;
    private CertificationCriterionDAO criteriaDao;
    private ValidationUtils validationUtils;

    @Autowired
    public RequiredData2015Reviewer(CertificationResultRules certRules, ErrorMessageUtil msgUtil, MacraMeasureDAO macraDao,
            TestFunctionalityDAO testFuncDao, TestProcedureDAO testProcDao, TestDataDAO testDataDao,
            CertificationCriterionDAO criteriaDao, ValidationUtils validationUtils, ResourcePermissions resourcePermissions) {
        super(certRules, msgUtil, resourcePermissions);
        this.macraDao = macraDao;
        this.testFuncDao = testFuncDao;
        this.testProcDao = testProcDao;
        this.testDataDao = testDataDao;
        this.criteriaDao = criteriaDao;
        this.validationUtils = validationUtils;

        e2e3Criterion.add("170.315 (e)(2)");
        e2e3Criterion.add("170.315 (e)(3)");

        g7g8g9Criterion.add("170.315 (g)(7)");
        g7g8g9Criterion.add("170.315 (g)(8)");
        g7g8g9Criterion.add("170.315 (g)(9)");

        d2d10Criterion.add("170.315 (d)(2)");
        d2d10Criterion.add("170.315 (d)(10)");
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        super.review(listing);

        if (listing.getIcs() == null || listing.getIcs().getInherits() == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingIcs"));
        }

        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);
        List<String> errors;
        List<String> warnings;

        errors = validationUtils.checkClassOfCriteriaForErrors("170.315 (a)", attestedCriteria,
                Arrays.asList(A_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        warnings = validationUtils.checkClassOfCriteriaForWarnings("170.315 (a)", attestedCriteria,
                Arrays.asList(A_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        errors = validationUtils.checkClassSubsetOfCriteriaForErrors("170.315 (a)", attestedCriteria,
                Arrays.asList(A_RELATED_CERTS_EXCEPTION), Arrays.asList(A_CERT_EXCEPTIONS));
        listing.getErrorMessages().addAll(errors);
        warnings = validationUtils.checkClassSubsetOfCriteriaForWarnings("170.315 (a)", attestedCriteria,
                Arrays.asList(A_RELATED_CERTS_EXCEPTION), Arrays.asList(A_CERT_EXCEPTIONS));
        addListingWarningsByPermission(listing, warnings);

        errors = validationUtils.checkClassOfCriteriaForErrors("170.315 (b)", attestedCriteria,
                Arrays.asList(B_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        warnings = validationUtils.checkClassOfCriteriaForWarnings("170.315 (b)", attestedCriteria,
                Arrays.asList(B_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        errors = validationUtils.checkClassOfCriteriaForErrors("170.315 (c)", attestedCriteria,
                Arrays.asList(C_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        warnings = validationUtils.checkClassOfCriteriaForWarnings("170.315 (c)", attestedCriteria,
                Arrays.asList(C_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        errors = validationUtils.checkClassOfCriteriaForErrors("170.315 (f)", attestedCriteria,
                Arrays.asList(F_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        warnings = validationUtils.checkClassOfCriteriaForWarnings("170.315 (f)", attestedCriteria,
                Arrays.asList(F_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        errors = validationUtils.checkClassOfCriteriaForErrors("170.315 (h)", attestedCriteria,
                Arrays.asList(H_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        warnings = validationUtils.checkClassOfCriteriaForWarnings("170.315 (h)", attestedCriteria,
                Arrays.asList(H_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        errors = validationUtils.checkSpecificCriteriaForErrors("170.315 (e)(1)", attestedCriteria,
                Arrays.asList(E1_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);

        // check for (e)(2) or (e)(3) certs
        List<String> e2e3ComplimentaryErrors = validationUtils.checkComplimentaryCriteriaAllRequired(e2e3Criterion,
                Arrays.asList(E2E3_RELATED_CERTS), attestedCriteria);
        listing.getErrorMessages().addAll(e2e3ComplimentaryErrors);

        // check for (g)(7) or (g)(8) or (g)(9) required complimentary certs
        List<String> g7g8g9ComplimentaryErrors = validationUtils.checkComplimentaryCriteriaAllRequired(g7g8g9Criterion,
                Arrays.asList(G7G8G9_RELATED_CERTS), attestedCriteria);
        listing.getErrorMessages().addAll(g7g8g9ComplimentaryErrors);

        // if g7, g8, or g9 is found then one of d2 or d10 is required
        g7g8g9ComplimentaryErrors = validationUtils.checkComplimentaryCriteriaAnyRequired(g7g8g9Criterion,
                d2d10Criterion, attestedCriteria);
        listing.getErrorMessages().addAll(g7g8g9ComplimentaryErrors);

        for (int i = 0; i < UCD_RELATED_CERTS.length; i++) {
            if (validationUtils.hasCert(UCD_RELATED_CERTS[i], attestedCriteria)) {
                // check for full set of UCD data
                for (CertificationResult cert : listing.getCertificationResults()) {
                    if (cert.isSuccess() != null && cert.isSuccess().equals(Boolean.TRUE)
                            && cert.getNumber().equals(UCD_RELATED_CERTS[i])) {
                        // make sure at least one UCD process has this criteria number
                        if (cert.isSed()) {
                            if (listing.getSed() == null || listing.getSed().getUcdProcesses() == null
                                    || listing.getSed().getUcdProcesses().size() == 0) {
                                addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.missingUcdProcess",
                                        Util.formatCriteriaNumber(cert.getCriterion()));
                            } else {

                                boolean foundCriteria = false;
                                for (UcdProcess ucd : listing.getSed().getUcdProcesses()) {
                                    for (CertificationCriterion criteria : ucd.getCriteria()) {
                                        if (criteria.getId().equals(cert.getCriterion().getId())) {
                                            foundCriteria = true;
                                        }
                                    }
                                }
                                if (!foundCriteria) {
                                    addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.missingUcdProcess",
                                            Util.formatCriteriaNumber(cert.getCriterion()));
                                }
                            }
                        }
                        if (cert.isSed()) {
                            if (listing.getSed() == null || listing.getSed().getTestTasks() == null
                                    || listing.getSed().getTestTasks().size() == 0) {
                                addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.missingTestTask",
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
                                    addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.missingTestTask",
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

        validateG3(listing);
        validateG3Inverse(listing);
        validateG6(listing);

        // g4 check
        boolean hasG4 = validationUtils.hasCert("170.315 (g)(4)", attestedCriteria);
        if (!hasG4) {
            listing.getErrorMessages().add("170.315 (g)(4) is required but was not found.");
        }

        // g5 check
        boolean hasG5 = validationUtils.hasCert("170.315 (g)(5)", attestedCriteria);
        if (!hasG5) {
            listing.getErrorMessages().add("170.315 (g)(5) is required but was not found.");
        }

        validateH1PlusB1(listing);

        if (listing.getQmsStandards() == null || listing.getQmsStandards().size() == 0) {
            listing.getErrorMessages().add("QMS Standards are required.");
        } else {
            for (CertifiedProductQmsStandard qms : listing.getQmsStandards()) {
                if (StringUtils.isEmpty(qms.getApplicableCriteria())) {
                    listing.getErrorMessages().add("Applicable criteria is required for each QMS Standard listed.");
                }
            }
        }

        if (listing.getAccessibilityStandards() == null || listing.getAccessibilityStandards().size() == 0) {
            listing.getErrorMessages().add("Accessibility standards are required.");
        }

        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.isSuccess() != null && cert.isSuccess()) {
                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP)
                        && cert.isGap() != null && cert.isGap()) {
                    gapEligibleAndTrue = true;
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.ATTESTATION_ANSWER)
                        && cert.getAttestationAnswer() == null) {
                    addCriterionErrorOrWarningByPermission(listing, cert,
                            "listing.criteria.missingAttestationAnswer", Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.PRIVACY_SECURITY)
                        && StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    addCriterionErrorOrWarningByPermission(listing, cert,
                            "listing.criteria.missingPrivacySecurityFramework", Util.formatCriteriaNumber(cert.getCriterion()));
                }
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.API_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getApiDocumentation())) {
                    addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.missingApiDocumentation",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.EXPORT_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getExportDocumentation())) {
                    addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.missingExportDocumentation",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.USE_CASES)
                        && StringUtils.isEmpty(cert.getUseCases())
                        && cert.getAttestationAnswer() != null && cert.getAttestationAnswer().equals(Boolean.TRUE)) {
                    addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.missingUseCases",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                } else if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.USE_CASES)
                        && !StringUtils.isEmpty(cert.getUseCases())
                        && (cert.getAttestationAnswer() == null || cert.getAttestationAnswer().equals(Boolean.FALSE))) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.useCasesWithoutAttestation",
                                    Util.formatCriteriaNumber(cert.getCriterion())));
                }

                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED)
                        && (cert.getTestToolsUsed() == null || cert.getTestToolsUsed().size() == 0)) {
                    addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.missingTestTool",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)
                        && cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
                    for (CertificationResultTestFunctionality funcMap : cert.getTestFunctionality()) {
                        if (funcMap.getTestFunctionalityId() == null) {
                            TestFunctionalityDTO foundTestFunc = testFuncDao.getByNumberAndEdition(funcMap.getName(),
                                    Long.valueOf(listing.getCertificationEdition()
                                            .get(CertifiedProductSearchDetails.EDITION_ID_KEY).toString()));
                            if (foundTestFunc == null || foundTestFunc.getId() == null) {
                                addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.invalidTestFunctionality",
                                        Util.formatCriteriaNumber(cert.getCriterion()), funcMap.getName());
                            }
                        }
                    }
                }

                // require at least one test procedure where gap does not exist
                // or is false
                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_PROCEDURE)
                        && (cert.getTestProcedures() == null || cert.getTestProcedures().size() == 0)) {
                    addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.missingTestProcedure",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                // if the criteria can and does have test procedures, make sure
                // they are each valid
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_PROCEDURE)
                        && cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0) {
                    for (CertificationResultTestProcedure crTestProc : cert.getTestProcedures()) {
                        if (crTestProc.getTestProcedure() == null) {
                            addCriterionErrorOrWarningByPermission(listing, cert,
                                    "listing.criteria.missingTestProcedureName",
                                    Util.formatCriteriaNumber(cert.getCriterion()));
                        }
                        if (crTestProc.getTestProcedure() != null && crTestProc.getTestProcedure().getId() == null) {
                            TestProcedureDTO foundTestProc = testProcDao.getByCriterionIdAndValue(cert.getCriterion().getId(),
                                    crTestProc.getTestProcedure().getName());
                            if (foundTestProc == null || foundTestProc.getId() == null) {
                                addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.badTestProcedureName",
                                        Util.formatCriteriaNumber(cert.getCriterion()), crTestProc.getTestProcedure().getName());
                            } else {
                                crTestProc.getTestProcedure().setId(foundTestProc.getId());
                            }
                        }

                        if (crTestProc.getTestProcedure() != null
                                && !StringUtils.isEmpty(crTestProc.getTestProcedure().getName())
                                && StringUtils.isEmpty(crTestProc.getTestProcedureVersion())) {
                            addCriterionErrorOrWarningByPermission(listing, cert,
                                    "listing.criteria.missingTestProcedureVersion",
                                    Util.formatCriteriaNumber(cert.getCriterion()));
                        }
                    }
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_DATA)
                        && cert.getTestDataUsed() != null && cert.getTestDataUsed().size() > 0) {
                    for (CertificationResultTestData crTestData : cert.getTestDataUsed()) {
                        if (crTestData.getTestData() == null) {
                            listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.missingTestDataName",
                                    Util.formatCriteriaNumber(cert.getCriterion()), TestDataDTO.DEFALUT_TEST_DATA));
                            TestDataDTO foundTestData = testDataDao.getByCriterionAndValue(cert.getCriterion().getId(),
                                    TestDataDTO.DEFALUT_TEST_DATA);
                            TestData foundTestDataDomain = new TestData(foundTestData.getId(), foundTestData.getName());
                            crTestData.setTestData(foundTestDataDomain);
                        } else if (crTestData.getTestData() != null && crTestData.getTestData().getId() == null) {
                            TestDataDTO foundTestData = testDataDao.getByCriterionAndValue(cert.getCriterion().getId(),
                                    crTestData.getTestData().getName());
                            if (foundTestData == null || foundTestData.getId() == null) {
                                listing.getWarningMessages()
                                .add(msgUtil.getMessage("listing.criteria.badTestDataName",
                                        crTestData.getTestData().getName(),  Util.formatCriteriaNumber(cert.getCriterion()),
                                        TestDataDTO.DEFALUT_TEST_DATA));
                                foundTestData = testDataDao.getByCriterionAndValue(cert.getCriterion().getId(),
                                        TestDataDTO.DEFALUT_TEST_DATA);
                                crTestData.getTestData().setId(foundTestData.getId());
                            } else {
                                crTestData.getTestData().setId(foundTestData.getId());
                            }
                        }

                        if (crTestData.getTestData() != null && !StringUtils.isEmpty(crTestData.getTestData().getName())
                                && StringUtils.isEmpty(crTestData.getVersion())) {
                            addCriterionErrorOrWarningByPermission(listing, cert,
                                    "listing.criteria.missingTestDataVersion",  Util.formatCriteriaNumber(cert.getCriterion()));
                        }
                    }
                }

                if (!gapEligibleAndTrue
                        && (cert.getNumber().equals(G1_CRITERIA_NUMBER) || cert.getNumber().equals(G2_CRITERIA_NUMBER))
                        && (cert.getTestDataUsed() == null || cert.getTestDataUsed().size() == 0)) {
                    listing.getErrorMessages().add("Test Data is required for certification "
                        +  Util.formatCriteriaNumber(cert.getCriterion()) + ".");
                }
            }
        }
    }

    private void validateH1PlusB1(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);

        boolean hasH1 = validationUtils.hasCert("170.315 (h)(1)", attestedCriteria);
        if (hasH1) {
            List<CertificationCriterionDTO> b1Criteria = criteriaDao.getAllByNumber(B1_CRITERIA_NUMBER);
            List<Long> b1Ids = b1Criteria.stream().map(b1Criterion -> b1Criterion.getId())
                    .collect(Collectors.toList());
            Optional<CertificationCriterion> b1AttestedCriterion =
                    attestedCriteria.stream().filter(
                            attestedCriterion -> certIdIsInCertList(attestedCriterion, b1Ids))
                    .findAny();
            if (!b1AttestedCriterion.isPresent()) {
                listing.getErrorMessages().add(
                        validationUtils.getAllCriteriaWithNumber(H1_CRITERIA_NUMBER)
                        + " was found so "
                        + validationUtils.getAllCriteriaWithNumber(B1_CRITERIA_NUMBER)
                        + " is required but was not found.");
            }
        }
    }

    private void validateG3(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);
        List<CertificationCriterion> presentAttestedUcdCriteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() == null || cert.getRemoved().equals(Boolean.FALSE))
                .filter(cert -> certNumberIsInCertList(cert, UCD_RELATED_CERTS))
                .collect(Collectors.<CertificationCriterion>toList());
        List<CertificationCriterion> removedAttestedUcdCriteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() != null && cert.getRemoved().equals(Boolean.TRUE))
                .filter(cert -> certNumberIsInCertList(cert, UCD_RELATED_CERTS))
                .collect(Collectors.<CertificationCriterion>toList());
        boolean hasG3 = validationUtils.hasCert(G3_CRITERIA_NUMBER, attestedCriteria);

        if (presentAttestedUcdCriteria != null && presentAttestedUcdCriteria.size() > 0 && !hasG3) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteriaRequired", G3_CRITERIA_NUMBER));
        }
        if (removedAttestedUcdCriteria != null && removedAttestedUcdCriteria.size() > 0
                && (presentAttestedUcdCriteria == null || presentAttestedUcdCriteria.size() == 0)
                && !hasG3) {
            addListingWarningByPermission(listing, msgUtil.getMessage("listing.criteriaRequired", G3_CRITERIA_NUMBER));
        }
    }

    private void validateG3Inverse(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);
        List<CertificationCriterion> presentAttestedUcdCriteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() == null || cert.getRemoved().equals(Boolean.FALSE))
                .filter(cert -> certNumberIsInCertList(cert, UCD_RELATED_CERTS))
                .collect(Collectors.<CertificationCriterion>toList());
        boolean hasG3 = validationUtils.hasCert(G3_CRITERIA_NUMBER, attestedCriteria);

        if ((presentAttestedUcdCriteria == null || presentAttestedUcdCriteria.size() == 0)
                && hasG3) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.g3NotAllowed"));
        }
    }

    private void validateG6(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);
        List<CertificationCriterion> presentAttestedG6Criteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() == null || cert.getRemoved().equals(Boolean.FALSE))
                .filter(cert -> certNumberIsInCertList(cert, CERTS_REQUIRING_G6))
                .collect(Collectors.<CertificationCriterion>toList());
        List<CertificationCriterion> removedAttestedG6Criteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() != null && cert.getRemoved().equals(Boolean.TRUE))
                .filter(cert -> certNumberIsInCertList(cert, CERTS_REQUIRING_G6))
                .collect(Collectors.<CertificationCriterion>toList());
        boolean hasG6 = validationUtils.hasCert(G6_CRITERIA_NUMBER, attestedCriteria);

       String g6Numbers = validationUtils.getAllCriteriaWithNumber(G6_CRITERIA_NUMBER);

        if (presentAttestedG6Criteria != null && presentAttestedG6Criteria.size() > 0 && !hasG6) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteriaRequired", g6Numbers));
        }
        if (removedAttestedG6Criteria != null && removedAttestedG6Criteria.size() > 0
                && (presentAttestedG6Criteria == null || presentAttestedG6Criteria.size() == 0)
                && !hasG6) {
            addListingWarningByPermission(listing, msgUtil.getMessage("listing.criteriaRequired", g6Numbers));
        }
    }

    private boolean certNumberIsInCertList(CertificationCriterion cert, String[] certNumberList) {
        boolean result = false;
        for (String currCertNumber : certNumberList) {
            if (currCertNumber.equals(cert.getNumber())) {
                result = true;
            }
        }
        return result;
    }

    private boolean certIdIsInCertList(CertificationCriterion cert, List<Long> certIdList) {
        boolean result = false;
        for (Long currCertId : certIdList) {
            if (currCertId.equals(cert.getId())) {
                result = true;
            }
        }
        return result;
    }
}
