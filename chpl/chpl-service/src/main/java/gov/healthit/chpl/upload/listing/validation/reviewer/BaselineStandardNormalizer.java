package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardCriteriaMap;
import gov.healthit.chpl.standard.StandardDAO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class BaselineStandardNormalizer {
    private StandardDAO standardDao;

    @Autowired
    public BaselineStandardNormalizer(StandardDAO standardDao) {
        this.standardDao = standardDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> populateStandards(listing, certResult));
        }
    }

    private void populateStandards(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getStandards() != null && certResult.getStandards().size() > 0) {
            certResult.getStandards().stream()
                .filter(standard -> standard.getId() == null)
                .forEach(standard -> populateStandard(listing, certResult, standard));
        }
    }

    private void populateStandard(CertifiedProductSearchDetails listing, CertificationResult certResult, CertificationResultStandard standard) {
        if (!StringUtils.isEmpty(standard.getStandard().getRegulatoryTextCitation())) {
            Standard foundStandard =
                    getStandard(standard.getStandard().getRegulatoryTextCitation(), certResult.getCriterion().getId());
            if (foundStandard != null) {
                standard.setStandard(foundStandard);
            }
        }
    }

    private Standard getStandard(String regulatoryTextCitation, Long criterionId) {
        Map<Long, List<Standard>> standardMappings = standardDao.getStandardCriteriaMaps();
        if (!standardMappings.containsKey(criterionId)) {
            return null;
        }
        List<Standard> standardForCriterion = standardMappings.get(criterionId);
        Optional<Standard> standardOpt = standardForCriterion.stream()
            .filter(standard -> standard.getRegulatoryTextCitation().equalsIgnoreCase(regulatoryTextCitation))
            .findAny();
        return standardOpt.isPresent() ? standardOpt.get() : null;
    }

    private CertificationResult addMissingStandards(LocalDate certificationDate, CertificationResult certResult) {
        List<Standard> validStandardsForCriterionAndListing = getValidStandardsForCriteriaAndListing(certResult.getCriterion(), certificationDate);

        validStandardsForCriterionAndListing
                .forEach(std -> {
                    List<Standard> standardsExistingInCertResult = certResult.getStandards().stream()
                            .map(crs -> crs.getStandard())
                            .toList();

                    if (!isStandardInList(std, standardsExistingInCertResult)) {
                        certResult.getStandards().add(CertificationResultStandard.builder()
                                .certificationResultId(certResult.getId())
                                .standard(std)
                                .build());
                    }
                });

        return certResult;
    }

    private Boolean isStandardInList(Standard standard, List<Standard> standards) {
        return standards.stream()
                .filter(std -> standard.getId().equals(std.getId()))
                .findAny()
                .isPresent();
    }

    private List<Standard> getValidStandardsForCriteriaAndListing(CertificationCriterion criterion, LocalDate certificationDate) {
        try {
            List<StandardCriteriaMap> maps = standardDao.getAllStandardCriteriaMap();
            maps.removeIf(map -> !map.getCriterion().getId().equals(criterion.getId()));
            return maps.stream()
                    .filter(map -> !isStandardInAGroup(map.getStandard())
                            && isStandardEndDateAfterCertificationDate(map.getStandard(), certificationDate)
                            && !isStandardRequiredDateBeforeCertificationDate(map.getStandard(), certificationDate))
                    .map(map -> map.getStandard())
                    .toList();

        } catch (EntityRetrievalException e) {
            LOGGER.info("Error retrieving Standards for Criterion");
            throw new RuntimeException(e);
        }
    }

    private Boolean isStandardInAGroup(Standard standard) {
        return standard.getGroupName() != null;
    }

    private Boolean isStandardEndDateAfterCertificationDate(Standard standard, LocalDate certificationDate) {
        return standard.getEndDay() == null
                || standard.getEndDay().isAfter(certificationDate);
    }

    private Boolean isStandardRequiredDateBeforeCertificationDate(Standard standard, LocalDate certificationDate) {
        return standard.getRequiredDay() == null
                || standard.getRequiredDay().isBefore(certificationDate);
    }

}
