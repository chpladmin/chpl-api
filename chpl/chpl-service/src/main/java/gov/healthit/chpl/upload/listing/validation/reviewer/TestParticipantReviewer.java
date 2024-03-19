package gov.healthit.chpl.upload.listing.validation.reviewer;

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
        reviewParticipantUniqueId(listing, testParticipant);
        reviewParticipantAgeRange(listing, testParticipant);
        reviewParticipantEducationLevel(listing, testParticipant);
        reviewParticipantGender(listing, testParticipant);
        reviewParticipantOccupation(listing, testParticipant);
        reviewParticipantAssistiveTechnologyNeeds(listing, testParticipant);
        reviewParticipantProfessionalExperienceMonths(listing, testParticipant);
        reviewParticipantProductExperienceMonths(listing, testParticipant);
        reviewParticipantComputerExperienceMonths(listing, testParticipant);
    }

    private void reviewParticipantUniqueId(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (testParticipant.getId() == null && StringUtils.isEmpty(testParticipant.getUniqueId())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingTestParticipantUniqueId"));
        }
    }

    private void reviewParticipantAgeRange(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (!hasAgeId(testParticipant) && !hasDeprecatedAgeId(testParticipant)
                && (hasAgeName(testParticipant) || hasDeprecatedAgeName(testParticipant))) {
            String suppliedAgeName = hasAgeName(testParticipant) ? testParticipant.getAge().getName() : testParticipant.getAgeRange();
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.invalidParticipantAgeRange",
                            suppliedAgeName, formatParticipantRef(testParticipant)));
        } else if (!hasAgeId(testParticipant) && !hasDeprecatedAgeId(testParticipant)
                && !hasAgeName(testParticipant) && !hasDeprecatedAgeName(testParticipant)) {
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

    private boolean hasDeprecatedAgeId(TestParticipant testParticipant) {
        return testParticipant.getAgeRangeId() != null;
    }

    private boolean hasDeprecatedAgeName(TestParticipant testParticipant) {
        return !StringUtils.isEmpty(testParticipant.getAgeRange());
    }

    private void reviewParticipantEducationLevel(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (!hasEducationId(testParticipant) && !hasDeprecatedEducationId(testParticipant)
                && (hasEducationName(testParticipant) || hasDeprecatedEducationName(testParticipant))) {
            String suppliedEducationName = hasEducationName(testParticipant) ? testParticipant.getEducationType().getName() : testParticipant.getEducationTypeName();
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.invalidParticipantEducationLevel",
                            suppliedEducationName, formatParticipantRef(testParticipant)));
        } else if (!hasEducationId(testParticipant) && !hasDeprecatedEducationId(testParticipant)
                && !hasEducationName(testParticipant) && !hasDeprecatedEducationName(testParticipant)) {
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

    private boolean hasDeprecatedEducationId(TestParticipant testParticipant) {
        return testParticipant.getEducationTypeId() != null;
    }

    private boolean hasDeprecatedEducationName(TestParticipant testParticipant) {
        return !StringUtils.isEmpty(testParticipant.getEducationTypeName());
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

    private String formatParticipantRef(TestParticipant testParticipant) {
        return !StringUtils.isEmpty(testParticipant.getUniqueId()) ? testParticipant.getUniqueId() : DEFAULT_PARTICIPANT_DECRIPTION;
    }
}
