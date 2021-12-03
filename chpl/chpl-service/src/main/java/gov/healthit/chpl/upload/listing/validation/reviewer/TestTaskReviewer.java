package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

@Component("listingUploadTestTaskReviewer")
public class TestTaskReviewer {
    private static final String DEFAULT_TASK_DECRIPTION = "<unknown>";
    private static final String DEFAULT_TASK_CRITERIA = "<none>";
    private static final int MINIMUM_TEST_PARTICIPANT_COUNT = 10;

    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private List<CertificationCriterion> testTaskCriteria = new ArrayList<CertificationCriterion>();

    @Autowired
    public TestTaskReviewer(CertificationCriterionService criterionService,
            ValidationUtils validationUtils,
            CertificationResultRules certResultRules,
            @Value("${sedCriteria}") String testTaskCriteria,
            ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;

        this.testTaskCriteria = Arrays.asList(testTaskCriteria.split(",")).stream()
                .map(id -> criterionService.get(Long.parseLong(id)))
                .collect(Collectors.toList());
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getSed() == null) {
            return;
        }
        reviewAllTestTaskCriteriaAreAllowed(listing);
        reviewCertResultsHaveTestTasksIfRequired(listing);
        reviewTestTaskFields(listing);
    }

    private void reviewAllTestTaskCriteriaAreAllowed(CertifiedProductSearchDetails listing) {
        if (listing.getSed() != null && !CollectionUtils.isEmpty(listing.getSed().getTestTasks())) {
            listing.getSed().getTestTasks().stream()
                .filter(testTask -> !CollectionUtils.isEmpty(testTask.getCriteria()))
                .flatMap(testTask -> testTask.getCriteria().stream())
                .filter(testTaskCriterion -> !certResultRules.hasCertOption(testTaskCriterion.getNumber(), CertificationResultRules.TEST_TASK))
                .filter(testTaskCriterion -> BooleanUtils.isFalse(testTaskCriterion.getRemoved()))
                .forEach(notAllowedTestTaskCriterion ->
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.testTasksNotApplicable", Util.formatCriteriaNumber(notAllowedTestTaskCriterion))));

            listing.getSed().getTestTasks().stream()
                .filter(testTask -> !CollectionUtils.isEmpty(testTask.getCriteria()))
                .flatMap(testTask -> testTask.getCriteria().stream())
                .filter(testTaskCriterion -> !doesListingAttestToCriterion(listing, testTaskCriterion))
                .filter(testTaskCriterion -> BooleanUtils.isFalse(testTaskCriterion.getRemoved()))
                .forEach(notAllowedTestTaskCriterion ->
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.testTasksNotApplicable", Util.formatCriteriaNumber(notAllowedTestTaskCriterion))));
        }
    }

    private boolean doesListingAttestToCriterion(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getCertificationResults().stream()
            .filter(certResult -> certResult.getCriterion() != null && BooleanUtils.isTrue(certResult.isSuccess())
                && certResult.getCriterion().getId().equals(criterion.getId()))
            .count() > 0;
    }

    private void reviewCertResultsHaveTestTasksIfRequired(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);

        testTaskCriteria.stream()
            .filter(criterion -> validationUtils.hasCriterion(criterion, attestedCriteria))
            .map(attestedTestTaskCriterion -> getCertificationResultForCriterion(listing, attestedTestTaskCriterion))
            .filter(certResult -> certResult != null && validationUtils.isEligibleForErrors(certResult))
            .forEach(certResult -> reviewCertResultHasTestTasksIfRequired(listing, certResult));
    }

    private CertificationResult getCertificationResultForCriterion(CertifiedProductSearchDetails listing, CertificationCriterion criterionToReview) {
        Optional<CertificationResult> certResultToReviewOpt = listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion().getId().equals(criterionToReview.getId()))
                .findAny();
        if (certResultToReviewOpt.isPresent()) {
            return certResultToReviewOpt.get();
        }
        return null;
    }

    private void reviewCertResultHasTestTasksIfRequired(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.isSed()) {
            if (listing.getSed() == null || CollectionUtils.isEmpty(listing.getSed().getTestTasks())) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingTestTask",
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            } else if (!doesTestTaskListContainCriterion(listing, certResult.getCriterion())) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingTestTask",
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private boolean doesTestTaskListContainCriterion(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getSed().getTestTasks().stream()
                .filter(testTask -> !CollectionUtils.isEmpty(testTask.getCriteria()))
                .flatMap(testTask -> testTask.getCriteria().stream())
                .filter(testTaskCriterion -> testTaskCriterion.getId().equals(criterion.getId()))
                .count() > 0;
    }

    private void reviewTestTaskFields(CertifiedProductSearchDetails listing) {
        if (listing.getSed() == null || CollectionUtils.isEmpty(listing.getSed().getTestTasks())) {
            return;
        }
        listing.getSed().getTestTasks().stream()
            .forEach(testTask -> reviewTaskCriteria(listing, testTask));

        listing.getSed().getTestTasks().stream()
            .filter(testTask -> doesTestTaskHaveNonRemovedCriteria(testTask))
            .forEach(testTask -> reviewTestTaskFields(listing, testTask));

    }

    private boolean doesTestTaskHaveNonRemovedCriteria(TestTask testTask) {
        if (CollectionUtils.isEmpty(testTask.getCriteria())) {
            return false;
        }

        return testTask.getCriteria().stream()
                .filter(criterion -> BooleanUtils.isFalse(criterion.getRemoved()))
                .findAny().isPresent();
    }

    private void reviewTestTaskFields(CertifiedProductSearchDetails listing, TestTask testTask) {
        reviewTaskUniqueId(listing, testTask);
        reviewTaskParticipantSize(listing, testTask);
        reviewTaskDescription(listing, testTask);
        reviewTaskSuccessAverage(listing, testTask);
        reviewTaskSuccessStddev(listing, testTask);
        reviewTaskPathDeviationObserved(listing, testTask);
        reviewTaskPathDeviationOptimal(listing, testTask);
        reviewTaskTimeAverage(listing, testTask);
        reviewTaskTimeStddev(listing, testTask);
        reviewTaskTimeDeviationObservedAvg(listing, testTask);
        reviewTaskTimeDeviationOptimalAvg(listing, testTask);
        reviewTestTaskErrors(listing, testTask);
        reviewTestTaskErrorsStddev(listing, testTask);
        reviewTestTaskRatingScale(listing, testTask);
        reviewTestTaskRating(listing, testTask);
        reviewTestTaskRatingStddev(listing, testTask);
    }

    private void reviewTaskUniqueId(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (StringUtils.isEmpty(testTask.getUniqueId())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingTestTaskUniqueId", formatTaskCriteria(testTask)));
        }
    }

    private void reviewTaskCriteria(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (CollectionUtils.isEmpty(testTask.getCriteria())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingTestTaskCriteria", formatTaskRef(testTask)));
        }
    }

    private void reviewTaskParticipantSize(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (CollectionUtils.isEmpty(testTask.getTestParticipants()) || testTask.getTestParticipants().size() < MINIMUM_TEST_PARTICIPANT_COUNT) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.badTestTaskParticipantsSize",
                    formatTaskRef(testTask), formatTaskCriteria(testTask)));
        }
    }

    private void reviewTaskDescription(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (StringUtils.isEmpty(testTask.getDescription())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingTestDescription",
                    formatTaskRef(testTask), formatTaskCriteria(testTask)));
        }
    }

    private void reviewTaskSuccessAverage(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (testTask.getTaskSuccessAverage() == null && !StringUtils.isEmpty(testTask.getTaskSuccessAverageStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidTestTaskSuccessAverage",
                            formatTaskRef(testTask), formatTaskCriteria(testTask), testTask.getTaskSuccessAverageStr()));
        } else if (testTask.getTaskSuccessAverage() == null && StringUtils.isEmpty(testTask.getTaskSuccessAverageStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestTaskSuccessAverage",
                            formatTaskRef(testTask), formatTaskCriteria(testTask)));
        }
    }

    private void reviewTaskSuccessStddev(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (testTask.getTaskSuccessStddev() == null && !StringUtils.isEmpty(testTask.getTaskSuccessStddevStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidTestTaskSuccessStddev",
                            formatTaskRef(testTask), formatTaskCriteria(testTask), testTask.getTaskSuccessStddevStr()));
        } else if (testTask.getTaskSuccessStddev() == null && StringUtils.isEmpty(testTask.getTaskSuccessStddevStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestTaskSuccessStddev",
                            formatTaskRef(testTask), formatTaskCriteria(testTask)));
        }
    }

    private void reviewTaskPathDeviationObserved(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (testTask.getTaskPathDeviationObserved() == null && !StringUtils.isEmpty(testTask.getTaskPathDeviationObservedStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidTestTaskPathDeviationObserved",
                            formatTaskRef(testTask), formatTaskCriteria(testTask), testTask.getTaskPathDeviationObservedStr()));
        } else if (testTask.getTaskPathDeviationObserved() == null && StringUtils.isEmpty(testTask.getTaskPathDeviationObservedStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestTaskPathDeviationObserved", formatTaskRef(testTask), formatTaskCriteria(testTask)));
        } else if (testTask.getTaskPathDeviationObserved() != null) {
            try {
                Integer.valueOf(testTask.getTaskPathDeviationObservedStr());
            } catch (NumberFormatException ex) {
                try {
                    int val = Math.round(Float.valueOf(testTask.getTaskPathDeviationObservedStr()));
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.roundedTestTaskNumber",
                                    formatTaskRef(testTask), "Task Path Deviation Observed",
                                    testTask.getTaskPathDeviationObservedStr(), String.valueOf(val)));
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void reviewTaskPathDeviationOptimal(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (testTask.getTaskPathDeviationOptimal() == null && !StringUtils.isEmpty(testTask.getTaskPathDeviationOptimalStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidTestTaskPathDeviationOptimal",
                            formatTaskRef(testTask), formatTaskCriteria(testTask), testTask.getTaskPathDeviationOptimalStr()));
        } else if (testTask.getTaskPathDeviationOptimal() == null && StringUtils.isEmpty(testTask.getTaskPathDeviationOptimalStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestTaskPathDeviationOptimal",
                            formatTaskRef(testTask), formatTaskCriteria(testTask)));
        } else if (testTask.getTaskPathDeviationOptimal() != null) {
            try {
                Integer.valueOf(testTask.getTaskPathDeviationOptimalStr());
            } catch (NumberFormatException ex) {
                try {
                    int val = Math.round(Float.valueOf(testTask.getTaskPathDeviationOptimalStr()));
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.roundedTestTaskNumber",
                                    formatTaskRef(testTask), "Task Path Deviation Optimal",
                                    testTask.getTaskPathDeviationOptimalStr(), String.valueOf(val)));
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void reviewTaskTimeAverage(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (testTask.getTaskTimeAvg() == null && !StringUtils.isEmpty(testTask.getTaskTimeAvgStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidTestTaskTimeAvg",
                            formatTaskRef(testTask), formatTaskCriteria(testTask), testTask.getTaskTimeAvgStr()));
        } else if (testTask.getTaskTimeAvg() == null && StringUtils.isEmpty(testTask.getTaskTimeAvgStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestTaskTimeAvg", formatTaskRef(testTask), formatTaskCriteria(testTask)));
        } else if (testTask.getTaskTimeAvg() != null) {
            try {
                Integer.valueOf(testTask.getTaskTimeAvgStr());
            } catch (NumberFormatException ex) {
                try {
                    int val = Math.round(Float.valueOf(testTask.getTaskTimeAvgStr()));
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.roundedTestTaskNumber",
                                    formatTaskRef(testTask), "Task Time Average",
                                    testTask.getTaskTimeAvgStr(), String.valueOf(val)));
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void reviewTaskTimeStddev(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (testTask.getTaskTimeStddev() == null && !StringUtils.isEmpty(testTask.getTaskTimeStddevStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidTestTaskTimeStddev",
                            formatTaskRef(testTask), formatTaskCriteria(testTask), testTask.getTaskTimeStddevStr()));
        } else if (testTask.getTaskTimeStddev() == null && StringUtils.isEmpty(testTask.getTaskTimeStddevStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestTaskTimeStddev", formatTaskRef(testTask), formatTaskCriteria(testTask)));
        } else if (testTask.getTaskTimeStddev() != null) {
            try {
                Integer.valueOf(testTask.getTaskTimeStddevStr());
            } catch (NumberFormatException ex) {
                try {
                    int val = Math.round(Float.valueOf(testTask.getTaskTimeStddevStr()));
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.roundedTestTaskNumber",
                                    formatTaskRef(testTask), "Task Time Standard Deviation",
                                    testTask.getTaskTimeStddevStr(), String.valueOf(val)));
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void reviewTaskTimeDeviationObservedAvg(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (testTask.getTaskTimeDeviationObservedAvg() == null && !StringUtils.isEmpty(testTask.getTaskTimeDeviationObservedAvgStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidTestTaskTimeDeviationObservedAvg",
                            formatTaskRef(testTask), formatTaskCriteria(testTask), testTask.getTaskTimeDeviationObservedAvgStr()));
        } else if (testTask.getTaskTimeDeviationObservedAvg() == null && StringUtils.isEmpty(testTask.getTaskTimeDeviationObservedAvgStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestTaskTimeDeviationObservedAvg", formatTaskRef(testTask), formatTaskCriteria(testTask)));
        } else if (testTask.getTaskTimeDeviationObservedAvg() != null) {
            try {
                Integer.valueOf(testTask.getTaskTimeDeviationObservedAvgStr());
            } catch (NumberFormatException ex) {
                try {
                    int val = Math.round(Float.valueOf(testTask.getTaskTimeDeviationObservedAvgStr()));
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.roundedTestTaskNumber",
                                    formatTaskRef(testTask), "Task Time Deviation Observed Average",
                                    testTask.getTaskTimeDeviationObservedAvgStr(), String.valueOf(val)));
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void reviewTaskTimeDeviationOptimalAvg(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (testTask.getTaskTimeDeviationOptimalAvg() == null && !StringUtils.isEmpty(testTask.getTaskTimeDeviationOptimalAvgStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidTestTaskTimeDeviationOptimalAvg",
                            formatTaskRef(testTask), formatTaskCriteria(testTask), testTask.getTaskTimeDeviationOptimalAvgStr()));
        } else if (testTask.getTaskTimeDeviationOptimalAvg() == null && StringUtils.isEmpty(testTask.getTaskTimeDeviationOptimalAvgStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestTaskTimeDeviationOptimalAvg", formatTaskRef(testTask), formatTaskCriteria(testTask)));
        } else if (testTask.getTaskTimeDeviationOptimalAvg() != null) {
            try {
                Integer.valueOf(testTask.getTaskTimeDeviationOptimalAvgStr());
            } catch (NumberFormatException ex) {
                try {
                    int val = Math.round(Float.valueOf(testTask.getTaskTimeDeviationOptimalAvgStr()));
                    listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.roundedTestTaskNumber",
                                    formatTaskRef(testTask), "Task Time Deviation Optimal Average",
                                    testTask.getTaskTimeDeviationOptimalAvgStr(), String.valueOf(val)));
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void reviewTestTaskErrors(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (testTask.getTaskErrors() == null && !StringUtils.isEmpty(testTask.getTaskErrorsStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidTestTaskErrors", formatTaskRef(testTask), formatTaskCriteria(testTask),
                            testTask.getTaskErrorsStr()));
        } else if (testTask.getTaskErrors() == null && StringUtils.isEmpty(testTask.getTaskErrorsStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestTaskErrors", formatTaskRef(testTask), formatTaskCriteria(testTask)));
        }
    }

    private void reviewTestTaskErrorsStddev(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (testTask.getTaskErrorsStddev() == null && !StringUtils.isEmpty(testTask.getTaskErrorsStddevStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidTestTaskErrorsStddev", formatTaskRef(testTask), formatTaskCriteria(testTask),
                            testTask.getTaskErrorsStddevStr()));
        } else if (testTask.getTaskErrorsStddev() == null && StringUtils.isEmpty(testTask.getTaskErrorsStddevStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestTaskErrorsStddev", formatTaskRef(testTask), formatTaskCriteria(testTask)));
        }
    }

    private void reviewTestTaskRatingScale(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (StringUtils.isEmpty(testTask.getTaskRatingScale())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingTestTaskRatingScale",
                    formatTaskRef(testTask), formatTaskCriteria(testTask)));
        }
        //TODO: should we add validation to confirm it is in a set of allowed values? This was not done before as best I can tell
    }

    private void reviewTestTaskRating(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (testTask.getTaskRating() == null && !StringUtils.isEmpty(testTask.getTaskRatingStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidTestTaskRating",
                            formatTaskRef(testTask), formatTaskCriteria(testTask), testTask.getTaskRatingStr()));
        } else if (testTask.getTaskRating() == null && StringUtils.isEmpty(testTask.getTaskRatingStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestTaskRating", formatTaskRef(testTask), formatTaskCriteria(testTask)));
        }
    }

    private void reviewTestTaskRatingStddev(CertifiedProductSearchDetails listing, TestTask testTask) {
        if (testTask.getTaskRatingStddev() == null && !StringUtils.isEmpty(testTask.getTaskRatingStddevStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidTestTaskRatingStddev",
                            formatTaskRef(testTask), formatTaskCriteria(testTask), testTask.getTaskRatingStddevStr()));
        } else if (testTask.getTaskRatingStddev() == null && StringUtils.isEmpty(testTask.getTaskRatingStddevStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestTaskRatingStddev", formatTaskRef(testTask), formatTaskCriteria(testTask)));
        }
    }

    private String formatTaskRef(TestTask testTask) {
        return !StringUtils.isEmpty(testTask.getUniqueId()) ? testTask.getUniqueId() : DEFAULT_TASK_DECRIPTION;
    }

    private String formatTaskCriteria(TestTask testTask) {
        if (CollectionUtils.isEmpty(testTask.getCriteria())) {
            return DEFAULT_TASK_CRITERIA;
        }
        return testTask.getCriteria().stream()
                .filter(criterion -> BooleanUtils.isFalse(criterion.getRemoved()))
                .map(criterion -> Util.formatCriteriaNumber(criterion))
                .collect(Collectors.joining(","));
    }
}
