package gov.healthit.chpl.dao.impl;


import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SurveillanceCertificationResultDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.SurveillanceCertificationResultDTO;
import gov.healthit.chpl.dto.SurveillanceDTO;
import junit.framework.TestCase;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class SurveillanceDAOTest extends TestCase {
	
	@Autowired
	private SurveillanceDAO surveillanceDao;
	@Autowired
	private SurveillanceCertificationResultDAO surveillanceCertDao;
	@Autowired
	private CertificationCriterionDAO certDao;
	
	private static JWTAuthenticatedUser adminUser;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	@Test
	public void testGetSurveillanceById() throws EntityRetrievalException {
		SurveillanceDTO sur = surveillanceDao.getById(0L);
		assertNotNull(sur);
		assertNotNull(sur.getId());
		assertEquals(0, sur.getId().longValue());
	}
	
	@Test
	public void testSurveillanceCertificationsExist() throws EntityRetrievalException {
		List<SurveillanceCertificationResultDTO> surResults = surveillanceCertDao.getAllForSurveillance(0L);
		assertNotNull(surResults);
		assertEquals(1, surResults.size());
		assertEquals(0, surResults.get(0).getId().longValue());
	}
	
	@Test
	public void testGetSurveillanceCertificationById() throws EntityRetrievalException {
		SurveillanceCertificationResultDTO surResult = surveillanceCertDao.getById(0L);
		assertNotNull(surResult);
		assertEquals(0, surResult.getId().longValue());
		assertEquals(0, surResult.getSurveillanceId().longValue());
	}
	
	@Test
	public void testGetPlanByCertifiedProduct() throws EntityRetrievalException {
		List<SurveillanceDTO> surs = surveillanceDao.getAllForCertifiedProduct(1L);
		assertNotNull(surs);
		assertEquals(1, surs.size());
		assertNotNull(surs.get(0).getId());
		assertEquals(0, surs.get(0).getId().longValue());
	}
	
	@Test
	@Transactional
	public void testUpdateSur() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Date endDate = new Date();
		
		SurveillanceDTO plan = surveillanceDao.getById(0L);
		plan.setEndDate(endDate);
		surveillanceDao.update(plan);
		
		plan = surveillanceDao.getById(0L);
		assertNotNull(plan);
		assertEquals(endDate, plan.getEndDate());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Transactional
	public void testCreateSur() throws EntityRetrievalException, EntityCreationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		SurveillanceDTO sur = new SurveillanceDTO();
		sur.setStartDate(new Date());
		sur.setEndDate(new Date());
		sur.setCertifiedProductId(1L);

		SurveillanceDTO createdSur = surveillanceDao.create(sur);
		assertNotNull(createdSur);
		assertNotNull(createdSur.getId());
	}
	
	@Test
	@Transactional
	public void testCreateSurWithCerts() throws EntityRetrievalException, EntityCreationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		SurveillanceDTO sur = new SurveillanceDTO();
		sur.setCertifiedProductId(1L);
		sur.setStartDate(new Date());
		sur.setEndDate(new Date());
		SurveillanceDTO createdSur = surveillanceDao.create(sur);
		assertNotNull(createdSur);
		assertNotNull(createdSur.getId());
		
		CertificationCriterionDTO cert = certDao.getById(1L);
		SurveillanceCertificationResultDTO surCert = new SurveillanceCertificationResultDTO();
		surCert.setId(2L);
		surCert.setNumSites(1);
		surCert.setPassRate("95");
		surCert.setResults("passed");
		surCert.setCertCriterion(cert);
		surCert.setSurveillanceId(createdSur.getId());
		SurveillanceCertificationResultDTO createdSurCert = surveillanceCertDao.create(surCert);
		assertNotNull(createdSurCert);
		assertNotNull(createdSurCert.getId());
	}
}
