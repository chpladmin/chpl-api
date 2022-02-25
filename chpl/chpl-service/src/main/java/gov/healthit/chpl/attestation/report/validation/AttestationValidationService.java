package gov.healthit.chpl.attestation.report.validation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.search.domain.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class AttestationValidationService {

    private List<CertificationCriterion> realWorldTestingCriteria;


    @Autowired
    public AttestationValidationService(CertificationCriterionService certificationCriterionService,
            @Value("${realWorldTestingCriteriaKeys}") String[] eligibleCriteriaKeys) {

        realWorldTestingCriteria = Arrays.asList(eligibleCriteriaKeys).stream()
                .map(key -> certificationCriterionService.get(key))
                .collect(Collectors.toList());
    }

    public Boolean validateRealWorldTesting(Developer developer, List<CertifiedProductBasicSearchResult> listings) {
        AttestationValidationContext context = AttestationValidationContext.builder()
                .developer(developer)
                .listings(listings)
                .realWorldTestingCriteria(realWorldTestingCriteria)
                .build();

        RealWorldTestingValidation rwtValidation = new RealWorldTestingValidation();
        return rwtValidation.isValid(context);
    }
}
