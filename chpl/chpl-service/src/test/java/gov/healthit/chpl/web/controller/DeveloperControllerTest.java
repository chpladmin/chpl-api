package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertTrue;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.web.controller.results.DeveloperResults;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class DeveloperControllerTest {
	@Autowired
	DeveloperController developerController = new DeveloperController();

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

	/** Description: Tests the total runtime performance of the  
	 * getDevelopers() 
	 * method
	 * Expected Result: Completes within 3 seconds
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_getDevelopers_CompletesWithinThreeSeconds() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		long getDevelopersStartTime = System.currentTimeMillis();
		DeveloperResults results = developerController.getDevelopers();
		long getDevelopersEndTime = System.currentTimeMillis();
		long getDevelopersTimeLength = getDevelopersEndTime - getDevelopersStartTime;
		double getDevelopersElapsedSeconds = getDevelopersTimeLength / 1000.0;
		
		System.out.println("DeveloperController.getDevelopers() should complete within 3 seconds. It took " + getDevelopersTimeLength
				+ " millis or " + getDevelopersElapsedSeconds + " seconds");
		assertTrue("DeveloperController.getDevelopers() should complete within 3 seconds but took " + getDevelopersTimeLength
				+ " millis or " + getDevelopersElapsedSeconds + " seconds", getDevelopersElapsedSeconds < 3);
	}
	
}