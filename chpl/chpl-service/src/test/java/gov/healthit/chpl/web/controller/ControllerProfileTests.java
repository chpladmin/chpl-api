package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class ControllerProfileTests {
	@Autowired
	SearchViewController searchViewController = new SearchViewController();
	
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
	
	/** Given ...
	 *  When ...
	 *  Then ...
	 * @throws InvalidArgumentsException 
	 */
	@Transactional 
	@Test
	public void test_SearchViewController_() throws InvalidArgumentsException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		SearchRequest searchFilters = new SearchRequest();
		List<String> cqms = new ArrayList<String>();
		cqms.add("0001");
		searchFilters.setCqms(cqms);
		searchFilters.setPageNumber(0);
		searchFilters.setPageSize(50);
		searchFilters.setOrderBy("developer");
		searchFilters.setSortDescending(true);
		
		SearchResponse searchResponse = new SearchResponse();
		searchResponse = searchViewController.advancedSearch(searchFilters);
		assertTrue("searchViewController.simpleSearch() should return a SearchResponse with records", searchResponse.getRecordCount() > 0);
	}
	
}
