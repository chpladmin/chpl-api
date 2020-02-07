package old.gov.healthit.chpl.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Tests for Certified Product basic DAO.
 * @author alarned
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = old.gov.healthit.chpl.CHPLTestConfig.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductDaoTest {

    @Autowired
    private CertifiedProductDAO productDao;
    private static final String URL_PATTERN = "^https?://([\\da-z\\.-]+)\\.([a-z\\.]{2,6})"
            + "(:[0-9]+)?([\\/\\w \\.\\-\\,=&%#]*)*(\\?([\\/\\w \\.\\-\\,=&%#]*)*)?";
    private static final long ADMIN_ID = -2L;
    private static final int LISTING_COUNT = 18;
    private static final long BASIC_LISTING_ID = 1L;
    private static final long ANOTHER_LISTING_ID = 2L;
    private static final long DELETED_LISTING_ID = 11L;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser authUser;
    private static Pattern urlPattern = Pattern.compile(URL_PATTERN);

    /**
     * Set up steps.
     */
    @BeforeClass
    public static void setUpClass() {
        authUser = new JWTAuthenticatedUser();
        authUser.setFullName("Admin");
        authUser.setId(ADMIN_ID);
        authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    /**
     * Test good vs bad URLs.
     */
    @Test
    public void testUrlPattern() {
        String badUrl = "www.google.com";

        assertEquals(false, urlPattern.matcher(badUrl).matches());

        String goodUrl = "http://www.google.com";
        assertEquals(true, urlPattern.matcher(goodUrl).matches());
    }

    /**
     * Test we can get all Listings.
     */
    @Test
    @Transactional(readOnly = true)
    public void getAllCertifiedProducts() {
        List<CertifiedProductDetailsDTO> results = productDao.findAll();
        assertNotNull(results);
        assertEquals(LISTING_COUNT, results.size());
    }

    /**
     * Test that we can get a Listing.
     */
    @Test
    @Transactional(readOnly = true)
    public void getById() {
        Long productId = BASIC_LISTING_ID;
        CertifiedProductDTO product = null;
        try {
            product = productDao.getById(productId);
        } catch (EntityRetrievalException ex) {
            fail("Could not find version with id " + productId);
        }
        assertNotNull(product);
        assertEquals(1, product.getId().longValue());
    }

    /**
     * Test that we can get Listings under a Version.
     */
    @Test
    @Transactional(readOnly = true)
    public void getProductsByVersion() {
        final int expectedListings = 3;
        Long versionId = -1L;
        List<CertifiedProductDetailsDTO> products = null;
        products = productDao.getDetailsByVersionId(versionId);
        assertNotNull(products);
        assertEquals(expectedListings, products.size());
    }

    /**
     * Test that we can get Listings based on CHPL Product Numbers.
     */
    @Test
    @Transactional(readOnly = true)
    public void getDetailsByChplNumbers() {
        final int expectedCount = 3;
        List<String> chplProductNumbers = new ArrayList<String>();
        chplProductNumbers.add("CHP-024050");
        chplProductNumbers.add("CHP-024051");
        chplProductNumbers.add("CHP-024052");
        List<CertifiedProductDetailsDTO> products = null;
        products = productDao.getDetailsByChplNumbers(chplProductNumbers);
        assertNotNull(products);
        assertEquals(expectedCount, products.size());
    }

    /**
     * Given that I am authenticated as an admin
     * when I delete a certified product
     * then that certified product no longer exists in the database.
     * @throws EntityRetrievalException if cannot retrieve entity
     */
    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void delete() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(authUser);
        Long productId = BASIC_LISTING_ID;
        productDao.delete(productId);
        CertifiedProductDTO deletedProduct = productDao.getById(productId);
        assertTrue(deletedProduct.getDeleted());
    }

    /**
     * Test that we can get Listings by array list of IDs.
     * @throws EntityRetrievalException if cannot retrieve listings
     */
    @Test
    @Transactional(readOnly = true)
    @Rollback
    public void getDetailsByIdsWorks() throws EntityRetrievalException {
        final int expectedCount = 2;
        List<Long> ids = new ArrayList<Long>();
        ids.add(BASIC_LISTING_ID);
        ids.add(ANOTHER_LISTING_ID);
        assertEquals(expectedCount, productDao.getDetailsByIds(ids).size());
    }

    /**
     * Test that we don't get deleted listings by array list of IDs.
     * @throws EntityRetrievalException if cannot retrieve listing
     */
    @Test
    @Transactional(readOnly = true)
    @Rollback
    public void getDetailsByIdsWithDeletedItem() throws EntityRetrievalException {
        final int expectedCount = 1;
        List<Long> ids = new ArrayList<Long>();
        ids.add(BASIC_LISTING_ID);
        ids.add(DELETED_LISTING_ID);
        assertEquals(expectedCount, productDao.getDetailsByIds(ids).size());
    }
}
