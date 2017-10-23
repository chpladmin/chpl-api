package gov.healthit.chpl.questionableActivity;


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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.manager.ProductManager;
import junit.framework.TestCase;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class ProductTest extends TestCase {
	
	@Autowired private QuestionableActivityDAO qaDao;	
	@Autowired private ProductManager productManager;
	
	private static JWTAuthenticatedUser adminUser;
	
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
	
	@Test
	@Transactional
	@Rollback
	public void testUpdateName() throws 
	    EntityCreationException, EntityRetrievalException, JsonProcessingException {
	    SecurityContextHolder.getContext().setAuthentication(adminUser);

	    Date beforeActivity = new Date();      
	    ProductDTO product = productManager.getById(-1L);
	    product.setName("NEW PRODUCT NAME");
	    productManager.update(product);
		Date afterActivity = new Date();
		
		List<QuestionableActivityProductDTO> activities = 
		        qaDao.findProductActivityBetweenDates(beforeActivity, afterActivity);
		assertNotNull(activities);
		assertEquals(1, activities.size());
		QuestionableActivityProductDTO activity = activities.get(0);
		assertEquals(-1, activity.getProductId().longValue());
		assertEquals("From Test Product 1 to NEW PRODUCT NAME", activity.getMessage());
		assertEquals(QuestionableActivityTriggerConcept.PRODUCT_NAME_EDITED.getName(), activity.getTrigger().getName());
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
