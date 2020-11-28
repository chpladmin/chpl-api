package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestToolDTO;

@Component
public class TestToolNormalizer {
    private TestToolDAO testToolDao;

    @Autowired
    public TestToolNormalizer(TestToolDAO testToolDao) {
        this.testToolDao = testToolDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> lookupTestToolIds(certResult.getTestToolsUsed()));
        }
    }

    private void lookupTestToolIds(List<CertificationResultTestTool> testTools) {
        if (testTools != null && testTools.size() > 0) {
            testTools.stream()
                .forEach(testStandard -> lookupTestToolId(testStandard));
        }
    }

    private void lookupTestToolId(CertificationResultTestTool testTool) {
        if (!StringUtils.isEmpty(testTool.getTestToolName())) {
            TestToolDTO testToolDto =
                    testToolDao.getByName(testTool.getTestToolName());
            if (testToolDto != null) {
                testTool.setTestToolId(testToolDto.getId());
                testTool.setRetired(testToolDto.isRetired());
            }
        }
    }
}
