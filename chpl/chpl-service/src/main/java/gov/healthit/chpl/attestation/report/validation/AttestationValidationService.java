package gov.healthit.chpl.attestation.report.validation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.realworldtesting.RealWorldTestingCriteriaService;

@Component
public class AttestationValidationService {

    private RealWorldTestingCriteriaService realWorldTestingCriteriaService;
    private List<CertificationCriterion> assurancesCriteria;
    private List<CertificationCriterion> apiCriteria;

    @Autowired
    public AttestationValidationService(RealWorldTestingCriteriaService realWorldTestingCriteriaService,
            CertificationCriterionService certificationCriterionService,
            @Value("${assurancesCriteriaKeys}") String[] assurancesCriteriaKeys,
            @Value("${apiCriteriaKeys}") String[] apiCriteriaKeys) {

        this.realWorldTestingCriteriaService = realWorldTestingCriteriaService;

        assurancesCriteria = Arrays.asList(assurancesCriteriaKeys).stream()
                .map(key -> certificationCriterionService.get(key))
                .collect(Collectors.toList());

        apiCriteria = Arrays.asList(apiCriteriaKeys).stream()
                .map(key -> certificationCriterionService.get(key))
                .collect(Collectors.toList());
    }

    public Boolean validateRealWorldTesting(Developer developer, List<ListingSearchResult> listings) {
        AttestationValidationContext context = AttestationValidationContext.builder()
                .developer(developer)
                .listings(listings)
                .realWorldTestingCriteria(realWorldTestingCriteriaService.getEligibleCriteria(year))
                .apiCriteria(apiCriteria)
                .build();

        RealWorldTestingValidation rwtValidation = new RealWorldTestingValidation();
        return rwtValidation.isValid(context);
    }

    public Boolean validateAssurances(Developer developer, List<ListingSearchResult> listings) {
        AttestationValidationContext context = AttestationValidationContext.builder()
                .developer(developer)
                .listings(listings)
                .assuranceCriteria(assurancesCriteria)
                .build();

        AssurancesValidation assurancesValidation = new AssurancesValidation();
        return assurancesValidation.isValid(context);
    }

    public Boolean validateApi(Developer developer, List<ListingSearchResult> listings) {
        AttestationValidationContext context = AttestationValidationContext.builder()
                .developer(developer)
                .listings(listings)
                .apiCriteria(apiCriteria)
                .build();

        ApiValidation apiValidation = new ApiValidation();
        return apiValidation.isValid(context);
    }

}
