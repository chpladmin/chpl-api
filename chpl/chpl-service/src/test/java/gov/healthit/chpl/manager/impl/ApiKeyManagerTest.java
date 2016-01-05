package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.List;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ApiKeyActivity;
import gov.healthit.chpl.dto.ApiKeyDTO;
import gov.healthit.chpl.manager.ApiKeyManager;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class ApiKeyManagerTest extends TestCase {

	@Autowired
	private ApiKeyManager apiKeyManager;
	
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
	public void testCreateKey() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		ApiKeyDTO retrieved = apiKeyManager.findKey(created.getId());
		
		assertEquals(toCreate.getApiKey(), created.getApiKey());
		assertEquals(toCreate.getApiKey(), retrieved.getApiKey());
		
		apiKeyManager.deleteKey(retrieved.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testDeleteKey() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		ApiKeyDTO retrieved = apiKeyManager.findKey(created.getId());
		
		assertEquals(toCreate.getApiKey(), created.getApiKey());
		assertEquals(toCreate.getApiKey(), retrieved.getApiKey());
		
		apiKeyManager.deleteKey(retrieved.getId());
		
		assertEquals(null, apiKeyManager.findKey(created.getId()));
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testDeleteByApiKey() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		ApiKeyDTO retrieved = apiKeyManager.findKey(created.getId());
		
		assertEquals(toCreate.getApiKey(), created.getApiKey());
		assertEquals(toCreate.getApiKey(), retrieved.getApiKey());
		
		apiKeyManager.deleteKey(retrieved.getId());
		
		assertEquals(null, apiKeyManager.findKey(toCreate.getApiKey()));
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	
	@Test
	public void testFindByApiKey() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		ApiKeyDTO retrieved = apiKeyManager.findKey(created.getApiKey());
		
		assertEquals(created.getApiKey(), retrieved.getApiKey());
		apiKeyManager.deleteKey(retrieved.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testFind() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		ApiKeyDTO retrieved = apiKeyManager.findKey(created.getId());
		
		assertEquals(created.getId(), retrieved.getId());
		apiKeyManager.deleteKey(retrieved.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	@Test
	public void testFindAll() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		Integer countAll = apiKeyManager.findAll().size();
		
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		
		Integer newCount = apiKeyManager.findAll().size();
		
		assertEquals((int) newCount, (countAll + 1));
		apiKeyManager.deleteKey(created.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	
	@Test
	public void TestLogApiKeyActivity() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
	
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		// create key
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		
		// log activity for key
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal");
		apiKeyManager.deleteKey(created.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testGetApiKeyActivity() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		int initialSize = apiKeyManager.getApiKeyActivity().size();
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		// create key
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		
		// log activity for key
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal");
		
		
		int finalSize = apiKeyManager.getApiKeyActivity().size();
		assertEquals((initialSize + 1), (finalSize));
		apiKeyManager.deleteKey(created.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testGetApiKeyActivityByKey() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		// create key
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		
		int initialSize = apiKeyManager.getApiKeyActivity(toCreate.getApiKey()).size();
		
		// log activity for key
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal");
		
		int finalSize = apiKeyManager.getApiKeyActivity(toCreate.getApiKey()).size();
		apiKeyManager.deleteKey(created.getId());
		assertEquals((initialSize + 1), (finalSize));
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	
	@Test
	public void testGetApiKeyActivityWithPaging() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		// create key
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		
		// log activity for key
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal1");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal2");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal3");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal4");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal5");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal6");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal7");
		
		
		int pageSize = 2;
		int pageNumber = 1;
		int finalSize = apiKeyManager.getApiKeyActivity(pageNumber, pageSize).size();
		assertEquals(pageSize, finalSize);
		
		apiKeyManager.deleteKey(created.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	
	@Test
	public void testGetApiKeyActivityWithKeyAndPaging() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		// create key
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		
		// log activity for key
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal1");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal2");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal3");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal4");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal5");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal6");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal7");
		
		
		int pageSize = 2;
		int pageNumber = 1;
		int finalSize = apiKeyManager.getApiKeyActivity(toCreate.getApiKey(), pageNumber, pageSize).size();
		assertEquals((int) pageSize, finalSize);
		apiKeyManager.deleteKey(created.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
}
