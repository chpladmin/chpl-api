package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.dto.TestFunctionalityCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;

@Service
@Transactional
public class TestingFunctionalityManager implements ApplicationListener<ContextRefreshedEvent> {

    private TestFunctionalityDAO testFunctionalityDAO;

    private Map<Long, List<TestFunctionalityDTO>> testFunctionalityByCriteria2015 =
            new HashMap<Long, List<TestFunctionalityDTO>>();

    private Map<Long, List<TestFunctionalityDTO>> testFunctionalityByCriteria2014 =
            new HashMap<Long, List<TestFunctionalityDTO>>();

    @Autowired
    public TestingFunctionalityManager(TestFunctionalityDAO testFunctionalityDAO) {
        this.testFunctionalityDAO = testFunctionalityDAO;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<TestFunctionalityCriteriaMapDTO> allMaps = testFunctionalityDAO.getTestFunctionalityCritieriaMaps();
        testFunctionalityByCriteria2015 = getTestFunctionalityByCriteriaAndEdition(allMaps, "2015");
        testFunctionalityByCriteria2014 = getTestFunctionalityByCriteriaAndEdition(allMaps, "2014");
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

    public Map<Long, List<TestFunctionalityDTO>> getTestFunctionalityCriteriaMap2015() {
        return testFunctionalityByCriteria2015;
    }

    public Map<Long, List<TestFunctionalityDTO>> getTestFunctionalityCriteriaMap2014() {
        return testFunctionalityByCriteria2014;
    }

    private List<TestFunctionality> get2014TestFunctionalities(Long criteriaId, Long practiceTypeId) {
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
        List<TestFunctionality> allowedTestFunctionalities = new ArrayList<TestFunctionality>();

        if (testFunctionalityByCriteria2015.containsKey(criteriaId)) {
            List<TestFunctionalityDTO> dtos = testFunctionalityByCriteria2015.get(criteriaId);
            for (TestFunctionalityDTO dto : dtos) {
                allowedTestFunctionalities.add(new TestFunctionality(dto));
            }
        }

        return allowedTestFunctionalities;
    }

    private Map<Long, List<TestFunctionalityDTO>> getTestFunctionalityByCriteriaAndEdition(List<TestFunctionalityCriteriaMapDTO> maps, String edition) {
        Map<Long, List<TestFunctionalityDTO>> mapping = new HashMap<Long, List<TestFunctionalityDTO>>();

        for (TestFunctionalityCriteriaMapDTO map : maps) {
            if (map.getCriteria().getCertificationEdition().equals(edition)) {
                if (!mapping.containsKey(map.getCriteria().getId())) {
                    mapping.put(map.getCriteria().getId(), new ArrayList<TestFunctionalityDTO>());
                }
                mapping.get(map.getCriteria().getId()).add(map.getTestFunctionality());
            }
        }
        return mapping;
    }
}
