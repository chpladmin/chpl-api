package gov.healthit.chpl.upload.certifiedProduct;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.ProductClassificationTypeDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductTestingLabMapEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("certifiedProductHandler")
public abstract class CertifiedProductHandler extends CertifiedProductUploadHandler {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductHandler.class);
    protected static final String PRACTICE_TYPE_AMBULATORY = "AMBULATORY";
    protected static final String PRACTICE_TYPE_INPATIENT = "INPATIENT";
    protected static final String FIRST_ROW_INDICATOR = "NEW";
    protected static final String SUBSEQUENT_ROW_INDICATOR = "SUBELEMENT";
    protected static final String CRITERIA_COL_HEADING_BEGIN = "CRITERIA_";
    protected static final String CURES_TITLE_KEY = "Cures Update";

    protected ErrorMessageUtil msgUtil;

    @Autowired
    public CertifiedProductHandler(final ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public abstract PendingCertifiedProductEntity handle() throws InvalidArgumentsException;

    public abstract TemplateColumnIndexMap getColumnIndexMap();

    public String getErrorMessage(final String errorField) {
        return msgUtil.getMessage(errorField);
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

        Developer foundDeveloper = developerDao.getByName(developer);
        if (foundDeveloper != null) {
            pendingCertifiedProduct.setDeveloperId(foundDeveloper.getId());

            // product
            Product foundProduct = productDao.getByDeveloperAndName(foundDeveloper.getId(), product);
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

    protected void parseDeveloperDetails(PendingCertifiedProductEntity pendingCertifiedProduct,
            CSVRecord record) {
        int devStartIndex = getColumnIndexMap().getDeveloperStartIndex();
        String developerStreetAddress = record.get(devStartIndex++).trim();
        String developerState = record.get(devStartIndex++).trim();
        String developerCity = record.get(devStartIndex++).trim();
        String developerZipcode = record.get(devStartIndex++).trim();
        String developerWebsite = record.get(devStartIndex++).trim();
        String developerEmail = record.get(devStartIndex++).trim();
        String developerPhone = record.get(devStartIndex++).trim();
        String developerContactName = record.get(devStartIndex++).trim();
        pendingCertifiedProduct.setDeveloperStreetAddress(developerStreetAddress);
        pendingCertifiedProduct.setDeveloperCity(developerCity);
        pendingCertifiedProduct.setDeveloperState(developerState);
        pendingCertifiedProduct.setDeveloperZipCode(developerZipcode);
        pendingCertifiedProduct.setDeveloperWebsite(developerWebsite);
        pendingCertifiedProduct.setDeveloperEmail(developerEmail);
        pendingCertifiedProduct.setDeveloperPhoneNumber(developerPhone);
        pendingCertifiedProduct.setDeveloperContactName(developerContactName);

        // look for contact in db
        PointOfContact contactToFind = new PointOfContact();
        contactToFind.setFullName(developerContactName);
        contactToFind.setEmail(developerEmail);
        contactToFind.setPhoneNumber(developerPhone);
        PointOfContact foundContact = contactDao.getByValues(contactToFind);
        if (foundContact != null) {
            pendingCertifiedProduct.setDeveloperContactId(foundContact.getContactId());
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

    protected void parseMandatoryDisclosures(final PendingCertifiedProductEntity pendingCertifiedProduct,
            final CSVRecord record) {
        // (k)(1) attestation url
        pendingCertifiedProduct.setMandatoryDisclosures(record.get(getColumnIndexMap().getK1Index()).trim());
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

    protected PendingCertificationResultEntity getCertificationResult(Long criterionId,
            String columnValue) throws InvalidArgumentsException {
        CertificationCriterionEntity certEntity = null;
        try {
            certEntity = certDao.getEntityById(criterionId);
        } catch (EntityRetrievalException ex) {
            throw new InvalidArgumentsException("Could not find a certification criterion with ID " + criterionId);
        }

        PendingCertificationResultEntity result = new PendingCertificationResultEntity();
        result.setMappedCriterion(certEntity);
        result.setMeetsCriteria(asBoolean(columnValue));
        return result;
    }

    protected PendingCertificationResultEntity getCertificationResult(String criterionName,
            String columnValue) throws InvalidArgumentsException {
        CertificationCriterionEntity certEntity = getCriterion(criterionName, false);

        PendingCertificationResultEntity result = new PendingCertificationResultEntity();
        result.setMappedCriterion(certEntity);
        result.setMeetsCriteria(asBoolean(columnValue));
        return result;
    }

    protected PendingCertificationResultEntity getCertificationResultCures(String criterionName,
            String columnValue) throws InvalidArgumentsException {
        CertificationCriterionEntity certEntity = getCriterion(criterionName, true);

        PendingCertificationResultEntity result = new PendingCertificationResultEntity();
        result.setMappedCriterion(certEntity);
        result.setMeetsCriteria(asBoolean(columnValue));
        return result;
    }

    protected CertificationCriterionEntity getCriterion(String criterionName, boolean isCures)
            throws InvalidArgumentsException {
        List<CertificationCriterionEntity> certEntities = certDao.getEntitiesByNumber(criterionName);
        if (certEntities == null || certEntities.size() == 0) {
            throw new InvalidArgumentsException("Could not find a certification criterion matching " + criterionName);
        }

        Optional<CertificationCriterionEntity> criterion = null;
        if (isCures) {
            criterion = certEntities.stream()
                    .filter(certEntity -> !StringUtils.isEmpty(certEntity.getTitle())
                    && certEntity.getTitle().contains(CURES_TITLE_KEY))
                    .findFirst();
        } else {
            criterion = certEntities.stream()
                    .filter(certEntity -> !StringUtils.isEmpty(certEntity.getTitle())
                    && !certEntity.getTitle().contains(CURES_TITLE_KEY))
                    .findFirst();
        }

        if (criterion == null || !criterion.isPresent()) {
            throw new InvalidArgumentsException("Could not find a certification criterion (cures="
                    + isCures + ") matching " + criterionName);
        }
        return criterion.get();
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
