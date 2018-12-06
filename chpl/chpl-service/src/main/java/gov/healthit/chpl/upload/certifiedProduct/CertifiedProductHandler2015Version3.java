package gov.healthit.chpl.upload.certifiedProduct;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestDataEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestProcedureEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap2015Version3;

/**
 * Adds ICS Source (family), Removes columns G1 and G2 for 170.315(g)(7),
 * Adds Test Tool + Test Tool Version + Test Data Version, Test Data Alteration, Test Data Alteration Description for criteria b8,
 * Removes Test Tool + Test Tool Version + Test Data fields for criteria f5
 * @author kekey
 *
 */
@Component("certifiedProductHandler2015Version3")
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
    protected void parseTestProceduresVersions(
            PendingCertificationResultEntity cert, boolean hasTestProcedureNameCol, int tpColumn) {
        for (CSVRecord row : getRecord()) {
            String tpName = "";
            if (hasTestProcedureNameCol) {
                tpName = row.get(tpColumn - 1).trim();
            }
            String tpVersion = row.get(tpColumn).trim();

            if (!StringUtils.isEmpty(tpName) || !StringUtils.isEmpty(tpVersion)) {
                if (StringUtils.isEmpty(tpName)) {
                    tpName = TestProcedureDTO.DEFAULT_TEST_PROCEDURE;
                }
                PendingCertificationResultTestProcedureEntity tpEntity = new PendingCertificationResultTestProcedureEntity();
                tpEntity.setVersion(tpVersion);
                tpEntity.setTestProcedureName(tpName);
                List<TestProcedureDTO> allowedTestProcedures =
                        testProcedureDao.getByCriteriaNumber(cert.getMappedCriterion().getNumber());
                if (allowedTestProcedures != null && allowedTestProcedures.size() > 0) {
                    for (TestProcedureDTO allowedTp : allowedTestProcedures) {
                        if (allowedTp.getName().equalsIgnoreCase(tpName)) {
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
            String tdName = row.get(tdColumnBegin).trim();
            String tdVersion = row.get(tdColumnBegin + 1).trim();
            String tdHasAlteration = row.get(tdColumnBegin + 2).trim();
            String tdAlteration = row.get(tdColumnBegin + 3).trim();

            if (!StringUtils.isEmpty(tdName) || !StringUtils.isEmpty(tdVersion) || !StringUtils.isEmpty(tdAlteration)) {
                if (StringUtils.isEmpty(tdName)) {
                    tdName = TestDataDTO.DEFALUT_TEST_DATA;
                }

                PendingCertificationResultTestDataEntity tdEntity = new PendingCertificationResultTestDataEntity();
                tdEntity.setTestDataName(tdName);
                List<TestDataDTO> allowedTestData =
                        testDataDao.getByCriteriaNumber(cert.getMappedCriterion().getNumber());
                if (allowedTestData != null && allowedTestData.size() > 0) {
                    for (TestDataDTO allowedTd : allowedTestData) {
                        if (allowedTd.getName().equalsIgnoreCase(tdName)) {
                            tdEntity.setTestDataId(allowedTd.getId());
                        }
                    }
                }
                tdEntity.setVersion(tdVersion);
                Boolean hasAlteration = asBoolean(tdHasAlteration);
                tdEntity.setHasAlteration(hasAlteration);
                if (tdEntity.isHasAlteration() && StringUtils.isEmpty(tdAlteration)) {
                    product.getErrorMessages()
                            .add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                                    + product.getUniqueId()
                                    + " indicates test data was altered however no test data alteration was found.");
                } else if (!tdEntity.isHasAlteration() && !StringUtils.isEmpty(tdAlteration)) {
                    product.getErrorMessages()
                            .add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                                    + product.getUniqueId()
                                    + " indicates test data was not altered however a test data alteration was found.");
                }
                tdEntity.setAlteration(tdAlteration);
                cert.getTestData().add(tdEntity);
            }
        }
    }
}
