package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
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
            .filter(testTask -> !CollectionUtils.isEmpty(testTask.getTestParticipants()))
            .flatMap(testTask -> testTask.getTestParticipants().stream())
            .forEach(testParticipant -> reviewTestParticipantFields(listing, testParticipant));
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
        if (StringUtils.isEmpty(testParticipant.getUniqueId())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingTestParticipantUniqueId"));
        }
    }

    private void reviewParticipantAgeRange(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (testParticipant.getAgeRangeId() == null && !StringUtils.isEmpty(testParticipant.getAgeRange())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidParticipantAgeRange",
                            testParticipant.getAgeRange(), formatParticipantRef(testParticipant)));
        } else if (testParticipant.getAgeRangeId() == null && StringUtils.isEmpty(testParticipant.getAgeRange())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingParticipantAgeRange", formatParticipantRef(testParticipant)));
        }
    }

    private void reviewParticipantEducationLevel(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (testParticipant.getEducationTypeId() == null && !StringUtils.isEmpty(testParticipant.getEducationTypeName())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidParticipantEducationLevel",
                            testParticipant.getEducationTypeName(), formatParticipantRef(testParticipant)));
        } else if (testParticipant.getEducationTypeId() == null && StringUtils.isEmpty(testParticipant.getEducationTypeName())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingParticipantEducationLevel", formatParticipantRef(testParticipant)));
        }
    }

    private void reviewParticipantGender(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (StringUtils.isEmpty(testParticipant.getGender())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingParticipantGender", formatParticipantRef(testParticipant)));
        }
    }

    private void reviewParticipantOccupation(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (StringUtils.isEmpty(testParticipant.getOccupation())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingParticipantOccupation", formatParticipantRef(testParticipant)));
        }
    }

    private void reviewParticipantAssistiveTechnologyNeeds(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (StringUtils.isEmpty(testParticipant.getAssistiveTechnologyNeeds())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingParticipantAssistiveTechnologyNeeds", formatParticipantRef(testParticipant)));
        }
    }

    private void reviewParticipantProfessionalExperienceMonths(CertifiedProductSearchDetails listing, TestParticipant testParticipant) {
        if (testParticipant.getProfessionalExperienceMonths() == null && !StringUtils.isEmpty(testParticipant.getProfessionalExperienceMonthsStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidParticipantProfessionalExperienceMonths",
                            testParticipant.getProfessionalExperienceMonthsStr(), formatParticipantRef(testParticipant)));
        } else if (testParticipant.getProfessionalExperienceMonths() == null && StringUtils.isEmpty(testParticipant.getProfessionalExperienceMonthsStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingParticipantProfessionalExperienceMonths", formatParticipantRef(testParticipant)));
        } else if (testParticipant.getProfessionalExperienceMonths() != null) {
            try {
                Integer.valueOf(testParticipant.getProfessionalExperienceMonthsStr());
            } catch (NumberFormatException ex) {
                try {
                    int val = Math.round(Float.valueOf(testParticipant.getProfessionalExperienceMonthsStr()));
                    listing.getWarningMessages().add(
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
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidParticipantProductExperienceMonths",
                            testParticipant.getProductExperienceMonthsStr(), formatParticipantRef(testParticipant)));
        } else if (testParticipant.getProductExperienceMonths() == null && StringUtils.isEmpty(testParticipant.getProductExperienceMonthsStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingParticipantProductExperienceMonths", formatParticipantRef(testParticipant)));
        } else if (testParticipant.getProductExperienceMonths() != null) {
            try {
                Integer.valueOf(testParticipant.getProductExperienceMonthsStr());
            } catch (NumberFormatException ex) {
                try {
                    int val = Math.round(Float.valueOf(testParticipant.getProductExperienceMonthsStr()));
                    listing.getWarningMessages().add(
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
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.invalidParticipantComputerExperienceMonths",
                            testParticipant.getComputerExperienceMonthsStr(), formatParticipantRef(testParticipant)));
        } else if (testParticipant.getComputerExperienceMonths() == null && StringUtils.isEmpty(testParticipant.getComputerExperienceMonthsStr())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingParticipantComputerExperienceMonths", formatParticipantRef(testParticipant)));
        } else if (testParticipant.getComputerExperienceMonths() != null) {
            try {
                Integer.valueOf(testParticipant.getComputerExperienceMonthsStr());
            } catch (NumberFormatException ex) {
                try {
                    int val = Math.round(Float.valueOf(testParticipant.getComputerExperienceMonthsStr()));
                    listing.getWarningMessages().add(
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
