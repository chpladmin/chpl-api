package gov.healthit.chpl.dao.impl;


import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.dto.ActivityDTO;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

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



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class SurveillanceDaoTest extends TestCase {
	
	@Autowired
	private SurveillanceDAO survDao;
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
	@Transactional
	public void testGetSurveillanceTypeRandomized() {
		SurveillanceType result = survDao.findSurveillanceType("Randomized");
		assertNotNull(result);
		assertNotNull(result.getId());
		assertEquals("Randomized", result.getName());
	}
	
	@Test
	@Transactional
	public void testGetSurveillanceTypeRandomizedCaseInsensitive() {
		SurveillanceType result = survDao.findSurveillanceType("rANdOmIzED");
		assertNotNull(result);
		assertNotNull(result.getId());
		assertEquals("Randomized", result.getName());
	}
	
	@Test
	@Transactional
	public void testGetAllSurveillanceTypes() {
		List<SurveillanceType> results = survDao.getAllSurveillanceTypes();
		assertNotNull(results);
		assertEquals(2, results.size());
		for(SurveillanceType result : results) {
			assertNotNull(result.getId());
			assertNotNull(result.getName());
			assertTrue(result.getName().length() > 0);
		}
	}
	
	@Test
	@Transactional
	public void testGetAllSurveillanceResultTypes() {
		List<SurveillanceResultType> results = survDao.getAllSurveillanceResultTypes();
		assertNotNull(results);
		assertEquals(2, results.size());
		for(SurveillanceResultType result : results) {
			assertNotNull(result.getId());
			assertNotNull(result.getName());
			assertTrue(result.getName().length() > 0);
		}
	}
	
	@Test
	@Transactional
	public void testGetAllSurveillanceRequirementTypes() {
		List<SurveillanceRequirementType> results = survDao.getAllSurveillanceRequirementTypes();
		assertNotNull(results);
		assertEquals(3, results.size());
		for(SurveillanceRequirementType result : results) {
			assertNotNull(result.getId());
			assertNotNull(result.getName());
			assertTrue(result.getName().length() > 0);
		}
	}
	
	@Test
	@Transactional
	public void testGetAllNonconformityStatusTypes() {
		List<SurveillanceNonconformityStatus> results = survDao.getAllSurveillanceNonconformityStatusTypes();
		assertNotNull(results);
		assertEquals(2, results.size());
		for(SurveillanceNonconformityStatus result : results) {
			assertNotNull(result.getId());
			assertNotNull(result.getName());
			assertTrue(result.getName().length() > 0);
		}
	}
}
