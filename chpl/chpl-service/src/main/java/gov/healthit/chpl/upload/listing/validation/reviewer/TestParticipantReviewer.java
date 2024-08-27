package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("listingUploadTestParticipantReviewer")
public class TestParticipantReviewer {
    private static final String DEFAULT_PARTICIPANT_DECRIPTION = "<unknown>";

    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestParticipantReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getSed() == null || CollectionUtils.isEmpty(listing.getSed().getTestTasks())) {
            return;
        }
        listing.getSed().getTestTasks().stream()
                .filter(testTask -> doesTestTaskHaveNonRemovedCriteria(testTask) && !CollectionUtils.isEmpty(testTask.getTestParticipants()))
                .flatMap(testTask -> testTask.getTestParticipants().stream())
                .forEach(testParticipant -> reviewTestParticipantFields(listing, testParticipant));
        reviewTestParticipantDuplicateFriendlyIds(listing);
    }

    private boolean doesTestTaskHaveNonRemovedCriteria(TestTask testTask) {
        if (CollectionUtils.isEmpty(testTask.getCriteria())) {
            return false;
        }

        return testTask.getCriteria().stream()
                .filter(criterion -> BooleanUtils.isFalse(criterion.isRemoved()))
                .findAny().isPresent();
    }

    private void reviewTestParticipantFields(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        reviewParticipantFriendlyId(listing, testParticipant);
        reviewParticipantAgeRange(listing, testParticipant);
        reviewParticipantEducationLevel(listing, testParticipant);
        reviewParticipantGender(listing, testParticipant);
        reviewParticipantOccupation(listing, testParticipant);
        reviewParticipantAssistiveTechnologyNeeds(listing, testParticipant);
        reviewParticipantProfessionalExperienceMonths(listing, testParticipant);
        reviewParticipantProductExperienceMonths(listing, testParticipant);
        reviewParticipantComputerExperienceMonths(listing, testParticipant);
    }

    private void reviewParticipantFriendlyId(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (testParticipant.getId() == null && StringUtils.isEmpty(testParticipant.getFriendlyId())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingTestParticipantUniqueId"));
        }
    }

    private void reviewParticipantAgeRange(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (!hasAgeId(testParticipant) && (hasAgeName(testParticipant))) {
            String suppliedAgeName = hasAgeName(testParticipant) ? testParticipant.getAge().getName() : "?";
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.invalidParticipantAgeRange",
                            suppliedAgeName, formatParticipantRef(testParticipant)));
        } else if (!hasAgeId(testParticipant) && !hasAgeName(testParticipant)) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.missingParticipantAgeRange", formatParticipantRef(testParticipant)));
        }
    }

    private boolean hasAgeId(TestParticipant testParticipant) {
        return testParticipant.getAge() != null && testParticipant.getAge().getId() != null;
    }

    private boolean hasAgeName(TestParticipant testParticipant) {
        return testParticipant.getAge() != null
                && !StringUtils.isEmpty(testParticipant.getAge().getName());
    }

    private void reviewParticipantEducationLevel(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (!hasEducationId(testParticipant) && (hasEducationName(testParticipant))) {
            String suppliedEducationName = hasEducationName(testParticipant) ? testParticipant.getEducationType().getName() : "?";
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.invalidParticipantEducationLevel",
                            suppliedEducationName, formatParticipantRef(testParticipant)));
        } else if (!hasEducationId(testParticipant) && !hasEducationName(testParticipant)) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.missingParticipantEducationLevel", formatParticipantRef(testParticipant)));
        }
    }

    private boolean hasEducationId(TestParticipant testParticipant) {
        return testParticipant.getEducationType() != null && testParticipant.getEducationType().getId() != null;
    }

    private boolean hasEducationName(TestParticipant testParticipant) {
        return testParticipant.getEducationType() != null
                && !StringUtils.isEmpty(testParticipant.getEducationType().getName());
    }

    private void reviewParticipantGender(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (StringUtils.isEmpty(testParticipant.getGender())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingParticipantGender", formatParticipantRef(testParticipant)));
        }
    }

    private void reviewParticipantOccupation(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (StringUtils.isEmpty(testParticipant.getOccupation())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingParticipantOccupation", formatParticipantRef(testParticipant)));
        }
    }

    private void reviewParticipantAssistiveTechnologyNeeds(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (StringUtils.isEmpty(testParticipant.getAssistiveTechnologyNeeds())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingParticipantAssistiveTechnologyNeeds", formatParticipantRef(testParticipant)));
        }
    }

    private void reviewParticipantProfessionalExperienceMonths(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (testParticipant.getProfessionalExperienceMonths() == null && !StringUtils.isEmpty(testParticipant.getProfessionalExperienceMonthsStr())) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.invalidParticipantProfessionalExperienceMonths",
                            testParticipant.getProfessionalExperienceMonthsStr(), formatParticipantRef(testParticipant)));
        } else if (testParticipant.getProfessionalExperienceMonths() == null && StringUtils.isEmpty(testParticipant.getProfessionalExperienceMonthsStr())) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.missingParticipantProfessionalExperienceMonths", formatParticipantRef(testParticipant)));
        } else if (testParticipant.getProfessionalExperienceMonths() != null) {
            try {
                Integer.valueOf(testParticipant.getProfessionalExperienceMonthsStr());
            } catch (NumberFormatException ex) {
                try {
                    int val = Math.round(Float.valueOf(testParticipant.getProfessionalExperienceMonthsStr()));
                    listing.addWarningMessage(
                            msgUtil.getMessage("listing.criteria.roundedParticipantNumber",
                                    formatParticipantRef(testParticipant), "Professional Experience Months",
                                    testParticipant.getProfessionalExperienceMonthsStr(), String.valueOf(val)));
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void reviewParticipantProductExperienceMonths(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (testParticipant.getProductExperienceMonths() == null && !StringUtils.isEmpty(testParticipant.getProductExperienceMonthsStr())) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.invalidParticipantProductExperienceMonths",
                            testParticipant.getProductExperienceMonthsStr(), formatParticipantRef(testParticipant)));
        } else if (testParticipant.getProductExperienceMonths() == null && StringUtils.isEmpty(testParticipant.getProductExperienceMonthsStr())) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.missingParticipantProductExperienceMonths", formatParticipantRef(testParticipant)));
        } else if (testParticipant.getProductExperienceMonths() != null) {
            try {
                Integer.valueOf(testParticipant.getProductExperienceMonthsStr());
            } catch (NumberFormatException ex) {
                try {
                    int val = Math.round(Float.valueOf(testParticipant.getProductExperienceMonthsStr()));
                    listing.addWarningMessage(
                            msgUtil.getMessage("listing.criteria.roundedParticipantNumber",
                                    formatParticipantRef(testParticipant), "Product Experience Months",
                                    testParticipant.getProductExperienceMonthsStr(), String.valueOf(val)));
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void reviewParticipantComputerExperienceMonths(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (testParticipant.getComputerExperienceMonths() == null && !StringUtils.isEmpty(testParticipant.getComputerExperienceMonthsStr())) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.invalidParticipantComputerExperienceMonths",
                            testParticipant.getComputerExperienceMonthsStr(), formatParticipantRef(testParticipant)));
        } else if (testParticipant.getComputerExperienceMonths() == null && StringUtils.isEmpty(testParticipant.getComputerExperienceMonthsStr())) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.missingParticipantComputerExperienceMonths", formatParticipantRef(testParticipant)));
        } else if (testParticipant.getComputerExperienceMonths() != null) {
            try {
                Integer.valueOf(testParticipant.getComputerExperienceMonthsStr());
            } catch (NumberFormatException ex) {
                try {
                    int val = Math.round(Float.valueOf(testParticipant.getComputerExperienceMonthsStr()));
                    listing.addWarningMessage(
                            msgUtil.getMessage("listing.criteria.roundedParticipantNumber",
                                    formatParticipantRef(testParticipant), "Computer Experience Months",
                                    testParticipant.getComputerExperienceMonthsStr(), String.valueOf(val)));
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void reviewTestParticipantDuplicateFriendlyIds(CertifiedProductSearchDetails listing) {
        if (listing.getSed() == null || CollectionUtils.isEmpty(listing.getSed().getTestTasks())) {
            return;
        }
        Map<String, List<TestParticipant>> allTestParticipantsByFriendlyId = listing.getSed().getTestTasks().stream()
                .filter(tt -> !CollectionUtils.isEmpty(tt.getTestParticipants()))
                .flatMap(tt -> tt.getTestParticipants().stream())
                .filter(tp -> !StringUtils.isEmpty(tp.getFriendlyId()))
                .collect(Collectors.groupingBy(TestParticipant::getFriendlyId));

        Set<String> participantsWithDuplicateFriendlyIds = allTestParticipantsByFriendlyId.keySet().stream()
            .flatMap(key -> allTestParticipantsByFriendlyId.get(key).stream())
            .filter(participant ->
                !matchesAllOtherParticipants(participant, allTestParticipantsByFriendlyId.get(participant.getFriendlyId())))
            .map(participant -> participant.getFriendlyId())
            .collect(Collectors.toSet());

        participantsWithDuplicateFriendlyIds.stream()
            .forEach(friendlyId -> listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.duplicateTestParticipantFriendlyId", friendlyId)));
    }

    private boolean matchesAllOtherParticipants(TestParticipant participant, List<TestParticipant> participants) {
        return participants.stream()
                .filter(otherPart -> !otherPart.matches(participant))
                .findAny().isEmpty();
    }

    private String formatParticipantRef(TestParticipant testParticipant) {
        return !StringUtils.isEmpty(testParticipant.getFriendlyId()) ? testParticipant.getFriendlyId() : DEFAULT_PARTICIPANT_DECRIPTION;
    }
}
