package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.dto.TestingLabDTO;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class TestingLabNormalizer {
    private TestingLabDAO atlDao;

    @Autowired
    public TestingLabNormalizer(TestingLabDAO atlDao) {
        this.atlDao = atlDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getTestingLabs() != null && listing.getTestingLabs().size() > 0) {
            listing.getTestingLabs().stream()
                .forEach(testingLab -> populateTestingLab(testingLab));
        }
    }

    private void populateTestingLab(CertifiedProductTestingLab testingLab) {
        if (testingLab == null) {
            return;
        }

        if (testingLab.getTestingLabId() == null && !StringUtils.isEmpty(testingLab.getTestingLabName())) {
            TestingLabDTO testingLabDto = atlDao.getByName(testingLab.getTestingLabName());
            if (testingLabDto != null) {
                testingLab.setTestingLabId(testingLabDto.getId());
                testingLab.setTestingLabName(testingLabDto.getName());
                testingLab.setTestingLabCode(testingLabDto.getTestingLabCode());
            }
        } else if (testingLab.getTestingLabId() == null
                && !StringUtils.isEmpty(testingLab.getTestingLabCode())
                && !testingLab.getTestingLabCode().equals(TestingLab.MULTIPLE_TESTING_LABS_CODE)) {
            TestingLabDTO testingLabDto = atlDao.getByCode(testingLab.getTestingLabCode());
            if (testingLabDto != null) {
                testingLab.setTestingLabId(testingLabDto.getId());
                testingLab.setTestingLabName(testingLabDto.getName());
                testingLab.setTestingLabCode(testingLabDto.getTestingLabCode());
            }
        } else if (testingLab.getTestingLabId() != null
                && (StringUtils.isEmpty(testingLab.getTestingLabName()) || StringUtils.isEmpty(testingLab.getTestingLabCode()))) {
            TestingLabDTO testingLabDto = null;
            try {
                testingLabDto = atlDao.getById(testingLab.getTestingLabId());
            } catch (Exception ex) {
                LOGGER.warn("Could not find Testing Lab with ID " + testingLab.getTestingLabId());
            }
            if (testingLabDto != null) {
                testingLab.setTestingLabName(testingLabDto.getName());
                testingLab.setTestingLabCode(testingLabDto.getTestingLabCode());
            }
        }
    }
}
