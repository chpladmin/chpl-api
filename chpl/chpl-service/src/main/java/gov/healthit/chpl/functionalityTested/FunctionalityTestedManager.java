package gov.healthit.chpl.functionalityTested;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FunctionalityTestedManager {

    private FunctionalityTestedDAO functionalityTestedDao;

    @Autowired
    public FunctionalityTestedManager(FunctionalityTestedDAO functionalityTestedDao) {
        this.functionalityTestedDao = functionalityTestedDao;
    }

    @Transactional
    public Set<FunctionalityTested> getFunctionalitiesTested() {
        List<FunctionalityTested> functionalitiesTested = this.functionalityTestedDao.findAll();
        return functionalitiesTested.stream()
                .collect(Collectors.toSet());
    }

    public List<FunctionalityTested> getFunctionalitiesTested(Long criteriaId, Long practiceTypeId) {
        List<FunctionalityTested> functionalitiesTestedForCriterion = new ArrayList<FunctionalityTested>();
        Map<Long, List<FunctionalityTested>> functionalitiesTestedByCriteria = functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps();
        if (functionalitiesTestedByCriteria.containsKey(criteriaId)) {
            functionalitiesTestedForCriterion = functionalitiesTestedByCriteria.get(criteriaId);
            if (practiceTypeId != null) {
                functionalitiesTestedForCriterion = functionalitiesTestedForCriterion.stream()
                        .filter(funcTest -> funcTest.getPracticeType() == null || funcTest.getPracticeType().getId().equals(practiceTypeId))
                        .collect(Collectors.toList());
            }
        }
        return functionalitiesTestedForCriterion;
    }
}
