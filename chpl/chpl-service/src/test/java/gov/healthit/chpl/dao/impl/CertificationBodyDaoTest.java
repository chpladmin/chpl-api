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
	public void testGetMaxAcbCode() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		String maxCode = acbDao.getMaxCode();
		assertNotNull(maxCode);
		assertEquals("07", maxCode);
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testGetAllAcbs() {
		SecurityContextHolder.getContext().setAuthentication(adminUser);

		List<CertificationBodyDTO> acbs = acbDao.findAll(false);
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
		address.setState("MD");
		address.setZipcode("21228");
		address.setCountry("USA");
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
	
	@Test
	public void testUpdateAcb() {
		CertificationBodyDTO toUpdate = acbDao.findAll(false).get(0);
		toUpdate.setName("UPDATED NAME");
		
		CertificationBodyDTO result = null;
		try {
			result = acbDao.update(toUpdate);
		} catch(Exception ex) {
			fail("could not update acb!");
			System.out.println(ex.getStackTrace());
		}
		assertNotNull(result);

		try {
			CertificationBodyDTO updatedAcb = acbDao.getById(toUpdate.getId());
			assertEquals("UPDATED NAME", updatedAcb.getName());
		} catch(Exception ex) {
			fail("could not find acb!");
			System.out.println(ex.getStackTrace());
		}
	}
	
	@Test
	public void testDeleteAcb() throws EntityRetrievalException {
		Long deleteId = -1L;
		acbDao.delete(deleteId);
		
		CertificationBodyDTO deleted = acbDao.getById(deleteId);
		assertNull(deleted);
	}
	
	@Test
	public void listUsersForAcb() {
		Long acbIdWithUsers=-3L;
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acbIdWithUsers);
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
