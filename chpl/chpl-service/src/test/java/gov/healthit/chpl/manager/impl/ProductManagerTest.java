package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.ContactDAO;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import junit.framework.TestCase;

@ActiveProfiles({
    "Ff4jMock"
})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class, gov.healthit.chpl.Ff4jTestConfiguration.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class ProductManagerTest extends TestCase {

    @Autowired
    private ProductManager productManager;
    @Autowired
    private DeveloperManager developerManager;
    @Autowired
    private CertifiedProductDetailsManager cpdManager;
    @Autowired
    private DeveloperStatusDAO devStatusDao;
    @Autowired
    private ContactDAO contactDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser, testUser2, testUser3;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        testUser2 = new JWTAuthenticatedUser();
        testUser2.setFullName("Test");
        testUser2.setId(2L);
        testUser2.setFriendlyName("User2");
        testUser2.setSubjectName("testUser2");
        testUser2.getPermissions().add(new GrantedPermission("ROLE_ACB"));

        testUser3 = new JWTAuthenticatedUser();
        testUser3.setFullName("Test");
        testUser3.setId(3L);
        testUser3.setFriendlyName("User3");
        testUser3.setSubjectName("testUser3");
        testUser3.getPermissions().add(new GrantedPermission("ROLE_ACB"));
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @Transactional(readOnly = true)
    public void getAllProducts() {
        List<ProductDTO> results = productManager.getAll();
        assertNotNull(results);
        assertEquals(7, results.size());
    }

    @Test
    @Transactional(readOnly = true)
    public void getProductById() {
        Long productId = -1L;
        ProductDTO product = null;
        try {
            product = productManager.getById(productId);
        } catch (EntityRetrievalException ex) {
            fail("Could not find product with id " + productId);
        }
        assertNotNull(product);
        assertEquals(-1, product.getId().longValue());
        assertNotNull(product.getOwnerHistory());
        assertEquals(1, product.getOwnerHistory().size());
        List<ProductOwnerDTO> previousOwners = product.getOwnerHistory();
        for (ProductOwnerDTO previousOwner : previousOwners) {
            assertNotNull(previousOwner.getDeveloper());
            assertEquals(-2, previousOwner.getDeveloper().getId().longValue());
            assertEquals("Test Developer 2", previousOwner.getDeveloper().getName());
        }
    }

    @Test
    @Transactional(readOnly = true)
    public void getProductByDeveloper() {
        Long developerId = -1L;
        List<ProductDTO> products = null;
        products = productManager.getByDeveloper(developerId);
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
        products = productManager.getByDevelopers(developerIds);
        assertNotNull(products);
        assertEquals(4, products.size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void createProductWithNewContact() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        ProductDTO toCreate = new ProductDTO();
        toCreate.setName("New Product");
        toCreate.setDeveloperId(-1L);
        toCreate.setReportFileLocation("http://www.google.com");
        ContactDTO contact = new ContactDTO();
        String fullName = "Testfirstname";
        String phoneNumber = "4445556666";
        String title = "Mrs.";
        String email = "test.email@domain.com";
        contact.setFullName(fullName);
        contact.setPhoneNumber(phoneNumber);
        contact.setTitle(title);
        contact.setEmail(email);
        toCreate.setContact(contact);

        ProductDTO created = null;
        try {
            created = productManager.create(toCreate);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        assertNotNull(created.getId());
        assertNotNull(created.getContact());
        assertNotNull(created.getContact().getId());
        assertEquals(fullName, created.getContact().getFullName());
        assertEquals(phoneNumber, created.getContact().getPhoneNumber());
        assertEquals(title, created.getContact().getTitle());
        assertEquals(email, created.getContact().getEmail());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void createProductWithExistingContact() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        ProductDTO toCreate = new ProductDTO();
        toCreate.setName("New Product");
        toCreate.setDeveloperId(-1L);
        toCreate.setReportFileLocation("http://www.google.com");
        Long contactId = 1L;
        ContactDTO contact = new ContactDTO();
        contact.setId(contactId);
        toCreate.setContact(contact);

        ProductDTO created = null;
        try {
            created = productManager.create(toCreate);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        assertNotNull(created.getId());
        assertNotNull(created.getContact());
        assertNotNull(created.getContact().getId());
        assertEquals(contactId.longValue(), created.getContact().getId().longValue());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateProductWithNewContact() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        ProductDTO toUpdate = productManager.getById(-1L);
        ContactDTO contact = new ContactDTO();
        String fullName = "Testfirstname";
        String phoneNumber = "4445556666";
        String title = "Mrs.";
        String email = "test.email@domain.com";
        contact.setFullName(fullName);
        contact.setPhoneNumber(phoneNumber);
        contact.setTitle(title);
        contact.setEmail(email);
        toUpdate.setContact(contact);

        ProductDTO updated = null;
        try {
            updated = productManager.update(toUpdate);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        assertNotNull(updated.getId());
        assertNotNull(updated.getContact());
        assertNotNull(updated.getContact().getId());
        assertEquals(fullName, updated.getContact().getFullName());
        assertEquals(phoneNumber, updated.getContact().getPhoneNumber());
        assertEquals(title, updated.getContact().getTitle());
        assertEquals(email, updated.getContact().getEmail());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateProductWithExistingContact() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        ProductDTO toUpdate = productManager.getById(-1L);
        Long contactId = 1L;
        ContactDTO contact = contactDao.getById(contactId);
        toUpdate.setContact(contact);

        ProductDTO updated = null;
        try {
            updated = productManager.update(toUpdate);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        assertNotNull(updated.getId());
        assertNotNull(updated.getContact());
        assertNotNull(updated.getContact().getId());
        assertEquals(contactId.longValue(), updated.getContact().getId().longValue());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void getCertifiedProductDetailsWithProductContact() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        ProductDTO toUpdate = productManager.getById(-1L);
        Long contactId = 1L;
        ContactDTO contact = contactDao.getById(contactId);
        toUpdate.setContact(contact);

        ProductDTO updated = null;
        try {
            updated = productManager.update(toUpdate);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        assertNotNull(updated.getId());
        assertNotNull(updated.getContact());
        assertNotNull(updated.getContact().getId());
        assertEquals(contactId.longValue(), updated.getContact().getId().longValue());

        CertifiedProductSearchDetails deets = cpdManager.getCertifiedProductDetails(1L);
        assertNotNull(deets);
        assertNotNull(deets.getProduct());
        assertEquals(-1, deets.getProduct().getProductId().longValue());
        assertNotNull(deets.getProduct().getContact());
        assertNotNull(deets.getProduct().getContact().getContactId());
        assertEquals(1, deets.getProduct().getContact().getContactId().longValue());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateProductOwnerHistory() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        ProductDTO product = productManager.getById(-1L);
        assertNotNull(product.getOwnerHistory());
        assertTrue(product.getOwnerHistory().size() == 1);

        product.setOwnerHistory(null);
        try {
            productManager.update(product);
        } catch (Exception ex) {
            fail("could not update product!");
            ex.printStackTrace();
        }

        try {
            ProductDTO updatedProduct = productManager.getById(product.getId());
            assertNotNull(updatedProduct);
            assertTrue(updatedProduct.getOwnerHistory() == null || updatedProduct.getOwnerHistory().size() == 0);
        } catch (Exception ex) {
            fail("could not find product!");
            ex.printStackTrace();
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testAllowedToUpdateProductWithActiveDeveloper()
            throws EntityRetrievalException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        ProductDTO product = productManager.getById(-1L);
        assertNotNull(product);
        product.setName("new product name");
        boolean failed = false;
        try {
            product = productManager.update(product);
        } catch (EntityCreationException ex) {
            System.out.println(ex.getMessage());
            failed = true;
        }
        assertFalse(failed);
        assertEquals("new product name", product.getName());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateProductWithInactiveDeveloper_adminAllowed()
            throws EntityRetrievalException, JsonProcessingException, MissingReasonException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        // change dev to suspended
        DeveloperDTO developer = developerManager.getById(-1L);
        assertNotNull(developer);
        DeveloperStatusDTO newStatus = devStatusDao.getById(2L);
        DeveloperStatusEventDTO newStatusHistory = new DeveloperStatusEventDTO();
        newStatusHistory.setDeveloperId(developer.getId());
        newStatusHistory.setStatus(newStatus);
        newStatusHistory.setStatusDate(new Date());
        developer.getStatusEvents().add(newStatusHistory);

        boolean failed = false;
        try {
            developer = developerManager.update(developer, false);
        } catch (EntityCreationException ex) {
            System.out.println(ex.getMessage());
            failed = true;
        }
        assertFalse(failed);
        DeveloperStatusEventDTO status = developer.getStatus();
        assertNotNull(status);
        assertNotNull(status.getId());
        assertNotNull(status.getStatus());
        assertNotNull(status.getStatus().getStatusName());
        assertEquals(DeveloperStatusType.SuspendedByOnc.toString(), status.getStatus().getStatusName());

        // try to update product
        ProductDTO product = productManager.getById(-1L);
        assertNotNull(product);
        product.setName("new product name");
        failed = false;
        try {
            product = productManager.update(product);
        } catch (EntityCreationException ex) {
            System.out.println(ex.getMessage());
            failed = true;
        }
        assertFalse(failed);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testProductSplitFailsWithoutAuthentication() throws EntityRetrievalException {
        ProductDTO origProduct = productManager.getById(-2L);
        ProductDTO newProduct = new ProductDTO();
        newProduct.setName("Split Product");
        newProduct.setDeveloperId(origProduct.getDeveloperId());
        List<ProductVersionDTO> newProductVersions = new ArrayList<ProductVersionDTO>();
        ProductVersionDTO newProductVersion = new ProductVersionDTO();
        newProductVersion.setId(5L);
        newProductVersions.add(newProductVersion);
        boolean failedAuth = false;
        try {
            productManager.split(origProduct, newProduct, "SPLIT", newProductVersions);
        } catch (AuthenticationCredentialsNotFoundException ex) {
            failedAuth = true;
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        assertTrue(failedAuth);
    }

    @Test
    @Transactional
    @Rollback
    public void testProductSplitFailsAsIncorrectAcbAdmin() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(testUser2);

        ProductDTO origProduct = productManager.getById(-2L);
        ProductDTO newProduct = new ProductDTO();
        newProduct.setName("Split Product");
        newProduct.setDeveloperId(origProduct.getDeveloperId());
        List<ProductVersionDTO> newProductVersions = new ArrayList<ProductVersionDTO>();
        ProductVersionDTO newProductVersion = new ProductVersionDTO();
        newProductVersion.setId(5L);
        newProductVersions.add(newProductVersion);
        boolean accessDeniedOnCps = false;
        try {
            productManager.split(origProduct, newProduct, "SPLIT", newProductVersions);
        } catch (AccessDeniedException ex) {
            accessDeniedOnCps = true;
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        assertTrue(accessDeniedOnCps);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testProductSplitFailsWithSuspendedDeveloper() throws EntityRetrievalException, EntityCreationException,
            JsonProcessingException, MissingReasonException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        DeveloperDTO developer = developerManager.getById(-1L);
        // suspended by ONC
        DeveloperStatusDTO newStatus = devStatusDao.getById(2L);
        DeveloperStatusEventDTO newStatusHistory = new DeveloperStatusEventDTO();
        newStatusHistory.setDeveloperId(developer.getId());
        newStatusHistory.setStatus(newStatus);
        newStatusHistory.setStatusDate(new Date());
        developer.getStatusEvents().add(newStatusHistory);
        developer = developerManager.update(developer, false);

        ProductDTO origProduct = productManager.getById(-2L);
        ProductDTO newProduct = new ProductDTO();
        newProduct.setName("Split Product");
        newProduct.setDeveloperId(origProduct.getDeveloperId());
        List<ProductVersionDTO> newProductVersions = new ArrayList<ProductVersionDTO>();
        ProductVersionDTO newProductVersion = new ProductVersionDTO();
        newProductVersion.setId(-5L);
        newProductVersions.add(newProductVersion);
        boolean productCreateError = false;
        try {
            productManager.split(origProduct, newProduct, "SPLIT", newProductVersions);
        } catch (EntityCreationException ex) {
            productCreateError = true;
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        assertFalse(productCreateError);

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testProductSplitAllowedAsAdmin() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        String name = "Split Product";
        String code = "SPLI";

        ProductDTO origProduct = productManager.getById(-2L);
        assertNotNull(origProduct.getProductVersions());
        assertEquals(3, origProduct.getProductVersions().size());
        CertifiedProductSearchDetails cpDetails = cpdManager.getCertifiedProductDetails(7L);
        assertFalse(cpDetails.getChplProductNumber().contains(code));

        ProductDTO newProduct = new ProductDTO();
        newProduct.setName(name);
        newProduct.setDeveloperId(origProduct.getDeveloperId());
        List<ProductVersionDTO> newProductVersions = new ArrayList<ProductVersionDTO>();
        ProductVersionDTO newProductVersion = new ProductVersionDTO();
        newProductVersion.setId(-7L);
        newProductVersions.add(newProductVersion);
        ProductDTO updatedNewProduct = null;
        try {
            updatedNewProduct = productManager.split(origProduct, newProduct, code, newProductVersions);
        } catch (Exception ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        }

        ProductDTO updatedOrigProduct = productManager.getById(origProduct.getId());
        assertNotNull(updatedOrigProduct.getProductVersions());
        assertEquals(2, updatedOrigProduct.getProductVersions().size());

        assertNotNull(updatedNewProduct);
        assertEquals(name, updatedNewProduct.getName());
        assertNotNull(updatedNewProduct.getProductVersions());
        assertEquals(1, updatedNewProduct.getProductVersions().size());
        cpDetails = cpdManager.getCertifiedProductDetails(7L);
        assertTrue(cpDetails.getChplProductNumber().contains(code));

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AccessDeniedException.class)
    @Transactional
    @Rollback
    public void testProductSplitAllowedAsAcbAdmin()
            throws EntityRetrievalException, AccessDeniedException, JsonProcessingException, EntityCreationException {
        SecurityContextHolder.getContext().setAuthentication(testUser3);

        String name = "Split Product";
        String code = "SPLI";

        ProductDTO origProduct = productManager.getById(-2L);
        assertNotNull(origProduct.getProductVersions());
        assertEquals(3, origProduct.getProductVersions().size());
        CertifiedProductSearchDetails cpDetails = cpdManager.getCertifiedProductDetails(7L);
        assertFalse(cpDetails.getChplProductNumber().contains(code));

        ProductDTO newProduct = new ProductDTO();
        newProduct.setName(name);
        newProduct.setDeveloperId(origProduct.getDeveloperId());
        List<ProductVersionDTO> newProductVersions = new ArrayList<ProductVersionDTO>();
        ProductVersionDTO newProductVersion = new ProductVersionDTO();
        newProductVersion.setId(7L);
        newProductVersions.add(newProductVersion);
        ProductDTO updatedNewProduct = null;
        try {
            updatedNewProduct = productManager.split(origProduct, newProduct, code, newProductVersions);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    @Test
    @Transactional
    @Rollback
    public void testProductSplitNotAllowedBadProductCodeAsAcbAdmin() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(testUser3);

        String name = "Split Product";
        String code = "%%%$$$%%%";

        ProductDTO origProduct = productManager.getById(-2L);
        assertNotNull(origProduct.getProductVersions());
        assertEquals(3, origProduct.getProductVersions().size());
        CertifiedProductSearchDetails cpDetails = cpdManager.getCertifiedProductDetails(7L);
        assertFalse(cpDetails.getChplProductNumber().contains(code));

        ProductDTO newProduct = new ProductDTO();
        newProduct.setName(name);
        newProduct.setDeveloperId(origProduct.getDeveloperId());
        List<ProductVersionDTO> newProductVersions = new ArrayList<ProductVersionDTO>();
        ProductVersionDTO newProductVersion = new ProductVersionDTO();
        newProductVersion.setId(7L);
        newProductVersions.add(newProductVersion);
        boolean failed = false;
        try {
            productManager.split(origProduct, newProduct, code, newProductVersions);
        } catch (Exception ex) {
            failed = true;
        }
        assertTrue(failed);

        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
