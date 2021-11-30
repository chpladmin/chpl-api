package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingTestTaskDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.pendingListing.reviewer.RequiredDataReviewer;

/**
 * Makes sure any required criteria, fields, etc are present
 * for a 2015 pending listing.
 * @author kekey
 *
 */
@Component("pendingRequiredData2015Reviewer")
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
    private static final String G3_CRITERIA_NUMBER = "170.315 (g)(3)";
    private static final String G6_CRITERIA_NUMBER = "170.315 (g)(6)";
    private static final String H1_CRITERIA_NUMBER = "170.315 (h)(1)";
    private static final int MINIMIMUM_PARTICIPANTS = 10;

    private List<String> e2e3Criterion = new ArrayList<String>();
    private List<String> g7g8g9Criterion = new ArrayList<String>();
    private List<String> d2d10Criterion = new ArrayList<String>();
    private List<CertificationCriterion> e1Criteria;

    private TestFunctionalityDAO testFuncDao;
    private TestProcedureDAO testProcDao;
    private TestDataDAO testDataDao;
    private CertificationCriterionDAO criteriaDao;
    private CertificationCriterionService criterionService;
    private ValidationUtils validationUtils;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public RequiredData2015Reviewer(TestFunctionalityDAO testFuncDao,
            TestProcedureDAO testProcDao, TestDataDAO testDataDao,
            CertificationCriterionDAO criteriaDao, CertificationCriterionService criterionService,
            ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions,
            CertificationResultRules certRules, ValidationUtils validationUtils) {
        super(msgUtil, resourcePermissions, certRules);

        this.testFuncDao = testFuncDao;
        this.testProcDao = testProcDao;
        this.testDataDao = testDataDao;
        this.criteriaDao = criteriaDao;
        this.criterionService = criterionService;
        this.validationUtils = validationUtils;

        e2e3Criterion.add("170.315 (e)(2)");
        e2e3Criterion.add("170.315 (e)(3)");

        g7g8g9Criterion.add("170.315 (g)(7)");
        g7g8g9Criterion.add("170.315 (g)(8)");
        g7g8g9Criterion.add("170.315 (g)(9)");

        d2d10Criterion.add("170.315 (d)(2)");
        d2d10Criterion.add("170.315 (d)(10)");

        e1Criteria = this.criterionService.getByNumber("170.315 (e)(1)");
    }

    @Override
    public void review(final PendingCertifiedProductDTO listing) {
        super.review(listing);

        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);
        List<String> errors;
        List<String> warnings;

        errors = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaNumberErrors("170.315 (a)", attestedCriteria,
                Arrays.asList(A_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        warnings = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaNumberWarnings("170.315 (a)", attestedCriteria,
                Arrays.asList(A_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        errors = validationUtils.checkClassSubsetOfCriteriaForMissingComplementaryCriteriaNumberErrors("170.315 (a)", attestedCriteria,
                Arrays.asList(A_RELATED_CERTS_EXCEPTION), Arrays.asList(A_CERT_EXCEPTIONS));
        listing.getErrorMessages().addAll(errors);
        warnings = validationUtils.checkClassSubsetOfCriteriaForMissingComplementaryCriteriaNumberWarnings("170.315 (a)", attestedCriteria,
                Arrays.asList(A_RELATED_CERTS_EXCEPTION), Arrays.asList(A_CERT_EXCEPTIONS));
        addListingWarningsByPermission(listing, warnings);

        errors = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaNumberErrors("170.315 (c)", attestedCriteria,
                Arrays.asList(C_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        warnings = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaNumberWarnings("170.315 (c)", attestedCriteria,
                Arrays.asList(C_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        errors = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaNumberErrors("170.315 (f)", attestedCriteria,
                Arrays.asList(F_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        warnings = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaNumberWarnings("170.315 (f)", attestedCriteria,
                Arrays.asList(F_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        errors = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaNumberErrors("170.315 (h)", attestedCriteria,
                Arrays.asList(H_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        warnings = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaNumberWarnings("170.315 (h)", attestedCriteria,
                Arrays.asList(H_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        e1Criteria.stream().forEach(e1Criterion -> {
            List<String> e1Errors = validationUtils.checkSpecificCriterionForMissingComplementaryCriteriaNumberErrors(e1Criterion, attestedCriteria,
                    Arrays.asList(E1_RELATED_CERTS));
            listing.getErrorMessages().addAll(e1Errors);
        });

        // check for (e)(2) or (e)(3) required complimentary certs
        List<String> e2e3ComplimentaryErrors =
                validationUtils.checkComplementaryCriteriaNumbersAllRequired(e2e3Criterion,
                        Arrays.asList(E2E3_RELATED_CERTS), attestedCriteria);
        listing.getErrorMessages().addAll(e2e3ComplimentaryErrors);

        // check for (g)(7) or (g)(8) or (g)(9) required complimentary certs
        List<String> g7g8g9ComplimentaryErrors =
                validationUtils.checkComplementaryCriteriaNumbersAllRequired(g7g8g9Criterion,
                        Arrays.asList(G7G8G9_RELATED_CERTS), attestedCriteria);
        listing.getErrorMessages().addAll(g7g8g9ComplimentaryErrors);

        //if g7, g8, or g9 is found then one of d2 or d10 is required
        g7g8g9ComplimentaryErrors =
                validationUtils.checkComplementaryCriteriaNumbersAnyRequired(g7g8g9Criterion, d2d10Criterion, attestedCriteria);
        listing.getErrorMessages().addAll(g7g8g9ComplimentaryErrors);

        // g3 checks
        for (int i = 0; i < UCD_RELATED_CERTS.length; i++) {
            if (validationUtils.hasCert(UCD_RELATED_CERTS[i], attestedCriteria)) {
                // check for full set of UCD data
                for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
                    if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria().equals(Boolean.TRUE)
                            && cert.getCriterion().getNumber().equals(UCD_RELATED_CERTS[i])) {
                        if (cert.getUcdProcesses() == null || cert.getUcdProcesses().size() == 0) {
                            addErrorIfCriterionIsNotRemoved(listing, cert, "listing.criteria.missingUcdProcess",
                                    Util.formatCriteriaNumber(cert.getCriterion()));
                        }
                        if (cert.getTestTasks() == null || cert.getTestTasks().size() == 0) {
                            addErrorIfCriterionIsNotRemoved(listing, cert, "listing.criteria.missingTestTask",
                                    Util.formatCriteriaNumber(cert.getCriterion()));
                        }

                        if (cert.getTestTasks() != null) {
                            for (PendingCertificationResultTestTaskDTO certResultTask : cert.getTestTasks()) {
                                PendingTestTaskDTO task = certResultTask.getPendingTestTask();
                                if (certResultTask.getTaskParticipants() == null
                                        || certResultTask.getTaskParticipants().size() < MINIMIMUM_PARTICIPANTS) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.badTestTaskParticipantsSize",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                }
                                if (StringUtils.isEmpty(task.getDescription())) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestDescription",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                }
                                if (task.getTaskSuccessAverage() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestTaskSuccessAverage",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                } else {
                                    try {
                                        Float.valueOf(task.getTaskSuccessAverage());
                                    } catch (final Exception e) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.badTestTaskNumber",
                                                        task.getUniqueId(), "Task Success Average",
                                                        task.getTaskSuccessAverage()));
                                    }
                                }
                                if (task.getTaskSuccessStddev() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestTaskSuccessStddev",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                } else {
                                    try {
                                        Float.valueOf(task.getTaskSuccessStddev());
                                    } catch (final Exception e) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.badTestTaskNumber",
                                                        task.getUniqueId(), "Task Success Standard Deviation",
                                                        task.getTaskSuccessStddev()));
                                    }
                                }
                                if (task.getTaskPathDeviationObserved() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestTaskPathDeviationObserved",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                } else {
                                    try {
                                        Integer.valueOf(task.getTaskPathDeviationObserved());
                                    } catch (final Exception e) {
                                        try {
                                            int val = Math.round(Float.valueOf(task.getTaskPathDeviationObserved()));
                                            listing.getWarningMessages().add(
                                                    msgUtil.getMessage("listing.criteria.roundedTestTaskNumber",
                                                            task.getUniqueId(), "Task Path Deviation Observed",
                                                            task.getTaskPathDeviationObserved(), String.valueOf(val)));
                                        } catch (final Exception ex) {
                                            listing.getErrorMessages().add(
                                                    msgUtil.getMessage("listing.criteria.badTestTaskNumber",
                                                            task.getUniqueId(), "Task Path Deviation Observed",
                                                            task.getTaskPathDeviationObserved()));
                                        }
                                    }
                                }
                                if (task.getTaskPathDeviationOptimal() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestTaskPathDeviationOptimal",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                } else {
                                    try {
                                        Integer.valueOf(task.getTaskPathDeviationOptimal());
                                    } catch (final Exception e) {
                                        try {
                                            int val = Math.round(Float.valueOf(task.getTaskPathDeviationOptimal()));
                                            listing.getWarningMessages().add(
                                                    msgUtil.getMessage("listing.criteria.roundedTestTaskNumber",
                                                            task.getUniqueId(), "Task Path Deviation Optimal",
                                                            task.getTaskPathDeviationOptimal(), String.valueOf(val)));
                                        } catch (final Exception ex) {
                                            listing.getErrorMessages().add(
                                                    msgUtil.getMessage("listing.criteria.badTestTaskNumber",
                                                            task.getUniqueId(), "Task Path Deviation Optimal",
                                                            task.getTaskPathDeviationOptimal()));
                                        }
                                    }
                                }
                                if (task.getTaskTimeAvg() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestTaskTimeAvg",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                } else {
                                    try {
                                        Long.valueOf(task.getTaskTimeAvg());
                                    } catch (final Exception e) {
                                        try {
                                            int val = Math.round(Float.valueOf(task.getTaskTimeAvg()));
                                            listing.getWarningMessages().add(
                                                    msgUtil.getMessage("listing.criteria.roundedTestTaskNumber",
                                                            task.getUniqueId(), "Task Time Average",
                                                            task.getTaskTimeAvg(), String.valueOf(val)));
                                        } catch (final Exception ex) {
                                            listing.getErrorMessages().add(
                                                    msgUtil.getMessage("listing.criteria.badTestTaskNumber",
                                                            task.getUniqueId(), "Task Time Average", task.getTaskTimeAvg()));
                                        }
                                    }
                                }
                                if (task.getTaskTimeStddev() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestTaskTimeStddev",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                } else {
                                    try {
                                        Integer.valueOf(task.getTaskTimeStddev());
                                    } catch (final Exception e) {
                                        try {
                                            int val = Math.round(Float.valueOf(task.getTaskTimeStddev()));
                                            listing.getWarningMessages().add(
                                                    msgUtil.getMessage("listing.criteria.roundedTestTaskNumber",
                                                            task.getUniqueId(), "Task Time Standard Deviation",
                                                            task.getTaskTimeStddev(), String.valueOf(val)));
                                        } catch (final Exception ex) {
                                            listing.getErrorMessages().add(
                                                    msgUtil.getMessage("listing.criteria.badTestTaskNumber",
                                                            task.getUniqueId(), "Task Time Standard Deviation",
                                                            task.getTaskTimeStddev()));
                                        }
                                    }
                                }
                                if (task.getTaskTimeDeviationObservedAvg() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestTaskTimeDeviationObservedAvg",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                } else {
                                    try {
                                        Integer.valueOf(task.getTaskTimeDeviationObservedAvg());
                                    } catch (final Exception e) {
                                        try {
                                            int val = Math.round(Float.valueOf(task.getTaskTimeDeviationObservedAvg()));
                                            listing.getWarningMessages().add(
                                                    msgUtil.getMessage("listing.criteria.roundedTestTaskNumber",
                                                            task.getUniqueId(), "Task Time Deviation Observed Average",
                                                            task.getTaskTimeDeviationObservedAvg(), String.valueOf(val)));
                                        } catch (final Exception ex) {
                                            listing.getErrorMessages().add(
                                                    msgUtil.getMessage("listing.criteria.badTestTaskNumber",
                                                            task.getUniqueId(), "Task Time Deviation Observed Average",
                                                            task.getTaskTimeDeviationObservedAvg()));
                                        }
                                    }
                                }
                                if (task.getTaskTimeDeviationOptimalAvg() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestTaskTimeDeviationOptimalAvg",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                } else {
                                    try {
                                        Integer.valueOf(task.getTaskTimeDeviationOptimalAvg());
                                    } catch (final Exception e) {
                                        try {
                                            int val = Math.round(Float.valueOf(task.getTaskTimeDeviationOptimalAvg()));
                                            listing.getWarningMessages().add(
                                                    msgUtil.getMessage("listing.criteria.roundedTestTaskNumber",
                                                            task.getUniqueId(), "Task Time Deviation Optimal Average",
                                                            task.getTaskTimeDeviationOptimalAvg(), String.valueOf(val)));
                                        } catch (final Exception ex) {
                                            listing.getErrorMessages().add(
                                                    msgUtil.getMessage("listing.criteria.badTestTaskNumber",
                                                            task.getUniqueId(), "Task Time Deviation Optimal Average",
                                                            task.getTaskTimeDeviationOptimalAvg()));
                                        }
                                    }
                                }
                                if (task.getTaskErrors() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestTaskErrors",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                } else {
                                    try {
                                        Float.valueOf(task.getTaskErrors());
                                    } catch (final Exception e) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.badTestTaskNumber",
                                                        task.getUniqueId(), "Task Errors", task.getTaskErrors()));
                                    }
                                }
                                if (task.getTaskErrorsStddev() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestTaskErrorsStddev",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                } else {
                                    try {
                                        Float.valueOf(task.getTaskErrorsStddev());
                                    } catch (final Exception e) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.badTestTaskNumber",
                                                        task.getUniqueId(), "Task Errors Standard Deviation",
                                                        task.getTaskErrorsStddev()));
                                    }
                                }
                                if (StringUtils.isEmpty(task.getTaskRatingScale())) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestTaskRatingScale",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                }
                                if (task.getTaskRating() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestTaskRating",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                } else {
                                    try {
                                        Float.valueOf(task.getTaskRating());
                                    } catch (final Exception e) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.badTestTaskNumber",
                                                        task.getUniqueId(), "Task Rating", task.getTaskRating()));
                                    }
                                }
                                if (task.getTaskRatingStddev() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.missingTestTaskRatingStddev",
                                                    task.getUniqueId(),
                                                    Util.formatCriteriaNumber(cert.getCriterion())));
                                } else {
                                    try {
                                        Float.valueOf(task.getTaskRatingStddev());
                                    } catch (final Exception e) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.badTestTaskNumber",
                                                        task.getUniqueId(), "Task Rating Standard Deviation",
                                                        task.getTaskRatingStddev()));
                                    }
                                }
                                for (PendingCertificationResultTestTaskParticipantDTO part : certResultTask
                                        .getTaskParticipants()) {
                                    if (part.getTestParticipant().getEducationTypeId() == null) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.invalidParticipantEducationLevel",
                                                        (part.getTestParticipant().getUserEnteredEducationType() == null
                                                        ? "'unknown'"
                                                                : part.getTestParticipant()
                                                                .getUserEnteredEducationType()),
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (part.getTestParticipant().getAgeRangeId() == null) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.invalidParticipantAgeRange",
                                                        (part.getTestParticipant().getUserEnteredAgeRange() == null
                                                        ? "'unknown'"
                                                                : part.getTestParticipant().getUserEnteredAgeRange()),
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (StringUtils.isEmpty(part.getTestParticipant().getGender())) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.missingParticipantGender",
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (StringUtils.isEmpty(part.getTestParticipant().getOccupation())) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.missingParticipantOccupation",
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (StringUtils.isEmpty(part.getTestParticipant().getAssistiveTechnologyNeeds())) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.missingParticipantAssistiveTechnologyNeeds",
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (part.getTestParticipant().getProfessionalExperienceMonths() == null) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.missingParticipantProfessionalExperienceMonths",
                                                        part.getTestParticipant().getUniqueId()));
                                    } else {
                                        try {
                                            Integer.parseInt(part.getTestParticipant().getProfessionalExperienceMonths());
                                        } catch (Exception e) {
                                            try {
                                                int val = Math.round(Float.valueOf(part.getTestParticipant()
                                                        .getProfessionalExperienceMonths()));
                                                listing.getWarningMessages().add(
                                                        msgUtil.getMessage("listing.criteria.roundedParticipantNumber",
                                                                part.getTestParticipant().getUniqueId(),
                                                                "Professional Experience Months",
                                                                part.getTestParticipant().getProfessionalExperienceMonths(),
                                                                String.valueOf(val)));
                                            } catch (final Exception ex) {
                                                listing.getErrorMessages().add(
                                                        msgUtil.getMessage("listing.criteria.badParticipantNumber",
                                                                part.getTestParticipant().getUniqueId(),
                                                                "Professional Experience Months",
                                                                part.getTestParticipant().getProfessionalExperienceMonths()));
                                            }
                                        }
                                    }
                                    if (part.getTestParticipant().getProductExperienceMonths() == null) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.missingParticipantProductExperienceMonths",
                                                        part.getTestParticipant().getUniqueId()));
                                    } else {
                                        try {
                                            Integer.parseInt(part.getTestParticipant().getProductExperienceMonths());
                                        } catch (Exception e) {
                                            try {
                                                int val = Math.round(Float.valueOf(part.getTestParticipant()
                                                        .getProductExperienceMonths()));
                                                listing.getWarningMessages().add(
                                                        msgUtil.getMessage("listing.criteria.roundedParticipantNumber",
                                                                part.getTestParticipant().getUniqueId(),
                                                                "Product Experience Months",
                                                                part.getTestParticipant().getProductExperienceMonths(),
                                                                String.valueOf(val)));
                                            } catch (final Exception ex) {
                                                listing.getErrorMessages().add(
                                                        msgUtil.getMessage("listing.criteria.badParticipantNumber",
                                                                part.getTestParticipant().getUniqueId(),
                                                                "Product Experience Months",
                                                                part.getTestParticipant().getProductExperienceMonths()));
                                            }
                                        }
                                    }
                                    if (part.getTestParticipant().getComputerExperienceMonths() == null) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.missingParticipantComputerExperienceMonths",
                                                        part.getTestParticipant().getUniqueId()));
                                    } else {
                                        try {
                                            Integer.parseInt(part.getTestParticipant().getComputerExperienceMonths());
                                        } catch (Exception e) {
                                            try {
                                                int val = Math.round(Float.valueOf(part.getTestParticipant()
                                                        .getComputerExperienceMonths()));
                                                listing.getWarningMessages().add(
                                                        msgUtil.getMessage("listing.criteria.roundedParticipantNumber",
                                                                part.getTestParticipant().getUniqueId(),
                                                                "Computer Experience Months",
                                                                part.getTestParticipant().getComputerExperienceMonths(),
                                                                String.valueOf(val)));
                                            } catch (final Exception ex) {
                                                listing.getErrorMessages().add(
                                                        msgUtil.getMessage("listing.criteria.badParticipantNumber",
                                                                part.getTestParticipant().getUniqueId(),
                                                                "Computer Experience Months",
                                                                part.getTestParticipant().getComputerExperienceMonths()));
                                            }
                                        }
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
        validateG6Inverse(listing);
        validateH1PlusB1(listing);

        if (listing.getQmsStandards() == null || listing.getQmsStandards().size() == 0) {
            listing.getErrorMessages().add("QMS Standards are required.");
        } else {
            for (PendingCertifiedProductQmsStandardDTO qms : listing.getQmsStandards()) {
                if (StringUtils.isEmpty(qms.getApplicableCriteria())) {
                    listing.getErrorMessages().add("Applicable criteria is required for each QMS Standard listed.");
                }
            }
        }

        if (listing.getAccessibilityStandards() == null || listing.getAccessibilityStandards().size() == 0) {
            listing.getErrorMessages().add("Accessibility standards are required.");
        } // accessibility standards do not have to match the set list of
        // standards.

        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria()) {
                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.GAP)
                        && cert.getGap() != null && cert.getGap()) {
                    gapEligibleAndTrue = true;
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.ATTESTATION_ANSWER)
                        && cert.getAttestationAnswer() == null) {
                    addErrorIfCriterionIsNotRemoved(listing, cert,
                            "listing.criteria.missingAttestationAnswer",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.PRIVACY_SECURITY)
                        && StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    addErrorIfCriterionIsNotRemoved(listing, cert,
                            "listing.criteria.missingPrivacySecurityFramework",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.API_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getApiDocumentation())) {
                    addErrorIfCriterionIsNotRemoved(listing, cert, "listing.criteria.missingApiDocumentation",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.EXPORT_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getExportDocumentation())) {
                    addErrorIfCriterionIsNotRemoved(listing, cert, "listing.criteria.missingExportDocumentation",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.USE_CASES)
                        && StringUtils.isEmpty(cert.getUseCases())
                        && cert.getAttestationAnswer() != null && cert.getAttestationAnswer().equals(Boolean.TRUE)) {
                    addErrorIfCriterionIsNotRemoved(listing, cert, "listing.criteria.missingUseCases",
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
                    addErrorIfCriterionIsNotRemoved(listing, cert, "listing.criteria.missingServiceBaseUrlList",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                // jennifer asked to not make functionality tested be a required
                // field
                // if(certRules.hasCertOption(cert.getCriterion().getNumber(),
                // CertificationResultRules.FUNCTIONALITY_TESTED) &&
                // (cert.getTestFunctionality() == null ||
                // cert.getTestFunctionality().size() == 0)) {
                // listing.getErrorMessages().add("Functionality Tested is
                // required for certification " + cert.getCriterion().getNumber() + ".");
                // }

                //a test tool is required to exist if the criteria is not gap
                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.TEST_TOOLS_USED)
                        && (cert.getTestTools() == null || cert.getTestTools().size() == 0)) {
                    addErrorIfCriterionIsNotRemoved(listing, cert,
                            "listing.criteria.missingTestTool", Util.formatCriteriaNumber(cert.getCriterion()));
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)
                        && cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
                    Iterator<PendingCertificationResultTestFunctionalityDTO> crtfIter =
                            cert.getTestFunctionality().iterator();
                    while (crtfIter.hasNext()) {
                        PendingCertificationResultTestFunctionalityDTO crtf = crtfIter.next();
                        if (crtf.getTestFunctionalityId() == null) {
                            TestFunctionalityDTO foundTestFunc = testFuncDao.getByNumberAndEdition(
                                    crtf.getNumber(), listing.getCertificationEditionId());
                            if (foundTestFunc == null || foundTestFunc.getId() == null) {
                                addErrorIfCriterionIsNotRemoved(listing, cert,
                                        "listing.criteria.testFunctionalityNotFoundAndRemoved",
                                        Util.formatCriteriaNumber(cert.getCriterion()), crtf.getNumber());
                                crtfIter.remove();
                            }
                        }
                    }
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.TEST_PROCEDURE)
                        && cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0) {
                    for (PendingCertificationResultTestProcedureDTO crTestProc : cert.getTestProcedures()) {
                        if (crTestProc.getTestProcedure() == null && crTestProc.getTestProcedureId() == null) {
                            addErrorIfCriterionIsNotRemoved(listing, cert, "listing.criteria.badTestProcedureName",
                                    Util.formatCriteriaNumber(cert.getCriterion()), crTestProc.getEnteredName());
                        } else if (crTestProc.getTestProcedure() != null && crTestProc.getTestProcedure().getId() == null) {
                            TestProcedureDTO foundTestProc =
                                    testProcDao.getByCriterionIdAndValue(cert.getCriterion().getId(),
                                            crTestProc.getTestProcedure().getName());
                            if (foundTestProc == null || foundTestProc.getId() == null) {
                                addErrorIfCriterionIsNotRemoved(listing, cert, "listing.criteria.badTestProcedureName",
                                        Util.formatCriteriaNumber(cert.getCriterion()), crTestProc.getTestProcedure().getName());
                            } else {
                                crTestProc.getTestProcedure().setId(foundTestProc.getId());
                            }
                        }

                        if ((!StringUtils.isEmpty(crTestProc.getEnteredName()) || crTestProc.getTestProcedure() != null) && StringUtils.isEmpty(crTestProc.getVersion())) {
                            addErrorIfCriterionIsNotRemoved(listing, cert, "listing.criteria.missingTestProcedureVersion",
                                    Util.formatCriteriaNumber(cert.getCriterion()));
                        }
                    }
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.TEST_DATA)
                        && cert.getTestData() != null && cert.getTestData().size() > 0) {
                    for (PendingCertificationResultTestDataDTO crTestData : cert.getTestData()) {
                        if (crTestData.getTestData() == null && crTestData.getTestDataId() == null) {
                            listing.getWarningMessages().add(
                                    msgUtil.getMessage("listing.criteria.badTestDataName",
                                            crTestData.getEnteredName(),
                                            Util.formatCriteriaNumber(cert.getCriterion()),
                                            TestDataDTO.DEFALUT_TEST_DATA));
                            TestDataDTO foundTestData = testDataDao.getByCriterionAndValue(
                                    cert.getCriterion().getId(), TestDataDTO.DEFALUT_TEST_DATA);
                            crTestData.setTestData(foundTestData);
                        } else if (crTestData.getTestData() != null && crTestData.getTestData().getId() == null) {
                            TestDataDTO foundTestData =
                                    testDataDao.getByCriterionAndValue(cert.getCriterion().getId(), crTestData.getTestData().getName());
                            if (foundTestData == null || foundTestData.getId() == null) {
                                listing.getWarningMessages().add(
                                        msgUtil.getMessage("listing.criteria.badTestDataName",
                                                crTestData.getTestData().getName(),
                                                Util.formatCriteriaNumber(cert.getCriterion()),
                                                TestDataDTO.DEFALUT_TEST_DATA));
                            } else {
                                crTestData.getTestData().setId(foundTestData.getId());
                            }
                        }

                        if ((!StringUtils.isEmpty(crTestData.getEnteredName()) || crTestData.getTestData() != null) && StringUtils.isEmpty(crTestData.getVersion())) {
                            addErrorIfCriterionIsNotRemoved(listing, cert,
                                    "listing.criteria.missingTestDataVersion",
                                    Util.formatCriteriaNumber(cert.getCriterion()));
                        }
                    }
                }

                // g1 and g2 are the only criteria that ONC supplies test data for
                // other criteria probably should have test data but do not have to
                if (!gapEligibleAndTrue
                        && (cert.getCriterion().getNumber().equals("170.315 (g)(1)") || cert.getCriterion().getNumber().equals("170.315 (g)(2)"))
                        && (cert.getTestData() == null || cert.getTestData().size() == 0)) {
                    listing.getErrorMessages().add("Test Data is required for certification "
                        + Util.formatCriteriaNumber(cert.getCriterion()) + ".");
                }
            }
        }

        if (listing.getIcs() == null) {
            listing.getErrorMessages().add("ICS is required.");
        }
    }

    private void validateH1PlusB1(PendingCertifiedProductDTO listing) {
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

    private void validateG3(PendingCertifiedProductDTO listing) {
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

    private void validateG3Inverse(PendingCertifiedProductDTO listing) {
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

    private void validateG6(PendingCertifiedProductDTO listing) {
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

    private void validateG6Inverse(PendingCertifiedProductDTO listing) {
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
        if ((presentAttestedG6Criteria == null || presentAttestedG6Criteria.size() == 0)
                && (removedAttestedG6Criteria == null || removedAttestedG6Criteria.size() == 0)
                && hasG6) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.g6NotAllowed", g6Numbers));
        }
        if (removedAttestedG6Criteria != null && removedAttestedG6Criteria.size() > 0
                && (presentAttestedG6Criteria == null || presentAttestedG6Criteria.size() == 0)
                && hasG6) {
            addListingWarningByPermission(listing, msgUtil.getMessage("listing.g6NotAllowed", g6Numbers));
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
