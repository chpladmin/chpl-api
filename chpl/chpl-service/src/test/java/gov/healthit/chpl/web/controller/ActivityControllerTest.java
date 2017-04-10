package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
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
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityEvent;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class ActivityControllerTest {
	private static JWTAuthenticatedUser adminUser;
	
	@Autowired ActivityController activityController;
	
	@Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	/** 
	 * Tests that listActivity returns results for a Certified Product
	 * @throws IOException 
	 */
	@Transactional
	@Test
	public void test_listActivity() throws EntityRetrievalException, EntityCreationException, IOException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		// Note: certification_criterion_id="59" has testTool="true", number 170.315 (h)(1) and title "Direct Project"
		Long cpId = 1L; // this CP has ics_code = "1" & associated certification_result_id = 8 with certification_criterion_id="59"
		Long cpId2 = 10L; // this CP has ics_code = "0" & associated certification_result_id = 9 with certification_criterion_id="59"
		
		List<ActivityEvent> cpActivityEvents = activityController.activityForCertifiedProductById(cpId, null, null);
		assertTrue(cpActivityEvents.size() == 4);
		
		List<ActivityEvent> cp2ActivityEvents = activityController.activityForCertifiedProductById(cpId2, null, null);
		assertTrue(cp2ActivityEvents.size() == 0);
	}
}
