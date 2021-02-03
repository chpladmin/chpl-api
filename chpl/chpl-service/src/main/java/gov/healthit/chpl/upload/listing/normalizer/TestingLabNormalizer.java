package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.dto.TestingLabDTO;

@Component
public class TestingLabNormalizer {
    private TestingLabDAO atlDao;

    @Autowired
    public TestingLabNormalizer(TestingLabDAO atlDao) {
        this.atlDao = atlDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getTestingLabs() != null && listing.getTestingLabs().size() > 0) {
            listing.getTestingLabs().stream()
                .forEach(testingLab -> populateTestingLabId(testingLab));
        }
    }

    private void populateTestingLabId(CertifiedProductTestingLab testingLab) {
        if (testingLab != null && testingLab.getTestingLabId() == null
                && !StringUtils.isEmpty(testingLab.getTestingLabName())) {
            TestingLabDTO testingLabDto = atlDao.getByName(testingLab.getTestingLabName());
            if (testingLabDto != null) {
                testingLab.setTestingLabId(testingLabDto.getId());
            }
        }
    }
}
