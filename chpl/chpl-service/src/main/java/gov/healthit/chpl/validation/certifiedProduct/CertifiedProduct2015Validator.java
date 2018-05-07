package gov.healthit.chpl.validation.certifiedProduct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.PendingCqmCertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;
import gov.healthit.chpl.dto.PendingTestTaskDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.util.CertificationResultRules;

@Component("certifiedProduct2015Validator")
public class CertifiedProduct2015Validator extends CertifiedProductValidatorImpl {
    private static final String[] aComplimentaryCerts = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)", "170.315 (d)(4)", "170.315 (d)(5)", "170.315 (d)(6)",
            "170.315 (d)(7)"
    };

    private static final String[] bComplimentaryCerts = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)", "170.315 (d)(5)", "170.315 (d)(6)", "170.315 (d)(7)",
            "170.315 (d)(8)"
    };

    private static final String[] cComplimentaryCerts = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)", "170.315 (d)(5)"
    };

    private static final String[] e1ComplimentaryCerts = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)", "170.315 (d)(5)", "170.315 (d)(7)", "170.315 (d)(9)"
    };

    private static final String[] e2Ore3ComplimentaryCerts = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)", "170.315 (d)(5)", "170.315 (d)(9)"
    };

    private static final String[] fComplimentaryCerts = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)", "170.315 (d)(7)"
    };

    private static final String[] g7Org8Org9ComplimentaryCerts = {
            "170.315 (d)(1)", "170.315 (d)(9)"
    };

    private static final String[] hComplimentaryCerts = {
            "170.315 (d)(1)", "170.315 (d)(2)", "170.315 (d)(3)"
    };

    private static final String[] ucdRequiredCerts = {
            "170.315 (a)(1)", "170.315 (a)(2)", "170.315 (a)(3)", "170.315 (a)(4)", "170.315 (a)(5)", "170.315 (a)(6)",
            "170.315 (a)(7)", "170.315 (a)(8)", "170.315 (a)(9)", "170.315 (a)(14)", "170.315 (b)(2)", "170.315 (b)(3)"
    };

    private static final String[] g6CertsToCheck = {
            "170.315 (b)(1)", "170.315 (b)(2)", "170.315 (b)(4)", "170.315 (b)(6)", "170.315 (b)(9)", "170.315 (e)(1)",
            "170.315 (g)(9)"
    };

    @Autowired
    TestToolDAO testToolDao;
    @Autowired
    TestProcedureDAO testProcDao;
    @Autowired
    TestDataDAO testDataDao;
    @Autowired
    TestFunctionalityDAO testFuncDao;
    @Autowired
    AccessibilityStandardDAO asDao;
    @Autowired
    MacraMeasureDAO macraDao;
    @Autowired
    CertifiedProductDetailsManager cpdManager;
    @Autowired
    CertifiedProductSearchDAO searchDao;

    @Override
    public void validate(PendingCertifiedProductDTO product) {
        super.validate(product);

        List<String> allMetCerts = new ArrayList<String>();
        for (PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
            if (certCriteria.getMeetsCriteria()) {
                allMetCerts.add(certCriteria.getNumber());
            }
        }

        List<String> errors = checkClassOfCriteriaForErrors("170.315 (a)", allMetCerts,
                Arrays.asList(aComplimentaryCerts));
        product.getErrorMessages().addAll(errors);

        errors = checkClassOfCriteriaForErrors("170.315 (b)", allMetCerts, Arrays.asList(bComplimentaryCerts));
        product.getErrorMessages().addAll(errors);

        errors = checkClassOfCriteriaForErrors("170.315 (c)", allMetCerts, Arrays.asList(cComplimentaryCerts));
        product.getErrorMessages().addAll(errors);

        errors = checkClassOfCriteriaForErrors("170.315 (f)", allMetCerts, Arrays.asList(fComplimentaryCerts));
        product.getErrorMessages().addAll(errors);

        errors = checkClassOfCriteriaForErrors("170.315 (h)", allMetCerts, Arrays.asList(hComplimentaryCerts));
        product.getErrorMessages().addAll(errors);

        errors = checkSpecificCriteriaForErrors("170.315 (e)(1)", allMetCerts, Arrays.asList(e1ComplimentaryCerts));
        product.getErrorMessages().addAll(errors);

        // check for (c)(1), (c)(2), (c)(3), (c)(4)
        boolean meetsC1Criterion = hasCert("170.315 (c)(1)", allMetCerts);
        boolean meetsC2Criterion = hasCert("170.315 (c)(2)", allMetCerts);
        boolean meetsC3Criterion = hasCert("170.315 (c)(3)", allMetCerts);
        boolean meetsC4Criterion = hasCert("170.315 (c)(4)", allMetCerts);
        boolean hasC1Cqm = false;
        boolean hasC2Cqm = false;
        boolean hasC3Cqm = false;
        boolean hasC4Cqm = false;
        for (PendingCqmCriterionDTO cqm : product.getCqmCriterion()) {
            List<String> cqmCerts = new ArrayList<String>();
            for (PendingCqmCertificationCriterionDTO criteria : cqm.getCertifications()) {
                cqmCerts.add(criteria.getCertificationCriteriaNumber());
            }
            hasC1Cqm = hasC1Cqm || hasCert("170.315 (c)(1)", cqmCerts);
            hasC2Cqm = hasC2Cqm || hasCert("170.315 (c)(2)", cqmCerts);
            hasC3Cqm = hasC3Cqm || hasCert("170.315 (c)(3)", cqmCerts);
            hasC4Cqm = hasC4Cqm || hasCert("170.315 (c)(4)", cqmCerts);
        }
        if (meetsC1Criterion && !hasC1Cqm) {
            product.getErrorMessages().add(
                    "Certification criterion 170.315 (c)(1) was found but no matching Clinical Quality Measurement was found.");
        } else if (!meetsC1Criterion && hasC1Cqm) {
            product.getErrorMessages().add(
                    "A Clinical Quality Measurement was found under Certification criterion 170.315 (c)(1), but the product does not attest to that criterion.");
        }
        if (meetsC2Criterion && !hasC2Cqm) {
            product.getErrorMessages().add(
                    "Certification criterion 170.315 (c)(2) was found but no matching Clinical Quality Measurement was found.");
        } else if (!meetsC2Criterion && hasC2Cqm) {
            product.getErrorMessages().add(
                    "A Clinical Quality Measurement was found under Certification criterion 170.315 (c)(2), but the product does not attest to that criterion.");
        }
        if (meetsC3Criterion && !hasC3Cqm) {
            product.getErrorMessages().add(
                    "Certification criterion 170.315 (c)(3) was found but no matching Clinical Quality Measurement was found.");
        } else if (!meetsC3Criterion && hasC3Cqm) {
            product.getErrorMessages().add(
                    "A Clinical Quality Measurement was found under Certification criterion 170.315 (c)(3), but the product does not attest to that criterion.");
        }
        if (meetsC4Criterion && !hasC4Cqm) {
            product.getErrorMessages().add(
                    "Certification criterion 170.315 (c)(4) was found but no matching Clinical Quality Measurement was found.");
        } else if (!meetsC4Criterion && hasC4Cqm) {
            product.getErrorMessages().add(
                    "A Clinical Quality Measurement was found under Certification criterion 170.315 (c)(4), but the product does not attest to that criterion.");
        }

        // check for (e)(2) or (e)(3) required complimentary certs
        List<String> e2e3Criterion = new ArrayList<String>();
        e2e3Criterion.add("170.315 (e)(2)");
        e2e3Criterion.add("170.315 (e)(3)");
        List<String> e2e3ComplimentaryErrors = 
                checkComplimentaryCriteriaAllRequired(e2e3Criterion, Arrays.asList(e2Ore3ComplimentaryCerts), allMetCerts);
        product.getErrorMessages().addAll(e2e3ComplimentaryErrors);

        // check for (g)(7) or (g)(8) or (g)(9) required complimentary certs
        List<String> g7g8g9Criterion = new ArrayList<String>();
        g7g8g9Criterion.add("170.315 (g)(7)");
        g7g8g9Criterion.add("170.315 (g)(8)");
        g7g8g9Criterion.add("170.315 (g)(9)");
        List<String> g7g8g9ComplimentaryErrors = 
                checkComplimentaryCriteriaAllRequired(g7g8g9Criterion, Arrays.asList(g7Org8Org9ComplimentaryCerts), allMetCerts);
        product.getErrorMessages().addAll(g7g8g9ComplimentaryErrors);
        
        //if g7, g8, or g9 is found then one of d2 or d10 is required
        List<String> d2d10Criterion = new ArrayList<String>();
        d2d10Criterion.add("170.315 (d)(2)");
        d2d10Criterion.add("170.315 (d)(10)");
        g7g8g9ComplimentaryErrors = 
                checkComplimentaryCriteriaAnyRequired(g7g8g9Criterion, d2d10Criterion, allMetCerts);
        product.getErrorMessages().addAll(g7g8g9ComplimentaryErrors);

        // g3 checks
        boolean needsG3 = false;
        for (int i = 0; i < ucdRequiredCerts.length; i++) {
            if (hasCert(ucdRequiredCerts[i], allMetCerts)) {
                needsG3 = true;

                // check for full set of UCD data
                for (PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
                    if (certCriteria.getNumber().equals(ucdRequiredCerts[i])) {
                        if (certCriteria.getUcdProcesses() == null || certCriteria.getUcdProcesses().size() == 0) {
                            product.getErrorMessages().add("Certification " + certCriteria.getNumber()
                                    + " requires at least one UCD process.");
                        }
                        if (certCriteria.getTestTasks() == null || certCriteria.getTestTasks().size() == 0) {
                            product.getErrorMessages().add(
                                    "Certification " + certCriteria.getNumber() + " requires at least one test task.");
                        }

                        if (certCriteria.getTestTasks() != null) {
                            for (PendingCertificationResultTestTaskDTO certResultTask : certCriteria.getTestTasks()) {
                                PendingTestTaskDTO task = certResultTask.getPendingTestTask();
                                if (certResultTask.getTaskParticipants() == null
                                        || certResultTask.getTaskParticipants().size() < 10) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskParticipantsSize"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (StringUtils.isEmpty(task.getDescription())) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestDescription"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (task.getTaskSuccessAverage() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskSuccessAverage"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (task.getTaskSuccessStddev() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskSuccessStddev"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (task.getTaskPathDeviationObserved() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskPathDeviationObserved"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (task.getTaskPathDeviationOptimal() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskPathDeviationOptimal"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (task.getTaskTimeAvg() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskTimeAvg"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (task.getTaskTimeStddev() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskTimeStddev"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (task.getTaskTimeDeviationObservedAvg() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskTimeDeviationObservedAvg"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (task.getTaskTimeDeviationOptimalAvg() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskTimeDeviationOptimalAvg"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (task.getTaskErrors() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskErrors"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (task.getTaskErrorsStddev() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskErrorsStddev"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (StringUtils.isEmpty(task.getTaskRatingScale())) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskRatingScale"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (task.getTaskRating() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskRating"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                if (task.getTaskRatingStddev() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(
                                                    messageSource.getMessage(
                                                            new DefaultMessageSourceResolvable(
                                                                    "listing.criteria.badTestTaskRatingStddev"),
                                                            LocaleContextHolder.getLocale()),
                                                    task.getUniqueId(), certCriteria.getNumber()));
                                }
                                for (PendingCertificationResultTestTaskParticipantDTO part : certResultTask
                                        .getTaskParticipants()) {
                                    if (part.getTestParticipant().getEducationTypeId() == null) {
                                        product.getErrorMessages()
                                                .add(String.format(
                                                        messageSource.getMessage(
                                                                new DefaultMessageSourceResolvable(
                                                                        "listing.criteria.badParticipantEducationLevel"),
                                                                LocaleContextHolder.getLocale()),
                                                        (part.getTestParticipant().getUserEnteredEducationType() == null
                                                                ? "'unknown'"
                                                                : part.getTestParticipant()
                                                                        .getUserEnteredEducationType()),
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (part.getTestParticipant().getAgeRangeId() == null) {
                                        product.getErrorMessages()
                                                .add(String.format(
                                                        messageSource.getMessage(
                                                                new DefaultMessageSourceResolvable(
                                                                        "listing.criteria.badParticipantAgeRange"),
                                                                LocaleContextHolder.getLocale()),
                                                        (part.getTestParticipant().getUserEnteredAgeRange() == null
                                                                ? "'unknown'"
                                                                : part.getTestParticipant().getUserEnteredAgeRange()),
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (StringUtils.isEmpty(part.getTestParticipant().getGender())) {
                                        product.getErrorMessages()
                                                .add(String.format(
                                                        messageSource.getMessage(
                                                                new DefaultMessageSourceResolvable(
                                                                        "listing.criteria.badParticipantGender"),
                                                                LocaleContextHolder.getLocale()),
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (StringUtils.isEmpty(part.getTestParticipant().getOccupation())) {
                                        product.getErrorMessages()
                                                .add(String.format(
                                                        messageSource.getMessage(
                                                                new DefaultMessageSourceResolvable(
                                                                        "listing.criteria.badParticipantOccupation"),
                                                                LocaleContextHolder.getLocale()),
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (StringUtils.isEmpty(part.getTestParticipant().getAssistiveTechnologyNeeds())) {
                                        product.getErrorMessages()
                                                .add(String.format(
                                                        messageSource.getMessage(
                                                                new DefaultMessageSourceResolvable(
                                                                        "listing.criteria.badParticipantAssistiveTechnologyNeeds"),
                                                                LocaleContextHolder.getLocale()),
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (part.getTestParticipant().getProfessionalExperienceMonths() == null) {
                                        product.getErrorMessages()
                                                .add(String.format(
                                                        messageSource.getMessage(
                                                                new DefaultMessageSourceResolvable(
                                                                        "listing.criteria.badParticipantProfessionalExperienceMonths"),
                                                                LocaleContextHolder.getLocale()),
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (part.getTestParticipant().getProductExperienceMonths() == null) {
                                        product.getErrorMessages()
                                                .add(String.format(
                                                        messageSource.getMessage(
                                                                new DefaultMessageSourceResolvable(
                                                                        "listing.criteria.badParticipantProductExperienceMonths"),
                                                                LocaleContextHolder.getLocale()),
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (part.getTestParticipant().getComputerExperienceMonths() == null) {
                                        product.getErrorMessages()
                                                .add(String.format(
                                                        messageSource.getMessage(
                                                                new DefaultMessageSourceResolvable(
                                                                        "listing.criteria.badParticipantComputerExperienceMonths"),
                                                                LocaleContextHolder.getLocale()),
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (needsG3) {
            boolean hasG3 = hasCert("170.315 (g)(3)", allMetCerts);
            if (!hasG3) {
                product.getErrorMessages().add("170.315 (g)(3) is required but was not found.");
            }
        }

        // g3 inverse check
        boolean hasG3ComplimentaryCerts = false;
        for (int i = 0; i < ucdRequiredCerts.length; i++) {
            if (hasCert(ucdRequiredCerts[i], allMetCerts)) {
                hasG3ComplimentaryCerts = true;
            }
        }
        if (!hasG3ComplimentaryCerts) {
            // make sure it doesn't have g3
            boolean hasG3 = hasCert("170.315 (g)(3)", allMetCerts);
            if (hasG3) {
                product.getErrorMessages().add("170.315 (g)(3) is not allowed but was found.");
            }
        }

        // g4 check
        boolean hasG4 = hasCert("170.315 (g)(4)", allMetCerts);
        if (!hasG4) {
            product.getErrorMessages().add("170.315 (g)(4) is required but was not found.");
        }

        // g5 check
        boolean hasG5 = hasCert("170.315 (g)(5)", allMetCerts);
        if (!hasG5) {
            product.getErrorMessages().add("170.315 (g)(5) is required but was not found.");
        }

        // g6 checks
        boolean needsG6 = false;
        for (int i = 0; i < g6CertsToCheck.length && !needsG6; i++) {
            if (hasCert(g6CertsToCheck[i], allMetCerts)) {
                needsG6 = true;
            }
        }
        if (needsG6) {
            boolean hasG6 = hasCert("170.315 (g)(6)", allMetCerts);
            if (!hasG6) {
                product.getErrorMessages().add("170.315 (g)(6) is required but was not found.");
            }
        }

        // reverse g6 check
        boolean hasG6 = hasCert("170.315 (g)(6)", allMetCerts);
        if (hasG6) {
            boolean hasG6ComplimentaryCerts = false;
            for (int i = 0; i < g6CertsToCheck.length; i++) {
                if (hasCert(g6CertsToCheck[i], allMetCerts)) {
                    hasG6ComplimentaryCerts = true;
                }
            }

            if (!hasG6ComplimentaryCerts) {
                product.getErrorMessages().add("170.315 (g)(6) was found but a related required cert was not found.");
            }
        }

        // TODO: detailed G6 check; waiting on rule from ONC

        // h1 plus b1
        boolean hasH1 = hasCert("170.315 (h)(1)", allMetCerts);
        if (hasH1) {
            boolean hasB1 = hasCert("170.315 (b)(1)", allMetCerts);
            if (!hasB1) {
                product.getErrorMessages()
                        .add("170.315 (h)(1) was found so 170.315 (b)(1) is required but was not found.");
            }
        }
    }

    protected void validateDemographics(PendingCertifiedProductDTO product) {
        super.validateDemographics(product);

        //make sure the ICS boolean and presence of parents match
        if (product.getIcs() != null) {
            if(product.getIcs().booleanValue() == true &&
                    (product.getIcsParents() == null || product.getIcsParents().size() == 0)) {
                product.getErrorMessages().add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable("listing.icsTrueAndNoParentsFound"),
                                                    LocaleContextHolder.getLocale())));
            } else if(product.getIcs().booleanValue() == false && product.getIcsParents() != null &&
                    product.getIcsParents().size() > 0) {
                product.getErrorMessages().add(String.format(messageSource.getMessage(
                        new DefaultMessageSourceResolvable("listing.icsFalseAndParentsFound"),
                        LocaleContextHolder.getLocale())));
            }
        } else {
            product.getErrorMessages().add(String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("listing.missingIcs"),
                    LocaleContextHolder.getLocale())));
        }

        //if parents exist make sure they are valid
        if(product.getIcsParents() != null && product.getIcsParents().size() > 0) {
            // parents are non-empty - check inheritance rules
            // certification edition must be the same as this listings
            List<Long> parentIds = new ArrayList<Long>();
            for (CertifiedProductDetailsDTO potentialParent : product.getIcsParents()) {
                //the id might be null if the user changed it in the UI 
                //even though it's a valid CHPL product number
                if(potentialParent.getId() == null) {
                    try {
                        CertifiedProduct found = searchDao.getByChplProductNumber(potentialParent.getChplProductNumber());
                        if (found != null) {
                            potentialParent.setId(found.getId());
                        }
                    } catch(Exception ignore) { }
                }
                
                //if the ID is still null after trying to look it up, that's a problem
                if(potentialParent.getId() == null) {
                    product.getErrorMessages().add(String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable("listing.icsUniqueIdNotFound"),
                            LocaleContextHolder.getLocale()), potentialParent.getChplProductNumber()));
                } else if(potentialParent.getId().toString().equals(product.getId().toString())) {
                    product.getErrorMessages().add(String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable("listing.icsSelfInheritance"),
                            LocaleContextHolder.getLocale())));
                } else {
                    parentIds.add(potentialParent.getId());
                }
            }
            
            if(parentIds != null && parentIds.size() > 0) {
                List<CertificationEditionDTO> parentEditions = certEditionDao.getEditions(parentIds);
                for (CertificationEditionDTO parentEdition : parentEditions) {
                    if (!product.getCertificationEdition().equals(parentEdition.getYear())) {
                        product.getErrorMessages().add(String.format(messageSource.getMessage(
                                new DefaultMessageSourceResolvable("listing.icsEditionMismatch"),
                                LocaleContextHolder.getLocale()), parentEdition.getYear()));
                    }
                }
                
                // this listing's ICS code must be greater than the max of
                // parent ICS codes
                Integer largestIcs = inheritanceDao.getLargestIcs(parentIds);
                if (largestIcs != null && icsCodeInteger.intValue() != (largestIcs.intValue() + 1)) {
                    product.getErrorMessages().add(String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable("listing.icsNotLargestCode"),
                            LocaleContextHolder.getLocale()), icsCodeInteger, largestIcs));
                }
            }
        }
        
        if (product.getQmsStandards() == null || product.getQmsStandards().size() == 0) {
            product.getErrorMessages().add("QMS Standards are required.");
        } else {
            for (PendingCertifiedProductQmsStandardDTO qms : product.getQmsStandards()) {
                if (StringUtils.isBlank(qms.getApplicableCriteria())) {
                    product.getErrorMessages().add("Applicable criteria is required for each QMS Standard listed.");
                }
            }
        }

        if (product.getAccessibilityStandards() == null || product.getAccessibilityStandards().size() == 0) {
            product.getErrorMessages().add("Accessibility standards are required.");
        } // accessibility standards do not have to match the set list of
          // standards.

        for (PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria() == Boolean.TRUE) {
                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP)
                        && cert.getGap() == Boolean.TRUE) {
                    gapEligibleAndTrue = true;
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.PRIVACY_SECURITY)
                        && StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    product.getErrorMessages().add(
                            "Privacy and Security Framework is required for certification " + cert.getNumber() + ".");
                }
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.API_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getApiDocumentation())) {
                    product.getErrorMessages()
                            .add("API Documentation is required for certification " + cert.getNumber() + ".");
                }
                // jennifer asked to not make functionality tested be a required
                // field
                // if(certRules.hasCertOption(cert.getNumber(),
                // CertificationResultRules.FUNCTIONALITY_TESTED) &&
                // (cert.getTestFunctionality() == null ||
                // cert.getTestFunctionality().size() == 0)) {
                // product.getErrorMessages().add("Functionality Tested is
                // required for certification " + cert.getNumber() + ".");
                // }
                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED)
                        && (cert.getTestTools() == null || cert.getTestTools().size() == 0)) {
                    product.getErrorMessages().add(String.format(messageSource.getMessage(
                        new DefaultMessageSourceResolvable("listing.criteria.missingTestTool"),
                        LocaleContextHolder.getLocale()),
                        cert.getNumber()));
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED)
                        && cert.getTestTools() != null && cert.getTestTools().size() > 0) {
                    for (PendingCertificationResultTestToolDTO pendingTestTool : cert.getTestTools()) {
                        //no new test tools are allowed to be added 
                        //so make sure a test tool by this name exists
                        if (pendingTestTool.getTestToolId() == null) {
                            TestToolDTO foundTestTool = testToolDao.getByName(pendingTestTool.getName());
                            if (foundTestTool == null || foundTestTool.getId() == null) {
                                product.getErrorMessages().add(String.format(messageSource.getMessage(
                                        new DefaultMessageSourceResolvable(
                                                "listing.criteria.invalidTestToolName"),
                                        LocaleContextHolder.getLocale()), cert.getNumber(), pendingTestTool.getName()));
                            } else {
                                //require test tool version
                                if(StringUtils.isEmpty(pendingTestTool.getVersion())) {
                                    product.getErrorMessages().add(String.format(messageSource.getMessage(
                                            new DefaultMessageSourceResolvable(
                                                    "listing.criteria.missingTestToolVersion"),
                                            LocaleContextHolder.getLocale()), pendingTestTool.getName(), cert.getNumber()));
                                }
                                
                                // Allow retired test tool only if listing ICS = true
                                if (foundTestTool.isRetired() && super.icsCodeInteger.intValue() == 0) {
                                    if (super.hasIcsConflict) {
                                        //the ics code is 0 but we can't be sure that's what the user meant
                                        //because the ICS value in the file is 1 (hence the conflict), 
                                        //so issue a warning since the listing may or may not truly have ICS
                                        product.getWarningMessages().add(String.format(messageSource.getMessage(
                                                new DefaultMessageSourceResolvable(
                                                        "listing.criteria.retiredTestToolNotAllowed"),
                                                LocaleContextHolder.getLocale()), foundTestTool.getName(), cert.getNumber()));
                                    } else {
                                        //the listing does not have ICS so retired tools are definitely not allowed - error
                                        product.getErrorMessages().add(String.format(messageSource.getMessage(
                                                new DefaultMessageSourceResolvable(
                                                        "listing.criteria.retiredTestToolNotAllowed"),
                                                LocaleContextHolder.getLocale()), foundTestTool.getName(), cert.getNumber()));
                                    }
                                }
                            }
                        }
                    }
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)
                        && cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
                    for (PendingCertificationResultTestFunctionalityDTO pendingFuncMap : cert.getTestFunctionality()) {
                        if (pendingFuncMap.getTestFunctionalityId() == null) {
                            TestFunctionalityDTO foundTestFunc = testFuncDao.getByNumberAndEdition(
                                    pendingFuncMap.getNumber(), product.getCertificationEditionId());
                            if (foundTestFunc == null || foundTestFunc.getId() == null) {
                                product.getErrorMessages()
                                        .add("Certification " + cert.getNumber()
                                                + " contains invalid test functionality: '" + pendingFuncMap.getNumber()
                                                + "'.");
                            }
                        }
                    }
                }

                if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_PROCEDURE)
                        && cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0) {
                    for (PendingCertificationResultTestProcedureDTO crTestProc : cert.getTestProcedures()) {
                        if (crTestProc.getTestProcedure() == null || crTestProc.getTestProcedureId() == null) {
                            product.getErrorMessages().add(
                                    String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable(
                                            "listing.criteria.badTestProcedureName"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), crTestProc.getEnteredName()));
                        } else if(crTestProc.getTestProcedure() != null && crTestProc.getTestProcedure().getId() == null) {
                            TestProcedureDTO foundTestProc = 
                                    testProcDao.getByCriteriaNumberAndValue(cert.getNumber(), crTestProc.getTestProcedure().getName());
                            if(foundTestProc == null || foundTestProc.getId() == null) {
                                product.getErrorMessages().add(
                                        String.format(messageSource.getMessage(
                                        new DefaultMessageSourceResolvable(
                                                "listing.criteria.badTestProcedureName"),
                                        LocaleContextHolder.getLocale()), cert.getNumber(), crTestProc.getTestProcedure().getName()));
                            } else {
                                crTestProc.getTestProcedure().setId(foundTestProc.getId());
                            }
                        }
                        
                        if(!StringUtils.isEmpty(crTestProc.getEnteredName()) && StringUtils.isEmpty(crTestProc.getVersion())) {
                            product.getErrorMessages().add(
                                    String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable(
                                            "listing.criteria.missingTestProcedureVersion"),
                                    LocaleContextHolder.getLocale()), cert.getNumber()));
                        }
                    }
                }
                
                if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_DATA)
                        && cert.getTestData() != null && cert.getTestData().size() > 0) {
                    for (PendingCertificationResultTestDataDTO crTestData : cert.getTestData()) {
                        if (crTestData.getTestData() == null || crTestData.getTestDataId() == null) {
                            product.getWarningMessages().add(
                                    String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable(
                                            "listing.criteria.badTestDataName"),
                                    LocaleContextHolder.getLocale()), crTestData.getEnteredName(), cert.getNumber(), TestDataDTO.DEFALUT_TEST_DATA));
                            TestDataDTO foundTestData = 
                                    testDataDao.getByCriteriaNumberAndValue(cert.getNumber(), TestDataDTO.DEFALUT_TEST_DATA);
                            crTestData.setTestData(foundTestData);
                        } else if(crTestData.getTestData() != null && crTestData.getTestData().getId() == null) {
                            TestDataDTO foundTestData = 
                                    testDataDao.getByCriteriaNumberAndValue(cert.getNumber(), crTestData.getTestData().getName());
                            if(foundTestData == null || foundTestData.getId() == null) {
                                product.getWarningMessages().add(
                                        String.format(messageSource.getMessage(
                                        new DefaultMessageSourceResolvable(
                                                "listing.criteria.badTestDataName"),
                                        LocaleContextHolder.getLocale()), crTestData.getTestData().getName(), cert.getNumber(), TestDataDTO.DEFALUT_TEST_DATA));
                                foundTestData = 
                                        testDataDao.getByCriteriaNumberAndValue(cert.getNumber(), TestDataDTO.DEFALUT_TEST_DATA);
                                crTestData.getTestData().setId(foundTestData.getId());
                            } else {
                                crTestData.getTestData().setId(foundTestData.getId());
                            }
                        }
                        
                        if(!StringUtils.isEmpty(crTestData.getEnteredName()) && StringUtils.isEmpty(crTestData.getVersion())) {
                            product.getErrorMessages().add(
                                    String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable(
                                            "listing.criteria.missingTestDataVersion"),
                                    LocaleContextHolder.getLocale()), cert.getNumber()));
                        }
                    }
                }
                
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G1_MACRA)
                        && cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
                    for (PendingCertificationResultMacraMeasureDTO pendingMeasureMap : cert.getG1MacraMeasures()) {
                        if (pendingMeasureMap.getMacraMeasureId() == null) {
                            MacraMeasureDTO foundMeasure = macraDao.getByCriteriaNumberAndValue(cert.getNumber(),
                                    pendingMeasureMap.getEnteredValue());
                            if (foundMeasure == null || foundMeasure.getId() == null) {
                                product.getErrorMessages()
                                        .add("Certification " + cert.getNumber()
                                                + " contains invalid G1 Macra Measure: '"
                                                + pendingMeasureMap.getEnteredValue() + "'.");
                            } else {
                                pendingMeasureMap.setMacraMeasure(foundMeasure);
                            }
                        }
                    }
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G2_MACRA)
                        && cert.getG2MacraMeasures() != null && cert.getG2MacraMeasures().size() > 0) {
                    for (PendingCertificationResultMacraMeasureDTO pendingMeasureMap : cert.getG2MacraMeasures()) {
                        if (pendingMeasureMap.getMacraMeasureId() == null) {
                            MacraMeasureDTO foundMeasure = macraDao.getByCriteriaNumberAndValue(cert.getNumber(),
                                    pendingMeasureMap.getEnteredValue());
                            if (foundMeasure == null || foundMeasure.getId() == null) {
                                product.getErrorMessages()
                                        .add("Certification " + cert.getNumber()
                                                + " contains invalid G2 Macra Measure: '"
                                                + pendingMeasureMap.getEnteredValue() + "'.");
                            } else {
                                pendingMeasureMap.setMacraMeasure(foundMeasure);
                            }
                        }
                    }
                }

                // g1 and g2 are the only criteria that ONC supplies test data
                // for
                // other criteria probably should have test data but do not have
                // to
                if (!gapEligibleAndTrue
                        && (cert.getNumber().equals("170.315 (g)(1)") || cert.getNumber().equals("170.315 (g)(2)"))
                        && (cert.getTestData() == null || cert.getTestData().size() == 0)) {
                    product.getErrorMessages().add("Test Data is required for certification " + cert.getNumber() + ".");
                }
            }
        }

        if (product.getIcs() == null) {
            product.getErrorMessages().add("ICS is required.");
        }
    }

    @Override
    public void validate(CertifiedProductSearchDetails product) {
        super.validate(product);

        List<String> allMetCerts = new ArrayList<String>();
        for (CertificationResult certCriteria : product.getCertificationResults()) {
            if (certCriteria.isSuccess()) {
                allMetCerts.add(certCriteria.getNumber());
            }
        }

        List<String> errors = checkClassOfCriteriaForErrors("170.315 (a)", allMetCerts,
                Arrays.asList(aComplimentaryCerts));
        product.getErrorMessages().addAll(errors);

        errors = checkClassOfCriteriaForErrors("170.315 (b)", allMetCerts, Arrays.asList(bComplimentaryCerts));
        product.getErrorMessages().addAll(errors);

        errors = checkClassOfCriteriaForErrors("170.315 (c)", allMetCerts, Arrays.asList(cComplimentaryCerts));
        product.getErrorMessages().addAll(errors);

        errors = checkClassOfCriteriaForErrors("170.315 (f)", allMetCerts, Arrays.asList(fComplimentaryCerts));
        product.getErrorMessages().addAll(errors);

        errors = checkClassOfCriteriaForErrors("170.315 (h)", allMetCerts, Arrays.asList(hComplimentaryCerts));
        product.getErrorMessages().addAll(errors);

        errors = checkSpecificCriteriaForErrors("170.315 (e)(1)", allMetCerts, Arrays.asList(e1ComplimentaryCerts));
        product.getErrorMessages().addAll(errors);

        // check for (c)(1), (c)(2), (c)(3), (c)(4)
        boolean meetsC1Criterion = hasCert("170.315 (c)(1)", allMetCerts);
        boolean meetsC2Criterion = hasCert("170.315 (c)(2)", allMetCerts);
        boolean meetsC3Criterion = hasCert("170.315 (c)(3)", allMetCerts);
        boolean meetsC4Criterion = hasCert("170.315 (c)(4)", allMetCerts);
        boolean hasC1Cqm = false;
        boolean hasC2Cqm = false;
        boolean hasC3Cqm = false;
        boolean hasC4Cqm = false;

        for (CQMResultDetails cqm : product.getCqmResults()) {
            List<String> certifications = new ArrayList<String>();
            for (CQMResultCertification criteria : cqm.getCriteria()) {
                certifications.add(criteria.getCertificationNumber());
            }
            hasC1Cqm = hasC1Cqm || hasCert("170.315 (c)(1)", certifications);
            hasC2Cqm = hasC2Cqm || hasCert("170.315 (c)(2)", certifications);
            hasC3Cqm = hasC3Cqm || hasCert("170.315 (c)(3)", certifications);
            hasC4Cqm = hasC4Cqm || hasCert("170.315 (c)(4)", certifications);
        }
        if (meetsC1Criterion && !hasC1Cqm) {
            product.getErrorMessages().add(
                    String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable(
                                    "listing.criteria.missingCqmFor170315c"),
                            LocaleContextHolder.getLocale()), "170.315 (c)(1)"));
        } else if (!meetsC1Criterion && hasC1Cqm) {
            product.getErrorMessages().add(
                    String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable(
                                    "listing.criteria.missing170315cForCqm"),
                            LocaleContextHolder.getLocale()), "170.315 (c)(1)"));
        }
        if (meetsC2Criterion && !hasC2Cqm) {
            product.getErrorMessages().add(
                    String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable(
                                    "listing.criteria.missingCqmFor170315c"),
                            LocaleContextHolder.getLocale()), "170.315 (c)(2)"));
            product.getErrorMessages().add(
                    String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable(
                                    "listing.criteria.missing170315cForCqm"),
                            LocaleContextHolder.getLocale()), "170.315 (c)(2)"));
        }
        if (meetsC3Criterion && !hasC3Cqm) {
            product.getErrorMessages().add(
                    String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable(
                                    "listing.criteria.missingCqmFor170315c"),
                            LocaleContextHolder.getLocale()), "170.315 (c)(3)"));
        } else if (!meetsC3Criterion && hasC3Cqm) {
            product.getErrorMessages().add(
                    String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable(
                                    "listing.criteria.missing170315cForCqm"),
                            LocaleContextHolder.getLocale()), "170.315 (c)(3)"));
        }
        if (meetsC4Criterion && !hasC4Cqm) {
            product.getErrorMessages().add(
                    String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable(
                                    "listing.criteria.missingCqmFor170315c"),
                            LocaleContextHolder.getLocale()), "170.315 (c)(4)"));
        } else if (!meetsC4Criterion && hasC4Cqm) {
            product.getErrorMessages().add(
                    String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable(
                                    "listing.criteria.missing170315cForCqm"),
                            LocaleContextHolder.getLocale()), "170.315 (c)(4)"));
        }

        // check for (e)(2) or (e)(3) certs
        List<String> e2e3Criterion = new ArrayList<String>();
        e2e3Criterion.add("170.315 (e)(2)");
        e2e3Criterion.add("170.315 (e)(3)");
        List<String> e2e3ComplimentaryErrors = 
                checkComplimentaryCriteriaAllRequired(e2e3Criterion, Arrays.asList(e2Ore3ComplimentaryCerts), allMetCerts);
        product.getErrorMessages().addAll(e2e3ComplimentaryErrors);

        // check for (g)(7) or (g)(8) or (g)(9) required complimentary certs
        List<String> g7g8g9Criterion = new ArrayList<String>();
        g7g8g9Criterion.add("170.315 (g)(7)");
        g7g8g9Criterion.add("170.315 (g)(8)");
        g7g8g9Criterion.add("170.315 (g)(9)");
        List<String> g7g8g9ComplimentaryErrors = 
                checkComplimentaryCriteriaAllRequired(g7g8g9Criterion, Arrays.asList(g7Org8Org9ComplimentaryCerts), allMetCerts);
        product.getErrorMessages().addAll(g7g8g9ComplimentaryErrors);
        
        //if g7, g8, or g9 is found then one of d2 or d10 is required
        List<String> d2d10Criterion = new ArrayList<String>();
        d2d10Criterion.add("170.315 (d)(2)");
        d2d10Criterion.add("170.315 (d)(10)");
        g7g8g9ComplimentaryErrors = 
                checkComplimentaryCriteriaAnyRequired(g7g8g9Criterion, d2d10Criterion, allMetCerts);
        product.getErrorMessages().addAll(g7g8g9ComplimentaryErrors);

        // g3 checks
        boolean needsG3 = false;
        for (int i = 0; i < ucdRequiredCerts.length; i++) {
            if (hasCert(ucdRequiredCerts[i], allMetCerts)) {
                needsG3 = true;

                // check for full set of UCD data
                for (CertificationResult certCriteria : product.getCertificationResults()) {
                    if (certCriteria.getNumber().equals(ucdRequiredCerts[i])) {
                        // make sure at least one UCD process has this criteria
                        // number
                        if (product.getSed() == null || product.getSed().getUcdProcesses() == null
                                || product.getSed().getUcdProcesses().size() == 0) {
                            product.getErrorMessages().add("Certification " + certCriteria.getNumber()
                                    + " requires at least one UCD process.");
                        } else {
                            boolean foundCriteria = false;
                            for (UcdProcess ucd : product.getSed().getUcdProcesses()) {
                                for (CertificationCriterion criteria : ucd.getCriteria()) {
                                    if (criteria.getNumber().equalsIgnoreCase(certCriteria.getNumber())) {
                                        foundCriteria = true;
                                    }
                                }
                            }
                            if (!foundCriteria) {
                                product.getErrorMessages().add("Certification " + certCriteria.getNumber()
                                        + " requires at least one UCD process.");
                            }
                        }

                        if (product.getSed() == null || product.getSed().getTestTasks() == null
                                || product.getSed().getTestTasks().size() == 0) {
                            product.getErrorMessages().add(
                                    "Certification " + certCriteria.getNumber() + " requires at least one test task.");
                        } else {
                            boolean foundCriteria = false;
                            for (TestTask tt : product.getSed().getTestTasks()) {
                                for (CertificationCriterion criteria : tt.getCriteria()) {
                                    if (criteria.getNumber().equalsIgnoreCase(certCriteria.getNumber())) {
                                        foundCriteria = true;
                                    }
                                }
                            }
                            if (!foundCriteria) {
                                product.getErrorMessages().add("Certification " + certCriteria.getNumber()
                                        + " requires at least one test task.");
                            }
                        }

                        if (product.getSed() != null && product.getSed().getTestTasks() != null) {
                            for (TestTask task : product.getSed().getTestTasks()) {
                                String description = StringUtils.isEmpty(task.getDescription()) ? "unknown"
                                        : task.getDescription();
                                if (task.getTestParticipants() == null || task.getTestParticipants().size() < 10) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable(
                                                            "listing.sed.badTestTaskParticipantsSize"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (StringUtils.isEmpty(task.getDescription())) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable(
                                                            "listing.sed.badTestDescription"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (task.getTaskSuccessAverage() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable(
                                                            "listing.sed.badTestTaskSuccessAverage"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (task.getTaskSuccessStddev() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable(
                                                            "listing.sed.badTestTaskSuccessStddev"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (task.getTaskPathDeviationObserved() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable(
                                                            "listing.sed.badTestTaskPathDeviationObserved"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (task.getTaskPathDeviationOptimal() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable(
                                                            "listing.sed.badTestTaskPathDeviationOptimal"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (task.getTaskTimeAvg() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable(
                                                            "listing.sed.badTestTaskTimeAvg"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (task.getTaskTimeStddev() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable(
                                                            "listing.sed.badTestTaskTimeStddev"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (task.getTaskTimeDeviationObservedAvg() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable(
                                                            "listing.sed.badTestTaskTimeDeviationObservedAvg"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (task.getTaskTimeDeviationOptimalAvg() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable(
                                                            "listing.sed.badTestTaskTimeDeviationOptimalAvg"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (task.getTaskErrors() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable("listing.sed.badTestTaskErrors"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (task.getTaskErrorsStddev() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable(
                                                            "listing.sed.badTestTaskErrorsStddev"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (StringUtils.isEmpty(task.getTaskRatingScale())) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable(
                                                            "listing.sed.badTestTaskRatingScale"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (task.getTaskRating() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable("listing.sed.badTestTaskRating"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                if (task.getTaskRatingStddev() == null) {
                                    product.getErrorMessages()
                                            .add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable(
                                                            "listing.sed.badTestTaskRatingStddev"),
                                                    LocaleContextHolder.getLocale()), description));
                                }
                                for (TestParticipant part : task.getTestParticipants()) {
                                    if (part.getEducationTypeId() == null) {
                                        product.getErrorMessages()
                                                .add(String.format(messageSource.getMessage(
                                                        new DefaultMessageSourceResolvable(
                                                                "listing.sed.badParticipantEducationLevel"),
                                                        LocaleContextHolder.getLocale()), description));
                                    }
                                    if (part.getAgeRangeId() == null) {
                                        product.getErrorMessages()
                                                .add(String.format(messageSource.getMessage(
                                                        new DefaultMessageSourceResolvable(
                                                                "listing.sed.badParticipantAgeRange"),
                                                        LocaleContextHolder.getLocale()), description));
                                    }
                                    if (StringUtils.isEmpty(part.getGender())) {
                                        product.getErrorMessages()
                                                .add(String.format(messageSource.getMessage(
                                                        new DefaultMessageSourceResolvable(
                                                                "listing.sed.badParticipantGender"),
                                                        LocaleContextHolder.getLocale()), description));
                                    }
                                    if (StringUtils.isEmpty(part.getOccupation())) {
                                        product.getErrorMessages()
                                                .add(String.format(messageSource.getMessage(
                                                        new DefaultMessageSourceResolvable(
                                                                "listing.sed.badParticipantOccupation"),
                                                        LocaleContextHolder.getLocale()), description));
                                    }
                                    if (StringUtils.isEmpty(part.getAssistiveTechnologyNeeds())) {
                                        product.getErrorMessages()
                                                .add(String.format(messageSource.getMessage(
                                                        new DefaultMessageSourceResolvable(
                                                                "listing.sed.badParticipantAssistiveTechnologyNeeds"),
                                                        LocaleContextHolder.getLocale()), description));
                                    }
                                    if (part.getProfessionalExperienceMonths() == null) {
                                        product.getErrorMessages()
                                                .add(String.format(messageSource.getMessage(
                                                        new DefaultMessageSourceResolvable(
                                                                "listing.sed.badParticipantProfessionalExperienceMonths"),
                                                        LocaleContextHolder.getLocale()), description));
                                    }
                                    if (part.getProductExperienceMonths() == null) {
                                        product.getErrorMessages()
                                                .add(String.format(messageSource.getMessage(
                                                        new DefaultMessageSourceResolvable(
                                                                "listing.sed.badParticipantProductExperienceMonths"),
                                                        LocaleContextHolder.getLocale()), description));
                                    }
                                    if (part.getComputerExperienceMonths() == null) {
                                        product.getErrorMessages()
                                                .add(String.format(messageSource.getMessage(
                                                        new DefaultMessageSourceResolvable(
                                                                "listing.sed.badParticipantComputerExperienceMonths"),
                                                        LocaleContextHolder.getLocale()), description));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (needsG3) {
            boolean hasG3 = hasCert("170.315 (g)(3)", allMetCerts);
            if (!hasG3) {
                product.getErrorMessages().add("170.315 (g)(3) is required but was not found.");
            }
        }

        // g3 inverse check
        boolean hasG3ComplimentaryCerts = false;
        for (int i = 0; i < ucdRequiredCerts.length; i++) {
            if (hasCert(ucdRequiredCerts[i], allMetCerts)) {
                hasG3ComplimentaryCerts = true;
            }
        }
        if (!hasG3ComplimentaryCerts) {
            // make sure it doesn't have g3
            boolean hasG3 = hasCert("170.315 (g)(3)", allMetCerts);
            if (hasG3) {
                product.getErrorMessages().add("170.315 (g)(3) is not allowed but was found.");
            }
        }

        // g4 check
        boolean hasG4 = hasCert("170.315 (g)(4)", allMetCerts);
        if (!hasG4) {
            product.getErrorMessages().add("170.315 (g)(4) is required but was not found.");
        }

        // g5 check
        boolean hasG5 = hasCert("170.315 (g)(5)", allMetCerts);
        if (!hasG5) {
            product.getErrorMessages().add("170.315 (g)(5) is required but was not found.");
        }

        // g6 checks
        boolean needsG6 = false;
        for (int i = 0; i < g6CertsToCheck.length && !needsG6; i++) {
            if (hasCert(g6CertsToCheck[i], allMetCerts)) {
                needsG6 = true;
            }
        }
        if (needsG6) {
            boolean hasG6 = hasCert("170.315 (g)(6)", allMetCerts);
            if (!hasG6) {
                product.getErrorMessages().add("170.315 (g)(6) is required but was not found.");
            }
        }

        // TODO: detailed G6 check

        // h1 plus b1
        boolean hasH1 = hasCert("170.315 (h)(1)", allMetCerts);
        if (hasH1) {
            boolean hasB1 = hasCert("170.315 (b)(1)", allMetCerts);
            if (!hasB1) {
                product.getErrorMessages()
                        .add("170.315 (h)(1) was found so 170.315 (b)(1) is required but was not found.");
            }
        }
    }

    protected void validateDemographics(CertifiedProductSearchDetails product) {
        super.validateDemographics(product);

        if (product.getIcs() == null || product.getIcs().getInherits() == null) {
            product.getErrorMessages().add(String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("listing.missingIcs"),
                    LocaleContextHolder.getLocale())));
        } else if (product.getIcs().getInherits().equals(Boolean.TRUE) && icsCodeInteger.intValue() > 0) {
            // if ICS is nonzero, and no parents are found, give error
            if (product.getIcs() == null || product.getIcs().getParents() == null
                    || product.getIcs().getParents().size() == 0) {
                product.getErrorMessages().add(String.format(messageSource.getMessage(
                                                    new DefaultMessageSourceResolvable("listing.icsTrueAndNoParentsFound"),
                                                    LocaleContextHolder.getLocale())));
            } else {
                // parents are non-empty - check inheritance rules
                // certification edition must be the same as this listings
                List<Long> parentIds = new ArrayList<Long>();
                for (CertifiedProduct potentialParent : product.getIcs().getParents()) {
                    //the id might be null if the user changed it in the UI 
                    //even though it's a valid CHPL product number
                    if(potentialParent.getId() == null) {
                        try {
                            CertifiedProduct found = searchDao.getByChplProductNumber(potentialParent.getChplProductNumber());
                            if (found != null) {
                                potentialParent.setId(found.getId());
                            }
                        } catch(Exception ignore) { }
                    }
                    
                    //if the ID is still null after trying to look it up, that's a problem
                    if(potentialParent.getId() == null) {
                        product.getErrorMessages().add(String.format(messageSource.getMessage(
                                new DefaultMessageSourceResolvable("listing.icsUniqueIdNotFound"),
                                LocaleContextHolder.getLocale()), potentialParent.getChplProductNumber()));
                    } else if (potentialParent.getId().toString().equals(product.getId().toString())) {
                        product.getErrorMessages().add(String.format(messageSource.getMessage(
                                new DefaultMessageSourceResolvable("listing.icsSelfInheritance"),
                                LocaleContextHolder.getLocale())));
                    } else {
                        parentIds.add(potentialParent.getId());
                    }
                }
                
                if(parentIds != null && parentIds.size() > 0) {
                    List<CertificationEditionDTO> parentEditions = certEditionDao.getEditions(parentIds);
                    for (CertificationEditionDTO parentEdition : parentEditions) {
                        if (!product.getCertificationEdition().get("id").toString()
                                .equals(parentEdition.getId().toString())) {
                            product.getErrorMessages().add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.icsEditionMismatch"),
                                    LocaleContextHolder.getLocale()), parentEdition.getYear()));
                        }
                    }
                    
                    // this listing's ICS code must be greater than the max of
                    // parent ICS codes
                    Integer largestIcs = inheritanceDao.getLargestIcs(parentIds);
                    if (largestIcs != null && icsCodeInteger.intValue() != (largestIcs.intValue() + 1)) {
                        product.getErrorMessages().add(String.format(messageSource.getMessage(
                                new DefaultMessageSourceResolvable("listing.icsNotLargestCode"),
                                LocaleContextHolder.getLocale()), icsCodeInteger, largestIcs));
                    }
                }
            }
        }

        if (product.getQmsStandards() == null || product.getQmsStandards().size() == 0) {
            product.getErrorMessages().add("QMS Standards are required.");
        } else {
            for (CertifiedProductQmsStandard qms : product.getQmsStandards()) {
                if (StringUtils.isBlank(qms.getApplicableCriteria())) {
                    product.getErrorMessages().add("Applicable criteria is required for each QMS Standard listed.");
                }
            }
        }

        if (product.getAccessibilityStandards() == null || product.getAccessibilityStandards().size() == 0) {
            product.getErrorMessages().add("Accessibility standards are required.");
        }

        // now check all the new certs for whatever is required
        for (CertificationResult cert : product.getCertificationResults()) {
            if (cert.isSuccess() != null && cert.isSuccess() == Boolean.TRUE) {
                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP)
                        && cert.isGap() == Boolean.TRUE) {
                    gapEligibleAndTrue = true;
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.PRIVACY_SECURITY)
                        && StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    product.getErrorMessages().add(
                            "Privacy and Security Framework is required for certification " + cert.getNumber() + ".");
                }
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.API_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getApiDocumentation())) {
                    product.getErrorMessages()
                            .add("API Documentation is required for certification " + cert.getNumber() + ".");
                }

                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED)
                        && (cert.getTestToolsUsed() == null || cert.getTestToolsUsed().size() == 0)) {
                    product.getErrorMessages().add(String.format(messageSource.getMessage(
                        new DefaultMessageSourceResolvable("listing.criteria.missingTestTool"),
                        LocaleContextHolder.getLocale()),
                        cert.getNumber()));
                }

                if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED)
                        && cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
                    for (CertificationResultTestTool testTool : cert.getTestToolsUsed()) {
                        //no new test tools are allowed to be added 
                        //so make sure a test tool by this name exists
                        if (testTool.getTestToolId() == null) {
                            TestToolDTO foundTestTool = testToolDao.getByName(testTool.getTestToolName());
                            if (foundTestTool == null || foundTestTool.getId() == null) {
                                product.getErrorMessages().add(String.format(messageSource.getMessage(
                                        new DefaultMessageSourceResolvable(
                                                "listing.criteria.invalidTestToolName"),
                                        LocaleContextHolder.getLocale()), cert.getNumber(), testTool.getTestToolName()));
                            } else {
                                //require test tool version
                                if(StringUtils.isEmpty(testTool.getTestToolVersion())) {
                                    product.getErrorMessages().add(String.format(messageSource.getMessage(
                                            new DefaultMessageSourceResolvable(
                                                    "listing.criteria.missingTestToolVersion"),
                                            LocaleContextHolder.getLocale()), testTool.getTestToolName(), cert.getNumber()));
                                }
                                
                                //Allow retired test tool only if listing ICS = true
                                if (foundTestTool.isRetired() && super.icsCodeInteger.intValue() == 0) {
                                    if (super.hasIcsConflict) {
                                        //the ics code is 0 but we can't be sure that's what the user meant
                                        //because the ICS value of the listing is TRUE (hence the conflict), 
                                        //so issue a warning since the listing may or may not truly have ICS
                                        product.getWarningMessages().add(String.format(messageSource.getMessage(
                                                new DefaultMessageSourceResolvable(
                                                        "listing.criteria.retiredTestToolNotAllowed"),
                                                LocaleContextHolder.getLocale()), foundTestTool.getName(), cert.getNumber()));
                                    } else {
                                        //the listing does not have ICS so retired tools are definitely not allowed - error
                                        product.getErrorMessages().add(String.format(messageSource.getMessage(
                                                new DefaultMessageSourceResolvable(
                                                        "listing.criteria.retiredTestToolNotAllowed"),
                                                LocaleContextHolder.getLocale()), foundTestTool.getName(), cert.getNumber()));
                                    }
                                }
                            }
                        }
                    }
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)
                        && cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
                    for (CertificationResultTestFunctionality funcMap : cert.getTestFunctionality()) {
                        if (funcMap.getTestFunctionalityId() == null) {
                            TestFunctionalityDTO foundTestFunc = testFuncDao.getByNumberAndEdition(funcMap.getName(),
                                    new Long(product.getCertificationEdition().get("id").toString()));
                            if (foundTestFunc == null || foundTestFunc.getId() == null) {
                                product.getErrorMessages().add("Certification " + cert.getNumber()
                                        + " contains invalid test functionality: '" + funcMap.getName() + "'.");
                            }
                        }
                    }
                }

                //require at least one test procedure where gap does not exist or is false
                if (!gapEligibleAndTrue
                    && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_PROCEDURE)
                    && (cert.getTestProcedures() == null || cert.getTestProcedures().size() == 0)) {
                    product.getErrorMessages().add(String.format(messageSource.getMessage(
                        new DefaultMessageSourceResolvable("listing.criteria.missingTestProcedure"),
                        LocaleContextHolder.getLocale()),
                        cert.getNumber()));
                }

                //if the criteria can and does have test procedures, make sure they are each valid
                if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_PROCEDURE)
                        && cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0) {
                    for (CertificationResultTestProcedure crTestProc : cert.getTestProcedures()) {
                        if(crTestProc.getTestProcedure() == null) {
                            product.getErrorMessages().add(
                                    String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable(
                                            "listing.criteria.missingTestProcedureName"),
                                    LocaleContextHolder.getLocale()), cert.getNumber()));
                        } if(crTestProc.getTestProcedure() != null && crTestProc.getTestProcedure().getId() == null) {
                            TestProcedureDTO foundTestProc = 
                                    testProcDao.getByCriteriaNumberAndValue(cert.getNumber(), crTestProc.getTestProcedure().getName());
                            if(foundTestProc == null || foundTestProc.getId() == null) {
                                product.getErrorMessages().add(
                                        String.format(messageSource.getMessage(
                                        new DefaultMessageSourceResolvable(
                                                "listing.criteria.badTestProcedureName"),
                                        LocaleContextHolder.getLocale()), cert.getNumber(), crTestProc.getTestProcedure().getName()));
                            } else {
                                crTestProc.getTestProcedure().setId(foundTestProc.getId());
                            }
                        }
                        
                        if(crTestProc.getTestProcedure() != null && 
                                !StringUtils.isEmpty(crTestProc.getTestProcedure().getName()) && 
                                StringUtils.isEmpty(crTestProc.getTestProcedureVersion())) {
                            product.getErrorMessages().add(
                                    String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable(
                                            "listing.criteria.missingTestProcedureVersion"),
                                    LocaleContextHolder.getLocale()), cert.getNumber()));
                        }
                    }
                }
                
                if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_DATA)
                        && cert.getTestDataUsed() != null && cert.getTestDataUsed().size() > 0) {
                    for (CertificationResultTestData crTestData : cert.getTestDataUsed()) {
                        if (crTestData.getTestData() == null) {
                            product.getWarningMessages().add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable(
                                            "listing.criteria.missingTestDataName"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), TestDataDTO.DEFALUT_TEST_DATA));
                            TestDataDTO foundTestData = 
                                    testDataDao.getByCriteriaNumberAndValue(cert.getNumber(), TestDataDTO.DEFALUT_TEST_DATA);
                            TestData foundTestDataDomain = new TestData(foundTestData.getId(), foundTestData.getName());
                            crTestData.setTestData(foundTestDataDomain);
                        } else if(crTestData.getTestData() != null && crTestData.getTestData().getId() == null) {
                            TestDataDTO foundTestData = 
                                    testDataDao.getByCriteriaNumberAndValue(cert.getNumber(), crTestData.getTestData().getName());
                            if(foundTestData == null || foundTestData.getId() == null) {
                                product.getWarningMessages().add(String.format(messageSource.getMessage(
                                        new DefaultMessageSourceResolvable(
                                                "listing.criteria.badTestDataName"),
                                        LocaleContextHolder.getLocale()), crTestData.getTestData().getName(), cert.getNumber(), TestDataDTO.DEFALUT_TEST_DATA));
                                foundTestData = 
                                        testDataDao.getByCriteriaNumberAndValue(cert.getNumber(), TestDataDTO.DEFALUT_TEST_DATA);
                                crTestData.getTestData().setId(foundTestData.getId());
                            } else {
                                crTestData.getTestData().setId(foundTestData.getId());
                            }
                        }
                        
                        if(crTestData.getTestData() != null && 
                                !StringUtils.isEmpty(crTestData.getTestData().getName()) && 
                                StringUtils.isEmpty(crTestData.getVersion())) {
                            product.getErrorMessages().add(
                                    String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable(
                                            "listing.criteria.missingTestDataVersion"),
                                    LocaleContextHolder.getLocale()), cert.getNumber()));
                        }
                    }
                }
                
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G1_MACRA)
                        && cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
                    for (int i = 0; i < cert.getG1MacraMeasures().size(); i++) {
                        MacraMeasure measure = cert.getG1MacraMeasures().get(i);
                        if (measure == null || measure.getId() == null) {
                            product.getErrorMessages()
                                    .add("Certification " + cert.getNumber() + " contains invalid G1 Macra Measure.");
                        } else {
                            // confirm the measure id is valid
                            MacraMeasureDTO foundMeasure = macraDao.getById(measure.getId());
                            if (foundMeasure == null || foundMeasure.getId() == null) {
                                product.getErrorMessages()
                                        .add("Certification " + cert.getNumber()
                                                + " contains invalid G1 Macra Measure. No measure found with ID '"
                                                + measure.getId() + "'.");
                            } else if (!foundMeasure.getCriteria().getNumber().equals(cert.getNumber())) {
                                product.getErrorMessages().add("Certification " + cert.getNumber()
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
                            product.getErrorMessages()
                                    .add("Certification " + cert.getNumber() + " contains invalid G2 Macra Measure.");
                        } else {
                            // confirm the measure id is valid
                            MacraMeasureDTO foundMeasure = macraDao.getById(measure.getId());
                            if (foundMeasure == null || foundMeasure.getId() == null) {
                                product.getErrorMessages()
                                        .add("Certification " + cert.getNumber()
                                                + " contains invalid G2 Macra Measure. No measure found with ID '"
                                                + measure.getId() + "'.");
                            } else if (!foundMeasure.getCriteria().getNumber().equals(cert.getNumber())) {
                                product.getErrorMessages().add("Certification " + cert.getNumber()
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
                        && (cert.getNumber().equals("170.315 (g)(1)") || cert.getNumber().equals("170.315 (g)(2)"))
                        && (cert.getTestDataUsed() == null || cert.getTestDataUsed().size() == 0)) {
                    product.getErrorMessages().add("Test Data is required for certification " + cert.getNumber() + ".");
                }
            }
        }
    }

    /**
     * Returns true if any of the passed in certs are present
     * @param toCheck
     * @param allCerts
     * @return
     */
    private boolean hasAnyCert(List<String> certsToCheck, List<String> allCerts) {
        boolean result = false;
        for(String currCertToCheck : certsToCheck) {
            if(hasCert(currCertToCheck, allCerts)) {
                result = true;
            }
        }
        return result;
    }
    
    private boolean hasCert(String toCheck, List<String> allCerts) {
        boolean hasCert = false;
        for (int i = 0; i < allCerts.size() && !hasCert; i++) {
            if (allCerts.get(i).equals(toCheck)) {
                hasCert = true;
            }
        }
        return hasCert;
    }

    /**
     * look for required complimentary certs when one of the criteria met is a
     * certain class of cert... such as 170.315 (a)(*)
     * 
     * @param criterionNumberStart
     * @param allCriteriaMet
     * @param complimentaryCertNumbers
     * @return
     */
    private List<String> checkClassOfCriteriaForErrors(String criterionNumberStart, List<String> allCriteriaMet,
            List<String> complimentaryCertNumbers) {
        List<String> errors = new ArrayList<String>();
        boolean hasCriterion = false;
        for (String currCriteria : allCriteriaMet) {
            if (currCriteria.startsWith(criterionNumberStart)) {
                hasCriterion = true;
            }
        }
        if (hasCriterion) {
            for (String currRequiredCriteria : complimentaryCertNumbers) {
                boolean hasComplimentaryCert = false;
                for (String certCriteria : complimentaryCertNumbers) {
                    if (certCriteria.equals(currRequiredCriteria)) {
                        hasComplimentaryCert = true;
                    }
                }

                if (!hasComplimentaryCert) {
                    errors.add("Certification criterion " + criterionNumberStart + "(*) was found " + "so "
                            + currRequiredCriteria + " is required but was not found.");
                }
            }
        }
        return errors;
    }

    /**
     * Look for required complimentary criteria 
     * 
     * @param criterionNumbers
     * @param allCriteriaMet
     * @param complimentaryCertNumbers
     * @return
     */
    private List<String> checkComplimentaryCriteriaAllRequired(List<String> criterionToCheck, 
            List<String> complimentaryCertNumbers, List<String> allCriteriaMet) {
        List<String> errors = new ArrayList<String>();
        boolean hasAnyCert = hasAnyCert(criterionToCheck, allCriteriaMet);
        if (hasAnyCert) {
            for (String complimentaryCert : complimentaryCertNumbers) {
                boolean hasComplimentaryCert = hasCert(complimentaryCert, allCriteriaMet);
                
                if (!hasComplimentaryCert) {
                    String criterionErrorString = "";
                    for(int i = 0; i < criterionToCheck.size(); i++) {
                        String checkedCriteria = criterionToCheck.get(i);
                        if(i > 0) {
                            criterionErrorString += " or ";
                        }
                        criterionErrorString += checkedCriteria;
                    }
                    errors.add("Certification criterion " + criterionErrorString + " was found so "
                            + complimentaryCert + " is required but was not found.");
                }
            }
        }
        return errors;
    }
    
    /**
     * Look for required complimentary criteria 
     * 
     * @param criterionNumbers
     * @param allCriteriaMet
     * @param complimentaryCertNumbers
     * @return
     */
    private List<String> checkComplimentaryCriteriaAnyRequired(List<String> criterionToCheck, 
            List<String> complimentaryCertNumbers, List<String> allCriteriaMet) {
        List<String> errors = new ArrayList<String>();
        boolean hasAnyCert = hasAnyCert(criterionToCheck, allCriteriaMet);
        if (hasAnyCert) {
            boolean hasAnyComplimentaryCert = hasAnyCert(complimentaryCertNumbers, allCriteriaMet);
            if (!hasAnyComplimentaryCert) {
                String criterionErrorString = "";
                for(int i = 0; i < criterionToCheck.size(); i++) {
                    String checkedCriteria = criterionToCheck.get(i);
                    if(i > 0) {
                        criterionErrorString += " or ";
                    }
                    criterionErrorString += checkedCriteria;
                }
                
                String complimentaryCriterionErrorString = "";
                for(int i = 0; i < complimentaryCertNumbers.size(); i++) {
                    String complimentaryCriteria = complimentaryCertNumbers.get(i);
                    if(i > 0) {
                        complimentaryCriterionErrorString += " or ";
                    }
                    complimentaryCriterionErrorString += complimentaryCriteria;
                }
                errors.add("Certification criterion " + criterionErrorString + " was found so "
                        + complimentaryCriterionErrorString + " is required but was not found.");
            }
        }
        return errors;
    }
    
    /**
     * Look for a required complimentary criteria when a specific criteria has
     * been met
     * 
     * @param criterionNumber
     * @param allCriteriaMet
     * @param complimentaryCertNumbers
     * @return
     */
    private List<String> checkSpecificCriteriaForErrors(String criterionNumber, List<String> allCriteriaMet,
            List<String> complimentaryCertNumbers) {
        List<String> errors = new ArrayList<String>();
        boolean hasCriterion = false;
        for (String currCriteria : allCriteriaMet) {
            if (currCriteria.equals(criterionNumber)) {
                hasCriterion = true;
            }
        }
        if (hasCriterion) {
            for (String currRequiredCriteria : complimentaryCertNumbers) {
                boolean hasComplimentaryCert = false;
                for (String certCriteria : complimentaryCertNumbers) {
                    if (certCriteria.equals(currRequiredCriteria)) {
                        hasComplimentaryCert = true;
                    }
                }

                if (!hasComplimentaryCert) {
                    errors.add("Certification criterion " + criterionNumber + "(*) was found " + "so "
                            + currRequiredCriteria + " is required but was not found.");
                }
            }
        }
        return errors;
    }
}
