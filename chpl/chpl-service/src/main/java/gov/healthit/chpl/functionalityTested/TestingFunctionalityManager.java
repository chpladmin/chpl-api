package gov.healthit.chpl.functionalityTested;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.concept.CertificationEditionConcept;

@Service
@Transactional
public class TestingFunctionalityManager {

    private TestFunctionalityDAO functionalityTestedDao;

    @Autowired
    public TestingFunctionalityManager(TestFunctionalityDAO functionalityTestedDao) {
        this.functionalityTestedDao = functionalityTestedDao;
    }

    public List<TestFunctionality> getFunctionalitiesTested(Long criteriaId, String certificationEdition, Long practiceTypeId) {
        if (!StringUtils.isEmpty(certificationEdition) && certificationEdition.equals("2014")) {
            return get2014FunctionalitiesTested(criteriaId, practiceTypeId);
        } else if (!StringUtils.isEmpty(certificationEdition) && certificationEdition.equals("2015")) {
            return get2015FunctionalitiesTested(criteriaId);
        } else {
            return new ArrayList<TestFunctionality>();
        }
    }

    private List<TestFunctionality> get2014FunctionalitiesTested(Long criteriaId, Long practiceTypeId) {
        Map<Long, List<TestFunctionality>> functionalitiesTestedByCriteria2014 =
                functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear());
        List<TestFunctionality> allowedFunctionalitiesTested = new ArrayList<TestFunctionality>();

        if (functionalitiesTestedByCriteria2014.containsKey(criteriaId)) {
            List<TestFunctionality> functionalitiesTested = functionalitiesTestedByCriteria2014.get(criteriaId);
            for (TestFunctionality functionalityTested : functionalitiesTested) {
                if (functionalityTested.getPracticeType() == null || functionalityTested.getPracticeType().getId().equals(practiceTypeId)) {
                    allowedFunctionalitiesTested.add(functionalityTested);
                }
            }
        }

        return allowedFunctionalitiesTested;
    }

    private List<TestFunctionality> get2015FunctionalitiesTested(Long criteriaId) {
        Map<Long, List<TestFunctionality>> functionalitiesTestedByCriteria2015 =
                functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        List<TestFunctionality> allowedFunctionalitiesTested = new ArrayList<TestFunctionality>();

        if (functionalitiesTestedByCriteria2015.containsKey(criteriaId)) {
            List<TestFunctionality> functionalitiesTested = functionalitiesTestedByCriteria2015.get(criteriaId);
            for (TestFunctionality functionalityTested : functionalitiesTested) {
                allowedFunctionalitiesTested.add(functionalityTested);
            }
        }

        return allowedFunctionalitiesTested;
    }
}
