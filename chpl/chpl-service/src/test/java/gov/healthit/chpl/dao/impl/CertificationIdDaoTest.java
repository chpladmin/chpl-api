package gov.healthit.chpl.dao.impl;


import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.CertificationIdDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertificationIdAndCertifiedProductDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CertificationIdDaoTest extends TestCase {
	
	@Autowired private CertificationIdDAO ehrDao;
	
	
	@Test
	@Transactional
	@Rollback
	public void testCreateEhrCertificationWithSingleProduct() throws EntityCreationException, EntityRetrievalException{
		List<Long> ids = new ArrayList<Long>();
		ids.add(1L);
		ehrDao.create(ids, "2014");	
		
		List<CertificationIdAndCertifiedProductDTO> results = ehrDao.getAllCertificationIdsWithProducts();
		assertNotNull(results);
		assertEquals(1, results.size());
		CertificationIdAndCertifiedProductDTO result = results.get(0);
		assertNotNull(result.getChplProductNumber());
		assertEquals("CHP-024050", result.getChplProductNumber());
		
	}
	
	@Test
	@Transactional
	@Rollback
	public void testCreateEhrCertificationWithMultipleProducts() throws EntityCreationException, EntityRetrievalException{
		List<Long> ids = new ArrayList<Long>();
		ids.add(1L);
		ids.add(2L);
		ehrDao.create(ids, "2014");	
		
		List<CertificationIdAndCertifiedProductDTO> results = ehrDao.getAllCertificationIdsWithProducts();
		assertNotNull(results);
		assertEquals(2, results.size());
	}
}
