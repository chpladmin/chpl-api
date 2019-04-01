package gov.healthit.chpl.upload.certifiedProduct;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.entity.CQMCriterionEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultAdditionalSoftwareEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestDataEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestFunctionalityEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestProcedureEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestStandardEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestToolEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultUcdProcessEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductQmsStandardEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCqmCriterionEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap2014Version1;

@Component("certifiedProductHandler2014Version1")
public class CertifiedProductHandler2014Version1 extends CertifiedProductHandler {

    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductHandler2014Version1.class);
    private TemplateColumnIndexMap templateColumnIndexMap;
    private String[] criteriaNames = {
            "170.314 (a)(1)", "170.314 (a)(2)", "170.314 (a)(3)", "170.314 (a)(4)", "170.314 (a)(5)",
            "170.314 (a)(6)", "170.314 (a)(7)", "170.314 (a)(8)", "170.314 (a)(9)", "170.314 (a)(10)",
            "170.314 (a)(11)", "170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(14)", "170.314 (a)(15)",
            "170.314 (a)(16)", "170.314 (a)(17)", "170.314 (a)(18)", "170.314 (a)(19)", "170.314 (a)(20)",
            "170.314 (b)(1)", "170.314 (b)(2)", "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (b)(5)(A)",
            "170.314 (b)(5)(B)", "170.314 (b)(6)", "170.314 (b)(7)", "170.314 (b)(8)", "170.314 (b)(9)",
            "170.314 (c)(1)", "170.314 (c)(2)", "170.314 (c)(3)", "170.314 (d)(1)", "170.314 (d)(2)",
            "170.314 (d)(3)", "170.314 (d)(4)", "170.314 (d)(5)", "170.314 (d)(6)", "170.314 (d)(7)",
            "170.314 (d)(8)", "170.314 (d)(9)", "170.314 (e)(1)", "170.314 (e)(2)", "170.314 (e)(3)",
            "170.314 (f)(1)", "170.314 (f)(2)", "170.314 (f)(3)", "170.314 (f)(4)", "170.314 (f)(5)",
            "170.314 (f)(6)", "170.314 (f)(7)", "170.314 (g)(1)", "170.314 (g)(2)", "170.314 (g)(3)",
            "170.314 (g)(4)", "170.314 (h)(1)",  "170.314 (h)(2)", "170.314 (h)(3)"
    };

    public CertifiedProductHandler2014Version1() {
        templateColumnIndexMap = new TemplateColumnIndexMap2014Version1();
    }

    public TemplateColumnIndexMap getColumnIndexMap() {
        return templateColumnIndexMap;
    }

    public String[] getCriteriaNames() {
        return this.criteriaNames;
    }

    public PendingCertifiedProductEntity handle() {
        PendingCertifiedProductEntity pendingCertifiedProduct = new PendingCertifiedProductEntity();

        // get the first row of the certified product
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(1);
            if (!StringUtils.isEmpty(statusStr) && FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)) {
                parseCertifiedProductDetails(record, pendingCertifiedProduct);
            }
        }

        // get ATL(s) for the certified product
        for (CSVRecord record: getRecord()) {
            String statusStr = record.get(getColumnIndexMap().getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseAtl(pendingCertifiedProduct, record);
            }
        }

        // get the QMS's for the certified product
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(1);
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseQms(record, pendingCertifiedProduct);
            }
        }

        // parse CQMs
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(1);
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseCqms(record, pendingCertifiedProduct);
            }
        }

        // parse criteria
        CSVRecord firstRow = null;
        for (int i = 0; i < getRecord().size() && firstRow == null; i++) {
            CSVRecord currRecord = getRecord().get(i);
            String statusStr = currRecord.get(1);
            if (!StringUtils.isEmpty(statusStr) && FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)) {
                firstRow = currRecord;
            }
        }
        if (firstRow != null) {
            int criteriaBeginIndex = getColumnIndexMap().getCriteriaStartIndex();
            for (int i = 0; i < getCriteriaNames().length; i++) {
                String criteriaName = getCriteriaNames()[i];
                if (criteriaName != null) {
                    int criteriaEndIndex = getColumnIndexMap()
                            .getLastIndexForCriteria(getHeading(), criteriaBeginIndex);
                    pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct,
                            criteriaName, firstRow, criteriaBeginIndex, criteriaEndIndex));
                    criteriaBeginIndex = criteriaEndIndex + 1;
                }
            }
        }

        return pendingCertifiedProduct;
    }

    private void parseCertifiedProductDetails(final CSVRecord record,
            final PendingCertifiedProductEntity pendingCertifiedProduct) {
        parseUniqueId(pendingCertifiedProduct, record);
        parseRecordStatus(pendingCertifiedProduct, record);
        parsePracticeType(pendingCertifiedProduct, record);
        parseDeveloperProductVersion(pendingCertifiedProduct, record);
        parseDeveloperAddress(pendingCertifiedProduct, record);
        parseEdition("2014", pendingCertifiedProduct, record);
        parseAcbCertificationId(pendingCertifiedProduct, record);
        parseAcb(pendingCertifiedProduct, record);
        parseProductClassification(pendingCertifiedProduct, record);
        parseCertificationDate(pendingCertifiedProduct, record);
        parseSed(pendingCertifiedProduct, record);
        parseHasQms(pendingCertifiedProduct, record);
        parseHasIcs(pendingCertifiedProduct, record);
        parseTransparencyAttestation(pendingCertifiedProduct, record);
    }

    protected void parseSed(final PendingCertifiedProductEntity pendingCertifiedProduct, final CSVRecord record) {
        int sedIndex = getColumnIndexMap().getSedStartIndex();
        // report file location
        pendingCertifiedProduct.setReportFileLocation(record.get(sedIndex++).trim());
        // sed report link
        pendingCertifiedProduct.setSedReportFileLocation(record.get(sedIndex).trim());
    }

    private void parseQms(final CSVRecord record, final PendingCertifiedProductEntity pendingCertifiedProduct) {
        int colIndex = getColumnIndexMap().getQmsStartIndex() + 1;
        if (!StringUtils.isEmpty(record.get(colIndex))) {
            String qmsStandardName = record.get(colIndex++).trim();
            QmsStandardDTO qmsStandard = qmsDao.getByName(qmsStandardName);
            String qmsMods = record.get(colIndex).trim();

            PendingCertifiedProductQmsStandardEntity qmsEntity = new PendingCertifiedProductQmsStandardEntity();
            qmsEntity.setMappedProduct(pendingCertifiedProduct);
            qmsEntity.setModification(qmsMods);
            qmsEntity.setName(qmsStandardName);
            if (qmsStandard != null) {
                qmsEntity.setQmsStandardId(qmsStandard.getId());
            }
            pendingCertifiedProduct.getQmsStandards().add(qmsEntity);
        }
    }

    private void parseCqms(final CSVRecord record, final PendingCertifiedProductEntity pendingCertifiedProduct) {
        int cqmNameIndex = getColumnIndexMap().getCqmStartIndex();
        int cqmVersionIndex = cqmNameIndex + 1;

        String cqmName = record.get(cqmNameIndex).trim();
        String cqmVersions = record.get(cqmVersionIndex).trim();

        List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion(pendingCertifiedProduct, cqmName,
                cqmVersions);
        for (PendingCqmCriterionEntity entity : criterion) {
            if (entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria()) {
                pendingCertifiedProduct.getCqmCriterion().add(entity);
            }
        }
    }

    private PendingCertificationResultEntity parseCriteria(final PendingCertifiedProductEntity pendingCertifiedProduct,
            final String criteriaNumber, final CSVRecord firstRow, final int beginIndex, final int endIndex) {
        int currIndex = beginIndex;
        PendingCertificationResultEntity cert = null;
        try {
            cert = getCertificationResult(criteriaNumber, firstRow.get(currIndex++).trim());

            while (currIndex <= endIndex) {
                String colTitle = getHeading().get(currIndex);
                if (!StringUtils.isEmpty(colTitle)) {
                    colTitle = colTitle.trim().toUpperCase();
                    if (colTitle.equalsIgnoreCase(getColumnIndexMap().getGapColumnLabel())) {
                        cert.setGap(asBoolean(firstRow.get(currIndex).trim()));
                        currIndex += getColumnIndexMap().getGapColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getTestStandardsColumnLabel())) {
                        parseTestStandards(pendingCertifiedProduct, cert, currIndex);
                        currIndex += getColumnIndexMap().getTestStandardsColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getTestFunctionalityColumnLabel())) {
                        parseTestFunctionality(pendingCertifiedProduct, cert, currIndex);
                        currIndex += getColumnIndexMap().getTestFunctionalityColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getG1MeasureColumnLabel())) {
                        if (firstRow.get(currIndex) == null && StringUtils.isEmpty(firstRow.get(currIndex).trim())) {
                            cert.setG1Success(null);
                        }  else {
                            cert.setG1Success(asBoolean(firstRow.get(currIndex).trim()));
                        }
                        currIndex += getColumnIndexMap().getG1MeasureColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getG2MeasureColumnLabel())) {
                        if (firstRow.get(currIndex) == null && StringUtils.isEmpty(firstRow.get(currIndex).trim())) {
                            cert.setG2Success(null);
                        }  else {
                            cert.setG2Success(asBoolean(firstRow.get(currIndex).trim()));
                        }
                        currIndex += getColumnIndexMap().getG2MeasureColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getAdditionalSoftwareColumnLabel())) {
                        Boolean hasAdditionalSoftware = asBoolean(firstRow.get(currIndex).trim());
                        cert.setHasAdditionalSoftware(hasAdditionalSoftware);
                        parseAdditionalSoftware(pendingCertifiedProduct, cert, currIndex);
                        currIndex += getColumnIndexMap().getAdditionalSoftwareColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getTestToolColumnLabel())) {
                        parseTestTools(pendingCertifiedProduct, cert, currIndex);
                        currIndex += getColumnIndexMap().getTestToolColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getTestProcedureVersionColumnLabel())) {
                        parseTestProcedures(cert, currIndex);
                        currIndex += getColumnIndexMap().getTestProcedureVersionColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getTestDataColumnLabel())) {
                        parseTestData(pendingCertifiedProduct, cert, currIndex);
                        currIndex += getColumnIndexMap().getTestDataColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getUcdColumnLabel())) {
                        cert.setSed(asBoolean(firstRow.get(currIndex).trim()));
                        String ucdProcessName = firstRow.get(currIndex + 1).trim();
                        String ucdProcessDetails = firstRow.get(currIndex + 2).trim();
                        if (!StringUtils.isEmpty(ucdProcessName)) {
                            PendingCertificationResultUcdProcessEntity ucd =
                                    new PendingCertificationResultUcdProcessEntity();
                            ucd.setUcdProcessName(ucdProcessName);
                            ucd.setUcdProcessDetails(ucdProcessDetails);
                            UcdProcessDTO dto = ucdDao.getByName(ucd.getUcdProcessName());
                            if (dto != null) {
                                ucd.setUcdProcessId(dto.getId());
                            }
                            cert.getUcdProcesses().add(ucd);
                        }
                        currIndex += getColumnIndexMap().getUcdColumnCount();
                    } else {
                        pendingCertifiedProduct.getErrorMessages()
                        .add("Invalid column title " + colTitle + " at index " + currIndex);
                        LOGGER.error("Could not handle column " + colTitle + " at index " + currIndex + ".");
                        currIndex++;
                    }
                }
            }
        } catch (final InvalidArgumentsException ex) {
            LOGGER.error(ex.getMessage());
        }
        return cert;
    }

    private void parseTestStandards(final PendingCertifiedProductEntity listing,
            final PendingCertificationResultEntity cert, final int tsColumn) {
        for (CSVRecord row : getRecord()) {
            String tsValue = row.get(tsColumn).trim();
            if (!StringUtils.isEmpty(tsValue)) {
                PendingCertificationResultTestStandardEntity tsEntity =
                        new PendingCertificationResultTestStandardEntity();
                tsEntity.setTestStandardName(tsValue);
                TestStandardDTO ts = testStandardDao.getByNumberAndEdition(tsValue,
                        listing.getCertificationEditionId());
                if (ts != null) {
                    tsEntity.setTestStandardId(ts.getId());
                }
                cert.getTestStandards().add(tsEntity);
            }
        }
    }

    private void parseTestFunctionality(final PendingCertifiedProductEntity listing,
            final PendingCertificationResultEntity cert, final int tfColumn) {
        for (CSVRecord row : getRecord()) {
            String tfValue = row.get(tfColumn).trim();
            if (!StringUtils.isEmpty(tfValue)) {
                PendingCertificationResultTestFunctionalityEntity tfEntity =
                        new PendingCertificationResultTestFunctionalityEntity();
                tfEntity.setTestFunctionalityNumber(tfValue);
                TestFunctionalityDTO tf = testFunctionalityDao.getByNumberAndEdition(tfValue,
                        listing.getCertificationEditionId());
                if (tf != null) {
                    tfEntity.setTestFunctionalityId(tf.getId());
                }
                cert.getTestFunctionality().add(tfEntity);
            }
        }
    }

    private void parseAdditionalSoftware(final PendingCertifiedProductEntity product,
            final PendingCertificationResultEntity cert, final int asColumnBegin) {
        int cpSourceColumn = asColumnBegin + 1;
        int nonCpSourceColumn = asColumnBegin + 2;

        for (CSVRecord row : getRecord()) {
            String cpSourceValue = row.get(cpSourceColumn).trim();
            if (!StringUtils.isEmpty(cpSourceValue)) {
                PendingCertificationResultAdditionalSoftwareEntity asEntity =
                        new PendingCertificationResultAdditionalSoftwareEntity();
                asEntity.setChplId(cpSourceValue);
                if (cpSourceValue.startsWith("CHP-")) {
                    CertifiedProductDTO cp = certifiedProductDao.getByChplNumber(cpSourceValue);
                    if (cp != null) {
                        asEntity.setCertifiedProductId(cp.getId());
                    }
                } else {
                    try {
                        CertifiedProductDetailsDTO cpd = certifiedProductDao.getByChplUniqueId(cpSourceValue);
                        if (cpd != null) {
                            asEntity.setCertifiedProductId(cpd.getId());
                        }
                    } catch (final EntityRetrievalException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }
                }
                cert.getAdditionalSoftware().add(asEntity);
            }
            String nonCpSourceValue = row.get(nonCpSourceColumn).trim();
            if (!StringUtils.isEmpty(nonCpSourceValue)) {
                PendingCertificationResultAdditionalSoftwareEntity asEntity =
                        new PendingCertificationResultAdditionalSoftwareEntity();
                asEntity.setSoftwareName(nonCpSourceValue);
                asEntity.setSoftwareVersion(row.get(nonCpSourceColumn + 1).trim());
                cert.getAdditionalSoftware().add(asEntity);
            }
        }

        if (cert.getHasAdditionalSoftware() != null && cert.getHasAdditionalSoftware().booleanValue()
                && cert.getAdditionalSoftware().size() == 0) {
            product.getErrorMessages().add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                    + product.getUniqueId() + " indicates additional software should be present but none was found.");
        } else if ((cert.getHasAdditionalSoftware() == null || !cert.getHasAdditionalSoftware().booleanValue())
                && cert.getAdditionalSoftware().size() > 0) {
            product.getErrorMessages()
            .add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                    + product.getUniqueId()
                    + " indicates additional software should not be present but some was found.");
        }
    }

    private void parseTestProcedures(final PendingCertificationResultEntity cert, final int tpColumn) {
        for (CSVRecord row : getRecord()) {
            String tpValue = row.get(tpColumn).trim();
            if (!StringUtils.isEmpty(tpValue)) {
                PendingCertificationResultTestProcedureEntity tpEntity
                = new PendingCertificationResultTestProcedureEntity();
                //there will only be 1 for 2014 entries
                List<TestProcedureDTO> allowedTestProcedures =
                        testProcedureDao.getByCriteriaNumber(cert.getMappedCriterion().getNumber());
                if (allowedTestProcedures.size() > 0) {
                    tpEntity.setTestProcedureId(allowedTestProcedures.get(0).getId());
                }
                tpEntity.setVersion(tpValue);
                cert.getTestProcedures().add(tpEntity);
            }
        }
    }

    private void parseTestData(final PendingCertifiedProductEntity product, final PendingCertificationResultEntity cert,
            final int tdColumnBegin) {
        for (CSVRecord row : getRecord()) {
            String tdVersionValue = row.get(tdColumnBegin).trim();
            if (!StringUtils.isEmpty(tdVersionValue)) {
                PendingCertificationResultTestDataEntity tdEntity = new PendingCertificationResultTestDataEntity();
                List<TestDataDTO> allowedTestData =
                        testDataDao.getByCriteriaNumber(cert.getMappedCriterion().getNumber());
                if (allowedTestData.size() > 0) {
                    tdEntity.setTestDataId(allowedTestData.get(0).getId());
                }
                tdEntity.setVersion(tdVersionValue);
                Boolean hasAlteration = asBoolean(row.get(tdColumnBegin + 1).trim());
                tdEntity.setHasAlteration(hasAlteration);
                String alterationStr = row.get(tdColumnBegin + 2).trim();
                if (tdEntity.isHasAlteration() && StringUtils.isEmpty(alterationStr)) {
                    product.getErrorMessages()
                    .add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                            + product.getUniqueId()
                            + " indicates test data was altered however no test data alteration was found.");
                } else if (!tdEntity.isHasAlteration() && !StringUtils.isEmpty(alterationStr)) {
                    product.getErrorMessages()
                    .add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                            + product.getUniqueId()
                            + " indicates test data was not altered however a test data alteration was found.");
                }
                tdEntity.setAlteration(row.get(tdColumnBegin + 2).trim());
                cert.getTestData().add(tdEntity);
            }
        }
    }

    private void parseTestTools(final PendingCertifiedProductEntity product,
            final PendingCertificationResultEntity cert, final int toolColumnBegin) {
        for (CSVRecord row : getRecord()) {
            String testToolName = row.get(toolColumnBegin).trim();
            String testToolVersion = row.get(toolColumnBegin + 1).trim();
            if (!StringUtils.isEmpty(testToolName)) {
                PendingCertificationResultTestToolEntity ttEntity = new PendingCertificationResultTestToolEntity();
                ttEntity.setTestToolName(testToolName);
                ttEntity.setTestToolVersion(testToolVersion);
                TestToolDTO testTool = testToolDao.getByName(testToolName);
                if (testTool != null) {
                    ttEntity.setTestToolId(testTool.getId());
                }
                cert.getTestTools().add(ttEntity);
            }
        }
    }

    /**
     * look up a CQM CMS criteria by number and version. throw an error if we
     * can't find it
     *
     * @param criterionNum
     * @param column
     * @return
     * @throws InvalidArgumentsException
     */
    protected List<PendingCqmCriterionEntity> handleCqmCmsCriterion(final PendingCertifiedProductEntity product,
            String criterionNum, String version) {
        if (!StringUtils.isEmpty(version)) {
            version = version.trim();
        }

        List<PendingCqmCriterionEntity> result = new ArrayList<PendingCqmCriterionEntity>();

        if (!StringUtils.isEmpty(version) && !"0".equals(version)) {
            // split on ;
            String[] versionList = version.split(";");
            if (versionList.length == 1) {
                // also try splitting on ,
                versionList = version.split(",");
            }

            for (int i = 0; i < versionList.length; i++) {
                String currVersion = versionList[i].trim();
                if (!criterionNum.startsWith("CMS")) {
                    criterionNum = "CMS" + criterionNum;
                }
                CQMCriterionEntity cqmEntity = cqmDao.getCMSEntityByNumberAndVersion(criterionNum, currVersion);
                if (cqmEntity == null) {
                    product.getErrorMessages().add("Could not find a CQM CMS criterion matching " + criterionNum
                            + " and version " + currVersion);
                }

                PendingCqmCriterionEntity currResult = new PendingCqmCriterionEntity();
                currResult.setMappedCriterion(cqmEntity);
                currResult.setMeetsCriteria(true);
                result.add(currResult);
            }
        }

        return result;
    }
}
