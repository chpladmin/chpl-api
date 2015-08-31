package gov.healthit.chpl.dao.impl;

import java.util.Date;
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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CertificationBodyDaoTest extends TestCase {

	@Autowired private CertificationBodyDAO acbDao;
	
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
	public void testGetAllAcbs() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		List<CertificationBodyDTO> acbs = acbDao.findAll();
		assertNotNull(acbs);
		assertEquals(7, acbs.size());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testCreateAcbWithoutAddress() throws EntityCreationException, EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		CertificationBodyDTO acb = new CertificationBodyDTO();
		acb.setName("ACB TEST");
		acb.setWebsite("http://www.google.com");
		acb.setCreationDate(new Date());
		acb.setDeleted(false);
		acb.setLastModifiedDate(new Date());
		acb.setLastModifiedUser(Util.getCurrentUser().getId());
		
		acb = acbDao.create(acb);
		
		assertNotNull(acb);
		assertNotNull(acb.getId());
		assertTrue(acb.getId() > 0L);
		assertNull(acb.getAddress());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testCreateAcbWithAddress() throws EntityCreationException, EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		CertificationBodyDTO acb = new CertificationBodyDTO();
		acb.setName("ACB TEST 2");
		acb.setWebsite("http://www.google.com");
		acb.setCreationDate(new Date());
		acb.setDeleted(false);
		acb.setLastModifiedDate(new Date());
		acb.setLastModifiedUser(Util.getCurrentUser().getId());
		AddressDTO address = new AddressDTO();
		address.setStreetLineOne("Some Street");
		address.setCity("Baltimore");
		address.setRegion("MD");
		address.setCountry("21228");
		address.setDeleted(false);
		address.setLastModifiedDate(new Date());
		address.setLastModifiedUser(Util.getCurrentUser().getId());
		acb.setAddress(address);
		acb = acbDao.create(acb);
		
		assertNotNull(acb);
		assertNotNull(acb.getId());
		assertTrue(acb.getId() > 0L);
		assertNotNull(acb.getAddress());
		assertNotNull(acb.getAddress().getId());
		assertTrue(acb.getAddress().getId() > 0L);
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
