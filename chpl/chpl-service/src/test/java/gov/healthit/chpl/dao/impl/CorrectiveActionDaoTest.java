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
import gov.healthit.chpl.dao.CorrectiveActionPlanCertificationResultDAO;
import gov.healthit.chpl.dao.CorrectiveActionPlanDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanCertificationResultDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanDTO;
import junit.framework.TestCase;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CorrectiveActionDaoTest extends TestCase {
	
	@Autowired
	private CorrectiveActionPlanDAO capDao;
	@Autowired
	private CorrectiveActionPlanCertificationResultDAO capCertDao;
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
	public void testGetCorrectiveActionPlanById() throws EntityRetrievalException {
		CorrectiveActionPlanDTO plan = capDao.getById(0L);
		assertNotNull(plan);
		assertNotNull(plan.getId());
		assertEquals(0, plan.getId().longValue());
	}
	
	@Test
	public void testPlanCertificationsExist() throws EntityRetrievalException {
		List<CorrectiveActionPlanCertificationResultDTO> capResults = capCertDao.getAllForCorrectiveActionPlan(0L);
		assertNotNull(capResults);
		assertEquals(1, capResults.size());
		assertEquals(0, capResults.get(0).getId().longValue());
	}
	
	@Test
	public void testGetPlanCertificationById() throws EntityRetrievalException {
		CorrectiveActionPlanCertificationResultDTO capResult = capCertDao.getById(0L);
		assertNotNull(capResult);
		assertEquals(0, capResult.getId().longValue());
		assertEquals(0, capResult.getCorrectiveActionPlanId().longValue());
	}
	
	@Test
	public void testGetPlanByCertifiedProduct() throws EntityRetrievalException {
		List<CorrectiveActionPlanDTO> plans = capDao.getAllForCertifiedProduct(1L);
		assertNotNull(plans);
		assertEquals(1, plans.size());
		assertNotNull(plans.get(0).getId());
		assertEquals(0, plans.get(0).getId().longValue());
	}
	
	@Test
	@Transactional
	public void testUpdatePlan() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		CorrectiveActionPlanDTO plan = capDao.getById(0L);
		String resolution = "This has been resolved";
		plan.setResolution(resolution);
		capDao.update(plan);
		
		plan = capDao.getById(0L);
		assertNotNull(plan);
		assertEquals(resolution, plan.getResolution());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Transactional
	public void testCreatePlan() throws EntityRetrievalException, EntityCreationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		CorrectiveActionPlanDTO plan = new CorrectiveActionPlanDTO();
		plan.setAcbSummary("Some summary");
		plan.setApprovalDate(new Date());
		plan.setCertifiedProductId(1L);
		plan.setDeveloperSummary("Developer summary");
		plan.setEffectiveDate(new Date());
		plan.setEstimatedCompletionDate(new Date());
		plan.setNoncomplainceDate(new Date());
		CorrectiveActionPlanDTO createdPlan = capDao.create(plan);
		assertNotNull(createdPlan);
		assertNotNull(createdPlan.getId());
	}
	
	@Test
	@Transactional
	public void testCreatePlanWithCerts() throws EntityRetrievalException, EntityCreationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		CorrectiveActionPlanDTO plan = new CorrectiveActionPlanDTO();
		plan.setAcbSummary("Some summary");
		plan.setApprovalDate(new Date());
		plan.setCertifiedProductId(1L);
		plan.setDeveloperSummary("Developer summary");
		plan.setEffectiveDate(new Date());
		plan.setEstimatedCompletionDate(new Date());
		plan.setNoncomplainceDate(new Date());
		CorrectiveActionPlanDTO createdPlan = capDao.create(plan);
		assertNotNull(createdPlan);
		assertNotNull(createdPlan.getId());
		
		CertificationCriterionDTO cert = certDao.getById(1L);
		CorrectiveActionPlanCertificationResultDTO planCert = new CorrectiveActionPlanCertificationResultDTO();
		planCert.setId(2L);
		planCert.setAcbSummary("cert acb summary");
		planCert.setCertCriterion(cert);
		planCert.setCorrectiveActionPlanId(createdPlan.getId());
		planCert.setDeveloperSummary("some dev summary");
		planCert.setResolution("fixed!");
		CorrectiveActionPlanCertificationResultDTO createdPlanCert = capCertDao.create(planCert);
		assertNotNull(createdPlanCert);
		assertNotNull(createdPlanCert.getId());
	}
}
