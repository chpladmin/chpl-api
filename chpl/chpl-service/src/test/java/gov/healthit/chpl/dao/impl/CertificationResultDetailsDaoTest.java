package gov.healthit.chpl.dao.impl;

import static org.junit.Assert.assertEquals;

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
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CertificationResultDetailsDaoTest {
	
	@Autowired
	CertificationResultDetailsDAO certificationResultDetailsDAO;
	
	@Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;
	
	@Test
	@Transactional
	public void testGetCQMResultDetailsByCertifiedProductId() throws EntityRetrievalException{
		
		List<CertificationResultDetailsDTO> dtos = certificationResultDetailsDAO.getCertificationResultDetailsByCertifiedProductId(1L);
		
		assertEquals(5, dtos.size());
		assertEquals("170.314 (a)(1)", dtos.get(0).getNumber());
		assertEquals(true, dtos.get(0).getSuccess());
	}
	
}
