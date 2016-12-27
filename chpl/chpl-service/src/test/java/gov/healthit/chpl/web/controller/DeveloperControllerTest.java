package gov.healthit.chpl.web.controller;

import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import net.sf.ehcache.CacheManager;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
//@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
//    DirtiesContextTestExecutionListener.class,
//    TransactionalTestExecutionListener.class,
//    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class DeveloperControllerTest {
	@Autowired
	DeveloperController developerController = new DeveloperController();
	
	@Autowired
	SearchViewController searchViewController = new SearchViewController();
	
	@Autowired
	CacheManager cacheManager = CacheManager.getInstance();

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
	
}