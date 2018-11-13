package gov.healthit.chpl.manager.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.TestFunctionalityCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;

public class TestingFunctionalityManagerImplTest {

    @Mock
    private TestFunctionalityDAO testFunctionalityDAO;

    @InjectMocks
    private TestingFunctionalityManagerImpl testingFunctionalityManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupMocks();
    }

    @Test
    public void getTestFunctionalityCriteriaMap2015Test() {
        Map<String, List<TestFunctionalityDTO>> mappings2015 =
                testingFunctionalityManager.getTestFunctionalityCriteriaMap2015();

        assertEquals(2, mappings2015.size());
        assertEquals(Boolean.TRUE, mappings2015.containsKey("170.315 (b)(1)"));
        assertEquals(Boolean.TRUE, mappings2015.containsKey("170.315 (a)(7)"));

        if (mappings2015.containsKey("170.315 (b)(1))")) {
            assertEquals(5, mappings2015.get("170.315 (b)(1))").size());
        }
        if (mappings2015.containsKey("170.315 (a)(7))")) {
            assertEquals(1, mappings2015.get("170.315 (a)(7))").size());
        }
    }

    @Test
    public void getTestFunctionalityCriteriaMap2014Test() {
        Map<String, List<TestFunctionalityDTO>> mappings2014 =
                testingFunctionalityManager.getTestFunctionalityCriteriaMap2014();

        assertEquals(2, mappings2014.size());
        assertEquals(Boolean.TRUE, mappings2014.containsKey("170.314 (b)(1)"));
        assertEquals(Boolean.TRUE, mappings2014.containsKey("170.314 (a)(7)"));

        if (mappings2014.containsKey("170.314 (b)(1))")) {
            assertEquals(5, mappings2014.get("170.314 (b)(1))").size());
        }
        if (mappings2014.containsKey("170.314 (a)(7))")) {
            assertEquals(1, mappings2014.get("170.314 (a)(7))").size());
        }
    }

    @Test
    public void getTestFunctionalitiesTest() {
        List<TestFunctionality> tfs = 
                testingFunctionalityManager.getTestFunctionalities("170.315 (b)(1)", "2015", null);
        assertEquals(5, tfs.size());

        tfs = testingFunctionalityManager.getTestFunctionalities("170.315 (a)(7)", "2015", null);
        assertEquals(1, tfs.size());

        tfs = testingFunctionalityManager.getTestFunctionalities("170.314 (b)(1)", "2014", 1l);
        assertEquals(3, tfs.size());

        tfs = testingFunctionalityManager.getTestFunctionalities("170.314 (b)(1)", "2014", 2l);
        assertEquals(3, tfs.size());

        tfs = testingFunctionalityManager.getTestFunctionalities("170.314 (a)(7)", "2014", 1l);
        assertEquals(1, tfs.size());

        tfs = testingFunctionalityManager.getTestFunctionalities("170.314 (a)(8)", "2014", 1l);
        assertEquals(0, tfs.size());
    }
    
    private void setupMocks() {
        doReturn(getTestFunctionalityCriteriaMaps()).when(testFunctionalityDAO).getTestFunctionalityCritieriaMaps();
        testingFunctionalityManager.onApplicationEvent(null);
    }
    
    private List<TestFunctionalityCriteriaMapDTO> getTestFunctionalityCriteriaMaps() {
        CertificationCriterionDTO criteria_b1_2015 = createCriteria(7l, "2015", 3l, "170.315 (b)(1)");
        TestFunctionalityDTO testFunctionality_b1iiA5i_2015 = createTestFunctionality(34l, "(b)(1)(ii)(A)(5)(i)", "2015");
        TestFunctionalityDTO testFunctionality_b1iiA5ii_2015 = createTestFunctionality(35l, "(b)(1)(ii)(A)(5)(ii)", "2015");
        TestFunctionalityDTO testFunctionality_b1iiiG1ii_2015 = createTestFunctionality(53l, "(b)(1)(iii)(G)(1)(ii)", "2015");
        TestFunctionalityDTO testFunctionality_b1iiiE_2015 = createTestFunctionality(63l, "(b)(1)(iii)(E)", "2015");
        TestFunctionalityDTO testFunctionality_b1iiiF_2015 = createTestFunctionality(73l, "(b)(1)(iii)(F)", "2015");
        CertificationCriterionDTO criteria_a7_2015 = createCriteria(7l, "2015", 3l, "170.315 (a)(7)");
        TestFunctionalityDTO testFunctionality_a7i_2015 = createTestFunctionality(61l, "(a)(7)(i)", "2015");

        TestFunctionalityCriteriaMapDTO map1 = createTestFunctionalitCriteriaMap(1l, criteria_b1_2015, testFunctionality_b1iiiG1ii_2015);
        TestFunctionalityCriteriaMapDTO map2 = createTestFunctionalitCriteriaMap(2l, criteria_b1_2015, testFunctionality_b1iiiE_2015);
        TestFunctionalityCriteriaMapDTO map3 = createTestFunctionalitCriteriaMap(3l, criteria_b1_2015, testFunctionality_b1iiiF_2015);
        TestFunctionalityCriteriaMapDTO map4 = createTestFunctionalitCriteriaMap(4l, criteria_b1_2015, testFunctionality_b1iiA5i_2015);
        TestFunctionalityCriteriaMapDTO map5 = createTestFunctionalitCriteriaMap(5l, criteria_b1_2015, testFunctionality_b1iiA5ii_2015);
        TestFunctionalityCriteriaMapDTO map6 = createTestFunctionalitCriteriaMap(6l, criteria_a7_2015, testFunctionality_a7i_2015);

        List<TestFunctionalityCriteriaMapDTO> maps = new ArrayList<TestFunctionalityCriteriaMapDTO>();
        maps.add(map1);
        maps.add(map2);
        maps.add(map3);
        maps.add(map4);
        maps.add(map5);
        maps.add(map6);

        CertificationCriterionDTO criteria_b1_2014 = createCriteria(7l, "2014", 3l, "170.314 (b)(1)");
        TestFunctionalityDTO testFunctionality_b1iiA5i_2014 = createTestFunctionality(34l, "(b)(1)(ii)(A)(5)(i)", "2014", 1l);
        TestFunctionalityDTO testFunctionality_b1iiA5ii_2014 = createTestFunctionality(35l, "(b)(1)(ii)(A)(5)(ii)", "2014", 2l);
        TestFunctionalityDTO testFunctionality_b1iiiG1ii_2014 = createTestFunctionality(53l, "(b)(1)(iii)(G)(1)(ii)", "2014", 1l);
        TestFunctionalityDTO testFunctionality_b1iiiE_2014 = createTestFunctionality(63l, "(b)(1)(iii)(E)", "2014", 2l);
        TestFunctionalityDTO testFunctionality_b1iiiF_2014 = createTestFunctionality(73l, "(b)(1)(iii)(F)", "2014");
        CertificationCriterionDTO criteria_a7_2014 = createCriteria(7l, "2014", 3l, "170.314 (a)(7)");
        TestFunctionalityDTO testFunctionality_a7i_2014 = createTestFunctionality(61l, "(a)(7)(i)", "2014", 1l);

        TestFunctionalityCriteriaMapDTO map7 = createTestFunctionalitCriteriaMap(1l, criteria_b1_2014, testFunctionality_b1iiiG1ii_2014);
        TestFunctionalityCriteriaMapDTO map8 = createTestFunctionalitCriteriaMap(2l, criteria_b1_2014, testFunctionality_b1iiiE_2014);
        TestFunctionalityCriteriaMapDTO map9 = createTestFunctionalitCriteriaMap(3l, criteria_b1_2014, testFunctionality_b1iiiF_2014);
        TestFunctionalityCriteriaMapDTO map10 = createTestFunctionalitCriteriaMap(4l, criteria_b1_2014, testFunctionality_b1iiA5i_2014);
        TestFunctionalityCriteriaMapDTO map11 = createTestFunctionalitCriteriaMap(5l, criteria_b1_2014, testFunctionality_b1iiA5ii_2014);
        TestFunctionalityCriteriaMapDTO map12 = createTestFunctionalitCriteriaMap(6l, criteria_a7_2014, testFunctionality_a7i_2014);

        maps.add(map7);
        maps.add(map8);
        maps.add(map9);
        maps.add(map10);
        maps.add(map11);
        maps.add(map12);

        return maps;
    }
    
    private CertificationCriterionDTO createCriteria(final Long id, final String year, final Long editionId, final String number) {
        CertificationCriterionDTO criteria = new CertificationCriterionDTO();
        criteria.setId(id);
        criteria.setCertificationEdition(year);
        criteria.setCertificationEditionId(editionId);
        criteria.setNumber(number);
        return criteria;
    }

    private TestFunctionalityDTO createTestFunctionality(final Long id, final String number, final String year) {
        return createTestFunctionality(id, number, year, null);
    }

    private TestFunctionalityDTO createTestFunctionality(final Long id, final String number, final String year, final Long ptId) {
        TestFunctionalityDTO tf = new TestFunctionalityDTO();
        tf.setId(id);
        tf.setYear(year);
        tf.setNumber(number);
        if (ptId != null) {
            PracticeTypeDTO pt = new PracticeTypeDTO();
            pt.setId(ptId);
            tf.setPracticeType(pt);    
        }

        return tf;
    }

    private TestFunctionalityCriteriaMapDTO createTestFunctionalitCriteriaMap(final Long id, final CertificationCriterionDTO criteria, final TestFunctionalityDTO tf) {
        TestFunctionalityCriteriaMapDTO map = new TestFunctionalityCriteriaMapDTO();
        map.setId(id);
        map.setCriteria(criteria);
        map.setTestFunctionality(tf);
        return map;
    }
}
