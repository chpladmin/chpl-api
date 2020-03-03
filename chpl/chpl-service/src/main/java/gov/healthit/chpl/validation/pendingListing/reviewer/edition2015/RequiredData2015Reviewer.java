package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingTestTaskDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
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

    private static final String G1_CRITERIA_NUMBER = "170.315 (g)(1)";
    private static final String G2_CRITERIA_NUMBER = "170.315 (g)(2)";
    private static final int MINIMIMUM_PARTICIPANTS = 10;
    private List<String> e2e3Criterion = new ArrayList<String>();
    private List<String> g7g8g9Criterion = new ArrayList<String>();
    private List<String> d2d10Criterion = new ArrayList<String>();

    private MacraMeasureDAO macraDao;
    private TestFunctionalityDAO testFuncDao;
    private TestProcedureDAO testProcDao;
    private TestDataDAO testDataDao;

    @Autowired
    public RequiredData2015Reviewer(MacraMeasureDAO macraDao, TestFunctionalityDAO testFuncDao,
            TestProcedureDAO testProcDao, TestDataDAO testDataDao, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions, CertificationResultRules certRules) {
        super(msgUtil, resourcePermissions, certRules);

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
    public void review(final PendingCertifiedProductDTO listing) {
        super.review(listing);

        List<CertificationCriterion> attestedCriteria = ValidationUtils.getAttestedCriteria(listing);
        List<String> errors = ValidationUtils.checkClassOfCriteriaForErrors("170.315 (a)", attestedCriteria,
                Arrays.asList(A_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        List<String> warnings = ValidationUtils.checkClassOfCriteriaForWarnings("170.315 (a)", attestedCriteria,
                Arrays.asList(A_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        errors = ValidationUtils.checkClassOfCriteriaForErrors("170.315 (b)", attestedCriteria,
                Arrays.asList(B_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        warnings = ValidationUtils.checkClassOfCriteriaForWarnings("170.315 (b)", attestedCriteria,
                Arrays.asList(B_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        errors = ValidationUtils.checkClassOfCriteriaForErrors("170.315 (c)", attestedCriteria,
                Arrays.asList(C_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        warnings = ValidationUtils.checkClassOfCriteriaForWarnings("170.315 (c)", attestedCriteria,
                Arrays.asList(C_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        errors = ValidationUtils.checkClassOfCriteriaForErrors("170.315 (f)", attestedCriteria,
                Arrays.asList(F_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        warnings = ValidationUtils.checkClassOfCriteriaForWarnings("170.315 (f)", attestedCriteria,
                Arrays.asList(F_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        errors = ValidationUtils.checkClassOfCriteriaForErrors("170.315 (h)", attestedCriteria,
                Arrays.asList(H_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);
        warnings = ValidationUtils.checkClassOfCriteriaForWarnings("170.315 (h)", attestedCriteria,
                Arrays.asList(H_RELATED_CERTS));
        addListingWarningsByPermission(listing, warnings);

        errors = ValidationUtils.checkSpecificCriteriaForErrors("170.315 (e)(1)", attestedCriteria,
                Arrays.asList(E1_RELATED_CERTS));
        listing.getErrorMessages().addAll(errors);

        // check for (e)(2) or (e)(3) required complimentary certs
        List<String> e2e3ComplimentaryErrors =
                ValidationUtils.checkComplimentaryCriteriaAllRequired(e2e3Criterion,
                        Arrays.asList(E2E3_RELATED_CERTS), attestedCriteria);
        listing.getErrorMessages().addAll(e2e3ComplimentaryErrors);

        // check for (g)(7) or (g)(8) or (g)(9) required complimentary certs
        List<String> g7g8g9ComplimentaryErrors =
                ValidationUtils.checkComplimentaryCriteriaAllRequired(g7g8g9Criterion,
                        Arrays.asList(G7G8G9_RELATED_CERTS), attestedCriteria);
        listing.getErrorMessages().addAll(g7g8g9ComplimentaryErrors);

        //if g7, g8, or g9 is found then one of d2 or d10 is required
        g7g8g9ComplimentaryErrors =
                ValidationUtils.checkComplimentaryCriteriaAnyRequired(g7g8g9Criterion, d2d10Criterion, attestedCriteria);
        listing.getErrorMessages().addAll(g7g8g9ComplimentaryErrors);

        //g1 macra check
        if (ValidationUtils.hasCert(G1_CRITERIA_NUMBER, attestedCriteria)) {
            //must have at least one criteria with g1 macras listed
            boolean hasG1Macra = false;
            for (int i = 0; i < listing.getCertificationCriterion().size() && !hasG1Macra; i++) {
                PendingCertificationResultDTO cert = listing.getCertificationCriterion().get(i);
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.G1_MACRA)
                        && cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
                    hasG1Macra = true;
                }
            }

            if (!hasG1Macra) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.missingG1Macras"));
            }
        }

        //g2 macra check
        if (ValidationUtils.hasCert(G2_CRITERIA_NUMBER, attestedCriteria)) {
            //must have at least one criteria with g2 macras listed
            boolean hasG2Macra = false;
            for (int i = 0; i < listing.getCertificationCriterion().size() && !hasG2Macra; i++) {
                PendingCertificationResultDTO cert = listing.getCertificationCriterion().get(i);
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.G2_MACRA)
                        && cert.getG2MacraMeasures() != null && cert.getG2MacraMeasures().size() > 0) {
                    hasG2Macra = true;
                }
            }

            if (!hasG2Macra) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.missingG2Macras"));
            }
        }

        // g3 checks
        for (int i = 0; i < UCD_RELATED_CERTS.length; i++) {
            if (ValidationUtils.hasCert(UCD_RELATED_CERTS[i], attestedCriteria)) {
                // check for full set of UCD data
                for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
                    if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria().equals(Boolean.TRUE)
                            && cert.getCriterion().getNumber().equals(UCD_RELATED_CERTS[i])) {
                        if (cert.getUcdProcesses() == null || cert.getUcdProcesses().size() == 0) {
                            addErrorOrWarningByPermission(listing, cert, "listing.criteria.missingUcdProcess",
                                    cert.getCriterion().getNumber());
                        }
                        if (cert.getTestTasks() == null || cert.getTestTasks().size() == 0) {
                            addErrorOrWarningByPermission(listing, cert, "listing.criteria.missingTestTask",
                                    cert.getCriterion().getNumber());
                        }

                        if (cert.getTestTasks() != null) {
                            for (PendingCertificationResultTestTaskDTO certResultTask : cert.getTestTasks()) {
                                PendingTestTaskDTO task = certResultTask.getPendingTestTask();
                                if (certResultTask.getTaskParticipants() == null
                                        || certResultTask.getTaskParticipants().size() < MINIMIMUM_PARTICIPANTS) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.badTestTaskParticipantsSize",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
                                }
                                if (StringUtils.isEmpty(task.getDescription())) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.badTestDescription",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
                                }
                                if (task.getTaskSuccessAverage() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.badTestTaskSuccessAverage",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
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
                                            msgUtil.getMessage("listing.criteria.badTestTaskSuccessStddev",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
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
                                            msgUtil.getMessage("listing.criteria.badTestTaskPathDeviationObserved",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
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
                                            msgUtil.getMessage("listing.criteria.badTestTaskPathDeviationOptimal",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
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
                                            msgUtil.getMessage("listing.criteria.badTestTaskTimeAvg",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
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
                                            msgUtil.getMessage("listing.criteria.badTestTaskTimeStddev",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
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
                                            msgUtil.getMessage("listing.criteria.badTestTaskTimeDeviationObservedAvg",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
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
                                            msgUtil.getMessage("listing.criteria.badTestTaskTimeDeviationOptimalAvg",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
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
                                            msgUtil.getMessage("listing.criteria.badTestTaskErrors",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
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
                                            msgUtil.getMessage("listing.criteria.badTestTaskErrorsStddev",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
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
                                            msgUtil.getMessage("listing.criteria.badTestTaskRatingScale",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
                                }
                                if (task.getTaskRating() == null) {
                                    listing.getErrorMessages().add(
                                            msgUtil.getMessage("listing.criteria.badTestTaskRating",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
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
                                            msgUtil.getMessage("listing.criteria.badTestTaskRatingStddev",
                                                    task.getUniqueId(), cert.getCriterion().getNumber()));
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
                                                msgUtil.getMessage("listing.criteria.badParticipantEducationLevel",
                                                        (part.getTestParticipant().getUserEnteredEducationType() == null
                                                        ? "'unknown'"
                                                                : part.getTestParticipant()
                                                                .getUserEnteredEducationType()),
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (part.getTestParticipant().getAgeRangeId() == null) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.badParticipantAgeRange",
                                                        (part.getTestParticipant().getUserEnteredAgeRange() == null
                                                        ? "'unknown'"
                                                                : part.getTestParticipant().getUserEnteredAgeRange()),
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (StringUtils.isEmpty(part.getTestParticipant().getGender())) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.badParticipantGender",
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (StringUtils.isEmpty(part.getTestParticipant().getOccupation())) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.badParticipantOccupation",
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (StringUtils.isEmpty(part.getTestParticipant().getAssistiveTechnologyNeeds())) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.badParticipantAssistiveTechnologyNeeds",
                                                        part.getTestParticipant().getUniqueId()));
                                    }
                                    if (part.getTestParticipant().getProfessionalExperienceMonths() == null) {
                                        listing.getErrorMessages().add(
                                                msgUtil.getMessage("listing.criteria.badParticipantProfessionalExperienceMonths",
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
                                                msgUtil.getMessage("listing.criteria.badParticipantProductExperienceMonths",
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
                                                msgUtil.getMessage("listing.criteria.badParticipantComputerExperienceMonths",
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

        // g4 check
        boolean hasG4 = ValidationUtils.hasCert("170.315 (g)(4)", attestedCriteria);
        if (!hasG4) {
            listing.getErrorMessages().add("170.315 (g)(4) is required but was not found.");
        }

        // g5 check
        boolean hasG5 = ValidationUtils.hasCert("170.315 (g)(5)", attestedCriteria);
        if (!hasG5) {
            listing.getErrorMessages().add("170.315 (g)(5) is required but was not found.");
        }

        // TODO: detailed G6 check; waiting on rule from ONC

        // h1 plus b1
        boolean hasH1 = ValidationUtils.hasCert("170.315 (h)(1)", attestedCriteria);
        if (hasH1) {
            boolean hasB1 = ValidationUtils.hasCert("170.315 (b)(1)", attestedCriteria);
            if (!hasB1) {
                listing.getErrorMessages()
                .add("170.315 (h)(1) was found so 170.315 (b)(1) is required but was not found.");
            }
        }

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
                    addErrorOrWarningByPermission(listing, cert,
                            "listing.criteria.missingAttestationAnswer", cert.getCriterion().getNumber());
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.PRIVACY_SECURITY)
                        && StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    addErrorOrWarningByPermission(listing, cert,
                            "listing.criteria.missingPrivacySecurityFramework", cert.getCriterion().getNumber());
                }
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.API_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getApiDocumentation())) {
                    addErrorOrWarningByPermission(listing, cert, "listing.criteria.missingApiDocumentation",
                            cert.getCriterion().getNumber());
                }
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.EXPORT_DOCUMENTATION)
                        && StringUtils.isEmpty(cert.getExportDocumentation())) {
                    addErrorOrWarningByPermission(listing, cert, "listing.criteria.missingExportDocumentation",
                            cert.getCriterion().getNumber());
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.USE_CASES)
                        && StringUtils.isEmpty(cert.getUseCases())
                        && cert.getAttestationAnswer() != null && cert.getAttestationAnswer().equals(Boolean.TRUE)) {
                    addErrorOrWarningByPermission(listing, cert, "listing.criteria.missingUseCases",
                            cert.getCriterion().getNumber());
                } else if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.USE_CASES)
                        && !StringUtils.isEmpty(cert.getUseCases())
                        && (cert.getAttestationAnswer() == null || cert.getAttestationAnswer().equals(Boolean.FALSE))) {
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.useCasesWithoutAttestation",
                            cert.getCriterion().getNumber()));
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
                    addErrorOrWarningByPermission(listing, cert,
                            "listing.criteria.missingTestTool", cert.getCriterion().getNumber());
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
                                addErrorOrWarningByPermission(listing, cert,
                                        "listing.criteria.testFunctionalityNotFoundAndRemoved",
                                        cert.getCriterion().getNumber(), crtf.getNumber());
                                crtfIter.remove();
                            }
                        }
                    }
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.TEST_PROCEDURE)
                        && cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0) {
                    for (PendingCertificationResultTestProcedureDTO crTestProc : cert.getTestProcedures()) {
                        if (crTestProc.getTestProcedure() == null && crTestProc.getTestProcedureId() == null) {
                            addErrorOrWarningByPermission(listing, cert, "listing.criteria.badTestProcedureName",
                                    cert.getCriterion().getNumber(), crTestProc.getEnteredName());
                        } else if (crTestProc.getTestProcedure() != null && crTestProc.getTestProcedure().getId() == null) {
                            TestProcedureDTO foundTestProc =
                                    testProcDao.getByCriteriaNumberAndValue(cert.getCriterion().getNumber(),
                                            crTestProc.getTestProcedure().getName());
                            if (foundTestProc == null || foundTestProc.getId() == null) {
                                addErrorOrWarningByPermission(listing, cert, "listing.criteria.badTestProcedureName",
                                        cert.getCriterion().getNumber(), crTestProc.getTestProcedure().getName());
                            } else {
                                crTestProc.getTestProcedure().setId(foundTestProc.getId());
                            }
                        }

                        if (!StringUtils.isEmpty(crTestProc.getEnteredName()) && StringUtils.isEmpty(crTestProc.getVersion())) {
                            addErrorOrWarningByPermission(listing, cert, "listing.criteria.missingTestProcedureVersion",
                                    cert.getCriterion().getNumber());
                        }
                    }
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.TEST_DATA)
                        && cert.getTestData() != null && cert.getTestData().size() > 0) {
                    for (PendingCertificationResultTestDataDTO crTestData : cert.getTestData()) {
                        if (crTestData.getTestData() == null && crTestData.getTestDataId() == null) {
                            listing.getWarningMessages().add(
                                    msgUtil.getMessage("listing.criteria.badTestDataName",
                                            crTestData.getEnteredName(), cert.getCriterion().getNumber(), TestDataDTO.DEFALUT_TEST_DATA));
                            TestDataDTO foundTestData =
                                    testDataDao.getByCriteriaNumberAndValue(cert.getCriterion().getNumber(), TestDataDTO.DEFALUT_TEST_DATA);
                            crTestData.setTestData(foundTestData);
                        } else if (crTestData.getTestData() != null && crTestData.getTestData().getId() == null) {
                            TestDataDTO foundTestData =
                                    testDataDao.getByCriteriaNumberAndValue(cert.getCriterion().getNumber(), crTestData.getTestData().getName());
                            if (foundTestData == null || foundTestData.getId() == null) {
                                listing.getWarningMessages().add(
                                        msgUtil.getMessage("listing.criteria.badTestDataName",
                                                crTestData.getTestData().getName(), cert.getCriterion().getNumber(),
                                                TestDataDTO.DEFALUT_TEST_DATA));
                                foundTestData =
                                        testDataDao.getByCriteriaNumberAndValue(cert.getCriterion().getNumber(), TestDataDTO.DEFALUT_TEST_DATA);
                                crTestData.getTestData().setId(foundTestData.getId());
                            } else {
                                crTestData.getTestData().setId(foundTestData.getId());
                            }
                        }

                        if (!StringUtils.isEmpty(crTestData.getEnteredName()) && StringUtils.isEmpty(crTestData.getVersion())) {
                            addErrorOrWarningByPermission(listing, cert,
                                    "listing.criteria.missingTestDataVersion", cert.getCriterion().getNumber());
                        }
                    }
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.G1_MACRA)
                        && cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
                    for (PendingCertificationResultMacraMeasureDTO pendingMeasureMap : cert.getG1MacraMeasures()) {
                        if (pendingMeasureMap.getMacraMeasureId() == null) {
                            MacraMeasureDTO foundMeasure = macraDao.getByCriteriaNumberAndValue(cert.getCriterion().getNumber(),
                                    pendingMeasureMap.getEnteredValue());
                            if (foundMeasure == null || foundMeasure.getId() == null) {
                                listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.invalidG1MacraMeasure",
                                        cert.getCriterion().getNumber(), pendingMeasureMap.getEnteredValue()));
                            } else {
                                pendingMeasureMap.setMacraMeasure(foundMeasure);
                            }
                        } else if (pendingMeasureMap.getMacraMeasure() == null) {
                            MacraMeasureDTO foundMeasure = macraDao.getById(pendingMeasureMap.getMacraMeasureId());
                            if (foundMeasure == null || foundMeasure.getId() == null) {
                                listing.getErrorMessages().add(
                                        msgUtil.getMessage("listing.criteria.invalidG1MacraMeasure",
                                                cert.getCriterion().getNumber(), pendingMeasureMap.getEnteredValue()));
                            } else {
                                pendingMeasureMap.setMacraMeasure(foundMeasure);
                            }
                        }

                        if (pendingMeasureMap.getMacraMeasure() != null
                                && (listing.getIcs() == null || !listing.getIcs().booleanValue())
                                && pendingMeasureMap.getMacraMeasure().getRemoved() != null
                                && pendingMeasureMap.getMacraMeasure().getRemoved().booleanValue()) {
                            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.removedG1MacraMeasureNoIcs",
                                    cert.getCriterion().getNumber(), pendingMeasureMap.getMacraMeasure().getValue()));
                        }
                    }
                }

                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.G2_MACRA)
                        && cert.getG2MacraMeasures() != null && cert.getG2MacraMeasures().size() > 0) {
                    for (PendingCertificationResultMacraMeasureDTO pendingMeasureMap : cert.getG2MacraMeasures()) {
                        if (pendingMeasureMap.getMacraMeasureId() == null) {
                            MacraMeasureDTO foundMeasure = macraDao.getByCriteriaNumberAndValue(cert.getCriterion().getNumber(),
                                    pendingMeasureMap.getEnteredValue());
                            if (foundMeasure == null || foundMeasure.getId() == null) {
                                listing.getErrorMessages().add(
                                        msgUtil.getMessage("listing.criteria.invalidG2MacraMeasure",
                                                cert.getCriterion().getNumber(), pendingMeasureMap.getEnteredValue()));
                            } else {
                                pendingMeasureMap.setMacraMeasure(foundMeasure);
                            }
                        } else if (pendingMeasureMap.getMacraMeasure() == null) {
                            MacraMeasureDTO foundMeasure = macraDao.getById(pendingMeasureMap.getMacraMeasureId());
                            if (foundMeasure == null || foundMeasure.getId() == null) {
                                listing.getErrorMessages().add(
                                        msgUtil.getMessage("listing.criteria.invalidG2MacraMeasure",
                                                cert.getCriterion().getNumber(), pendingMeasureMap.getEnteredValue()));
                            } else {
                                pendingMeasureMap.setMacraMeasure(foundMeasure);
                            }
                        }

                        if (pendingMeasureMap.getMacraMeasure() != null
                                && (listing.getIcs() == null || !listing.getIcs().booleanValue())
                                && pendingMeasureMap.getMacraMeasure().getRemoved() != null
                                && pendingMeasureMap.getMacraMeasure().getRemoved().booleanValue()) {
                            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.removedG2MacraMeasureNoIcs",
                                    cert.getCriterion().getNumber(), pendingMeasureMap.getMacraMeasure().getValue()));
                        }
                    }
                }

                // g1 and g2 are the only criteria that ONC supplies test data for
                // other criteria probably should have test data but do not have to
                if (!gapEligibleAndTrue
                        && (cert.getCriterion().getNumber().equals("170.315 (g)(1)") || cert.getCriterion().getNumber().equals("170.315 (g)(2)"))
                        && (cert.getTestData() == null || cert.getTestData().size() == 0)) {
                    listing.getErrorMessages().add("Test Data is required for certification " + cert.getCriterion().getNumber() + ".");
                }
            }
        }

        if (listing.getIcs() == null) {
            listing.getErrorMessages().add("ICS is required.");
        }
    }

    private void validateG3(PendingCertifiedProductDTO listing) {
        List<CertificationCriterion> attestedCriteria = ValidationUtils.getAttestedCriteria(listing);
        List<CertificationCriterion> presentAttestedUcdCriteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() == null || cert.getRemoved().equals(Boolean.FALSE))
                .filter(cert -> certNumberIsInCertList(cert, UCD_RELATED_CERTS))
                .collect(Collectors.<CertificationCriterion>toList());
        List<CertificationCriterion> removedAttestedUcdCriteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() != null && cert.getRemoved().equals(Boolean.TRUE))
                .filter(cert -> certNumberIsInCertList(cert, UCD_RELATED_CERTS))
                .collect(Collectors.<CertificationCriterion>toList());
        boolean hasG3 = ValidationUtils.hasCert("170.315 (g)(3)", attestedCriteria);

        String msg = "170.315 (g)(3) is required but was not found.";
        if (presentAttestedUcdCriteria != null && presentAttestedUcdCriteria.size() > 0 && !hasG3) {
            listing.getErrorMessages().add(msg);
        }
        if (removedAttestedUcdCriteria != null && removedAttestedUcdCriteria.size() > 0
                && (presentAttestedUcdCriteria == null || presentAttestedUcdCriteria.size() == 0)
                && !hasG3) {
            addListingWarningByPermission(listing, msg);
        }
    }

    private void validateG3Inverse(PendingCertifiedProductDTO listing) {
        List<CertificationCriterion> attestedCriteria = ValidationUtils.getAttestedCriteria(listing);
        List<CertificationCriterion> presentAttestedUcdCriteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() == null || cert.getRemoved().equals(Boolean.FALSE))
                .filter(cert -> certNumberIsInCertList(cert, UCD_RELATED_CERTS))
                .collect(Collectors.<CertificationCriterion>toList());
        boolean hasG3 = ValidationUtils.hasCert("170.315 (g)(3)", attestedCriteria);

        String msg = "170.315 (g)(3) is not allowed but was found.";
        if ((presentAttestedUcdCriteria == null || presentAttestedUcdCriteria.size() == 0)
                && hasG3) {
            listing.getErrorMessages().add(msg);
        }
    }

    private void validateG6(PendingCertifiedProductDTO listing) {
        List<CertificationCriterion> attestedCriteria = ValidationUtils.getAttestedCriteria(listing);
        List<CertificationCriterion> presentAttestedG6Criteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() == null || cert.getRemoved().equals(Boolean.FALSE))
                .filter(cert -> certNumberIsInCertList(cert, CERTS_REQUIRING_G6))
                .collect(Collectors.<CertificationCriterion>toList());
        List<CertificationCriterion> removedAttestedG6Criteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() != null && cert.getRemoved().equals(Boolean.TRUE))
                .filter(cert -> certNumberIsInCertList(cert, CERTS_REQUIRING_G6))
                .collect(Collectors.<CertificationCriterion>toList());
        boolean hasG6 = ValidationUtils.hasCert("170.315 (g)(6)", attestedCriteria);

        String msg = "170.315 (g)(6) is required but was not found.";
        if (presentAttestedG6Criteria != null && presentAttestedG6Criteria.size() > 0 && !hasG6) {
            listing.getErrorMessages().add(msg);
        }
        if (removedAttestedG6Criteria != null && removedAttestedG6Criteria.size() > 0
                && (presentAttestedG6Criteria == null || presentAttestedG6Criteria.size() == 0)
                && !hasG6) {
            addListingWarningByPermission(listing, msg);
        }
    }

    private void validateG6Inverse(PendingCertifiedProductDTO listing) {
        List<CertificationCriterion> attestedCriteria = ValidationUtils.getAttestedCriteria(listing);
        List<CertificationCriterion> presentAttestedG6Criteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() == null || cert.getRemoved().equals(Boolean.FALSE))
                .filter(cert -> certNumberIsInCertList(cert, CERTS_REQUIRING_G6))
                .collect(Collectors.<CertificationCriterion>toList());
        List<CertificationCriterion> removedAttestedG6Criteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() != null && cert.getRemoved().equals(Boolean.TRUE))
                .filter(cert -> certNumberIsInCertList(cert, CERTS_REQUIRING_G6))
                .collect(Collectors.<CertificationCriterion>toList());
        boolean hasG6 = ValidationUtils.hasCert("170.315 (g)(6)", attestedCriteria);

        String msg = "170.315 (g)(6) was found but a related required cert was not found.";
        if ((presentAttestedG6Criteria == null || presentAttestedG6Criteria.size() == 0)
                && (removedAttestedG6Criteria == null || removedAttestedG6Criteria.size() == 0)
                && hasG6) {
            listing.getErrorMessages().add(msg);
        }
        if (removedAttestedG6Criteria != null && removedAttestedG6Criteria.size() > 0
                && (presentAttestedG6Criteria == null || presentAttestedG6Criteria.size() == 0)
                && hasG6) {
            addListingWarningByPermission(listing, msg);
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
}
