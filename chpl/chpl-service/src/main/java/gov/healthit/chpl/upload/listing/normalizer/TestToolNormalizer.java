package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.testtool.CertificationResultTestTool;
import gov.healthit.chpl.testtool.TestTool;
import gov.healthit.chpl.testtool.TestToolDAO;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class TestToolNormalizer {
    private TestToolDAO testToolDao;

    @Autowired
    public TestToolNormalizer(TestToolDAO testToolDao) {
        this.testToolDao = testToolDao;
    }

    @Transactional
    public void normalize(CertifiedProductSearchDetails listing) {
        if (!CollectionUtils.isEmpty(listing.getCertificationResults())) {
            clearDataForUnattestedCriteria(listing);
            listing.getCertificationResults().stream()
                .forEach(certResult -> normalize(certResult));
        }
    }

    private void clearDataForUnattestedCriteria(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> (certResult.getSuccess() == null || BooleanUtils.isFalse(certResult.getSuccess()))
                    && certResult.getTestToolsUsed() != null && certResult.getTestToolsUsed().size() > 0)
            .forEach(unattestedCertResult -> unattestedCertResult.getTestToolsUsed().clear());
    }

    private void normalize(CertificationResult certResult) {
        if (!CollectionUtils.isEmpty(certResult.getTestToolsUsed())) {
            certResult.getTestToolsUsed().stream()
                .forEach(crtt -> setEmptyStringFieldsToNull(crtt));
        }
        populateTestToolIds(certResult.getTestToolsUsed());
    }

    private void setEmptyStringFieldsToNull(CertificationResultTestTool crtt) {
        if (StringUtils.isEmpty(crtt.getVersion())) {
            crtt.setVersion(null);
        }
    }

    private void populateTestToolIds(List<CertificationResultTestTool> testTools) {
        if (testTools != null && testTools.size() > 0) {
            testTools.stream()
                .forEach(testTool -> populateTestToolId(testTool));
        }
    }

    private void populateTestToolId(CertificationResultTestTool testTool) {
        if (!StringUtils.isEmpty(testTool.getTestTool().getValue())) {
            TestTool testToolFromDb =
                    testToolDao.getByName(testTool.getTestTool().getValue());
            if (testToolFromDb != null) {
                testTool.setTestTool(testToolFromDb);
            }
        }
    }
}
