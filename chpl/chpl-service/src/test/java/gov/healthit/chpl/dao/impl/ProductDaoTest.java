package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class ProductDaoTest extends TestCase {

    @Autowired
    private ProductDAO productDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Test
    @Transactional(readOnly = true)
    public void getAllProducts() {
        List<ProductDTO> results = productDao.findAll();
        assertNotNull(results);
        assertEquals(7, results.size());
    }

    @Test
    @Transactional(readOnly = true)
    public void getProductById() {
        Long productId = -1L;
        ProductDTO product = null;
        try {
            product = productDao.getById(productId);
        } catch (EntityRetrievalException ex) {
            fail("Could not find product with id " + productId);
        }
        assertNotNull(product);
        assertEquals(-1, product.getId().longValue());
    }

    @Test
    @Transactional(readOnly = true)
    public void getProductByDeveloper() {
        Long developerId = -1L;
        List<ProductDTO> products = null;
        products = productDao.getByDeveloper(developerId);
        assertNotNull(products);
        assertEquals(3, products.size());
    }

    @Test
    @Transactional(readOnly = true)
    public void getProductByDevelopers() {
        List<Long> developerIds = new ArrayList<Long>();
        developerIds.add(-1L);
        developerIds.add(-2L);
        List<ProductDTO> products = null;
        products = productDao.getByDevelopers(developerIds);
        assertNotNull(products);
        assertEquals(4, products.size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateProduct() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        ProductDTO product = productDao.getById(-1L);
        product.setDeveloperId(-2L);

        ProductDTO result = null;
        try {
            result = productDao.update(product);
        } catch (Exception ex) {
            fail("could not update product!");
            ex.printStackTrace();
        }
        assertNotNull(result);

        try {
            ProductDTO updatedProduct = productDao.getById(product.getId());
            assertTrue(updatedProduct.getDeveloperId() == -2L);
        } catch (Exception ex) {
            fail("could not find product!");
            ex.printStackTrace();
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    /**
     * Description: Tests that the productDAO can create a new product based on
     * the productDTO Expected Result: product is created successfully: result
     * is non-null result has a non-null id Result id > 0 ProductDAO returns
     * non-null product id
     */
    @Transactional
    @Rollback(true)
    @Test
    public void createProduct() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        ProductDTO product = new ProductDTO();
        product.setCreationDate(new Date());
        product.setDeleted(false);
        product.setLastModifiedDate(new Date());
        product.setLastModifiedUser(-2L);
        product.setName("Unit Test Developer!");
        product.setDeveloperId(-1L);

        ProductDTO result = null;
        try {
            result = productDao.create(product);
        } catch (Exception ex) {
            fail("could not create product!");
            ex.printStackTrace();
        }

        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(result.getId() > 0L);
        assertNotNull(productDao.getById(result.getId()));

        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
