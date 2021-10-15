package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class OptionalStandardNormalizer {
    private OptionalStandardDAO optionalStandardDao;
    private TestStandardDAO testStandardDao;

    @Autowired
    public OptionalStandardNormalizer(OptionalStandardDAO optionalStandardDao,
            TestStandardDAO testStandardDao) {
        this.optionalStandardDao = optionalStandardDao;
        this.testStandardDao = testStandardDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> populateOptionalStandardsFields(certResult.getOptionalStandards()));

            //for any optional standards that do not have an ID at this point try to look them up as test standards
            listing.getCertificationResults().stream()
                .filter(certResult -> doesCertResultHaveOptionalStandardWithoutId(certResult))
                .forEach(certResultWithInvalidOptionalStandards ->
                    parseNotFoundOptionalStandardsAsTestStandards(listing, certResultWithInvalidOptionalStandards));
        }
    }

    private void populateOptionalStandardsFields(List<CertificationResultOptionalStandard> optionalStandards) {
        if (optionalStandards != null && optionalStandards.size() > 0) {
            optionalStandards.stream()
                .forEach(optionalStandard -> populateOptionalStandardFields(optionalStandard));
        }
    }

    private void populateOptionalStandardFields(CertificationResultOptionalStandard cros) {
        if (!StringUtils.isEmpty(cros.getCitation())) {
            OptionalStandard optionalStandard = optionalStandardDao.getByCitation(cros.getCitation());
            if (optionalStandard != null) {
                cros.setOptionalStandardId(optionalStandard.getId());
                cros.setDescription(optionalStandard.getDescription());
            }
        }
    }

    private boolean doesCertResultHaveOptionalStandardWithoutId(CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getOptionalStandards())) {
            return false;
        }

        return certResult.getOptionalStandards().stream()
                .filter(optionalStandard -> optionalStandard.getOptionalStandardId() == null)
                .findAny().isPresent();
    }

    private void parseNotFoundOptionalStandardsAsTestStandards(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        certResult.getOptionalStandards().stream()
            .filter(optionalStandard -> optionalStandard.getOptionalStandardId() == null)
            .forEach(notFoundOptionalStandard -> parseAsTestStandard(listing, certResult, notFoundOptionalStandard.getCitation()));
    }

    private void parseAsTestStandard(CertifiedProductSearchDetails listing, CertificationResult certResult, String testStandardName) {
        Long editionId = getEditionId(listing);
        if (editionId != null) {
            TestStandardDTO testStandardDto =
                    testStandardDao.getByNumberAndEdition(testStandardName, editionId);
            if (testStandardDto != null) {
                certResult.getTestStandards().add(CertificationResultTestStandard.builder()
                        .testStandardId(testStandardDto.getId())
                        .testStandardName(testStandardDto.getName())
                        .testStandardDescription(testStandardDto.getDescription())
                        .build());
            }
        }
    }

    private Long getEditionId(CertifiedProductSearchDetails listing) {
        Long editionId = null;
        if (listing.getCertificationEdition() != null
                && listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY) != null) {
            try {
                editionId = Long.parseLong(
                        listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY).toString());
            } catch (NumberFormatException ex) {
                LOGGER.error("Could not get edition id as a number.", ex);
            }
        }
        return editionId;
    }
}
