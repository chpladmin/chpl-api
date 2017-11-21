package gov.healthit.chpl.upload.certifiedProduct;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestDataEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestProcedureEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultUcdProcessEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductParentListingEntity;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap2015Version2;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap2015Version3;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

/**
 * Adds ICS Source (family), Removes columns G1 and G2 for 170.315(g)(7), 
 * Adds Test Tool + Test Tool Version + Test Data Version, Test Data Alteration, Test Data Alteration Description for criteria b8,
 * Removes Test Tool + Test Tool Version + Test Data fields for criteria f5
 * @author kekey
 *
 */
@Component("certifiedProductHandler2015Version2")
public class CertifiedProductHandler2015Version3 extends CertifiedProductHandler2015Version2 {

    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductHandler2015Version3.class);
    private TemplateColumnIndexMap templateColumnIndexMap;
    
    public CertifiedProductHandler2015Version3() {
        templateColumnIndexMap = new TemplateColumnIndexMap2015Version3();
    }
    
    @Override
    public TemplateColumnIndexMap getColumnIndexMap() {
        return templateColumnIndexMap;
    }
    
    @Override
    protected PendingCertificationResultEntity parseCriteria(PendingCertifiedProductEntity pendingCertifiedProduct,
            String criteriaNumber, CSVRecord firstRow, int beginIndex, int endIndex) {
        int currIndex = beginIndex;
        PendingCertificationResultEntity cert = null;
        try {
            cert = getCertificationResult(criteriaNumber, firstRow.get(currIndex++).toString());

            while (currIndex <= endIndex) {
                String colTitle = getHeading().get(currIndex).trim();
                if (!StringUtils.isEmpty(colTitle)) {
                    colTitle = colTitle.trim().toUpperCase();
                    switch (colTitle) {
                    case "GAP":
                        cert.setGap(asBoolean(firstRow.get(currIndex++).trim()));
                        break;
                    case "PRIVACY AND SECURITY FRAMEWORK":
                        cert.setPrivacySecurityFramework(
                                CertificationResult.formatPrivacyAndSecurityFramework(firstRow.get(currIndex++)));
                        break;
                    case "API DOCUMENTATION LINK":
                        cert.setApiDocumentation(firstRow.get(currIndex++).trim());
                        break;
                    case "STANDARD TESTED AGAINST":
                        parseTestStandards(pendingCertifiedProduct, cert, currIndex);
                        currIndex++;
                        break;
                    case "FUNCTIONALITY TESTED":
                        parseTestFunctionality(pendingCertifiedProduct, cert, currIndex);
                        currIndex++;
                        break;
                    case "MEASURE SUCCESSFULLY TESTED FOR G1":
                        parseG1Measures(cert, currIndex);
                        currIndex++;
                        break;
                    case "MEASURE SUCCESSFULLY TESTED FOR G2":
                        parseG2Measures(cert, currIndex);
                        currIndex++;
                        break;
                    case "ADDITIONAL SOFTWARE":
                        Boolean hasAdditionalSoftware = asBoolean(firstRow.get(currIndex).trim());
                        cert.setHasAdditionalSoftware(hasAdditionalSoftware);
                        parseAdditionalSoftware(pendingCertifiedProduct, cert, currIndex);
                        currIndex += 6;
                        break;
                    case "TEST TOOL NAME":
                        parseTestTools(pendingCertifiedProduct, cert, currIndex);
                        currIndex += 2;
                    case "TEST PROCEDURE":
                        parseTestProcedures(cert, currIndex);
                        currIndex += 2;
                        break;
                    case "TEST DATA":
                        parseTestData(pendingCertifiedProduct, cert, currIndex);
                        currIndex += 4;
                        break;
                    case "UCD PROCESS SELECTED":
                        PendingCertificationResultUcdProcessEntity ucd = new PendingCertificationResultUcdProcessEntity();
                        String ucdName = firstRow.get(currIndex++).trim();
                        String ucdDetails = firstRow.get(currIndex++).trim();

                        if (!StringUtils.isEmpty(ucdName)) {
                            ucd.setUcdProcessName(ucdName);
                            ucd.setUcdProcessDetails(ucdDetails);
                            UcdProcessDTO dto = ucdDao.getByName(ucd.getUcdProcessName());
                            if (dto != null) {
                                ucd.setUcdProcessId(dto.getId());
                            }
                            cert.getUcdProcesses().add(ucd);
                        }
                        break;
                    case "TASK IDENTIFIER":
                        parseTasksAndParticipants(pendingCertifiedProduct, cert, currIndex);
                        currIndex += 2;
                        break;
                    default:
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
    
    @Override
    protected void parseTestProcedures(PendingCertificationResultEntity cert, int tpColumn) {
        for (CSVRecord row : getRecord()) {
            String tpValue = row.get(tpColumn).trim();
            if (!StringUtils.isEmpty(tpValue)) {
                PendingCertificationResultTestProcedureEntity tpEntity = new PendingCertificationResultTestProcedureEntity();
                tpEntity.setVersion(tpValue);
                tpEntity.setTestProcedureName(null);
                List<TestProcedureDTO> allowedTestProcedures = 
                        testProcedureDao.getByCriteriaNumber(cert.getMappedCriterion().getNumber());
                if(allowedTestProcedures != null && allowedTestProcedures.size() > 0) {
                    for(TestProcedureDTO allowedTp : allowedTestProcedures) {
                        if(allowedTp.getName().equalsIgnoreCase(TestProcedureDTO.DEFAULT_TEST_PROCEDURE)) {
                            tpEntity.setTestProcedureId(allowedTp.getId());
                        }
                    }
                }
                cert.getTestProcedures().add(tpEntity);
            }
        }
    }

    @Override
    protected void parseTestData(PendingCertifiedProductEntity product, PendingCertificationResultEntity cert,
            int tdColumnBegin) {
        for (CSVRecord row : getRecord()) {
            String tdVersionValue = row.get(tdColumnBegin).trim();
            if (!StringUtils.isEmpty(tdVersionValue)) {
                PendingCertificationResultTestDataEntity tdEntity = new PendingCertificationResultTestDataEntity();
                tdEntity.setTestDataName(null);
                List<TestDataDTO> allowedTestData = 
                        testDataDao.getByCriteriaNumber(cert.getMappedCriterion().getNumber());
                if(allowedTestData != null && allowedTestData.size() > 0) {
                    for(TestDataDTO allowedTd : allowedTestData) {
                        if(allowedTd.getName().equalsIgnoreCase(TestDataDTO.DEFALUT_TEST_DATA)) {
                            tdEntity.setTestDataId(allowedTd.getId());
                        }
                    }
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
}
