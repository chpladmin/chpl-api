package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.criteriaattribute.testtool.TestTool;
import gov.healthit.chpl.criteriaattribute.testtool.TestToolDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestToolCriteriaMap;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class TestToolNormalizer {
    private TestToolDAO testToolDao;
    private List<TestToolCriteriaMap> testToolCriteriaMap = new ArrayList<TestToolCriteriaMap>();


    @Autowired
    public TestToolNormalizer(TestToolDAO testToolDao) {
        this.testToolDao = testToolDao;
        try {
            testToolCriteriaMap = testToolDao.getAllTestToolCriteriaMap();
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not initialize test tool criteria map for flexible upload.", ex);
        }
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (!CollectionUtils.isEmpty(listing.getCertificationResults())) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> fillInTestToolData(certResult));
        }
    }

    private void fillInTestToolData(CertificationResult certResult) {
        populateAllowedTestTools(certResult);
        populateTestToolIds(certResult.getTestToolsUsed());
    }

    private void populateAllowedTestTools(CertificationResult certResult) {
        if (certResult != null && certResult.getCriterion() != null
                && certResult.getCriterion().getId() != null) {
            List<TestTool> allowedTestTools = testToolCriteriaMap.stream()
                .filter(ttcm -> ttcm.getCriterion().getId().equals(certResult.getCriterion().getId()))
                .map(ttm -> ttm.getTestTool())
                .collect(Collectors.toList());
            certResult.setAllowedTestTools(allowedTestTools);
        }
    }

    private void populateTestToolIds(List<CertificationResultTestTool> testTools) {
        if (testTools != null && testTools.size() > 0) {
            testTools.stream()
                .forEach(testTool -> populateTestToolId(testTool));
        }
    }

    private void populateTestToolId(CertificationResultTestTool testTool) {
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
