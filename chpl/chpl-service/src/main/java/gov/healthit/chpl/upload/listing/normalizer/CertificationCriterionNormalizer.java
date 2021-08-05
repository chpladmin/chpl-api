package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertificationCriterionDTO;

@Component
public class CertificationCriterionNormalizer {

    private CertificationCriterionDAO criterionDao;

    @Autowired
    public CertificationCriterionNormalizer(CertificationCriterionDAO criterionDao) {
        this.criterionDao = criterionDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        List<CertificationCriterionDTO> all2015Criteria = criterionDao.findByCertificationEditionYear(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        if (listing != null && listing.getCertificationResults() != null) {
            List<CertificationCriterionDTO> criteriaToAdd = all2015Criteria.stream()
                .filter(criterionDto -> !existsInListing(listing.getCertificationResults(), criterionDto))
                .collect(Collectors.toList());
            criteriaToAdd.stream().forEach(criterionToAdd -> {
                listing.getCertificationResults().add(buildCertificationResult(criterionToAdd));
            });
        }
    }

    private boolean existsInListing(List<CertificationResult> listingCertResults, CertificationCriterionDTO criterionDto) {
        if (listingCertResults == null || listingCertResults.size() == 0) {
            return false;
        }
        return listingCertResults.stream()
                    .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId() != null
                        && certResult.getCriterion().getId().equals(criterionDto.getId()))
                    .findAny().isPresent();
    }

    private CertificationResult buildCertificationResult(CertificationCriterionDTO criterion) {
        return CertificationResult.builder()
            .criterion(new CertificationCriterion(criterion))
            .number(criterion.getNumber())
            .title(criterion.getTitle())
            .success(Boolean.FALSE)
        .build();
    }
}
