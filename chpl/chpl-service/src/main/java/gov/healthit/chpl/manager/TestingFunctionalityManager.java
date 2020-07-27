package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.TestFunctionalityDTO;

@Service
@Transactional
public class TestingFunctionalityManager {

    private TestFunctionalityDAO testFunctionalityDAO;

    @Autowired
    public TestingFunctionalityManager(TestFunctionalityDAO testFunctionalityDAO) {
        this.testFunctionalityDAO = testFunctionalityDAO;
    }

    public List<TestFunctionality> getTestFunctionalities(Long criteriaId, String certificationEdition, Long practiceTypeId) {

        if (certificationEdition.equals("2014")) {
            return get2014TestFunctionalities(criteriaId, practiceTypeId);
        } else if (certificationEdition.equals("2015")) {
            return get2015TestFunctionalities(criteriaId);
        } else {
            return new ArrayList<TestFunctionality>();
        }
    }

    private List<TestFunctionality> get2014TestFunctionalities(Long criteriaId, Long practiceTypeId) {
        Map<Long, List<TestFunctionalityDTO>> testFunctionalityByCriteria2014 =
                testFunctionalityDAO.getTestFunctionalityCriteriaMaps(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear());
        List<TestFunctionality> allowedTestFunctionalities = new ArrayList<TestFunctionality>();

        if (testFunctionalityByCriteria2014.containsKey(criteriaId)) {
            List<TestFunctionalityDTO> dtos = testFunctionalityByCriteria2014.get(criteriaId);

            for (TestFunctionalityDTO dto : dtos) {
                if (dto.getPracticeType() == null || dto.getPracticeType().getId().equals(practiceTypeId)) {
                    allowedTestFunctionalities.add(new TestFunctionality(dto));
                }
            }
        }

        return allowedTestFunctionalities;
    }

    private List<TestFunctionality> get2015TestFunctionalities(Long criteriaId) {
        Map<Long, List<TestFunctionalityDTO>> testFunctionalityByCriteria2015 =
                testFunctionalityDAO.getTestFunctionalityCriteriaMaps(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        List<TestFunctionality> allowedTestFunctionalities = new ArrayList<TestFunctionality>();

        if (testFunctionalityByCriteria2015.containsKey(criteriaId)) {
            List<TestFunctionalityDTO> dtos = testFunctionalityByCriteria2015.get(criteriaId);
            for (TestFunctionalityDTO dto : dtos) {
                allowedTestFunctionalities.add(new TestFunctionality(dto));
            }
        }

        return allowedTestFunctionalities;
    }
}
