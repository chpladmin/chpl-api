package gov.healthit.chpl.attestation.report.validation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
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

    public Object validateRealWorldTesting
}
