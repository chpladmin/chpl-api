package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductTestingLabDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("pendingChplNumberReviewer")
public class ChplNumberReviewer implements Reviewer {
    @Autowired private CertifiedProductDAO cpDao;
    @Autowired private TestingLabDAO atlDao;
    @Autowired private CertificationBodyDAO acbDao;
    @Autowired private DeveloperDAO developerDao;
    @Autowired private CertificationEditionDAO certEditionDao;
    @Autowired private ChplProductNumberUtil chplProductNumberUtil;
    @Autowired private ErrorMessageUtil msgUtil;

    /**
     * Looks at the format of the CHPL Product Number
     * Makes sure each part of the identifier is correctly formatted and is the correct value.
     * May change the CHPL ID if necessary (if additional software was added or certification date was changed)
     * and if the CHPL ID is changed, confirms that the new ID is unique.
     */
    public void review(PendingCertifiedProductDTO listing) {
        String uniqueId = listing.getUniqueId();
        String[] uniqueIdParts = uniqueId.split("\\.");
        if (uniqueIdParts.length != CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            listing.getErrorMessages().add("The unique CHPL ID '" + uniqueId + "' must have "
                    + CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS + " parts separated by '.'");
            return;
        }
        // validate that these pieces match up with data
        String editionCode = uniqueIdParts[CertifiedProductDTO.EDITION_CODE_INDEX];
        String atlCode = uniqueIdParts[CertifiedProductDTO.ATL_CODE_INDEX];
        String acbCode = uniqueIdParts[CertifiedProductDTO.ACB_CODE_INDEX];
        String developerCode = uniqueIdParts[CertifiedProductDTO.DEVELOPER_CODE_INDEX];
        String additionalSoftwareCode = uniqueIdParts[CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX];
        String certifiedDateCode = uniqueIdParts[CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX];

        //Ensure the new chpl product number is unique
        String chplProductNumber;
        try {
            chplProductNumber =
                    chplProductNumberUtil.generate(
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
                    if (!"99".equals(atlCode)) {
                        listing.getWarningMessages()
                        .add(msgUtil.getMessage("atl.shouldBe99"));
                    }
                } else {
                    TestingLabDTO testingLab = atlDao.getByName(testingLabs.get(0).getTestingLabName());
                    if ("99".equals(atlCode)) {
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
                        + " cannot be determined. A developer must be listed as Active in order to create certified products belongong to it.");
                    } else if (!mostRecentStatus.getStatus().getStatusName()
                            .equals(DeveloperStatusType.Active.toString())) {
                        listing.getErrorMessages().add("The developer " + developer.getName() + " has a status of "
                                + mostRecentStatus.getStatus().getStatusName()
                                + ". Certified products belonging to this developer cannot be created until its status returns to Active.");
                    }

                    if (!developer.getDeveloperCode().equals(developerCode)) {
                        listing.getErrorMessages()
                        .add("The developer code '" + developerCode
                                + "' does not match the assigned developer code for "
                                + listing.getDeveloperName() + ": '" + developer.getDeveloperCode() + "'.");
                    }
                    if (certificationBody != null) {
                        DeveloperACBMapDTO mapping = developerDao.getTransparencyMapping(developer.getId(),
                                certificationBody.getId());
                        if (mapping != null) {
                            // check transparency attestation and url for
                            // warnings
                            if ((mapping.getTransparencyAttestation() == null
                                    && listing.getTransparencyAttestation() != null)
                                    || (mapping.getTransparencyAttestation() != null
                                    && listing.getTransparencyAttestation() == null)
                                    || (mapping.getTransparencyAttestation() != null
                                    && !mapping.getTransparencyAttestation()
                                    .equals(listing.getTransparencyAttestation()))) {
                                listing.getWarningMessages().add(msgUtil.getMessage("transparencyAttestation.save"));
                            }
                        } else if (!StringUtils.isEmpty(listing.getTransparencyAttestation())) {
                            listing.getWarningMessages().add(msgUtil.getMessage(
                                    "transparencyAttestation.save"));
                        }
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

        if (!validateProductCodeCharacters(listing.getUniqueId())) {
            listing.getErrorMessages()
            .add(msgUtil.getMessage("listing.badProductCodeChars",
                    CertifiedProductDTO.PRODUCT_CODE_LENGTH));
        }

        if (!validateVersionCodeCharacters(listing.getUniqueId())) {
            listing.getErrorMessages()
            .add(msgUtil.getMessage("listing.badVersionCodeChars",
                    CertifiedProductDTO.VERSION_CODE_LENGTH));
        }

        if (!validateIcsCodeCharacters(listing.getUniqueId())) {
            listing.getErrorMessages()
            .add(msgUtil.getMessage("listing.badIcsCodeChars",
                    CertifiedProductDTO.ICS_CODE_LENGTH));
        } else {
            Integer icsCodeInteger = Integer.valueOf(uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX]);
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

        if (!validateAdditionalSoftwareCodeCharacters(listing.getUniqueId())) {
            listing.getErrorMessages()
            .add(msgUtil.getMessage("listing.badAdditionalSoftwareCodeChars",
                    CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_LENGTH));
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
                            "The unique id indicates the product does not have additional software but some is specified in the upload file.");
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
                            "The unique id indicates the product has additional software but none is specified in the upload file.");
                }
            }
        }

        if (!validateCertifiedDateCodeCharacters(listing.getUniqueId())) {
            listing.getErrorMessages()
            .add(msgUtil.getMessage("listing.badCertifiedDateCodeChars",
                    CertifiedProductDTO.CERTIFIED_DATE_CODE_LENGTH));
        }
        SimpleDateFormat idDateFormat = new SimpleDateFormat("yyMMdd");
        try {
            Date idDate = idDateFormat.parse(certifiedDateCode);
            if (listing.getCertificationDate() == null
                    || idDate.getTime() != listing.getCertificationDate().getTime()) {
                listing.getErrorMessages().add(
                        "The certification date provided in the unique id does not match the certification date in the upload file.");
            }
        } catch (final ParseException pex) {
            listing.getErrorMessages()
            .add("Could not parse the certification date part of the product id: " + certifiedDateCode);
        }
    }

    private boolean validateUniqueId(final String chplProductNumber) {
        try {
            CertifiedProductDetailsDTO dup = cpDao.getByChplUniqueId(chplProductNumber);
            if (dup != null) {
                return false;
            }
        } catch (final EntityRetrievalException ex) {
        }
        return true;
    }

    private boolean validateProductCodeCharacters(final String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {

            // validate that these pieces match up with data
            String productCode = uniqueIdParts[CertifiedProductDTO.PRODUCT_CODE_INDEX];
            if (StringUtils.isEmpty(productCode)
                    || !productCode.matches("^[a-zA-Z0-9_]{" + CertifiedProductDTO.PRODUCT_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

    private boolean validateVersionCodeCharacters(final String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {

            // validate that these pieces match up with data
            String versionCode = uniqueIdParts[CertifiedProductDTO.VERSION_CODE_INDEX];
            if (StringUtils.isEmpty(versionCode)
                    || !versionCode.matches("^[a-zA-Z0-9_]{" + CertifiedProductDTO.VERSION_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

    private boolean validateIcsCodeCharacters(final String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String icsCode = uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX];
            if (StringUtils.isEmpty(icsCode)
                    || !icsCode.matches("^[0-9]{" + CertifiedProductDTO.ICS_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

    private boolean validateAdditionalSoftwareCodeCharacters(final String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String additionalSoftwareCode = uniqueIdParts[CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX];
            if (StringUtils.isEmpty(additionalSoftwareCode) || !additionalSoftwareCode.matches("^0|1$")) {
                return false;
            }
        }
        return true;
    }

    private boolean validateCertifiedDateCodeCharacters(final String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String certifiedDateCode = uniqueIdParts[CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX];
            if (StringUtils.isEmpty(certifiedDateCode)
                    || !certifiedDateCode.matches("^[0-9]{" + CertifiedProductDTO.CERTIFIED_DATE_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

}
