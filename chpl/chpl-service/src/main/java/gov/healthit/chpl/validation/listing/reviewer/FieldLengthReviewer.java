package gov.healthit.chpl.validation.listing.reviewer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("fieldLengthReviewer")
public class FieldLengthReviewer implements Reviewer {
    private static final String MAX_LENGTH_PROPERTY_PREFIX = "maxLength.";
    private static final String MAX_LENGTH_PROPERTY_SUFFIX = ".maxlength";

    private ErrorMessageUtil msgUtil;
    private MessageSource messageSource;

    @Autowired
    public FieldLengthReviewer(ErrorMessageUtil msgUtil, MessageSource messageSource) {
        this.msgUtil = msgUtil;
        this.messageSource = messageSource;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY) != null) {
            checkFieldLength(listing, MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY),
                    "certificationEdition");
        }
        if (listing.getDeveloper() != null) {
            if (!StringUtils.isEmpty(listing.getDeveloper().getName())) {
                checkFieldLength(listing, listing.getDeveloper().getName(), "developerName");
            }
            if (listing.getDeveloper().getAddress() != null && !StringUtils.isEmpty(listing.getDeveloper().getAddress().getLine1())) {
                checkFieldLength(listing, listing.getDeveloper().getAddress().getLine1(), "developerStreetAddress");
            }
            if (listing.getDeveloper().getAddress() != null && !StringUtils.isEmpty(listing.getDeveloper().getAddress().getLine2())) {
                checkFieldLength(listing, listing.getDeveloper().getAddress().getLine2(), "developerStreetAddress");
            }
            if (listing.getDeveloper().getAddress() != null && !StringUtils.isEmpty(listing.getDeveloper().getAddress().getCity())) {
                checkFieldLength(listing, listing.getDeveloper().getAddress().getCity(), "developerCity");
            }
            if (listing.getDeveloper().getAddress() != null && !StringUtils.isEmpty(listing.getDeveloper().getAddress().getState())) {
                checkFieldLength(listing, listing.getDeveloper().getAddress().getState(), "developerState");
            }
            if (listing.getDeveloper().getAddress() != null && !StringUtils.isEmpty(listing.getDeveloper().getAddress().getZipcode())) {
                checkFieldLength(listing, listing.getDeveloper().getAddress().getZipcode(), "developerZip");
            }
            if (!StringUtils.isEmpty(listing.getDeveloper().getWebsite())) {
                checkFieldLength(listing, listing.getDeveloper().getWebsite(), "developerWebsite");
            }
            if (listing.getDeveloper().getContact() != null && !StringUtils.isEmpty(listing.getDeveloper().getContact().getFullName())) {
                checkFieldLength(listing, listing.getDeveloper().getContact().getFullName(), "developerContactName");
            }
            if (listing.getDeveloper().getContact() != null && !StringUtils.isEmpty(listing.getDeveloper().getContact().getEmail())) {
                checkFieldLength(listing, listing.getDeveloper().getContact().getEmail(), "developerEmail");
            }
            if (listing.getDeveloper().getContact() != null && !StringUtils.isEmpty(listing.getDeveloper().getContact().getPhoneNumber())) {
                checkFieldLength(listing, listing.getDeveloper().getContact().getPhoneNumber(), "developerPhone");
            }
        }
        if (listing.getProduct() != null && !StringUtils.isEmpty(listing.getProduct().getName())) {
            checkFieldLength(listing, listing.getProduct().getName(), "productName");
        }
        if (listing.getVersion() != null && !StringUtils.isEmpty(listing.getVersion().getVersion())) {
            checkFieldLength(listing, listing.getVersion().getVersion(), "productVersion");
        }
        if (!StringUtils.isEmpty(listing.getAcbCertificationId())) {
            checkFieldLength(listing, listing.getAcbCertificationId(), "acbCertificationId");
        }
        if (!StringUtils.isEmpty(listing.getMandatoryDisclosures())) {
            checkFieldLength(listing, listing.getMandatoryDisclosures(), "170523k1Url");
        }
        checkQmsStandardsFieldLength(listing);
        checkAccessibilityStandardsFieldLength(listing);
        checkTargetedUsersFieldLength(listing);
        checkCriteria(listing);
        checkSed(listing);
    }

    private void checkQmsStandardsFieldLength(CertifiedProductSearchDetails listing) {
        if (listing.getQmsStandards() != null && listing.getQmsStandards().size() > 0) {
            listing.getQmsStandards().stream()
                    .filter(qmsStandard -> !StringUtils.isEmpty(qmsStandard.getQmsStandardName()))
                    .forEach(qmsStandard -> checkFieldLength(listing, qmsStandard.getQmsStandardName(), "qmsStandard"));
        }
    }

    private void checkAccessibilityStandardsFieldLength(CertifiedProductSearchDetails listing) {
        if (listing.getAccessibilityStandards() != null && listing.getAccessibilityStandards().size() > 0) {
            listing.getAccessibilityStandards().stream()
                    .filter(accStandard -> !StringUtils.isEmpty(accStandard.getAccessibilityStandardName()))
                    .forEach(accStandard -> checkFieldLength(listing, accStandard.getAccessibilityStandardName(), "accessibilityStandard"));
        }
    }

    private void checkTargetedUsersFieldLength(CertifiedProductSearchDetails listing) {
        if (listing.getTargetedUsers() != null && listing.getTargetedUsers().size() > 0) {
            listing.getTargetedUsers().stream()
                    .filter(targetedUser -> !StringUtils.isEmpty(targetedUser.getTargetedUserName()))
                    .forEach(targetedUser -> checkFieldLength(listing, targetedUser.getTargetedUserName(), "targetedUser"));
        }
    }

    private void checkCriteria(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .forEach(certResult -> {
                    checkFieldLength(listing, certResult.getApiDocumentation(), "apiDocumentationLink");
                    checkFieldLength(listing, certResult.getExportDocumentation(), "exportDocumentationLink");
                    checkFieldLength(listing, certResult.getDocumentationUrl(), "documentationUrlLink");
                    checkFieldLength(listing, certResult.getUseCases(), "useCasesLink");
                    checkFieldLength(listing, certResult.getServiceBaseUrlList(), "serviceBaseUrlListLink");
                    checkTestToolFields(listing, certResult);
                    checkTestDataFields(listing, certResult);
                    checkTestProcedureFields(listing, certResult);
                });
    }

    private void checkTestToolFields(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getTestToolsUsed() != null && certResult.getTestToolsUsed().size() > 0) {
            certResult.getTestToolsUsed().stream()
                    .forEach(testTool -> {
                        checkFieldLength(listing, testTool.getTestToolVersion(), "testToolVersion");
                    });
        }
    }

    private void checkTestDataFields(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getTestDataUsed() != null && certResult.getTestDataUsed().size() > 0) {
            certResult.getTestDataUsed().stream()
                    .forEach(testData -> {
                        checkFieldLength(listing, testData.getVersion(), "testDataVersion");
                    });
        }
    }

    private void checkTestProcedureFields(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getTestProcedures() != null && certResult.getTestProcedures().size() > 0) {
            certResult.getTestProcedures().stream()
                    .forEach(testProcedure -> {
                        checkFieldLength(listing, testProcedure.getTestProcedureVersion(), "testProcedureVersion");
                    });
        }
    }

    private void checkSed(CertifiedProductSearchDetails listing) {
        if (!StringUtils.isEmpty(listing.getSedReportFileLocation())) {
            checkFieldLength(listing, listing.getSedReportFileLocation(), "sedReportHyperlink");
        }
        if (listing.getSed() != null) {
            checkTestTasks(listing);
            checkTestParticipants(listing);
        }
    }

    private void checkTestTasks(CertifiedProductSearchDetails listing) {
        if (!CollectionUtils.isEmpty(listing.getSed().getTestTasks())) {
            listing.getSed().getTestTasks().stream()
                    .forEach(testTask -> {
                        checkFieldLength(listing, testTask.getUniqueId(), "taskIdentifier");
                        checkFieldLength(listing, testTask.getTaskRatingScale(), "taskRatingScale");
                    });
        }
    }

    private void checkTestParticipants(CertifiedProductSearchDetails listing) {
        if (!CollectionUtils.isEmpty(listing.getSed().getTestTasks())) {
            listing.getSed().getTestTasks().stream()
                    .filter(testTask -> !CollectionUtils.isEmpty(testTask.getTestParticipants()))
                    .flatMap(testTask -> testTask.getTestParticipants().stream())
                    .forEach(testParticipant -> {
                        checkFieldLength(listing, testParticipant.getUniqueId(), "participantIdentifier");
                        checkFieldLength(listing, testParticipant.getGender(), "participantGender");
                        checkFieldLength(listing, testParticipant.getOccupation(), "participantOccupation");
                        checkFieldLength(listing, testParticipant.getAssistiveTechnologyNeeds(), "participantAssistiveTechnology");
                    });
        }
    }

    private void checkFieldLength(CertifiedProductSearchDetails product, String field, String errorField) {
        int maxAllowedFieldLength = getMaxLength(MAX_LENGTH_PROPERTY_PREFIX + errorField);
        if (!StringUtils.isEmpty(field) && field.length() > maxAllowedFieldLength) {
            product.addBusinessErrorMessage(
                    msgUtil.getMessage("listing." + errorField + MAX_LENGTH_PROPERTY_SUFFIX, maxAllowedFieldLength, field));
        }
    }

    private int getMaxLength(String field) {
        return Integer.parseInt(String.format(messageSource.getMessage(field, null, LocaleContextHolder.getLocale())));
    }
}
