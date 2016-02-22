package gov.healthit.chpl.dao.impl;

import static org.junit.Assert.*;

import java.util.List;

import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;


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
	
	@Test
	public void testGetCQMResultDetailsByCertifiedProductId() throws EntityRetrievalException{
		
		List<CertificationResultDetailsDTO> dtos = certificationResultDetailsDAO.getCertificationResultDetailsByCertifiedProductId(1L);
		
		assertEquals(dtos.size(), 6);
		assertEquals(dtos.get(0).getNumber(), "170.314 (a)(1)");
		assertEquals(dtos.get(0).getSuccess(), true);
	}
	
}
