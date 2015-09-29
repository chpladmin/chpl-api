package gov.healthit.chpl.dao.impl;

import static org.junit.Assert.*;

import java.util.List;

import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;

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
public class CQMResultDetailsDaoTest {
	
	@Autowired
	CQMResultDetailsDAO cqmResultDetailsDAO;
	
	@Test
	public void testGetCQMResultDetailsByCertifiedProductId() throws EntityRetrievalException{
		
		List<CQMResultDetailsDTO> dtos = cqmResultDetailsDAO.getCQMResultDetailsByCertifiedProductId(1L);
		
		assertEquals(dtos.size(), 3);
		assertEquals(dtos.get(0).getNumber(), "NQF 0001(A)");
		assertEquals(dtos.get(0).getSuccess(), false);
		assertEquals(dtos.get(0).getVersion(), null);
		
	}
	
	
	

}
