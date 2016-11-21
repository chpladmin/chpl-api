package gov.healthit.chpl.web.controller;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class CertifiedProductControllerTest {

	@Autowired
	CertifiedProductController certifiedProductController;
	
	
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
	
	/** Description: Tests 
	 * 
	 * Expected Result: 
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_uploadMeaningfulUseCsv_testCase_expectedResult() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		// Create input data for API
		
		
		// Simulate API inputs
		

		// Call controller
		//List<MeaningfulUseUsers> activitiesList = certifiedProductController.someMethod(params);
		//assertTrue("Activities list should contain some activities", activitiesList.size() > 0);
		
		// Validate returned result
		//long firstActivityApiKeyCreationDate = activitiesList.get(0).getCreationDate().getTime();
		//assertTrue("The api key id returned is not equivalent to the oldest recorded", 
		//		oldestApiKeyActivity.getCreationDate().getTime() == firstActivityApiKeyCreationDate);
	}
}
