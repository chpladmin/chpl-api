package gov.healthit.chpl.service.realworldtesting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class RealWorldTestingCriteriaService {

    private CertificationCriterionService certificationCriterionService;
    private Map<Integer, List<String>> eligibleCriteriaKeysMap = new HashMap<>();

    public RealWorldTestingCriteriaService(CertificationCriterionService certificationCriterionService,
            @Value("#{${realWorldTestingCriteriaKeys}}") Map<Integer, String> eligibleCriteriaKeysMap) {
        this.certificationCriterionService = certificationCriterionService;
        this.eligibleCriteriaKeysMap = eligibleCriteriaKeysMap.entrySet().stream()
                .collect(Collectors.toMap(
                        (entry -> entry.getKey()),
                        (entry -> Arrays.asList(entry.getValue().split("\\s*,\\s*")))));
    }

    public List<CertificationCriterion> getEligibleCriteria(Integer year) {
        try {
            return getRwtEligibleCriteria(eligibleCriteriaKeysMap.get(getYearOrMostRecentPastYear(year)));
        } catch (InvalidArgumentsException e) {
            LOGGER.error("Could not determine list of RWT Criteria for year {}", year);
            return null;
        }
    }

    private Integer getYearOrMostRecentPastYear(Integer requestedYear) throws InvalidArgumentsException {
        Integer i = eligibleCriteriaKeysMap.keySet().stream()
                .filter(y -> y <= requestedYear)
                .max(Integer::compareTo)
                .orElseThrow(InvalidArgumentsException::new);
        return i;
    }

    private List<CertificationCriterion> getRwtEligibleCriteria(List<String> eligibleCriteriaKeys) {
        return eligibleCriteriaKeys.stream()
                .map(key -> certificationCriterionService.get(key))
                .collect(Collectors.toList());
    }

}
