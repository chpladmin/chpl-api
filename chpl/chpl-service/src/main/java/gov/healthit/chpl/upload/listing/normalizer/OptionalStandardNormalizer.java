package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class OptionalStandardNormalizer {
    private Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap = null;
    private TestStandardDAO testStandardDao;

    @Autowired
    public OptionalStandardNormalizer(OptionalStandardDAO optionalStandardDao,
            TestStandardDAO testStandardDao) {
        try {
            optionalStandardCriteriaMap = optionalStandardDao.getAllOptionalStandardCriteriaMap().stream()
                    .collect(Collectors.groupingBy(scm -> scm.getCriterion().getId()));
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not normalize Optional Standards", ex);
            return;
        }
        this.testStandardDao = testStandardDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> populateOptionalStandardsFields(certResult.getOptionalStandards(), optionalStandardCriteriaMap.get(certResult.getCriterion().getId())));

            //for any optional standards that do not have an ID at this point try to look them up as test standards
            listing.getCertificationResults().stream()
                .filter(certResult -> doesCertResultHaveOptionalStandardWithoutId(certResult))
                .forEach(certResultWithInvalidOptionalStandards ->
                    parseNotFoundOptionalStandardsAsTestStandards(listing, certResultWithInvalidOptionalStandards));
        }
    }

    private void populateOptionalStandardsFields(List<CertificationResultOptionalStandard> optionalStandards,
            List<OptionalStandardCriteriaMap> allowedOptionalStandards) {
        if (optionalStandards != null && optionalStandards.size() > 0) {
            optionalStandards.stream()
                .forEach(optionalStandard -> populateOptionalStandardFields(optionalStandard, allowedOptionalStandards));
        }
    }

    private void populateOptionalStandardFields(CertificationResultOptionalStandard cros,
            List<OptionalStandardCriteriaMap> allowedOptionalStandards) {
        if (cros.getOptionalStandardId() != null) {
            Optional<OptionalStandard> optionalStandard = getOptionalStandard(cros.getOptionalStandardId(), allowedOptionalStandards);
            if (optionalStandard.isPresent()) {
                cros.setCitation(optionalStandard.get().getCitation());
                cros.setDescription(optionalStandard.get().getDescription());
            }
        } else if (!StringUtils.isEmpty(cros.getCitation())) {
            Optional<OptionalStandard> optionalStandard = getOptionalStandard(cros.getCitation(), allowedOptionalStandards);
            if (optionalStandard.isPresent()) {
                cros.setOptionalStandardId(optionalStandard.get().getId());
                cros.setDescription(optionalStandard.get().getDescription());
            }
        }
    }

    private Optional<OptionalStandard> getOptionalStandard(Long optionalStandardId, List<OptionalStandardCriteriaMap> allowedOptionalStandards) {
        if (CollectionUtils.isEmpty(allowedOptionalStandards)) {
            return  Optional.empty();
        }

        return allowedOptionalStandards.stream()
                .map(oscm -> oscm.getOptionalStandard())
                .filter(os -> os.getId().equals(optionalStandardId))
                .findAny();
    }

    private Optional<OptionalStandard> getOptionalStandard(String citation, List<OptionalStandardCriteriaMap> allowedOptionalStandards) {
        if (CollectionUtils.isEmpty(allowedOptionalStandards)) {
            return Optional.empty();
        }

        return allowedOptionalStandards.stream()
                .map(oscm -> oscm.getOptionalStandard())
                .filter(os -> os.getCitation().equals(citation))
                .findAny();
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
