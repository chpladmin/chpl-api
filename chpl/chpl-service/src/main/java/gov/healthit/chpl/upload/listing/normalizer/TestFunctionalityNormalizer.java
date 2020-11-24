package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class TestFunctionalityNormalizer {
    private TestFunctionalityDAO testFunctionalityDao;

    @Autowired
    public TestFunctionalityNormalizer(TestFunctionalityDAO testFunctionalityDao) {
        this.testFunctionalityDao = testFunctionalityDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> lookupTestFunctionalityIds(listing, certResult.getTestFunctionality()));
        }
    }

    private void lookupTestFunctionalityIds(CertifiedProductSearchDetails listing,
            List<CertificationResultTestFunctionality> testFunctionalities) {
        if (testFunctionalities != null && testFunctionalities.size() > 0) {
            testFunctionalities.stream()
                .forEach(testFunctionality -> lookupTestFunctionalityId(listing, testFunctionality));
        }
    }

    private void lookupTestFunctionalityId(CertifiedProductSearchDetails listing,
            CertificationResultTestFunctionality testFunctionality) {
        if (!StringUtils.isEmpty(testFunctionality.getName())
                && listing.getCertificationEdition() != null
                && listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY) == null) {
            Long editionId = null;
            try {
                editionId = Long.parseLong(
                        listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY).toString());
            } catch (NumberFormatException ex) {
                LOGGER.error("Could not get edition id as a number.", ex);
            }

            if (editionId != null) {
                TestFunctionalityDTO testFunctionalityDto =
                        testFunctionalityDao.getByNumberAndEdition(testFunctionality.getName(), editionId);
                if (testFunctionalityDto != null) {
                    testFunctionality.setTestFunctionalityId(testFunctionalityDto.getId());
                }
            }
        }
    }
}
