package gov.healthit.chpl.manager.impl;

import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.manager.VendorManager;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.ChplTestConfig.class })
public class VendorManagerTest extends TestCase {

	@Autowired
	private VendorManager vm;

	private static JWTAuthenticatedUser authUser;

	@BeforeClass
	public static void setUpClass() throws Exception {
		// rcarver - setup the jndi context and the datasource
		try {
			// Create initial context
			System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
			System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
			InitialContext ic = new InitialContext();

			ic.createSubcontext("java:");
			ic.createSubcontext("java:/comp");
			ic.createSubcontext("java:/comp/env");
			ic.createSubcontext("java:/comp/env/jdbc");

			// Construct DataSource
			PGPoolingDataSource ds = new PGPoolingDataSource();
			ds.setServerName("localhost/openchpl");

			// ds.setURL("jdbc:oracle:thin:@localhost:5432:chpl_acl");
			ds.setUser("openchpl");
			ds.setPassword("openchpl1!");

			ic.bind("java:/comp/env/jdbc/openchpl", ds);
		} catch (NamingException ex) {
			// Logger.getLogger(MyDAOTest.class.getName()).log(Level.SEVERE,
			// null, ex);
			ex.printStackTrace();
		}

		authUser = new JWTAuthenticatedUser();
		authUser.setFirstName("Admin");
		authUser.setId(-2L);
		authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}

	@Test
	public void getAllVendors() {
		List<VendorDTO> results = vm.getAll();
		assertNotNull(results);
		assertEquals(2, results.size());
	}

	@Test
	public void getVendorWithAddress() {
		Long vendorId = 1L;
		VendorDTO vendor = null;
		try {
			vendor = vm.getById(vendorId);
		} catch(EntityRetrievalException ex) {
			fail("Could not find vendor with id " + vendorId);
		}
		assertNotNull(vendor);
		assertEquals(1, vendor.getId().longValue());
		assertNotNull(vendor.getAddress());
		assertEquals(1, vendor.getAddress().getId().longValue());
	}
	
	@Test
	public void getVendorWithoutAddress() {
		Long vendorId = 2L;
		VendorDTO vendor = null;
		try {
			vendor = vm.getById(vendorId);
		} catch(EntityRetrievalException ex) {
			fail("Could not find vendor with id " + vendorId);
		}
		assertNotNull(vendor);
		assertEquals(2, vendor.getId().longValue());
		assertNull(vendor.getAddress());
	}
	// @Test
	// public void getAllVendorsAuthenticated() {
	// SecurityContextHolder.getContext().setAuthentication(authUser);
	// List<VendorDTO> results = vm.getAll();
	// assertNotNull(results);
	// assertEquals(1, results.size());
	// SecurityContextHolder.getContext().setAuthentication(null);
	// }
}
