package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.TransparencyAttestationDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTestingLabDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

@Component("pendingChplNumberReviewer")
public class ChplNumberReviewer implements Reviewer {
    private TestingLabDAO atlDao;
    private CertificationBodyDAO acbDao;
    private DeveloperDAO developerDao;
    private CertificationEditionDAO certEditionDao;
    private ValidationUtils validationUtils;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ChplNumberReviewer(TestingLabDAO atlDao, CertificationBodyDAO acbDao, DeveloperDAO developerDao,
            CertificationEditionDAO certEditionDao, ValidationUtils validationUtils,
            ChplProductNumberUtil chplProductNumberUtil, ErrorMessageUtil msgUtil) {
        this.atlDao = atlDao;
        this.acbDao = acbDao;
        this.developerDao = developerDao;
        this.certEditionDao = certEditionDao;
        this.validationUtils = validationUtils;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.msgUtil = msgUtil;
    }

    /**
     * Looks at the format of the CHPL Product Number Makes sure each part of the identifier is correctly formatted and
     * is the correct value. May change the CHPL ID if necessary (if additional software was added or certification date
     * was changed) and if the CHPL ID is changed, confirms that the new ID is unique.
     */
    @SuppressWarnings({"checkstyle:methodlength"})
    public void review(PendingCertifiedProductDTO listing) {
        String uniqueId = listing.getUniqueId();
        String[] uniqueIdParts = uniqueId.split("\\.");
        if (uniqueIdParts.length != ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            listing.getErrorMessages().add("The unique CHPL ID '" + uniqueId + "' must have "
                    + ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS + " parts separated by '.'");
            return;
        }
        // validate that these pieces match up with data
        String editionCode = uniqueIdParts[ChplProductNumberUtil.EDITION_CODE_INDEX];
        String atlCode = uniqueIdParts[ChplProductNumberUtil.ATL_CODE_INDEX];
        String acbCode = uniqueIdParts[ChplProductNumberUtil.ACB_CODE_INDEX];
        String developerCode = uniqueIdParts[ChplProductNumberUtil.DEVELOPER_CODE_INDEX];
        String additionalSoftwareCode = uniqueIdParts[ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_INDEX];
        String certifiedDateCode = uniqueIdParts[ChplProductNumberUtil.CERTIFIED_DATE_CODE_INDEX];

        // Ensure the new chpl product number is unique
        String chplProductNumber;
        try {
            chplProductNumber = chplProductNumberUtil.generate(
                    uniqueId,
                    listing.getCertificationEdition(),
                    listing.getTestingLabs(),
                    listing.getCertificationBodyId(),
                    listing.getDeveloperId());
            if (!chplProductNumberUtil.isUnique(chplProductNumber)) {
                listing.getErrorMessages().add(msgUtil.getMessage(
                        "listing.chplProductNumber.notUnique", chplProductNumber));
            }
        } catch (IndexOutOfBoundsException e) {
            listing.getErrorMessages().add(msgUtil.getMessage("atl.notFound"));
        }

        try {
            CertificationEditionDTO certificationEdition = certEditionDao.getById(listing.getCertificationEditionId());
            if (("2014".equals(certificationEdition.getYear()) && !"14".equals(editionCode))
                    || ("2015".equals(certificationEdition.getYear()) && !"15".equals(editionCode))) {
                listing.getErrorMessages()
                        .add("The first part of the CHPL ID must match the certification year of the listing.");
            }

            List<PendingCertifiedProductTestingLabDTO> testingLabs = null;
            if (listing.getTestingLabs() == null || listing.getTestingLabs().size() == 0) {
                listing.getErrorMessages().add(msgUtil.getMessage("atl.notFound"));
            } else {
                testingLabs = listing.getTestingLabs();
                if (testingLabs.size() > 1) {
                    if (!TestingLab.MULTIPLE_TESTING_LABS_CODE.equals(atlCode)) {
                        listing.getWarningMessages()
                                .add(msgUtil.getMessage("atl.shouldBe99"));
                    }
                } else {
                    TestingLabDTO testingLab = atlDao.getByName(testingLabs.get(0).getTestingLabName());
                    if (TestingLab.MULTIPLE_TESTING_LABS_CODE.equals(atlCode)) {
                        listing.getErrorMessages()
                                .add(msgUtil.getMessage("atl.shouldNotBe99"));
                    } else if (!testingLab.getTestingLabCode().equals(atlCode)) {
                        listing.getErrorMessages()
                                .add(msgUtil.getMessage("atl.codeMismatch", testingLab.getName(), atlCode));
                    }
                }
            }

            CertificationBodyDTO certificationBody = null;
            if (listing.getCertificationBodyId() == null) {
                listing.getErrorMessages().add("No certification body was found matching the name '"
                        + listing.getCertificationBodyName() + "'.");
            } else {
                certificationBody = acbDao.getById(listing.getCertificationBodyId());
                if (certificationBody != null && !certificationBody.getAcbCode().equals(acbCode)) {
                    listing.getErrorMessages().add("The ACB code provided does not match the assigned ACB code '"
                            + certificationBody.getAcbCode() + "'.");
                }
            }

            if (listing.getDeveloperId() != null && !developerCode.matches("X+")) {
                DeveloperDTO developer = developerDao.getById(listing.getDeveloperId());
                if (developer != null) {
                    DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
                    if (mostRecentStatus == null || mostRecentStatus.getStatus() == null) {
                        listing.getErrorMessages().add("The current status of the developer " + developer.getName()
                                + " cannot be determined. A developer must be listed as Active "
                                + "in order to create certified products belongong to it.");
                    } else if (!mostRecentStatus.getStatus().getStatusName()
                            .equals(DeveloperStatusType.Active.toString())) {
                        listing.getErrorMessages().add("The developer " + developer.getName() + " has a status of "
                                + mostRecentStatus.getStatus().getStatusName()
                                + ". Certified products belonging to this developer cannot be "
                                + "created until its status returns to Active.");
                    }

                    if (!developer.getDeveloperCode().equals(developerCode)) {
                        listing.getErrorMessages()
                                .add("The developer code '" + developerCode
                                        + "' does not match the assigned developer code for "
                                        + listing.getDeveloperName() + ": '" + developer.getDeveloperCode() + "'.");
                    }
                }
            } else if (!developerCode.matches("X+")) {
                DeveloperDTO developerByCode = developerDao.getByCode(developerCode);
                if (developerByCode == null) {
                    listing.getErrorMessages().add("The developer code " + developerCode
                            + " does not match any developer in the system. New developers should use the code 'XXXX'.");
                } else {
                    listing.getErrorMessages()
                            .add("The developer code " + developerCode + " is for '" + developerByCode.getName()
                                    + "' which does not match the developer name in the upload file '"
                                    + listing.getDeveloperName() + "'");
                }
            }
        } catch (final EntityRetrievalException ex) {
            listing.getErrorMessages().add(ex.getMessage());
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getUniqueId(),
                ChplProductNumberUtil.PRODUCT_CODE_INDEX,
                ChplProductNumberUtil.PRODUCT_CODE_REGEX)) {
            listing.getErrorMessages()
                    .add(msgUtil.getMessage("listing.badProductCodeChars",
                            ChplProductNumberUtil.PRODUCT_CODE_LENGTH));
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getUniqueId(),
                ChplProductNumberUtil.VERSION_CODE_INDEX,
                ChplProductNumberUtil.VERSION_CODE_REGEX)) {
            listing.getErrorMessages()
                    .add(msgUtil.getMessage("listing.badVersionCodeChars",
                            ChplProductNumberUtil.VERSION_CODE_LENGTH));
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getUniqueId(),
                ChplProductNumberUtil.ICS_CODE_INDEX,
                ChplProductNumberUtil.ICS_CODE_REGEX)) {
            listing.getErrorMessages()
                    .add(msgUtil.getMessage("listing.badIcsCodeChars",
                            ChplProductNumberUtil.ICS_CODE_LENGTH));
        } else {
            Integer icsCodeInteger = Integer.valueOf(uniqueIdParts[ChplProductNumberUtil.ICS_CODE_INDEX]);
            if (icsCodeInteger != null) {
                if (icsCodeInteger.intValue() == 0 && listing.getIcs() != null
                        && listing.getIcs().equals(Boolean.TRUE)) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.icsCodeFalseValueTrue"));
                } else if (icsCodeInteger.intValue() > 0 && listing.getIcs() != null
                        && listing.getIcs().equals(Boolean.FALSE)) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.icsCodeTrueValueFalse"));
                }
            }
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getUniqueId(),
                ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_INDEX,
                ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_REGEX)) {
            listing.getErrorMessages()
                    .add(msgUtil.getMessage("listing.badAdditionalSoftwareCodeChars",
                            ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_LENGTH));
        } else {
            if (additionalSoftwareCode.equals("0")) {
                boolean hasAS = false;
                for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
                    if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                        hasAS = true;
                    }
                }
                if (hasAS) {
                    listing.getErrorMessages().add(
                            "The unique id indicates the product does not have additional "
                            + "software but some is specified in the upload file.");
                }
            } else if (additionalSoftwareCode.equals("1")) {
                boolean hasAS = false;
                for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
                    if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                        hasAS = true;
                    }
                }
                if (!hasAS) {
                    listing.getErrorMessages().add(
                            "The unique id indicates the product has additional "
                            + "software but none is specified in the upload file.");
                }
            }
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getUniqueId(),
                ChplProductNumberUtil.CERTIFIED_DATE_CODE_INDEX,
                ChplProductNumberUtil.CERTIFIED_DATE_CODE_REGEX)) {
            listing.getErrorMessages()
                    .add(msgUtil.getMessage("listing.badCertifiedDateCodeChars",
                            ChplProductNumberUtil.CERTIFIED_DATE_CODE_LENGTH));
        }
        SimpleDateFormat idDateFormat = new SimpleDateFormat("yyMMdd");
        try {
            Date idDate = idDateFormat.parse(certifiedDateCode);
            if (listing.getCertificationDate() == null
                    || idDate.getTime() != listing.getCertificationDate().getTime()) {
                listing.getErrorMessages().add(
                        "The certification date provided in the unique id does not match the "
                        + "certification date in the upload file.");
            }
        } catch (final ParseException pex) {
            listing.getErrorMessages()
                    .add("Could not parse the certification date part of the product id: " + certifiedDateCode);
        }
    }

    private Boolean areTransparencyAttestationsEqual(TransparencyAttestationDTO ta1, TransparencyAttestationDTO ta2) {
        Boolean equal = false;
        if (ta1 != null && ta2 != null) {
            if (ta1.getTransparencyAttestation() != null && ta2.getTransparencyAttestation() != null) {
                equal = ta1.getTransparencyAttestation().equals(ta2.getTransparencyAttestation());
            }
        }
        return equal;
    }
}
