package gov.healthit.chpl.validation.listing.reviewer;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

@Component("unsupportedCharacterReviewer")
public class UnsupportedCharacterReviewer implements Reviewer {

    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public UnsupportedCharacterReviewer(ValidationUtils validationUtils, ErrorMessageUtil msgUtil) {
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        reviewListingFields(listing);
        reviewDeveloperFields(listing);
        reviewQmsStandardFields(listing);
        reviewAccessibilityStandardFields(listing);
        reviewTargetedUserFields(listing);

        // check all criteria fields
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.isSuccess() != null && cert.isSuccess().booleanValue()) {
                addCriteriaWarningIfNotValid(listing, cert, cert.getApiDocumentation(), "API Documentation");
                if (cert.getAdditionalSoftware() != null) {
                    for (CertificationResultAdditionalSoftware addSoft : cert.getAdditionalSoftware()) {
                        addCriteriaWarningIfNotValid(listing, cert,
                                addSoft.getName(), "Additional Software Name '" + addSoft.getName() + "'");
                        addCriteriaWarningIfNotValid(listing, cert,
                                addSoft.getVersion(), "Additional Software Version '" + addSoft.getVersion() + "'");
                        addCriteriaWarningIfNotValid(listing, cert,
                                addSoft.getJustification(), "Additional Software Justification '"
                                        + addSoft.getJustification() + "'");
                    }
                }
                if (cert.getTestDataUsed() != null) {
                    for (CertificationResultTestData testData : cert.getTestDataUsed()) {
                        // not checking test data name because it has to match one of the existing names
                        addCriteriaWarningIfNotValid(listing, cert,
                                testData.getVersion(), "Test Data Version '" + testData.getVersion() + "'");
                        addCriteriaWarningIfNotValid(listing, cert,
                                testData.getAlteration(), "Test Data Alteration '" + testData.getAlteration() + "'");
                    }
                }

                // not checking test functionality name because it has to match one of the existing options

                if (cert.getTestProcedures() != null) {
                    for (CertificationResultTestProcedure testProc : cert.getTestProcedures()) {
                        // not checking name because it has to match one of the existing options
                        addCriteriaWarningIfNotValid(listing, cert, testProc.getTestProcedureVersion(),
                                "Test Procedure Version '" + testProc.getTestProcedureVersion() + "'");
                    }
                }
                if (cert.getTestStandards() != null) {
                    for (CertificationResultTestStandard testStd : cert.getTestStandards()) {
                        addCriteriaWarningIfNotValid(listing, cert,
                                testStd.getTestStandardName(),
                                "Test Standard Name '" + testStd.getTestStandardName() + "'");
                    }
                }
                if (cert.getTestToolsUsed() != null) {
                    for (CertificationResultTestTool testTool : cert.getTestToolsUsed()) {
                        // not checking name because it has to match one of the existing options
                        addCriteriaWarningIfNotValid(listing, cert, testTool.getTestToolVersion(),
                                "Test Tool Version '" + testTool.getTestToolVersion() + "'");
                    }
                }
            }
        }
        CertifiedProductSed sed = listing.getSed();
        if (sed != null) {
            if (sed.getTestTasks() != null) {
                for (TestTask task : sed.getTestTasks()) {
                    if (task != null) {
                        // not checking anything converted to a number
                        addListingWarningIfNotValid(listing, task.getDescription(),
                                "Test Task Description '" + task.getDescription() + "'");
                        addListingWarningIfNotValid(listing, task.getTaskRatingScale(),
                                "Test Task Rating Scale '" + task.getTaskRatingScale() + "'");

                        if (task.getTestParticipants() != null) {
                            for (TestParticipant participant : task.getTestParticipants()) {
                                if (participant != null) {
                                    // not checking age range or education level because they have to map
                                    // to existing values. also not checking anything converted to a number
                                    addListingWarningIfNotValid(listing, participant.getAssistiveTechnologyNeeds(),
                                            "Participant Assistive Technology Needs '"
                                                    + participant.getAssistiveTechnologyNeeds() + "'");
                                    addListingWarningIfNotValid(listing, participant.getGender(),
                                            "Participant Gender '" + participant.getGender() + "'");
                                    addListingWarningIfNotValid(listing, participant.getOccupation(),
                                            "Participant Occupation '" + participant.getOccupation() + "'");
                                }
                            }
                        }
                    }
                }
            }
            if (sed.getUcdProcesses() != null) {
                for (UcdProcess ucd : sed.getUcdProcesses()) {
                    addListingWarningIfNotValid(listing,
                            ucd.getName(), "UCD Process Name '" + ucd.getName() + "'");
                    addListingWarningIfNotValid(listing,
                            ucd.getDetails(), "UCD Process Details '" + ucd.getDetails() + "'");
                }
            }
        }
    }

    private void reviewListingFields(CertifiedProductSearchDetails listing) {
        addListingWarningIfNotValid(listing, listing.getAcbCertificationId(), "ACB Certification ID '" + listing.getAcbCertificationId() + "'");
        addListingWarningIfNotValid(listing, listing.getOtherAcb(), "Other ACB '" + listing.getOtherAcb() + "'");

        if (MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY) != null) {
            addListingWarningIfNotValid(listing,
                    MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY),
                    "ACB Name '" + MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY) + "'");
        }
        if (MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY) != null) {
            addListingWarningIfNotValid(listing,
                    MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY),
                    "Certification Edition '" + MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY) + "'");
        }
        addListingWarningIfNotValid(listing, listing.getProductAdditionalSoftware(),
                "Listing-level Additional Software '" + listing.getProductAdditionalSoftware() + "'");
        if (listing.getProduct() != null) {
            addListingWarningIfNotValid(listing, listing.getProduct().getName(),
                    "Product Name '" + listing.getProduct().getName() + "'");
        }
        if (listing.getVersion() != null) {
            addListingWarningIfNotValid(listing, listing.getVersion().getVersion(),
                    "Version Name '" + listing.getVersion().getVersion() + "'");
        }

        if (MapUtils.getString(listing.getPracticeType(), "name") != null) {
            addListingWarningIfNotValid(listing, MapUtils.getString(listing.getPracticeType(), "name"),
                    "Practice Type '" + MapUtils.getString(listing.getPracticeType(), "name") + "'");
        }
        if (MapUtils.getString(listing.getClassificationType(), "name") != null) {
            addListingWarningIfNotValid(listing, MapUtils.getString(listing.getClassificationType(), "name"),
                    "Product Classification '" + MapUtils.getString(listing.getClassificationType(), "name") + "'");
        }
        addListingWarningIfNotValid(listing, listing.getReportFileLocation(),
                "Report File Location '" + listing.getReportFileLocation() + "'");
        addListingWarningIfNotValid(listing, listing.getSedIntendedUserDescription(),
                "SED Intended User Description '" + listing.getSedIntendedUserDescription() + "'");
        addListingWarningIfNotValid(listing, listing.getSedReportFileLocation(),
                "SED Report File Location '" + listing.getSedReportFileLocation() + "'");
        if (listing.getTransparencyAttestation() != null) {
            addListingWarningIfNotValid(listing, listing.getTransparencyAttestation().getTransparencyAttestation(),
                    "Transparency Attestation '" + listing.getTransparencyAttestation().getTransparencyAttestation() + "'");
        }
        addListingWarningIfNotValid(listing, listing.getMandatoryDisclosures(),
                "Mandatory Disclosures '" + listing.getMandatoryDisclosures() + "'");
    }

    private void reviewDeveloperFields(CertifiedProductSearchDetails listing) {
        if (listing.getDeveloper() == null) {
            return;
        }

        addListingWarningIfNotValid(listing, listing.getDeveloper().getName(),
                "Developer's Name '" + listing.getDeveloper().getName() + "'");
        addListingWarningIfNotValid(listing, listing.getDeveloper().getWebsite(),
                "Developer's Website '" + listing.getDeveloper().getWebsite() + "'");

        if (listing.getDeveloper().getAddress() != null) {
            Address address = listing.getDeveloper().getAddress();
            addListingWarningIfNotValid(listing, address.getLine1(),
                    "Developer's Street Address (Line 1) '" + address.getLine1() + "'");
            addListingWarningIfNotValid(listing, address.getLine2(),
                    "Developer's Street Address (Line 2) '" + address.getLine2() + "'");
            addListingWarningIfNotValid(listing, address.getCity(),
                    "Developer's City '" + address.getCity() + "'");
            addListingWarningIfNotValid(listing, address.getState(),
                    "Developer's State '" + address.getState() + "'");
            addListingWarningIfNotValid(listing, address.getZipcode(),
                    "Developer's Zip Code '" + address.getZipcode() + "'");
            addListingWarningIfNotValid(listing, address.getCountry(),
                    "Developer's Country  '" + address.getCountry() + "'");
        }

        if (listing.getDeveloper().getContact() != null) {
            PointOfContact contact = listing.getDeveloper().getContact();
            addListingWarningIfNotValid(listing, contact.getFullName(),
                    "Developer Contact's Name '" + contact.getFullName() + "'");
            addListingWarningIfNotValid(listing, contact.getEmail(),
                    "Developer Contact's Email Address '" + contact.getEmail() + "'");
            addListingWarningIfNotValid(listing, contact.getPhoneNumber(),
                    "Developer Contact's Phone Number '" + contact.getPhoneNumber() + "'");
            addListingWarningIfNotValid(listing, contact.getTitle(),
                    "Developer Contact's Title '" + contact.getTitle() + "'");
        }
    }

    private void reviewQmsStandardFields(CertifiedProductSearchDetails listing) {
        for (CertifiedProductQmsStandard qmsStd : listing.getQmsStandards()) {
            addListingWarningIfNotValid(listing, qmsStd.getQmsStandardName(),
                    "QMS Standard '" + qmsStd.getQmsStandardName() + "'");
            addListingWarningIfNotValid(listing, qmsStd.getApplicableCriteria(),
                    "QMS Applicable Criteria '" + qmsStd.getApplicableCriteria() + "'");
            addListingWarningIfNotValid(listing, qmsStd.getQmsModification(),
                    "QMS Modification '" + qmsStd.getQmsModification() + "'");
        }
    }

    private void reviewAccessibilityStandardFields(CertifiedProductSearchDetails listing) {
        for (CertifiedProductAccessibilityStandard accStd : listing.getAccessibilityStandards()) {
            addListingWarningIfNotValid(listing, accStd.getAccessibilityStandardName(),
                    "Accessibility Standard '" + accStd.getAccessibilityStandardName() + "'");
        }
    }

    private void reviewTargetedUserFields(CertifiedProductSearchDetails listing) {
        for (CertifiedProductTargetedUser tu : listing.getTargetedUsers()) {
            addListingWarningIfNotValid(listing, tu.getTargetedUserName(),
                    "Targeted User '" + tu.getTargetedUserName() + "'");
        }
    }

    private void addListingWarningIfNotValid(CertifiedProductSearchDetails listing, String input, String fieldName) {
        if (!validationUtils.isValidUtf8(input)) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.badCharacterFound", fieldName));
        }
        if (validationUtils.hasNewline(input)) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.newlineCharacterFound", fieldName));
        }
    }

    private void addCriteriaWarningIfNotValid(CertifiedProductSearchDetails listing, CertificationResult criteria, String input, String fieldName) {
        if (!validationUtils.isValidUtf8(input)) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.badCharacterFound",
                            Util.formatCriteriaNumber(criteria.getCriterion()), fieldName));
        }
        if (validationUtils.hasNewline(input)) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.newlineCharacterFound",
                            Util.formatCriteriaNumber(criteria.getCriterion()), fieldName));
        }
    }
}
