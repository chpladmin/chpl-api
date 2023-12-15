package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardCriteriaMap;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.standard.StandardGroupService;
import gov.healthit.chpl.upload.listing.normalizer.CertificationResultLevelNormalizer;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class BaselineStandardNormalizer implements CertificationResultLevelNormalizer {
    private StandardGroupService standardGroupService;
    private StandardDAO standardDao;

    public BaselineStandardNormalizer(StandardGroupService standardGroup, StandardDAO standardDao) {
        this.standardGroupService = standardGroup;
        this.standardDao = standardDao;
    }

    @Override
    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> addMissingStandards(listing.getCertificationDay(), certResult));
        }
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
            Map<String, List<Standard>> standardGroups = standardGroupService.getGroupedStandardsForCriteria(criterion, certificationDate);

            maps.removeIf(map -> !map.getCriterion().getId().equals(criterion.getId()));
            return maps.stream()
                    .filter(map -> !isStandardInAGroup(standardGroups, map.getStandard())
                            && DateUtil.isDateBetweenInclusive(Pair.of(map.getStandard().getRequiredDay(), map.getStandard().getEndDay()), certificationDate))
                    .map(map -> map.getStandard())
                    .toList();
        } catch (EntityRetrievalException e) {
            LOGGER.info("Error retrieving Standards for Criterion");
            throw new RuntimeException(e);
        }
    }

    private Boolean isStandardInAGroup(Map<String, List<Standard>> standardGroups, Standard standard) {

        var x = standardGroups.entrySet().stream()
            .flatMap(mapEntry -> mapEntry.getValue().stream())
            .filter(std -> std.getId().equals(standard.getId()))
            .findAny()
            .isPresent();
        return x;
    }
}
