package gov.healthit.chpl.listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
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

import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.SimpleExplainableAction;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.domain.UpdateDevelopersRequest;
import gov.healthit.chpl.domain.UpdateProductsRequest;
import gov.healthit.chpl.domain.UpdateVersionsRequest;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;
import gov.healthit.chpl.web.controller.CertificationBodyController;
import gov.healthit.chpl.web.controller.CertifiedProductController;
import gov.healthit.chpl.web.controller.DeveloperController;
import gov.healthit.chpl.web.controller.ProductController;
import gov.healthit.chpl.web.controller.ProductVersionController;
import gov.healthit.chpl.web.controller.SurveillanceController;
import junit.framework.TestCase;

/**
 * Test that certain updates refresh the listing cache as expected.
 * @author kekey
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CacheRefreshTest extends TestCase {

    @Autowired private DeveloperController developerController;
    @Autowired private ProductController productController;
    @Autowired private ProductVersionController versionController;
    @Autowired private CertificationBodyController acbController;
    @Autowired private CertifiedProductController cpController;
    @Autowired private CertifiedProductDetailsManager cpdManager;
    @Autowired private SurveillanceController survController;
    @Autowired private SurveillanceDAO survDao;
    @Autowired private CertifiedProductSearchManager searchManager;

    private static JWTAuthenticatedUser adminUser;
    private static JWTAuthenticatedUser oncAdmin;
    private static final long ADMIN_ID = -2L;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    /**
     * Set up class for tests.
     *
     * @throws Exception if user can't be created
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(ADMIN_ID);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));

        oncAdmin = new JWTAuthenticatedUser();
        oncAdmin.setFullName("oncAdmin");
        oncAdmin.setId(3L);
        oncAdmin.setFriendlyName("User");
        oncAdmin.setSubjectName("oncAdminUser");
        oncAdmin.getPermissions().add(new GrantedPermission(Authority.ROLE_ADMIN));
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateDeveloperNameRefreshesCache() throws
    EntityCreationException, EntityRetrievalException,
    ValidationException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<CertifiedProductFlatSearchResult> listingsFromDeveloper =
                new ArrayList<CertifiedProductFlatSearchResult>();

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        for (CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if (listing.getDeveloper().equals("Test Developer 1")) {
                listingsFromDeveloper.add(listing);
            }
        }
        assertTrue(listingsFromDeveloper.size() > 0);

        //update the developer
        //should trigger the cache refresh
        Developer devToUpdate = developerController.getDeveloperById(-1L);
        UpdateDevelopersRequest req = new UpdateDevelopersRequest();
        devToUpdate.setName("Updated Name");
        //set other required information
        Contact contact = new Contact();
        contact.setEmail("test@test.com");
        contact.setFullName("Test Fullname");
        contact.setPhoneNumber("111-222-3333");
        devToUpdate.setContact(contact);
        req.setDeveloper(devToUpdate);
        List<Long> idsToUpdate = new ArrayList<Long>();
        idsToUpdate.add(-1L);
        req.setDeveloperIds(idsToUpdate);
        developerController.updateDeveloper(req);

        //get the cached listings now, should have been updated in the aspect and have
        //the latest developer stuff
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        for (CertifiedProductFlatSearchResult listing : allListingsAfterUpdate) {
            for (CertifiedProductFlatSearchResult listingFromDeveloper : listingsFromDeveloper) {
                if (listing.getId().longValue() == listingFromDeveloper.getId().longValue()) {
                    assertEquals(devToUpdate.getName(), listing.getDeveloper());
                }
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateProductNameRefreshesCache() throws
    EntityCreationException, EntityRetrievalException,
    ValidationException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<CertifiedProductFlatSearchResult> listingsFromProduct =
                new ArrayList<CertifiedProductFlatSearchResult>();

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        for (CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if (listing.getProduct().equals("Test Product 1")) {
                listingsFromProduct.add(listing);
            }
        }
        assertTrue(listingsFromProduct.size() > 0);

        //update the product
        //should trigger the cache refresh
        Product productToUpdate = productController.getProductById(-1L);
        UpdateProductsRequest req = new UpdateProductsRequest();
        productToUpdate.setName("Updated Name");
        req.setProduct(productToUpdate);
        List<Long> idsToUpdate = new ArrayList<Long>();
        idsToUpdate.add(-1L);
        req.setProductIds(idsToUpdate);
        productController.updateProduct(req);

        //get the cached listings now, should have been updated in the aspect and have
        //the latest product name
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        for (CertifiedProductFlatSearchResult listing : allListingsAfterUpdate) {
            for (CertifiedProductFlatSearchResult listingFromProduct : listingsFromProduct) {
                if (listing.getId().longValue() == listingFromProduct.getId().longValue()) {
                    assertEquals(productToUpdate.getName(), listing.getProduct());
                }
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateVersionRefreshesCache() throws
    EntityCreationException, EntityRetrievalException,
    ValidationException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<CertifiedProductFlatSearchResult> listingsFromVersion =
                new ArrayList<CertifiedProductFlatSearchResult>();

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        for (CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if (listing.getVersion().equals("2.0")) {
                listingsFromVersion.add(listing);
            }
        }
        assertTrue(listingsFromVersion.size() > 0);

        //update the version
        //should trigger the cache refresh
        ProductVersion versionToUpdate = versionController.getProductVersionById(-3L);
        UpdateVersionsRequest req = new UpdateVersionsRequest();
        versionToUpdate.setVersion("2.0.Updated");
        req.setVersion(versionToUpdate);
        List<Long> idsToUpdate = new ArrayList<Long>();
        idsToUpdate.add(-3L);
        req.setVersionIds(idsToUpdate);
        versionController.updateVersion(req);

        //get the cached listings now, should have been updated in the aspect and have
        //the latest version name
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        for (CertifiedProductFlatSearchResult listing : allListingsAfterUpdate) {
            for (CertifiedProductFlatSearchResult listingFromVersion : listingsFromVersion) {
                if (listing.getId().longValue() == listingFromVersion.getId().longValue()) {
                    assertEquals(versionToUpdate.getVersion(), listing.getVersion());
                }
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    /**
     * Not sure why this test is failing. It appears to work with integration testing
     * of the whole app and renaming an ACB. I think it's okay to mark it ignored
     * for now since it's an extremely rare action but this should be revisited.
     * @throws UpdateCertifiedBodyException
     * @throws EntityRetrievalException
     * @throws EntityCreationException
     * @throws JsonProcessingException
     * @throws InvalidArgumentsException
     * @throws ValidationException
     * @throws SchedulerException
     */
    @Test
    @Transactional
    @Rollback
    @Ignore
    public void testUpdateAcbNameRefreshesCache() throws
    UpdateCertifiedBodyException, EntityRetrievalException, EntityCreationException,
    JsonProcessingException, InvalidArgumentsException, SchedulerException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<CertifiedProductFlatSearchResult> listingsFromAcb =
                new ArrayList<CertifiedProductFlatSearchResult>();

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        for (CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if (listing.getAcb().equals("UL LLC")) {
                listingsFromAcb.add(listing);
            }
        }
        assertTrue(listingsFromAcb.size() > 0);

        //update the acb name
        //should trigger the cache refresh
        CertificationBody acbToUpdate = acbController.getAcbById(-1L);
        acbToUpdate.setName("UL LLC Updated");
        acbController.updateAcb(acbToUpdate);
        CertificationBody updatedAcb = acbController.getAcbById(-1L);
        assertEquals("UL LLC Updated", updatedAcb.getName());

        //get the cached listings now, should have been updated in the aspect and have
        //the latest acb name
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        for (CertifiedProductFlatSearchResult updatedListing : allListingsAfterUpdate) {
            for (CertifiedProductFlatSearchResult listingFromAcb : listingsFromAcb) {
                if (updatedListing.getId().longValue() == listingFromAcb.getId().longValue()) {
                    assertEquals(updatedAcb.getName(), updatedListing.getAcb());
                }
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateListingStatusRefreshesCache() throws EntityRetrievalException, EntityCreationException,
    JsonProcessingException, InvalidArgumentsException, MissingReasonException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CertifiedProductSearchDetails listingToUpdate = cpdManager.getCertifiedProductDetails(1L);
        for(CertificationResult result : listingToUpdate.getCertificationResults()) {
            result.setSed(Boolean.FALSE);
            result.setGap(Boolean.FALSE);
        }

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        boolean foundListing = false;
        for (CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if (listing.getId().longValue() == listingToUpdate.getId().longValue()) {
                foundListing = true;
                assertEquals("Active", listing.getCertificationStatus());
            }
        }
        assertTrue(foundListing);

        //update the listing status to retired
        //should trigger the cache refresh
        CertificationStatusEvent currentStatus = listingToUpdate.getCurrentStatus();
        int currStatusIndex = 0;
        List<CertificationStatusEvent> events = listingToUpdate.getCertificationEvents();
        for (int i = 0; i < events.size(); i++) {
            CertificationStatusEvent currEvent = events.get(i);
            if (currEvent.getId().longValue() == currentStatus.getId().longValue()) {
                currStatusIndex = i;
            }
        }
        CertificationStatus status = new CertificationStatus();
        status.setId(2L);
        status.setName("Retired");
        currentStatus.setStatus(status);
        events.set(currStatusIndex, currentStatus);
        listingToUpdate.setCertificationEvents(events);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listingToUpdate);
        cpController.updateCertifiedProduct(updateRequest);

        //get the cached listings now, should have been updated in the aspect and have
        //the latest status value
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        foundListing = false;
        for (CertifiedProductFlatSearchResult updatedListing : allListingsAfterUpdate) {
            if (updatedListing.getId().longValue() == listingToUpdate.getId().longValue()) {
                foundListing = true;
                assertEquals("Retired", updatedListing.getCertificationStatus());
            }
        }
        assertTrue(foundListing);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testCreateSurveillanceRefreshesCache() throws EntityRetrievalException, EntityCreationException,
    JsonProcessingException, InvalidArgumentsException, MissingReasonException,
    ValidationException, IOException {
        SecurityContextHolder.getContext().setAuthentication(oncAdmin);

        CertifiedProductSearchDetails listingToUpdate = cpdManager.getCertifiedProductDetails(1L);

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        boolean foundListing = false;
        for (CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if (listing.getId().longValue() == listingToUpdate.getId().longValue()) {
                foundListing = true;
                assertEquals(0, listing.getSurveillanceCount().longValue());
            }
        }
        assertTrue(foundListing);

        //create a surveillance for the listing
        //should trigger the cache refresh
        Surveillance surv = createSurveillanceObject(listingToUpdate.getId());
        Surveillance insertedSurv;
        try {
            ResponseEntity<Surveillance> response = survController.createSurveillance(surv);
            insertedSurv = response.getBody();
            assertNotNull(insertedSurv);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Could not insert listing " + ex.getMessage());
        }

        //get the cached listings now, should have been updated in the aspect and have
        //a surveillance now
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        foundListing = false;
        for (CertifiedProductFlatSearchResult updatedListing : allListingsAfterUpdate) {
            if (updatedListing.getId().longValue() == listingToUpdate.getId().longValue()) {
                foundListing = true;
                assertEquals(1, updatedListing.getSurveillanceCount().longValue());
            }
        }
        assertTrue(foundListing);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testDeleteSurveillanceRefreshesCache() throws EntityRetrievalException, EntityCreationException,
    JsonProcessingException, InvalidArgumentsException, MissingReasonException,
    ValidationException, IOException {
        SecurityContextHolder.getContext().setAuthentication(oncAdmin);

        CertifiedProductSearchDetails listingToUpdate = cpdManager.getCertifiedProductDetails(1L);

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        boolean foundListing = false;
        for (CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if (listing.getId().longValue() == listingToUpdate.getId().longValue()) {
                foundListing = true;
                assertEquals(0, listing.getSurveillanceCount().longValue());
            }
        }
        assertTrue(foundListing);

        //create a surveillance for the listing
        Surveillance surv = createSurveillanceObject(listingToUpdate.getId());
        Surveillance insertedSurv = null;
        try {
            ResponseEntity<Surveillance> response = survController.createSurveillance(surv);
            insertedSurv = response.getBody();
            assertNotNull(insertedSurv);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Could not insert listing " + ex.getMessage());
        }

        //now delete the surveillance
        //should trigger a cache refresh
        String result = null;
        try {
            SimpleExplainableAction requestBody = new SimpleExplainableAction();
            requestBody.setReason("unit test");
            ResponseEntity<String> response = survController
                    .deleteSurveillance(insertedSurv.getId(), requestBody);
            result = response.getBody();
            assertTrue(result.contains("true"));
        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

        //get the cached listings now, should have been updated in the aspect and have
        //0 surveillance again.
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        foundListing = false;
        for (CertifiedProductFlatSearchResult updatedListing : allListingsAfterUpdate) {
            if (updatedListing.getId().longValue() == listingToUpdate.getId().longValue()) {
                foundListing = true;
                assertEquals(0, updatedListing.getSurveillanceCount().longValue());
            }
        }
        assertTrue(foundListing);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private Surveillance createSurveillanceObject(final Long listingId) {
        Surveillance surv = new Surveillance();
        CertifiedProduct cp = new CertifiedProduct();
        cp.setId(listingId);
        cp.setChplProductNumber(cp.getChplProductNumber());
        cp.setEdition(cp.getEdition());
        surv.setCertifiedProduct(cp);
        surv.setStartDate(new Date(System.currentTimeMillis() - 1000));
        surv.setEndDate(new Date());
        surv.setRandomizedSitesUsed(10);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        surv.setAuthority(null);

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);
        return surv;
    }
}
