package gov.healthit.chpl.manager.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.entity.ApiKeyActivityEntity;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
@Component
public class ApiKeyTestHelper {
	@PersistenceContext
	@Autowired
	EntityManager entityManager;
	
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
	
	/* Gets the oldest or newest API key activity based on creation_date
	 * If isOldest = true, returns the oldest API key activity; if false, returns the newest API key activity
	 * Returns the ApiKeyActivityEntity
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ApiKeyActivityEntity getNewestOrOldestApiKeyActivityByCreationDate(boolean isOldest) {
		String sql = "FROM ApiKeyActivityEntity WHERE (NOT deleted = true) ORDER BY creationDate ";
		if(isOldest){
			sql += "ASC";
		}
		else{
			sql += "DESC";
		}
		Query query = entityManager.createQuery
				(sql, ApiKeyActivityEntity.class);
		query.setMaxResults(1);
		ApiKeyActivityEntity apiKeyActivityEntity = (gov.healthit.chpl.entity.ApiKeyActivityEntity) query.getSingleResult();
		return apiKeyActivityEntity;
	}
	
	/* Gets an API key activity entity from the database with a creation_date that is not the oldest or newest 
	 * Returns the API key activity entity
	 * Assumes there must be at least 3 api key activities in testData.xml
	 */ 
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ApiKeyActivityEntity getAnApiKeyActivityByCreationDateThatIsNotNewestOrOldest(){
		String sql = "FROM ApiKeyActivityEntity WHERE (NOT deleted = true) ORDER BY creationDate ASC";
		Query query = entityManager.createQuery
				(sql, ApiKeyActivityEntity.class);
		List<ApiKeyActivityEntity> ApiKeyActivityEntityList = query.getResultList();
		Assert.assertTrue("There should be a list of API key activity entitites returned from the database, but there are only " + ApiKeyActivityEntityList.size(), 
				ApiKeyActivityEntityList.size() > 3);
		return ApiKeyActivityEntityList.get(3);
	}
	
}