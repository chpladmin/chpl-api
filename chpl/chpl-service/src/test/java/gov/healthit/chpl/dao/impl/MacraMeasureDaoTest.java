package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
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

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.CacheInvalidationRule;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.DecertifiedDeveloperDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.entity.DeveloperStatusType;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class MacraMeasureDaoTest extends TestCase {

	@Autowired 
	private MacraMeasureDAO macraDao;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@Test
	@Transactional
	public void getAllMeasures() {
		List<MacraMeasureDTO> results = macraDao.findAll();
		assertNotNull(results);
		assertEquals(83, results.size());
	}
	
	@Test
	@Transactional
	public void getMeasuresForCertificationCriteriaSingleResult() {
		List<MacraMeasureDTO> results = macraDao.getByCriteriaNumber("170.315 (a)(1)");
		assertNotNull(results);
		assertEquals(1, results.size());
	}
	
	@Test
	@Transactional
	public void getMeasuresForCertificationCriteriaMultipleResults() {
		List<MacraMeasureDTO> results = macraDao.getByCriteriaNumber("170.315 (b)(1)");
		assertNotNull(results);
		assertEquals(8, results.size());
	}
	
	@Test
	@Transactional
	public void getMeasureForCriteriaAndValue() {
		MacraMeasureDTO result = macraDao.getByCriteriaNumberAndValue("170.315 (b)(1)", "RT8 EP Individual");
		assertNotNull(result);
		assertEquals("RT8 EP Individual", result.getValue());
	}
	
	@Test
	@Transactional
	public void getNoMeasureForCriteriaAndValue() {
		MacraMeasureDTO result = macraDao.getByCriteriaNumberAndValue("170.315 (b)(1)", "Junk Value");
		assertNull(result);
	}
	
}
