package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestStandardDTO;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class TestStandardNormalizer {
    private TestStandardDAO testStandardDao;

    @Autowired
    public TestStandardNormalizer(TestStandardDAO testStandardDao) {
        this.testStandardDao = testStandardDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> populateTestStandardIds(listing, certResult.getTestStandards()));
        }
    }

    private void populateTestStandardIds(CertifiedProductSearchDetails listing,
            List<CertificationResultTestStandard> testStandards) {
        if (testStandards != null && testStandards.size() > 0) {
            testStandards.stream()
                .forEach(testStandard -> populateTestStandardId(listing, testStandard));
        }
    }

    private void populateTestStandardId(CertifiedProductSearchDetails listing, CertificationResultTestStandard testStandard) {
        if (!StringUtils.isEmpty(testStandard.getTestStandardName())
                && listing.getCertificationEdition() != null
                && listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY) != null) {
            Long editionId = null;
            try {
                editionId = Long.parseLong(
                        listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY).toString());
            } catch (NumberFormatException ex) {
                LOGGER.error("Could not get edition id as a number.", ex);
            }

            if (editionId != null) {
                TestStandardDTO testStandardDto =
                        testStandardDao.getByNumberAndEdition(testStandard.getTestStandardName(), editionId);
                if (testStandardDto != null) {
                    testStandard.setTestStandardId(testStandardDto.getId());
                }
            }
        }
    }
}
