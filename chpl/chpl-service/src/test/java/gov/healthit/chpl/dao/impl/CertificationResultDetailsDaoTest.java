package gov.healthit.chpl.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultMacraMeasureDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class CertificationResultDetailsDaoTest {

    @Autowired
    CertificationResultDetailsDAO certificationResultDetailsDAO;

    @Autowired
    CertificationResultDAO certResultDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional(readOnly = true)
    public void testGetG1MacraMeasuresForCertificationResult() {
        List<CertificationResultMacraMeasureDTO> g1Measures = certResultDao
                .getG1MacraMeasuresForCertificationResult(10L);
        assertNotNull(g1Measures);
        assertEquals(1, g1Measures.size());
        assertNotNull(g1Measures.get(0).getId());
        assertNotNull(g1Measures.get(0).getMeasure());
        assertEquals(1L, g1Measures.get(0).getMeasure().getId().longValue());
    }

    @Test
    @Transactional(readOnly = true)
    public void testGetG2MacraMeasuresForCertificationResult() {
        List<CertificationResultMacraMeasureDTO> g2Measures = certResultDao
                .getG2MacraMeasuresForCertificationResult(10L);
        assertNotNull(g2Measures);
        assertEquals(0, g2Measures.size());
    }

    @Test
    @Transactional
    public void testGetCQMResultDetailsByCertifiedProductId() throws EntityRetrievalException {

        List<CertificationResultDetailsDTO> dtos = certificationResultDetailsDAO
                .getCertificationResultDetailsByCertifiedProductId(1L);

        assertEquals(8, dtos.size());
        Boolean hasNumber = false;
        for (CertificationResultDetailsDTO dto : dtos) {
            if (dto.getNumber().equalsIgnoreCase("170.314 (a)(1)")) {
                hasNumber = true;
                assertEquals(true, dto.getSuccess());
            }
        }
        assertTrue("Result should contain CertificationResultDTO with number equal to 170.314 (a)(1)", hasNumber);

    }

}
