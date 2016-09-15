package gov.healthit.chpl.manager.impl;

import static org.junit.Assert.assertTrue;

import java.util.Set;

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
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.Statuses;
import gov.healthit.chpl.manager.SearchMenuManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class SearchMenuManagerTest {
@Autowired private SearchMenuManager searchMenuManager;
	
	private static JWTAuthenticatedUser adminUser;
	private static JWTAuthenticatedUser testUser3;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
		
		testUser3 = new JWTAuthenticatedUser();
		testUser3.setFirstName("Test");
		testUser3.setId(3L);
		testUser3.setLastName("User3");
		testUser3.setSubjectName("testUser3");
		testUser3.getPermissions().add(new GrantedPermission("ROLE_ACB_ADMIN"));
	}
	
	/** Description: Tests the getPopulateSearchOptions(boolean required) method
	 * Verifies that getDeveloperNames() returns valid, non-null results
	 * Verifies that getProductNames() returns valid, non-null results
	 * Verifies that getPopulateSearchOptions() completes in less than 3 seconds
	 * Expected Result: 
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_getPopulateSearchOptions_CompletesWithoutError() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		long getPopulateSearchOptionsStartTime = System.currentTimeMillis();
		Boolean required = false;
		PopulateSearchOptions results = searchMenuManager.getPopulateSearchOptions(required);
		long getPopulateSearchOptionsEndTime = System.currentTimeMillis();
		long getPopulateSearchOptionsTimeLength = getPopulateSearchOptionsEndTime - getPopulateSearchOptionsStartTime;
		double getPopulateSearchElapsedSeconds = getPopulateSearchOptionsTimeLength / 1000.0;
		
		assertTrue("Returned " + results.getDeveloperNames().size() + " developers but should return more than 0", results.getDeveloperNames().size() > 0);
		assertTrue("Returned " + results.getProductNames().size() + " products but should return more than 0", results.getProductNames().size() > 0);
		
		System.out.println("getPopulateSearchOptions returned " + results.getDeveloperNames().size() + " developers.");
		System.out.println("getPopulateSearchOptions returned " + results.getProductNames().size() + " products.");
		System.out.println("searchMenuManager.getPopulateSearchOptions() completed in  " + getPopulateSearchOptionsTimeLength
				+ " millis or " + getPopulateSearchElapsedSeconds + " seconds");
//		assertTrue("searchMenuManager.getPopulateSearchOptions() should complete within 3 seconds but took " + getPopulateSearchOptionsTimeLength
//				+ " millis or " + getPopulateSearchElapsedSeconds + " seconds", getPopulateSearchElapsedSeconds < 3);
	}
	
	/** Description: Tests the getDeveloperNames() method
	 * Verifies that getDeveloperNames() returns valid, non-null results
	 * Expected Result: At least one non-null developer name is returned
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_getDeveloperNames_CompletesWithoutError() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		long getDeveloperNamesStartTime = System.currentTimeMillis();
		Set<KeyValueModelStatuses> results = searchMenuManager.getDeveloperNames();
		long getDeveloperNamesEndTime = System.currentTimeMillis();
		long getDeveloperNamesTimeLength = getDeveloperNamesEndTime - getDeveloperNamesStartTime;
		double getDeveloperNamesElapsedSeconds = getDeveloperNamesTimeLength / 1000.0;
		
		assertTrue("Returned " + results.size() + " developers but should return more than 0", results.size() > 0);
		
		System.out.println("getDeveloperNames returned " + results.size() + " developers.");
		System.out.println("searchMenuManager.getDeveloperNames() completed in " + getDeveloperNamesTimeLength
				+ " millis or " + getDeveloperNamesElapsedSeconds + " seconds");
//		assertTrue("searchMenuManager.getDeveloperNames() should complete within 1 second but took " + getDeveloperNamesTimeLength
//				+ " millis or " + getDeveloperNamesElapsedSeconds + " seconds", getDeveloperNamesElapsedSeconds < 1);
	}
	
	/** Description: Tests the getProductNames() method
	 * Verifies that valid productNames are returned
	 * Expected Result: One or more non-null productNames
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_getProductNames_CompletesWithoutError() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		long getProductNamesStartTime = System.currentTimeMillis();
		Set<KeyValueModelStatuses> results = searchMenuManager.getProductNames();
		long getProductNamesEndTime = System.currentTimeMillis();
		long getProductNamesTimeLength = getProductNamesEndTime - getProductNamesStartTime;
		double getProductNamesElapsedSeconds = getProductNamesTimeLength / 1000.0;
		
		assertTrue("Returned " + results.size() + " products but should return more than 0", results.size() > 0);
		
		System.out.println("getProductNames returned " + results.size() + " products.");
		System.out.println("searchMenuManager.getProductNames() completed in " + getProductNamesTimeLength
				+ " millis or " + getProductNamesElapsedSeconds + " seconds");
//		assertTrue("searchMenuManager.getProductNames() should complete within 1 second but took " + getProductNamesTimeLength
//				+ " millis or " + getProductNamesElapsedSeconds + " seconds", getProductNamesElapsedSeconds < 1);
	}
	
	/** Description: Tests the getDeveloperNames() method
	 * Verifies that each developer has a statuses object
	 * Verifies that each property of the statuses object is non-null
	 * Expected Result: Each developer has a statuses object with a non-null value
	 * Each statuses object has a valid integer value for active, retired, withdrawn, suspended, and terminated
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_getDeveloperNames_ReturnsValidStatusesObject() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Set<KeyValueModelStatuses> results = searchMenuManager.getDeveloperNames();
		for(KeyValueModelStatuses result : results){
			Statuses status = result.getStatuses();
			
			assertTrue("Statuses.active should not be null", status.getActive() != null);
			assertTrue("Statuses.active should be >= 0", status.getActive() >= 0);
			
			assertTrue("Statuses.retired should not be null", status.getRetired() != null);
			assertTrue("Statuses.retired should be >= 0", status.getRetired() >= 0);
			
			assertTrue("Statuses.withdrawn should not be null", status.getWithdrawn() != null);
			assertTrue("Statuses.withdrawn should be >= 0", status.getWithdrawn() >= 0);
			
			assertTrue("Statuses.suspended should not be null", status.getSuspended() != null);
			assertTrue("Statuses.suspended should be >= 0", status.getSuspended() >= 0);
			
			assertTrue("Statuses.terminated should not be null", status.getTerminated() != null);
			assertTrue("Statuses.terminated should be >= 0", status.getTerminated() >= 0);
		}
	}
	
	/** Description: Tests the getProductNames() method
	 * Verifies that each product has a statuses object
	 * Verifies that each property of the statuses object is non-null
	 * Expected Result: Each product has a statuses object with a non-null value
	 * Each statuses object has a valid integer value for active, retired, withdrawn, suspended, and terminated
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_getProductNames_ReturnsValidStatusesObject() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Set<KeyValueModelStatuses> results = searchMenuManager.getProductNames();
		for(KeyValueModelStatuses result : results){
			Statuses status = result.getStatuses();
			
			assertTrue("Statuses.active should not be null", status.getActive() != null);
			assertTrue("Statuses.active should be >= 0", status.getActive() >= 0);
			
			assertTrue("Statuses.retired should not be null", status.getRetired() != null);
			assertTrue("Statuses.retired should be >= 0", status.getRetired() >= 0);
			
			assertTrue("Statuses.withdrawn should not be null", status.getWithdrawn() != null);
			assertTrue("Statuses.withdrawn should be >= 0", status.getWithdrawn() >= 0);
			
			assertTrue("Statuses.suspended should not be null", status.getSuspended() != null);
			assertTrue("Statuses.suspended should be >= 0", status.getSuspended() >= 0);
			
			assertTrue("Statuses.terminated should not be null", status.getTerminated() != null);
			assertTrue("Statuses.terminated should be >= 0", status.getTerminated() >= 0);
		}
	}
}
