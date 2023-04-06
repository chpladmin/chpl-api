package gov.healthit.chpl.functionalityTested;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.comparator.CertificationCriterionComparator;

@Service
@Transactional
public class FunctionalityTestedManager {

    private FunctionalityTestedDAO functionalityTestedDao;
    private CertificationCriterionComparator criteriaComparator;
    private FunctionalityTestedComparator funcTestedComparator;

    @Autowired
    public FunctionalityTestedManager(FunctionalityTestedDAO functionalityTestedDao,
            CertificationCriterionComparator criteriaComparator) {
        this.functionalityTestedDao = functionalityTestedDao;
        this.criteriaComparator = criteriaComparator;

        this.funcTestedComparator = new FunctionalityTestedComparator();
    }

    @Transactional
    public List<FunctionalityTested> getFunctionalitiesTested() {
        List<FunctionalityTested> functionalitiesTested = this.functionalityTestedDao.findAll();
        functionalitiesTested.stream()
            .forEach(funcTested -> funcTested.setCriteria(funcTested.getCriteria().stream()
                    .sorted(criteriaComparator)
                    .collect(Collectors.toList())));
        return functionalitiesTested.stream()
                .sorted(funcTestedComparator)
                .collect(Collectors.toList());
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
        functionalitiesTestedForCriterion.stream()
            .forEach(funcTested -> funcTested.setCriteria(funcTested.getCriteria().stream()
                .sorted(criteriaComparator)
                .collect(Collectors.toList())));
        return functionalitiesTestedForCriterion.stream()
                .sorted(funcTestedComparator)
                .collect(Collectors.toList());
    }
}
