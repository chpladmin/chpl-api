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
public class FunctionalityTestedManager {

    private FunctionalityTestedDAO functionalityTestedDao;

    @Autowired
    public FunctionalityTestedManager(FunctionalityTestedDAO functionalityTestedDao) {
        this.functionalityTestedDao = functionalityTestedDao;
    }

    public List<FunctionalityTested> getFunctionalitiesTested(Long criteriaId, String certificationEdition, Long practiceTypeId) {
        if (!StringUtils.isEmpty(certificationEdition) && certificationEdition.equals("2014")) {
            return get2014FunctionalitiesTested(criteriaId, practiceTypeId);
        } else if (!StringUtils.isEmpty(certificationEdition) && certificationEdition.equals("2015")) {
            return get2015FunctionalitiesTested(criteriaId);
        } else {
            return new ArrayList<FunctionalityTested>();
        }
    }

    private List<FunctionalityTested> get2014FunctionalitiesTested(Long criteriaId, Long practiceTypeId) {
        Map<Long, List<FunctionalityTested>> functionalitiesTestedByCriteria2014 =
                functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear());
        List<FunctionalityTested> allowedFunctionalitiesTested = new ArrayList<FunctionalityTested>();

        if (functionalitiesTestedByCriteria2014.containsKey(criteriaId)) {
            List<FunctionalityTested> functionalitiesTested = functionalitiesTestedByCriteria2014.get(criteriaId);
            for (FunctionalityTested functionalityTested : functionalitiesTested) {
                if (functionalityTested.getPracticeType() == null || functionalityTested.getPracticeType().getId().equals(practiceTypeId)) {
                    allowedFunctionalitiesTested.add(functionalityTested);
                }
            }
        }

        return allowedFunctionalitiesTested;
    }

    private List<FunctionalityTested> get2015FunctionalitiesTested(Long criteriaId) {
        Map<Long, List<FunctionalityTested>> functionalitiesTestedByCriteria2015 =
                functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        List<FunctionalityTested> allowedFunctionalitiesTested = new ArrayList<FunctionalityTested>();

        if (functionalitiesTestedByCriteria2015.containsKey(criteriaId)) {
            List<FunctionalityTested> functionalitiesTested = functionalitiesTestedByCriteria2015.get(criteriaId);
            for (FunctionalityTested functionalityTested : functionalitiesTested) {
                allowedFunctionalitiesTested.add(functionalityTested);
            }
        }

        return allowedFunctionalitiesTested;
    }
}
