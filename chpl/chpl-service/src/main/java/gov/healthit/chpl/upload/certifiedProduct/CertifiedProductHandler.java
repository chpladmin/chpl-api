package gov.healthit.chpl.upload.certifiedProduct;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.ProductClassificationTypeDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductTestingLabMapEntity;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("certifiedProductHandler")
public abstract class CertifiedProductHandler extends CertifiedProductUploadHandlerImpl {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductHandler.class);
    protected static final String PRACTICE_TYPE_AMBULATORY = "AMBULATORY";
    protected static final String PRACTICE_TYPE_INPATIENT = "INPATIENT";
    protected static final String FIRST_ROW_INDICATOR = "NEW";
    protected static final String SUBSEQUENT_ROW_INDICATOR = "SUBELEMENT";
    protected static final String CRITERIA_COL_HEADING_BEGIN = "CRITERIA_";

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ErrorMessageUtil msgUtil;

    @Override
    public abstract PendingCertifiedProductEntity handle() throws InvalidArgumentsException;

    public abstract TemplateColumnIndexMap getColumnIndexMap();

    public abstract String[] getCriteriaNames();

    public String getErrorMessage(final String errorField) {
        return String.format(messageSource.getMessage(new DefaultMessageSourceResolvable(errorField),
                LocaleContextHolder.getLocale()));
    }

    protected void parseUniqueId(final PendingCertifiedProductEntity pendingCertifiedProduct, final CSVRecord record) {
        String uniqueId = record.get(getColumnIndexMap().getUniqueIdIndex()).trim();
        pendingCertifiedProduct.setUniqueId(uniqueId);
    }

    protected void parseRecordStatus(final PendingCertifiedProductEntity pendingCertifiedProduct,
            final CSVRecord record) {
        String status = record.get(getColumnIndexMap().getRecordStatusIndex()).trim();
        pendingCertifiedProduct.setRecordStatus(status);
    }

    protected void parsePracticeType(final PendingCertifiedProductEntity pendingCertifiedProduct,
            final CSVRecord record) {
        PracticeTypeDTO foundPracticeType = null;
        String practiceType = record.get(getColumnIndexMap().getPracticeTypeIndex()).trim();
        pendingCertifiedProduct.setPracticeType(practiceType);
        if (!practiceType.equals("")) {
            foundPracticeType = practiceTypeDao.getByName(practiceType);
        }
        if (foundPracticeType != null) {
            pendingCertifiedProduct.setPracticeTypeId(foundPracticeType.getId());
        }
    }

    protected void parseDeveloperProductVersion(final PendingCertifiedProductEntity pendingCertifiedProduct,
            final CSVRecord record) {
        String developer = record.get(getColumnIndexMap().getDeveloperIndex()).trim();
        String product = record.get(getColumnIndexMap().getProductIndex()).trim();
        String productVersion = record.get(getColumnIndexMap().getVersionIndex()).trim();
        pendingCertifiedProduct.setDeveloperName(developer);
        pendingCertifiedProduct.setProductName(product);
        pendingCertifiedProduct.setProductVersion(productVersion);

        DeveloperDTO foundDeveloper = developerDao.getByName(developer);
        if (foundDeveloper != null) {
            pendingCertifiedProduct.setDeveloperId(foundDeveloper.getId());

            // product
            ProductDTO foundProduct = productDao.getByDeveloperAndName(foundDeveloper.getId(), product);
            if (foundProduct != null) {
                pendingCertifiedProduct.setProductId(foundProduct.getId());

                // version
                ProductVersionDTO foundVersion = versionDao.getByProductAndVersion(foundProduct.getId(),
                        productVersion);
                if (foundVersion != null) {
                    pendingCertifiedProduct.setProductVersionId(foundVersion.getId());
                }
            }
        }
    }

    protected void parseDeveloperAddress(final PendingCertifiedProductEntity pendingCertifiedProduct,
            final CSVRecord record) {
        int devAddressIndex = getColumnIndexMap().getDeveloperAddressStartIndex();
        String developerStreetAddress = record.get(devAddressIndex++).trim();
        String developerState = record.get(devAddressIndex++).trim();
        String developerCity = record.get(devAddressIndex++).trim();
        String developerZipcode = record.get(devAddressIndex++).trim();
        String developerWebsite = record.get(devAddressIndex++).trim();
        String developerEmail = record.get(devAddressIndex++).trim();
        String developerPhone = record.get(devAddressIndex++).trim();
        String developerContactName = record.get(devAddressIndex++).trim();
        pendingCertifiedProduct.setDeveloperStreetAddress(developerStreetAddress);
        pendingCertifiedProduct.setDeveloperCity(developerCity);
        pendingCertifiedProduct.setDeveloperState(developerState);
        pendingCertifiedProduct.setDeveloperZipCode(developerZipcode);
        pendingCertifiedProduct.setDeveloperWebsite(developerWebsite);
        pendingCertifiedProduct.setDeveloperEmail(developerEmail);
        pendingCertifiedProduct.setDeveloperPhoneNumber(developerPhone);
        pendingCertifiedProduct.setDeveloperContactName(developerContactName);

        // look for contact in db
        ContactDTO contactToFind = new ContactDTO();
        contactToFind.setFriendlyName(developerContactName);
        contactToFind.setEmail(developerEmail);
        contactToFind.setPhoneNumber(developerPhone);
        ContactDTO foundContact = contactDao.getByValues(contactToFind);
        if (foundContact != null) {
            pendingCertifiedProduct.setDeveloperContactId(foundContact.getId());
        }
    }

    protected void parseEdition(final String expected, final PendingCertifiedProductEntity pendingCertifiedProduct,
            final CSVRecord record) {
        String certificaitonYear = record.get(getColumnIndexMap().getEditionIndex()).trim();
        pendingCertifiedProduct.setCertificationEdition(certificaitonYear);
        if (!pendingCertifiedProduct.getCertificationEdition().equals(expected.trim())) {
            pendingCertifiedProduct.getErrorMessages()
                    .add("Expecting certification year " + expected.trim() + " but found '"
                            + pendingCertifiedProduct.getCertificationEdition() + "' for product "
                            + pendingCertifiedProduct.getUniqueId());
        }
        CertificationEditionDTO foundEdition = editionDao.getByYear(certificaitonYear);
        if (foundEdition != null) {
            pendingCertifiedProduct.setCertificationEditionId(foundEdition.getId());
        }
    }

    protected void parseAcbCertificationId(final PendingCertifiedProductEntity pendingCertifiedProduct,
            final CSVRecord record) {
        pendingCertifiedProduct
                .setAcbCertificationId(record.get(getColumnIndexMap().getAcbCertificationIdIndex()).trim());
    }

    protected void parseAcb(final PendingCertifiedProductEntity pendingCertifiedProduct, final CSVRecord record) {
        String acbName = record.get(getColumnIndexMap().getAcbIndex()).trim();
        pendingCertifiedProduct.setCertificationBodyName(acbName);
        CertificationBodyDTO foundAcb = acbDao.getByName(acbName);
        if (foundAcb != null) {
            pendingCertifiedProduct.setCertificationBodyId(foundAcb.getId());
        } else {
            pendingCertifiedProduct.getErrorMessages()
                    .add("No certification body with name " + acbName + " could be found.");
        }
    }

    /**
     * Parse ATL(s).
     * 
     * @param pendingCertifiedProduct
     *            the pending product
     * @param record
     *            the record to parse
     */
    protected void parseAtl(final PendingCertifiedProductEntity pendingCertifiedProduct, final CSVRecord record) {
        String atlName = record.get(getColumnIndexMap().getAtlIndex()).trim();
        if (!StringUtils.isEmpty(atlName)) {
            TestingLabDTO foundAtl = atlDao.getByName(atlName);
            PendingCertifiedProductTestingLabMapEntity tlEntity = new PendingCertifiedProductTestingLabMapEntity();
            tlEntity.setMappedProduct(pendingCertifiedProduct);
            if (foundAtl != null) {
                tlEntity.setTestingLabId(foundAtl.getId());
                tlEntity.setTestingLabName(foundAtl.getName());
            } else {
                pendingCertifiedProduct.getErrorMessages()
                        .add("No testing lab with name " + atlName + " could be found.");
            }
            pendingCertifiedProduct.getTestingLabs().add(tlEntity);
        }
    }

    protected void parseProductClassification(final PendingCertifiedProductEntity pendingCertifiedProduct,
            final CSVRecord record) {
        String classification = record.get(getColumnIndexMap().getProductClassificationIndex()).trim();
        pendingCertifiedProduct.setProductClassificationName(classification);
        ProductClassificationTypeDTO foundClassification = classificationDao.getByName(classification);
        if (foundClassification != null) {
            pendingCertifiedProduct.setProductClassificationId(foundClassification.getId());
        }
    }

    protected void parseCertificationDate(final PendingCertifiedProductEntity pendingCertifiedProduct,
            final CSVRecord record) {
        String dateStr = record.get(getColumnIndexMap().getCertificationDateIndex()).trim();
        try {
            Date certificationDate = dateFormatter.parse(dateStr);
            pendingCertifiedProduct.setCertificationDate(certificationDate);
        } catch (final ParseException ex) {
            pendingCertifiedProduct.getErrorMessages().add(msgUtil.getMessage("listing.badCertificationDate", dateStr));
            pendingCertifiedProduct.setCertificationDate(null);
        }
    }

    protected void parseHasQms(final PendingCertifiedProductEntity pendingCertifiedProduct, final CSVRecord record) {
        String hasQmsStr = record.get(getColumnIndexMap().getQmsStartIndex());
        Boolean hasQms = asBoolean(hasQmsStr);
        if (hasQms != null) {
            pendingCertifiedProduct.setHasQms(hasQms);
        }
    }

    protected void parseHasIcs(final PendingCertifiedProductEntity pendingCertifiedProduct, final CSVRecord record) {
        String hasIcsStr = record.get(getColumnIndexMap().getIcsStartIndex()).trim();
        pendingCertifiedProduct.setIcs(asBoolean(hasIcsStr));
    }

    protected void parseTransparencyAttestation(final PendingCertifiedProductEntity pendingCertifiedProduct,
            final CSVRecord record) {
        // (k)(1) attestation url
        pendingCertifiedProduct.setTransparencyAttestationUrl(record.get(getColumnIndexMap().getK1Index()).trim());

        // (k)(2) attestation status
        String k2AttestationStr = record.get(getColumnIndexMap().getK2Index()).trim();
        if (!StringUtils.isEmpty(k2AttestationStr)) {
            if ("0".equals(k2AttestationStr.trim())) {
                pendingCertifiedProduct.setTransparencyAttestation(AttestationType.Negative);
            } else if ("1".equals(k2AttestationStr.trim())) {
                pendingCertifiedProduct.setTransparencyAttestation(AttestationType.Affirmative);
            } else if ("2".equals(k2AttestationStr.trim())) {
                pendingCertifiedProduct.setTransparencyAttestation(AttestationType.NA);
            }
        } else {
            pendingCertifiedProduct.setTransparencyAttestation(null);
        }
    }

    @Override
    public List<CQMCriterion> getApplicableCqmCriterion(final List<CQMCriterion> allCqms) {
        List<CQMCriterion> criteria = new ArrayList<CQMCriterion>();
        for (CQMCriterion criterion : allCqms) {
            if (!StringUtils.isEmpty(criterion.getCmsId()) && criterion.getCmsId().startsWith("CMS")) {
                criteria.add(criterion);
            }
        }
        return criteria;
    }

    /**
     * look up the certification criteria by name and throw an error if we can't
     * find it
     *
     * @param criterionName
     * @param column
     * @return
     * @throws InvalidArgumentsException
     */
    protected PendingCertificationResultEntity getCertificationResult(final String criterionName,
            final String columnValue) throws InvalidArgumentsException {
        CertificationCriterionEntity certEntity = certDao.getEntityByName(criterionName);
        if (certEntity == null) {
            throw new InvalidArgumentsException("Could not find a certification criterion matching " + criterionName);
        }

        PendingCertificationResultEntity result = new PendingCertificationResultEntity();
        result.setMappedCriterion(certEntity);
        result.setMeetsCriteria(asBoolean(columnValue));
        return result;
    }

    protected Boolean asBoolean(final String value) {
        if (StringUtils.isEmpty(value.trim())) {
            return false;
        }
        return parseBoolean(value);
    }

    protected Boolean parseBoolean(final String value) {
        // look for a string
        if (value.equalsIgnoreCase("t") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("y")) {
            return true;
        }
        if (value.equalsIgnoreCase("f") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no")
                || value.equalsIgnoreCase("n")) {
            return false;
        }

        try {
            double numValue = Double.parseDouble(value);
            if (numValue > 0) {
                return true;
            }
        } catch (final NumberFormatException ex) {
            LOGGER.error("Could not parse " + value + " as an integer");
        }

        return false;
    }
}
