package gov.healthit.chpl.dao.impl;

import java.util.List;


import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class ProductVersionDaoTest extends TestCase {

	@Autowired
	private ProductVersionDAO versionDao;

	private static JWTAuthenticatedUser authUser;

	@BeforeClass
	public static void setUpClass() throws Exception {
		authUser = new JWTAuthenticatedUser();
		authUser.setFirstName("Admin");
		authUser.setId(-2L);
		authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}

	@Test
	public void getAllProductVersions() {
		List<ProductVersionDTO> results = versionDao.findAll();
		assertNotNull(results);
		assertEquals(9, results.size());
	}

	@Test
	public void getVersionById() {
		Long versionId = 1L;
		ProductVersionDTO version = null;
		try {
			version = versionDao.getById(versionId);
		} catch(EntityRetrievalException ex) {
			fail("Could not find version with id " + versionId);
		}
		assertNotNull(version);
		assertEquals(1, version.getId().longValue());
	}
	
	@Test
	public void getVersionByProduct() {
		Long productId = 1L;
		List<ProductVersionDTO> versions = null;
		versions = versionDao.getByProductId(productId);
		assertNotNull(versions);
		assertEquals(4, versions.size());
	}
}