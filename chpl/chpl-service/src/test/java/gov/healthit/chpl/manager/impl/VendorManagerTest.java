package gov.healthit.chpl.manager.impl;

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

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.manager.VendorManager;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class VendorManagerTest extends TestCase {
	
	@Autowired private VendorManager vendorManager;
	
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
	
	@Test
	public void testGetVendorAsAdmin() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		VendorDTO vendor = vendorManager.getById(-1L);
		assertNotNull(vendor);
		assertNull(vendor.getTransparencyAttestation());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testGetVendorAsAcbAdmin() throws EntityRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(testUser3);
		VendorDTO vendor = vendorManager.getById(-1L);
		assertNotNull(vendor);
		assertNotNull(vendor.getTransparencyAttestation());
		assertTrue(vendor.getTransparencyAttestation());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
