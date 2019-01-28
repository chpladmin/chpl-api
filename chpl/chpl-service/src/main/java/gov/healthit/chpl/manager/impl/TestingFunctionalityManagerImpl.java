package gov.healthit.chpl.manager.impl;

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
import gov.healthit.chpl.manager.TestingFunctionalityManager;

@Service
@Transactional
public class TestingFunctionalityManagerImpl implements TestingFunctionalityManager, ApplicationListener<ContextRefreshedEvent> {

    private TestFunctionalityDAO testFunctionalityDAO;

    private Map<String, List<TestFunctionalityDTO>> testFunctionalityByCriteria2015 =
            new HashMap<String, List<TestFunctionalityDTO>>();

    private Map<String, List<TestFunctionalityDTO>> testFunctionalityByCriteria2014 =
            new HashMap<String, List<TestFunctionalityDTO>>();

    @Autowired
    public TestingFunctionalityManagerImpl(TestFunctionalityDAO testFunctionalityDAO) {
        this.testFunctionalityDAO = testFunctionalityDAO;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<TestFunctionalityCriteriaMapDTO> allMaps = testFunctionalityDAO.getTestFunctionalityCritieriaMaps();
        testFunctionalityByCriteria2015 = getTestFunctionalityByCriteriaAndEdition(allMaps, "2015");
        testFunctionalityByCriteria2014 = getTestFunctionalityByCriteriaAndEdition(allMaps, "2014");
    }

    @Override
    public List<TestFunctionality> getTestFunctionalities(
            final String criteriaNumber, final String certificationEdition, final Long practiceTypeId) {

        if (certificationEdition.equals("2014")) {
            return get2014TestFunctionalities(criteriaNumber, practiceTypeId);
        } else if (certificationEdition.equals("2015")) {
            return get2015TestFunctionalities(criteriaNumber);
        } else {
            return new ArrayList<TestFunctionality>();
        }
    }

    @Override
    public Map<String, List<TestFunctionalityDTO>> getTestFunctionalityCriteriaMap2015() {
        return testFunctionalityByCriteria2015;
    }

    @Override
    public Map<String, List<TestFunctionalityDTO>> getTestFunctionalityCriteriaMap2014() {
        return testFunctionalityByCriteria2014;
    }

    private List<TestFunctionality> get2014TestFunctionalities(final String criteria, final Long practiceTypeId) {
        List<TestFunctionality> allowedTestFunctionalities = new ArrayList<TestFunctionality>();

        if (testFunctionalityByCriteria2014.containsKey(criteria)) {
            List<TestFunctionalityDTO> dtos = testFunctionalityByCriteria2014.get(criteria);
            for (TestFunctionalityDTO dto : dtos) {
                if (dto.getPracticeType() == null || dto.getPracticeType().getId().equals(practiceTypeId)) {
                    allowedTestFunctionalities.add(new TestFunctionality(dto));
                }
            }
        }

        return allowedTestFunctionalities;
    }

    private List<TestFunctionality> get2015TestFunctionalities(final String criteria) {
        List<TestFunctionality> allowedTestFunctionalities = new ArrayList<TestFunctionality>();

        if (testFunctionalityByCriteria2015.containsKey(criteria)) {
            List<TestFunctionalityDTO> dtos = testFunctionalityByCriteria2015.get(criteria);
            for (TestFunctionalityDTO dto : dtos) {
                allowedTestFunctionalities.add(new TestFunctionality(dto));
            }
        }

        return allowedTestFunctionalities;
    }

    private Map<String, List<TestFunctionalityDTO>> getTestFunctionalityByCriteriaAndEdition(List<TestFunctionalityCriteriaMapDTO> maps, String edition) {
        Map<String, List<TestFunctionalityDTO>> mapping = new HashMap<String, List<TestFunctionalityDTO>>();

        for (TestFunctionalityCriteriaMapDTO map : maps) {
            if (map.getCriteria().getCertificationEdition().equals(edition)) {
                if (!mapping.containsKey(map.getCriteria().getNumber())) {
                    mapping.put(map.getCriteria().getNumber(), new ArrayList<TestFunctionalityDTO>());
                }
                mapping.get(map.getCriteria().getNumber()).add(map.getTestFunctionality());
            }
        }
        return mapping;
    }
}
