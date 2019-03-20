package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.listing.pending.PendingTestParticipantDTO;
import gov.healthit.chpl.dto.listing.pending.PendingTestTaskDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

@Component("pendingUnsupportedCharacterReviewer")
public class UnsupportedCharacterReviewer implements Reviewer {

    @Autowired ErrorMessageUtil msgUtil;

    public void review(PendingCertifiedProductDTO listing) {
      //check all string fields at the listing level
        addListingWarningIfNotValid(listing, listing.getAcbCertificationId(),
                "ACB Certification ID '" + listing.getAcbCertificationId() + "'");
        addListingWarningIfNotValid(listing, listing.getCertificationBodyName(),
                "ACB Name '" + listing.getCertificationBodyName() + "'");
        addListingWarningIfNotValid(listing, listing.getCertificationEdition(),
                "Certification Edition '" + listing.getCertificationEdition() + "'");
        if (listing.getDeveloperAddress() != null) {
            addListingWarningIfNotValid(listing, listing.getDeveloperAddress().getStreetLineOne(),
                    "Developer's Street Address (Line 1) '" + listing.getDeveloperAddress().getStreetLineOne() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperAddress().getStreetLineTwo(),
                    "Developer's Street Address (Line 2) '" + listing.getDeveloperAddress().getStreetLineTwo() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperAddress().getCity(),
                    "Developer's City '" + listing.getDeveloperAddress().getCity() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperAddress().getState(),
                    "Developer's State '" + listing.getDeveloperAddress().getState() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperAddress().getZipcode(),
                    "Developer's Zip Code '" + listing.getDeveloperAddress().getZipcode() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperAddress().getCountry(),
                    "Developer's Country '" + listing.getDeveloperAddress().getCountry() + "'");
        } else {
            addListingWarningIfNotValid(listing, listing.getDeveloperStreetAddress(),
                    "Developer's Street Address '" + listing.getDeveloperStreetAddress() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperCity(),
                    "Developer's City '" + listing.getDeveloperCity() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperState(),
                    "Developer's State '" + listing.getDeveloperState() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperZipCode(),
                    "Developer's Zip Code '" + listing.getDeveloperZipCode() + "'");
        }
        addListingWarningIfNotValid(listing, listing.getDeveloperContactName(),
                "Developer's Contact Name '" + listing.getDeveloperContactName() + "'");
        addListingWarningIfNotValid(listing, listing.getDeveloperEmail(),
                "Developer's Email Address '" + listing.getDeveloperEmail() + "'");
        addListingWarningIfNotValid(listing, listing.getDeveloperPhoneNumber(),
                "Developer's Phone Number '" + listing.getDeveloperPhoneNumber() + "'");
        addListingWarningIfNotValid(listing, listing.getDeveloperWebsite(),
                "Developer's Website '" + listing.getDeveloperWebsite() + "'");
        addListingWarningIfNotValid(listing, listing.getPracticeType(),
                "Practice Type '" + listing.getPracticeType() + "'");
        addListingWarningIfNotValid(listing, listing.getProductClassificationName(),
                "Product Classification '" + listing.getProductClassificationName() + "'");
        addListingWarningIfNotValid(listing, listing.getProductName(),
                "Product Name '" + listing.getProductName() + "'");
        addListingWarningIfNotValid(listing, listing.getProductVersion(),
                "Version '" + listing.getProductVersion() + "'");
        addListingWarningIfNotValid(listing, listing.getReportFileLocation(),
                "Report File Location '" + listing.getReportFileLocation() + "'");
        addListingWarningIfNotValid(listing, listing.getSedIntendedUserDescription(),
                "SED Intended User Description '" + listing.getSedIntendedUserDescription() + "'");
        addListingWarningIfNotValid(listing, listing.getSedReportFileLocation(),
                "SED Report File Location '" + listing.getSedReportFileLocation() + "'");
        addListingWarningIfNotValid(listing, listing.getTransparencyAttestation(),
                "Transparency Attestation '" + listing.getTransparencyAttestation() + "'");
        addListingWarningIfNotValid(listing, listing.getTransparencyAttestationUrl(),
                "Transparency Attestation URL '" + listing.getTransparencyAttestationUrl() + "'");

        //users can add to accessibility standards so check these
        for (PendingCertifiedProductAccessibilityStandardDTO accStd : listing.getAccessibilityStandards()) {
            addListingWarningIfNotValid(listing, accStd.getName(), "Accessibility Standard '" + accStd.getName() + "'");
        }

        //users can add to qms standards so check these
        for (PendingCertifiedProductQmsStandardDTO qmsStd : listing.getQmsStandards()) {
            addListingWarningIfNotValid(listing, qmsStd.getName(), "QMS Standard '" + qmsStd.getName() + "'");
            addListingWarningIfNotValid(listing, qmsStd.getModification(),
                    "QMS Modification '" + qmsStd.getModification() + "'");
        }

        //users can add to targeted users so check these
        for (PendingCertifiedProductTargetedUserDTO tu : listing.getTargetedUsers()) {
            addListingWarningIfNotValid(listing, tu.getName(), "Targeted User '" + tu.getName() + "'");
        }

        //check all criteria fields
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria().booleanValue()) {
                addCriteriaWarningIfNotValid(listing, cert, cert.getApiDocumentation(), "API Documentation");
                if (cert.getAdditionalSoftware() != null) {
                    for (PendingCertificationResultAdditionalSoftwareDTO addSoft : cert.getAdditionalSoftware()) {
                        addCriteriaWarningIfNotValid(listing, cert,
                                addSoft.getName(), "Additional Software Name '" + addSoft.getName() + "'");
                        addCriteriaWarningIfNotValid(listing, cert,
                                addSoft.getVersion(), "Additional Software Version '" + addSoft.getVersion() + "'");
                        addCriteriaWarningIfNotValid(listing, cert,
                                addSoft.getJustification(),
                                "Additional Software Justification '" + addSoft.getJustification() + "'");
                    }
                }
                if (cert.getTestData() != null) {
                    for (PendingCertificationResultTestDataDTO testData : cert.getTestData()) {
                        //not checking test data name because it has to match one of the existing names
                        addCriteriaWarningIfNotValid(listing, cert,
                                testData.getVersion(), "Test Data Version '" + testData.getVersion() + "'");
                        addCriteriaWarningIfNotValid(listing, cert,
                                testData.getAlteration(), "Test Data Alteration '" + testData.getAlteration() + "'");
                    }
                }

                //not checking test functionality name because it has to match one of the existing options

                if (cert.getTestProcedures() != null) {
                    for (PendingCertificationResultTestProcedureDTO testProc : cert.getTestProcedures()) {
                        //not checking name because it has to match one of the existing options
                        addCriteriaWarningIfNotValid(listing, cert,
                                testProc.getVersion(), "Test Procedure Version '" + testProc.getVersion() + "'");
                    }
                }
                if (cert.getTestStandards() != null) {
                    for (PendingCertificationResultTestStandardDTO testStd : cert.getTestStandards()) {
                        addCriteriaWarningIfNotValid(listing, cert,
                                testStd.getName(), "Test Standard Name '" + testStd.getName() + "'");
                    }
                }
                if (cert.getTestTools() != null) {
                    for (PendingCertificationResultTestToolDTO testTool : cert.getTestTools()) {
                        //not checking name because it has to match one of the existing options
                        addCriteriaWarningIfNotValid(listing, cert,
                                testTool.getVersion(), "Test Tool Version '" + testTool.getVersion() + "'");
                    }
                }
                if (cert.getTestTasks() != null) {
                    for (PendingCertificationResultTestTaskDTO crTestTask : cert.getTestTasks()) {
                        PendingTestTaskDTO testTask = crTestTask.getPendingTestTask();
                        if (testTask != null) {
                            //not checking anything converted to a number
                            addCriteriaWarningIfNotValid(listing, cert,
                                    testTask.getDescription(),
                                    "Test Task Description '" + testTask.getDescription() + "'");
                            addCriteriaWarningIfNotValid(listing, cert,
                                    testTask.getTaskRatingScale(),
                                    "Test Task Rating Scale '" + testTask.getTaskRatingScale() + "'");
                        }
                        if (crTestTask.getTaskParticipants() != null) {
                            for (PendingCertificationResultTestTaskParticipantDTO crPart
                                    : crTestTask.getTaskParticipants()) {
                                PendingTestParticipantDTO part = crPart.getTestParticipant();
                                if (part != null) {
                                    //not checking age range or education level because they have to map
                                    //to existing values. also not checking anything converted to a number
                                    addCriteriaWarningIfNotValid(listing, cert,
                                            part.getAssistiveTechnologyNeeds(),
                                            "Participant Assistive Technology Needs '"
                                                    + part.getAssistiveTechnologyNeeds() + "'");
                                    addCriteriaWarningIfNotValid(listing, cert,
                                            part.getGender(),
                                            "Participant Gender '" + part.getGender() + "'");
                                    addCriteriaWarningIfNotValid(listing, cert,
                                            part.getOccupation(),
                                            "Participant Occupation '" + part.getOccupation() + "'");
                                }
                            }
                        }
                    }
                }
                if (cert.getUcdProcesses() != null) {
                    for (PendingCertificationResultUcdProcessDTO ucd : cert.getUcdProcesses()) {
                        addCriteriaWarningIfNotValid(listing, cert,
                                ucd.getUcdProcessName(), "UCD Process Name '" + ucd.getUcdProcessName() + "'");
                        addCriteriaWarningIfNotValid(listing, cert,
                                ucd.getUcdProcessDetails(), "UCD Process Details '" + ucd.getUcdProcessDetails() + "'");
                    }
                }
            }
        }
    }

    private void addListingWarningIfNotValid(final PendingCertifiedProductDTO listing,
            final String input, final String fieldName) {
        if (!ValidationUtils.isValidUtf8(input)) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.badCharacterFound", fieldName));
        }

        if (ValidationUtils.hasNewline(input)) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.newlineCharacterFound", fieldName));
        }
    }

    private void addCriteriaWarningIfNotValid(final PendingCertifiedProductDTO listing,
            final PendingCertificationResultDTO criteria, final String input, final String fieldName) {
        if (!ValidationUtils.isValidUtf8(input)) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.badCharacterFound", criteria.getNumber(), fieldName));
        }
        if (ValidationUtils.hasNewline(input)) {
            listing.getWarningMessages().add(
                    msgUtil.getMessage("listing.criteria.newlineCharacterFound", criteria.getNumber(), fieldName));
        }
    }
}
