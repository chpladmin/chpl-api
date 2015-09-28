package gov.healthit.chpl.dao.impl;

import static org.junit.Assert.*;

import java.util.Date;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.AdditionalSoftwareDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;

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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class AdditionalSoftwareDaoTest {
	
	@Autowired
	private AdditionalSoftwareDAO additionalSoftwareDAO;
	
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
	@Transactional
	public void testCreate() throws EntityCreationException, EntityRetrievalException {
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		AdditionalSoftwareDTO dto = new AdditionalSoftwareDTO();
		dto.setCertifiedProductId(1L);
		dto.setName("Addtional Software A");
		dto.setVersion("1.0");
		dto.setJustification("Because...");
		dto.setCreationDate(new Date());
		dto.setDeleted(false);
		dto.setLastModifiedDate(new Date());
		dto.setLastModifiedUser(Util.getCurrentUser().getId());
		
		AdditionalSoftwareDTO result = additionalSoftwareDAO.create(dto);
		AdditionalSoftwareDTO check = additionalSoftwareDAO.getById(result.getId());
		
		assertEquals(result.getName(), check.getName());
		assertEquals(result.getVersion(), check.getVersion());
		assertEquals(result.getJustification(), check.getJustification());
		assertEquals(result.getCreationDate(), check.getCreationDate());
		assertEquals(result.getDeleted(), check.getDeleted());
		assertEquals(result.getId(), check.getId());
		assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
		
		additionalSoftwareDAO.delete(result.getId());
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	
	@Test
	@Transactional
	public void testUpdate() throws EntityCreationException, EntityRetrievalException {
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		AdditionalSoftwareDTO dto = new AdditionalSoftwareDTO();
		dto.setCertifiedProductId(1L);
		dto.setName("Addtional Software A");
		dto.setVersion("1.0");
		dto.setJustification("Because...");
		dto.setCreationDate(new Date());
		dto.setDeleted(false);
		dto.setLastModifiedDate(new Date());
		dto.setLastModifiedUser(Util.getCurrentUser().getId());
		
		AdditionalSoftwareDTO result = additionalSoftwareDAO.create(dto);
		
		result.setCertifiedProductId(1L);
		result.setName("Addtional Software B");
		result.setVersion("1.1");
		result.setJustification("I said so...");
		result.setCreationDate(new Date());
		result.setDeleted(false);
		result.setLastModifiedDate(new Date());
		result.setLastModifiedUser(Util.getCurrentUser().getId());
		
		additionalSoftwareDAO.update(result);
		
		AdditionalSoftwareDTO check = additionalSoftwareDAO.getById(result.getId());
		
		assertEquals(result.getName(), check.getName());
		assertEquals(result.getVersion(), check.getVersion());
		assertEquals(result.getJustification(), check.getJustification());
		assertEquals(result.getCreationDate(), check.getCreationDate());
		assertEquals(result.getDeleted(), check.getDeleted());
		assertEquals(result.getId(), check.getId());
		assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
		
		additionalSoftwareDAO.delete(result.getId());
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}

	
	@Test
	@Transactional
	public void testDelete() throws EntityCreationException, EntityRetrievalException {
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		AdditionalSoftwareDTO dto = new AdditionalSoftwareDTO();
		dto.setCertifiedProductId(1L);
		dto.setName("Addtional Software A");
		dto.setVersion("1.0");
		dto.setJustification("Because...");
		dto.setCreationDate(new Date());
		dto.setDeleted(false);
		dto.setLastModifiedDate(new Date());
		dto.setLastModifiedUser(Util.getCurrentUser().getId());
		
		AdditionalSoftwareDTO result = additionalSoftwareDAO.create(dto);
		AdditionalSoftwareDTO check = additionalSoftwareDAO.getById(result.getId());
		
		assertEquals(result.getName(), check.getName());
		assertEquals(result.getVersion(), check.getVersion());
		assertEquals(result.getJustification(), check.getJustification());
		assertEquals(result.getCreationDate(), check.getCreationDate());
		assertEquals(result.getDeleted(), check.getDeleted());
		assertEquals(result.getId(), check.getId());
		assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
		
		additionalSoftwareDAO.delete(result.getId());
		
		assertNull(additionalSoftwareDAO.getById(result.getId()));
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	@Test
	@Transactional
	public void testFindAll() {
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		assertNotNull(additionalSoftwareDAO.findAll());
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	
	@Test
	@Transactional
	public void testGetById() throws EntityCreationException, EntityRetrievalException {
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		AdditionalSoftwareDTO dto = new AdditionalSoftwareDTO();
		dto.setCertifiedProductId(1L);
		dto.setName("Addtional Software A");
		dto.setVersion("1.0");
		dto.setJustification("Because...");
		dto.setCreationDate(new Date());
		dto.setDeleted(false);
		dto.setLastModifiedDate(new Date());
		dto.setLastModifiedUser(Util.getCurrentUser().getId());
		
		AdditionalSoftwareDTO result = additionalSoftwareDAO.create(dto);
		AdditionalSoftwareDTO check = additionalSoftwareDAO.getById(result.getId());
		
		assertEquals(result.getName(), check.getName());
		assertEquals(result.getVersion(), check.getVersion());
		assertEquals(result.getJustification(), check.getJustification());
		assertEquals(result.getCreationDate(), check.getCreationDate());
		assertEquals(result.getDeleted(), check.getDeleted());
		assertEquals(result.getId(), check.getId());
		assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
		
		additionalSoftwareDAO.delete(result.getId());
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
}
