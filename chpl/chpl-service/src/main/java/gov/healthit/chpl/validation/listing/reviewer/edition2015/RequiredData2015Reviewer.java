package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.RequiredDataReviewer;

@Component("requiredData2015Reviewer")
public class RequiredData2015Reviewer extends RequiredDataReviewer {
    private static final String[] A_RELATED_CERTS = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)", "170.315 (d)(4)", "170.315 (d)(5)", "170.315 (d)(6)",
            "170.315 (d)(7)"
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

    private static final String D_13_CERT = "170.315 (d)(13)";

    private static final String G1_CRITERIA_NUMBER = "170.315 (g)(1)";
    private static final String G2_CRITERIA_NUMBER = "170.315 (g)(2)";
    private List<String> e2e3Criterion = new ArrayList<String>();
    private List<String> g7g8g9Criterion = new ArrayList<String>();
    private List<String> d2d10Criterion = new ArrayList<String>();

    private static final int MINIMUM_TEST_PARTICIPANT_COUNT = 10;

    private MacraMeasureDAO macraDao;
    private TestFunctionalityDAO testFuncDao;
    private TestProcedureDAO testProcDao;
    private TestDataDAO testDataDao;

    @Autowired
    public RequiredData2015Reviewer(CertificationResultRules certRules, ErrorMessageUtil msgUtil, MacraMeasureDAO macraDao,
            TestFunctionalityDAO testFuncDao, TestProcedureDAO testProcDao, TestDataDAO testDataDao) {
        super(certRules, msgUtil);
        this.macraDao = macraDao;
        this.testFuncDao = testFuncDao;
        this.testProcDao = testProcDao;
        this.testDataDao = testDataDao;

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

        List<String> allMetCerts = new ArrayList<String>();
        for (CertificationResult certCriteria : listing.getCertificationResults()) {
            if (certCriteria.isSuccess()) {
                allMetCerts.add(certCriteria.getNumber());
            }
        }

        List<String> errors = ValidationUtils.checkClassOfCriteriaForErrors("170.315 (a)", allMetCerts,
                Arrays.asList(A_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);

        errors = ValidationUtils.checkClassOfCriteriaForErrors("170.315 (b)", allMetCerts,
                Arrays.asList(B_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);

        errors = ValidationUtils.checkClassOfCriteriaForErrors("170.315 (c)", allMetCerts,
                Arrays.asList(C_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);

        errors = ValidationUtils.checkClassOfCriteriaForErrors("170.315 (f)", allMetCerts,
                Arrays.asList(F_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);

        errors = ValidationUtils.checkClassOfCriteriaForErrors("170.315 (h)", allMetCerts,
                Arrays.asList(H_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);

        errors = ValidationUtils.checkSpecificCriteriaForErrors("170.315 (e)(1)", allMetCerts,
                Arrays.asList(E1_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);

        // check for (c)(1), (c)(2), (c)(3), (c)(4)
        boolean meetsC1Criterion = ValidationUtils.hasCert("170.315 (c)(1)", allMetCerts);
        boolean meetsC2Criterion = ValidationUtils.hasCert("170.315 (c)(2)", allMetCerts);
        boolean meetsC3Criterion = ValidationUtils.hasCert("170.315 (c)(3)", allMetCerts);
        boolean meetsC4Criterion = ValidationUtils.hasCert("170.315 (c)(4)", allMetCerts);
        boolean hasC1Cqm = false;
        boolean hasC2Cqm = false;
        boolean hasC3Cqm = false;
        boolean hasC4Cqm = false;

        for (CQMResultDetails cqm : listing.getCqmResults()) {
            List<String> certifications = new ArrayList<String>();
            for (CQMResultCertification criteria : cqm.getCriteria()) {
                certifications.add(criteria.getCertificationNumber());
            }
            hasC1Cqm = hasC1Cqm || ValidationUtils.hasCert("170.315 (c)(1)", certifications);
            hasC2Cqm = hasC2Cqm || ValidationUtils.hasCert("170.315 (c)(2)", certifications);
            hasC3Cqm = hasC3Cqm || ValidationUtils.hasCert("170.315 (c)(3)", certifications);
            hasC4Cqm = hasC4Cqm || ValidationUtils.hasCert("170.315 (c)(4)", certifications);
        }
        if (meetsC1Criterion && !hasC1Cqm) {
            listing.getErrorMessages()
            .add(msgUtil.getMessage("listing.criteria.missingCqmFor170315c", "170.315 (c)(1)"));
        } else if (!meetsC1Criterion && hasC1Cqm) {
            listing.getErrorMessages()
            .add(msgUtil.getMessage("listing.criteria.missing170315cForCqm", "170.315 (c)(1)"));
        }
        if (meetsC2Criterion && !hasC2Cqm) {
            listing.getErrorMessages()
            .add(msgUtil.getMessage("listing.criteria.missingCqmFor170315c", "170.315 (c)(2)"));
            listing.getErrorMessages()
            .add(msgUtil.getMessage("listing.criteria.missing170315cForCqm", "170.315 (c)(2)"));
        }
        if (meetsC3Criterion && !hasC3Cqm) {
            listing.getErrorMessages()
            .add(msgUtil.getMessage("listing.criteria.missingCqmFor170315c", "170.315 (c)(3)"));
        } else if (!meetsC3Criterion && hasC3Cqm) {
            listing.getErrorMessages()
            .add(msgUtil.getMessage("listing.criteria.missing170315cForCqm", "170.315 (c)(3)"));
        }
        if (meetsC4Criterion && !hasC4Cqm) {
            listing.getErrorMessages()
            .add(msgUtil.getMessage("listing.criteria.missingCqmFor170315c", "170.315 (c)(4)"));
        } else if (!meetsC4Criterion && hasC4Cqm) {
            listing.getErrorMessages()
            .add(msgUtil.getMessage("listing.criteria.missing170315cForCqm", "170.315 (c)(4)"));
        }

        // check for (e)(2) or (e)(3) certs
        List<String> e2e3ComplimentaryErrors = ValidationUtils.checkComplimentaryCriteriaAllRequired(e2e3Criterion,
                Arrays.asList(E2E3_RELATED_CERTS), allMetCerts);
        listing.getErrorMessages().addAll(e2e3ComplimentaryErrors);

        // check for (g)(7) or (g)(8) or (g)(9) required complimentary certs
        List<String> g7g8g9ComplimentaryErrors = ValidationUtils.checkComplimentaryCriteriaAllRequired(g7g8g9Criterion,
                Arrays.asList(G7G8G9_RELATED_CERTS), allMetCerts);
        listing.getErrorMessages().addAll(g7g8g9ComplimentaryErrors);

        // if g7, g8, or g9 is found then one of d2 or d10 is required
        g7g8g9ComplimentaryErrors = ValidationUtils.checkComplimentaryCriteriaAnyRequired(g7g8g9Criterion,
                d2d10Criterion, allMetCerts);
        listing.getErrorMessages().addAll(g7g8g9ComplimentaryErrors);

        // g1 macra check
        if (ValidationUtils.hasCert(G1_CRITERIA_NUMBER, allMetCerts)) {
            // must have at least one criteria with g1 macras listed
            boolean hasG1Macra = false;
            for (int i = 0; i < listing.getCertificationResults().size() && !hasG1Macra; i++) {
                CertificationResult cert = listing.getCertificationResults().get(i);
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G1_MACRA)
                        && cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
                    hasG1Macra = true;
                }
            }

            if (!hasG1Macra) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.missingG1Macras"));
            }
        }

        // g2 macra check
        if (ValidationUtils.hasCert(G2_CRITERIA_NUMBER, allMetCerts)) {
            // must have at least one criteria with g2 macras listed
            boolean hasG2Macra = false;
            for (int i = 0; i < listing.getCertificationResults().size() && !hasG2Macra; i++) {
                CertificationResult cert = listing.getCertificationResults().get(i);
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G2_MACRA)
                        && cert.getG2MacraMeasures() != null && cert.getG2MacraMeasures().size() > 0) {
                    hasG2Macra = true;
                }
            }

            if (!hasG2Macra) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.missingG2Macras"));
            }
        }

        // g3 checks
        boolean needsG3 = false;
        for (int i = 0; i < UCD_RELATED_CERTS.length; i++) {
            if (ValidationUtils.hasCert(UCD_RELATED_CERTS[i], allMetCerts)) {
                needsG3 = true;

                // check for full set of UCD data
                for (CertificationResult certCriteria : listing.getCertificationResults()) {
                    if (certCriteria.getNumber().equals(UCD_RELATED_CERTS[i])) {
                        // make sure at least one UCD process has this criteria
                        // number
                        if (certCriteria.isSed()) {
                            if (listing.getSed() == null || listing.getSed().getUcdProcesses() == null
                                    || listing.getSed().getUcdProcesses().size() == 0) {
                                listing.getErrorMessages().add("Certification " + certCriteria.getNumber()
                                + " requires at least one UCD process.");
                            } else {

                                boolean foundCriteria = false;
                                for (UcdProcess ucd : listing.getSed().getUcdProcesses()) {
                                    for (CertificationCriterion criteria : ucd.getCriteria()) {
                                        if (criteria.getNumber().equalsIgnoreCase(certCriteria.getNumber())) {
                                            foundCriteria = true;
                                        }
                                    }
                                }
                                if (!foundCriteria) {
                                    listing.getErrorMessages().add("Certification " + certCriteria.getNumber()
                                    + " requires at least one UCD process.");
                                }
                            }
                        }
                        if (certCriteria.isSed()) {
                            if (listing.getSed() == null || listing.getSed().getTestTasks() == null
                                    || listing.getSed().getTestTasks().size() == 0) {
                                listing.getErrorMessages().add("Certification " + certCriteria.getNumber()
                                + " requires at least one test task.");
                            } else {

                                boolean foundCriteria = false;
                                for (TestTask tt : listing.getSed().getTestTasks()) {
                                    for (CertificationCriterion criteria : tt.getCriteria()) {
                                        if (criteria.getNumber().equalsIgnoreCase(certCriteria.getNumber())) {
                                            foundCriteria = true;
                                        }
                                    }
                                }
                                if (!foundCriteria) {
                                    listing.getErrorMessages().add("Certification " + certCriteria.getNumber()
                                    + " requires at least one test task.");
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
        if (needsG3) {
            boolean hasG3 = ValidationUtils.hasCert("170.315 (g)(3)", allMetCerts);
            if (!hasG3) {
                listing.getErrorMessages().add("170.315 (g)(3) is required but was not found.");
            }
        }

        // g3 inverse check
        boolean hasG3ComplimentaryCerts = false;
        for (int i = 0; i < UCD_RELATED_CERTS.length; i++) {
            if (ValidationUtils.hasCert(UCD_RELATED_CERTS[i], allMetCerts)) {
                hasG3ComplimentaryCerts = true;
            }
        }
        if (!hasG3ComplimentaryCerts) {
            // make sure it doesn't have g3
            boolean hasG3 = ValidationUtils.hasCert("170.315 (g)(3)", allMetCerts);
            if (hasG3) {
                listing.getErrorMessages().add("170.315 (g)(3) is not allowed but was found.");
            }
        }

        // g4 check
        boolean hasG4 = ValidationUtils.hasCert("170.315 (g)(4)", allMetCerts);
        if (!hasG4) {
            listing.getErrorMessages().add("170.315 (g)(4) is required but was not found.");
        }

        // g5 check
        boolean hasG5 = ValidationUtils.hasCert("170.315 (g)(5)", allMetCerts);
        if (!hasG5) {
            listing.getErrorMessages().add("170.315 (g)(5) is required but was not found.");
        }

        // g6 checks
        boolean needsG6 = false;
        for (int i = 0; i < CERTS_REQUIRING_G6.length && !needsG6; i++) {
            if (ValidationUtils.hasCert(CERTS_REQUIRING_G6[i], allMetCerts)) {
                needsG6 = true;
            }
        }
        if (needsG6) {
            boolean hasG6 = ValidationUtils.hasCert("170.315 (g)(6)", allMetCerts);
            if (!hasG6) {
                listing.getErrorMessages().add("170.315 (g)(6) is required but was not found.");
            }
        }

        // TODO: detailed G6 check

        // h1 plus b1
        boolean hasH1 = ValidationUtils.hasCert("170.315 (h)(1)", allMetCerts);
        if (hasH1) {
            boolean hasB1 = ValidationUtils.hasCert("170.315 (b)(1)", allMetCerts);
            if (!hasB1) {
                listing.getErrorMessages()
                .add("170.315 (h)(1) was found so 170.315 (b)(1) is required but was not found.");
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
                    listing.getErrorMessages()
                    .add("Attestation Answer is required for certification " + cert.getCriterion().getNumber() + ".");
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.PRIVACY_SECURITY)
                        && StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    listing.getErrorMessages().add(
                            "Privacy and Security Framework is required for certification " + cert.getNumber() + ".");
                }
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.API_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getApiDocumentation())) {
                    listing.getErrorMessages()
                    .add("API Documentation is required for certification " + cert.getNumber() + ".");
                }
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.EXPORT_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getExportDocumentation())) {
                    listing.getErrorMessages()
                    .add("Export Documentation is required for certification " + cert.getNumber() + ".");
                }
                if (cert.getNumber().equals(D_13_CERT)
                        && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.DOCUMENTATION_URL)
                        && StringUtils.isEmpty(cert.getDocumentationUrl())
                        && cert.getAttestationAnswer() != null && cert.getAttestationAnswer().equals(Boolean.FALSE)) {
                    listing.getErrorMessages()
                    .add("Documentation Url is required for certification " + cert.getNumber()
                    + " when Attestation Answer is \"No\".");
                }
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.USE_CASES)
                        && StringUtils.isEmpty(cert.getUseCases())
                        && cert.getAttestationAnswer() != null && cert.getAttestationAnswer().equals(Boolean.TRUE)) {
                    listing.getErrorMessages()
                    .add("Use Cases is required for certification " + cert.getNumber() + " when Attestation Answer is \"Yes\".");
                }

                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED)
                        && (cert.getTestToolsUsed() == null || cert.getTestToolsUsed().size() == 0)) {
                    listing.getErrorMessages()
                    .add(msgUtil.getMessage("listing.criteria.missingTestTool", cert.getNumber()));
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)
                        && cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
                    for (CertificationResultTestFunctionality funcMap : cert.getTestFunctionality()) {
                        if (funcMap.getTestFunctionalityId() == null) {
                            TestFunctionalityDTO foundTestFunc = testFuncDao.getByNumberAndEdition(funcMap.getName(),
                                    Long.valueOf(listing.getCertificationEdition()
                                            .get(CertifiedProductSearchDetails.EDITION_ID_KEY).toString()));
                            if (foundTestFunc == null || foundTestFunc.getId() == null) {
                                listing.getErrorMessages().add("Certification " + cert.getNumber()
                                + " contains invalid test functionality: '" + funcMap.getName() + "'.");
                            }
                        }
                    }
                }

                // require at least one test procedure where gap does not exist
                // or is false
                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_PROCEDURE)
                        && (cert.getTestProcedures() == null || cert.getTestProcedures().size() == 0)) {
                    listing.getErrorMessages()
                    .add(msgUtil.getMessage("listing.criteria.missingTestProcedure", cert.getNumber()));
                }

                // if the criteria can and does have test procedures, make sure
                // they are each valid
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_PROCEDURE)
                        && cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0) {
                    for (CertificationResultTestProcedure crTestProc : cert.getTestProcedures()) {
                        if (crTestProc.getTestProcedure() == null) {
                            listing.getErrorMessages().add(
                                    msgUtil.getMessage("listing.criteria.missingTestProcedureName", cert.getNumber()));
                        }
                        if (crTestProc.getTestProcedure() != null && crTestProc.getTestProcedure().getId() == null) {
                            TestProcedureDTO foundTestProc = testProcDao.getByCriteriaNumberAndValue(cert.getNumber(),
                                    crTestProc.getTestProcedure().getName());
                            if (foundTestProc == null || foundTestProc.getId() == null) {
                                listing.getErrorMessages()
                                .add(msgUtil.getMessage("listing.criteria.badTestProcedureName",
                                        cert.getNumber(), crTestProc.getTestProcedure().getName()));
                            } else {
                                crTestProc.getTestProcedure().setId(foundTestProc.getId());
                            }
                        }

                        if (crTestProc.getTestProcedure() != null
                                && !StringUtils.isEmpty(crTestProc.getTestProcedure().getName())
                                && StringUtils.isEmpty(crTestProc.getTestProcedureVersion())) {
                            listing.getErrorMessages().add(msgUtil
                                    .getMessage("listing.criteria.missingTestProcedureVersion", cert.getNumber()));
                        }
                    }
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_DATA)
                        && cert.getTestDataUsed() != null && cert.getTestDataUsed().size() > 0) {
                    for (CertificationResultTestData crTestData : cert.getTestDataUsed()) {
                        if (crTestData.getTestData() == null) {
                            listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.missingTestDataName",
                                    cert.getNumber(), TestDataDTO.DEFALUT_TEST_DATA));
                            TestDataDTO foundTestData = testDataDao.getByCriteriaNumberAndValue(cert.getNumber(),
                                    TestDataDTO.DEFALUT_TEST_DATA);
                            TestData foundTestDataDomain = new TestData(foundTestData.getId(), foundTestData.getName());
                            crTestData.setTestData(foundTestDataDomain);
                        } else if (crTestData.getTestData() != null && crTestData.getTestData().getId() == null) {
                            TestDataDTO foundTestData = testDataDao.getByCriteriaNumberAndValue(cert.getNumber(),
                                    crTestData.getTestData().getName());
                            if (foundTestData == null || foundTestData.getId() == null) {
                                listing.getWarningMessages()
                                .add(msgUtil.getMessage("listing.criteria.badTestDataName",
                                        crTestData.getTestData().getName(), cert.getNumber(),
                                        TestDataDTO.DEFALUT_TEST_DATA));
                                foundTestData = testDataDao.getByCriteriaNumberAndValue(cert.getNumber(),
                                        TestDataDTO.DEFALUT_TEST_DATA);
                                crTestData.getTestData().setId(foundTestData.getId());
                            } else {
                                crTestData.getTestData().setId(foundTestData.getId());
                            }
                        }

                        if (crTestData.getTestData() != null && !StringUtils.isEmpty(crTestData.getTestData().getName())
                                && StringUtils.isEmpty(crTestData.getVersion())) {
                            listing.getErrorMessages().add(
                                    msgUtil.getMessage("listing.criteria.missingTestDataVersion", cert.getNumber()));
                        }
                    }
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G1_MACRA)
                        && cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
                    for (int i = 0; i < cert.getG1MacraMeasures().size(); i++) {
                        MacraMeasure measure = cert.getG1MacraMeasures().get(i);
                        if (measure == null || measure.getId() == null) {
                            listing.getErrorMessages()
                            .add("Certification " + cert.getNumber() + " contains invalid G1 Macra Measure.");
                        } else {
                            // confirm the measure id is valid
                            MacraMeasureDTO foundMeasure = macraDao.getById(measure.getId());
                            if (foundMeasure == null || foundMeasure.getId() == null) {
                                listing.getErrorMessages()
                                .add("Certification " + cert.getNumber()
                                + " contains invalid G1 Macra Measure. No measure found with ID '"
                                + measure.getId() + "'.");
                            } else if (!foundMeasure.getCriteria().getNumber().equals(cert.getNumber())) {
                                listing.getErrorMessages().add("Certification " + cert.getNumber()
                                + " contains an invalid G1 Macra Measure. Measure with ID '" + measure.getId()
                                + "' is the measure '" + foundMeasure.getName() + "' and is for criteria '"
                                + foundMeasure.getCriteria().getNumber() + "'.");
                            } else {
                                cert.getG1MacraMeasures().set(i, new MacraMeasure(foundMeasure));
                            }
                        }
                    }
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G2_MACRA)
                        && cert.getG2MacraMeasures() != null && cert.getG2MacraMeasures().size() > 0) {
                    for (int i = 0; i < cert.getG2MacraMeasures().size(); i++) {
                        MacraMeasure measure = cert.getG2MacraMeasures().get(i);
                        if (measure == null || measure.getId() == null) {
                            listing.getErrorMessages()
                            .add("Certification " + cert.getNumber() + " contains invalid G2 Macra Measure.");
                        } else {
                            // confirm the measure id is valid
                            MacraMeasureDTO foundMeasure = macraDao.getById(measure.getId());
                            if (foundMeasure == null || foundMeasure.getId() == null) {
                                listing.getErrorMessages()
                                .add("Certification " + cert.getNumber()
                                + " contains invalid G2 Macra Measure. No measure found with ID '"
                                + measure.getId() + "'.");
                            } else if (!foundMeasure.getCriteria().getNumber().equals(cert.getNumber())) {
                                listing.getErrorMessages().add("Certification " + cert.getNumber()
                                + " contains an invalid G2 Macra Measure. Measure with ID '" + measure.getId()
                                + "' is the measure '" + foundMeasure.getName() + "' and is for criteria '"
                                + foundMeasure.getCriteria().getNumber() + "'.");
                            } else {
                                cert.getG2MacraMeasures().set(i, new MacraMeasure(foundMeasure));
                            }
                        }
                    }
                }

                if (!gapEligibleAndTrue
                        && (cert.getNumber().equals(G1_CRITERIA_NUMBER) || cert.getNumber().equals(G2_CRITERIA_NUMBER))
                        && (cert.getTestDataUsed() == null || cert.getTestDataUsed().size() == 0)) {
                    listing.getErrorMessages().add("Test Data is required for certification " + cert.getNumber() + ".");
                }
            }
        }
    }
}
