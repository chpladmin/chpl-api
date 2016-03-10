package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
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
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class TestingLabDaoTest extends TestCase {

	@Autowired private TestingLabDAO atlDao;
	@Autowired private MutableAclService mutableAclService;
	
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
	public void testGetMaxAtlCode() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		String maxCode = atlDao.getMaxCode();
		assertNotNull(maxCode);
		assertEquals("01", maxCode);
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testGetAllAtls() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		List<TestingLabDTO> atls = atlDao.findAll(false);
		assertNotNull(atls);
		assertEquals(1, atls.size());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testCreateAtlWithoutAddress() throws EntityCreationException, EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		TestingLabDTO atl = new TestingLabDTO();
		atl.setName("ATL TEST");
		atl.setWebsite("http://www.google.com");
		atl.setCreationDate(new Date());
		atl.setDeleted(false);
		atl.setLastModifiedDate(new Date());
		atl.setLastModifiedUser(Util.getCurrentUser().getId());
		
		atl = atlDao.create(atl);
		
		assertNotNull(atl);
		assertNotNull(atl.getId());
		assertTrue(atl.getId() > 0L);
		assertNull(atl.getAddress());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testCreateAtlWithAddress() throws EntityCreationException, EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		TestingLabDTO atl = new TestingLabDTO();
		atl.setName("ATL TEST 2");
		atl.setWebsite("http://www.google.com");
		atl.setCreationDate(new Date());
		atl.setDeleted(false);
		atl.setLastModifiedDate(new Date());
		atl.setLastModifiedUser(Util.getCurrentUser().getId());
		AddressDTO address = new AddressDTO();
		address.setStreetLineOne("Some Street");
		address.setCity("Baltimore");
		address.setState("MD");
		address.setZipcode("21228");
		address.setCountry("USA");
		address.setDeleted(false);
		address.setLastModifiedDate(new Date());
		address.setLastModifiedUser(Util.getCurrentUser().getId());
		atl.setAddress(address);
		atl = atlDao.create(atl);
		
		assertNotNull(atl);
		assertNotNull(atl.getId());
		assertTrue(atl.getId() > 0L);
		assertNotNull(atl.getAddress());
		assertNotNull(atl.getAddress().getId());
		assertTrue(atl.getAddress().getId() > 0L);
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testUpdateAtl() {
		TestingLabDTO toUpdate = atlDao.findAll(false).get(0);
		toUpdate.setName("UPDATED NAME");
		
		TestingLabDTO result = null;
		try {
			result = atlDao.update(toUpdate);
		} catch(Exception ex) {
			fail("could not update atl!");
			System.out.println(ex.getStackTrace());
		}
		assertNotNull(result);

		try {
			TestingLabDTO updatedAtl = atlDao.getById(toUpdate.getId());
			assertEquals("UPDATED NAME", updatedAtl.getName());
		} catch(Exception ex) {
			fail("could not find atl!");
			System.out.println(ex.getStackTrace());
		}
	}
	
	@Test
	public void testDeleteAtl() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Long deleteId = -1L;
		atlDao.delete(deleteId);
		
		TestingLabDTO deleted = atlDao.getById(deleteId);
		assertNull(deleted);
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void listUsersForAtl() {
		Long atlIdWithUsers=-1L;
		ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, atlIdWithUsers);
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		List<String> userNames = new ArrayList<String>();
		List<AccessControlEntry> entries = acl.getEntries();
		for (int i = 0; i < entries.size(); i++) {
			Sid sid = entries.get(i).getSid();
			if(sid instanceof PrincipalSid) {
				PrincipalSid psid = (PrincipalSid)sid;
				userNames.add(psid.getPrincipal());
			} else {
				userNames.add(sid.toString());
			}
		}

		assertNotNull(userNames);
		assertEquals(2, userNames.size());
	}
}
