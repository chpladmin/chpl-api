package gov.healthit.chpl.attestation.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.realworldtesting.RealWorldTestingCriteriaService;

@Component
public class ListingApplicabilityService {
    private List<CertificationCriterion> assurancesCriteria;
    private List<CertificationCriterion> apiCriteria;
    private List<CertificationCriterion> rwtCriteria;

    @Autowired
    public ListingApplicabilityService(RealWorldTestingCriteriaService realWorldTestingCriteriaService,
            CertificationCriterionService certificationCriterionService,
            @Value("${assurancesCriteriaKeys}") String[] assurancesCriteriaKeys,
            @Value("${apiCriteriaKeys}") String[] apiCriteriaKeys) {
        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
        rwtCriteria = realWorldTestingCriteriaService.getEligibleCriteria(currentYear);
        assurancesCriteria = Arrays.asList(assurancesCriteriaKeys).stream()
                .map(key -> certificationCriterionService.get(key))
                .collect(Collectors.toList());
        apiCriteria = Arrays.asList(apiCriteriaKeys).stream()
                .map(key -> certificationCriterionService.get(key))
                .collect(Collectors.toList());
    }

    public Boolean isRealWorldTestingApplicable(List<ListingSearchResult> listings) {
        return doListingsAttestToAnySpecifiedCriteria(listings, rwtCriteria);
    }

    public Boolean isAssurancesApplicable(List<ListingSearchResult> listings) {
        return doListingsAttestToAnySpecifiedCriteria(listings, assurancesCriteria);
    }

    public Boolean isApiApplicable(List<ListingSearchResult> listings) {
        return doListingsAttestToAnySpecifiedCriteria(listings, apiCriteria);
    }

    private Boolean doListingsAttestToAnySpecifiedCriteria(List<ListingSearchResult> listings, List<CertificationCriterion> criteria) {
        return listings.stream()
                .filter(result -> result.getCriteriaMet().stream()
                        .filter(met -> isCriteriaInList(met.getId(), criteria))
                        .findAny()
                        .isPresent())
                .findAny()
                .isPresent();
    }

    private boolean isCriteriaInList(Long criteriaId, List<CertificationCriterion> criteria) {
        return criteria.stream()
                .filter(crit -> crit.getId().equals(criteriaId))
                .findAny()
                .isPresent();
    }
}
