package gov.healthit.chpl.dao.impl;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationEventDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationEventDTO;

import java.util.Date;

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
public class CertificationEventDaoTest extends TestCase {

	
	@Autowired
	private CertificationEventDAO certificationEventDAO;
	
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
		
		CertificationEventDTO dto = new CertificationEventDTO();
		dto.setCertifiedProductId(1L);
		dto.setCity("NYC");
		dto.setCreationDate(new Date());
		dto.setDeleted(false);
		dto.setEventDate(new Date());
		dto.setEventTypeId(1L);
		dto.setLastModifiedDate(new Date());
		dto.setLastModifiedUser(Util.getCurrentUser().getId());
		dto.setState("NY");
		
		
		CertificationEventDTO result = create(dto);
		CertificationEventDTO check = certificationEventDAO.getById(result.getId());
		
		assertEquals(result.getCity(), check.getCity());
		assertEquals(result.getEventDate(), check.getEventDate());
		assertEquals(result.getEventTypeId(), check.getEventTypeId());
		assertEquals(result.getState(), check.getState());
		assertEquals(result.getCreationDate(), check.getCreationDate());
		assertEquals(result.getDeleted(), check.getDeleted());
		assertEquals(result.getId(), check.getId());
		assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
		
		
		delete(result.getId());
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}

	@Test
	@Transactional
	public void testUpdate() throws EntityRetrievalException, EntityCreationException {
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		CertificationEventDTO dto = new CertificationEventDTO();
		dto.setCertifiedProductId(1L);
		dto.setCity("NYC");
		dto.setCreationDate(new Date());
		dto.setDeleted(false);
		dto.setEventDate(new Date());
		dto.setEventTypeId(1L);
		dto.setId(1L);
		dto.setLastModifiedDate(new Date());
		dto.setLastModifiedUser(Util.getCurrentUser().getId());
		dto.setState("NY");
		
		CertificationEventDTO result = certificationEventDAO.create(dto);
		
		
		result.setCity("Jersey City");
		result.setCreationDate(new Date());
		result.setDeleted(false);
		result.setEventDate(new Date());
		result.setEventTypeId(1L);
		//result.setId(1L);
		result.setLastModifiedDate(new Date());
		result.setLastModifiedUser(Util.getCurrentUser().getId());
		result.setState("NY");
		
		
		certificationEventDAO.update(result);
		
		CertificationEventDTO check = certificationEventDAO.getById(result.getId());
		
		assertEquals(result.getCity(), check.getCity());
		assertEquals(result.getEventDate(), check.getEventDate());
		assertEquals(result.getEventTypeId(), check.getEventTypeId());
		assertEquals(result.getState(), check.getState());
		assertEquals(result.getCreationDate(), check.getCreationDate());
		assertEquals(result.getDeleted(), check.getDeleted());
		assertEquals(result.getId(), check.getId());
		assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
		
		certificationEventDAO.delete(result.getId());
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Transactional
	public void testDelete() throws EntityCreationException, EntityRetrievalException {
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		CertificationEventDTO dto = new CertificationEventDTO();
		dto.setCertifiedProductId(1L);
		dto.setCity("NYC");
		dto.setCreationDate(new Date());
		dto.setDeleted(false);
		dto.setEventDate(new Date());
		dto.setEventTypeId(1L);
		dto.setId(1L);
		dto.setLastModifiedDate(new Date());
		dto.setLastModifiedUser(Util.getCurrentUser().getId());
		dto.setState("NY");
		
		
		CertificationEventDTO result = create(dto);
		CertificationEventDTO check = certificationEventDAO.getById(result.getId());
		
		assertEquals(result.getCity(), check.getCity());
		assertEquals(result.getEventDate(), check.getEventDate());
		assertEquals(result.getEventTypeId(), check.getEventTypeId());
		assertEquals(result.getState(), check.getState());
		assertEquals(result.getCreationDate(), check.getCreationDate());
		assertEquals(result.getDeleted(), check.getDeleted());
		assertEquals(result.getId(), check.getId());
		assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
		
		
		delete(result.getId());
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
		assertNull(certificationEventDAO.getById(result.getId()));
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	@Transactional
	public void testFindAll() {
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		assertNotNull(certificationEventDAO.findAll());
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	@Transactional
	public void testGetById() throws EntityRetrievalException, EntityCreationException {
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		CertificationEventDTO dto = new CertificationEventDTO();
		dto.setCertifiedProductId(1L);
		dto.setCity("NYC");
		dto.setCreationDate(new Date());
		dto.setDeleted(false);
		dto.setEventDate(new Date());
		dto.setEventTypeId(1L);
		dto.setId(1L);
		dto.setLastModifiedDate(new Date());
		dto.setLastModifiedUser(Util.getCurrentUser().getId());
		dto.setState("NY");
		
		
		CertificationEventDTO result = create(dto);
		CertificationEventDTO check = certificationEventDAO.getById(result.getId());
		
		assertEquals(result.getCity(), check.getCity());
		assertEquals(result.getEventDate(), check.getEventDate());
		assertEquals(result.getEventTypeId(), check.getEventTypeId());
		assertEquals(result.getState(), check.getState());
		assertEquals(result.getCreationDate(), check.getCreationDate());
		assertEquals(result.getDeleted(), check.getDeleted());
		assertEquals(result.getId(), check.getId());
		assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
		
		
		delete(result.getId());
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Transactional
	public CertificationEventDTO create(CertificationEventDTO dto) throws EntityCreationException, EntityRetrievalException{
		CertificationEventDTO result = certificationEventDAO.create(dto);
		return result;
	}
	
	@Transactional
	public CertificationEventDTO update(CertificationEventDTO dto) throws EntityRetrievalException, EntityCreationException{
		CertificationEventDTO result = certificationEventDAO.update(dto);
		return result;
	}
	
	@Transactional
	public void delete(Long id) throws EntityRetrievalException, EntityCreationException{
		certificationEventDAO.delete(id);
	}
}