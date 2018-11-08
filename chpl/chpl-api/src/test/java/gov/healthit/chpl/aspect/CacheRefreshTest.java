package gov.healthit.chpl.aspect;

import java.io.IOException;
import java.util.ArrayList;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
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
import junit.framework.TestCase;

/**
 * Test that certain updates refresh the listing cache as expected
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
    @Autowired private CertifiedProductSearchManager searchManager;

    private static JWTAuthenticatedUser adminUser;
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
        for(CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if(listing.getDeveloper().equals("Test Developer 1")) {
                listingsFromDeveloper.add(listing);
            }
        }
        assertTrue(listingsFromDeveloper.size() > 0);

        //update the developer
        //should trigger the cache refresh
        Developer devToUpdate = developerController.getDeveloperById(-1L);
        UpdateDevelopersRequest req = new UpdateDevelopersRequest();
        devToUpdate.setName("Updated Name");
        req.setDeveloper(devToUpdate);
        List<Long> idsToUpdate = new ArrayList<Long>();
        idsToUpdate.add(-1L);
        req.setDeveloperIds(idsToUpdate);
        developerController.updateDeveloper(req);

        //get the cached listings now, should have been updated in the aspect and have
        //the latest developer stuff
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        for(CertifiedProductFlatSearchResult listing : allListingsAfterUpdate) {
            for(CertifiedProductFlatSearchResult listingFromDeveloper : listingsFromDeveloper) {
                if(listing.getId().longValue() == listingFromDeveloper.getId().longValue()) {
                    assertEquals(devToUpdate.getName(), listing.getDeveloper());
                }
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateDeveloperNameDeprecatedRefreshesCache() throws
    EntityCreationException, EntityRetrievalException,
    ValidationException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<CertifiedProductFlatSearchResult> listingsFromDeveloper =
                new ArrayList<CertifiedProductFlatSearchResult>();

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        for(CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if(listing.getDeveloper().equals("Test Developer 1")) {
                listingsFromDeveloper.add(listing);
            }
        }
        assertTrue(listingsFromDeveloper.size() > 0);

        //update the developer
        //should trigger the cache refresh
        Developer devToUpdate = developerController.getDeveloperById(-1L);
        UpdateDevelopersRequest req = new UpdateDevelopersRequest();
        devToUpdate.setName("Updated Name");
        req.setDeveloper(devToUpdate);
        List<Long> idsToUpdate = new ArrayList<Long>();
        idsToUpdate.add(-1L);
        req.setDeveloperIds(idsToUpdate);
        developerController.updateDeveloperDeprecated(req);

        //get the cached listings now, should have been updated in the aspect and have
        //the latest developer stuff
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        for(CertifiedProductFlatSearchResult listing : allListingsAfterUpdate) {
            for(CertifiedProductFlatSearchResult listingFromDeveloper : listingsFromDeveloper) {
                if(listing.getId().longValue() == listingFromDeveloper.getId().longValue()) {
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
        for(CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if(listing.getProduct().equals("Test Product 1")) {
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
        for(CertifiedProductFlatSearchResult listing : allListingsAfterUpdate) {
            for(CertifiedProductFlatSearchResult listingFromProduct : listingsFromProduct) {
                if(listing.getId().longValue() == listingFromProduct.getId().longValue()) {
                    assertEquals(productToUpdate.getName(), listing.getProduct());
                }
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateProductNameDeprecatedRefreshesCache() throws
    EntityCreationException, EntityRetrievalException,
    ValidationException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<CertifiedProductFlatSearchResult> listingsFromProduct =
                new ArrayList<CertifiedProductFlatSearchResult>();

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        for(CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if(listing.getProduct().equals("Test Product 1")) {
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
        productController.updateProductDeprecated(req);

        //get the cached listings now, should have been updated in the aspect and have
        //the latest product name
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        for(CertifiedProductFlatSearchResult listing : allListingsAfterUpdate) {
            for(CertifiedProductFlatSearchResult listingFromProduct : listingsFromProduct) {
                if(listing.getId().longValue() == listingFromProduct.getId().longValue()) {
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
        for(CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if(listing.getVersion().equals("2.0")) {
                listingsFromVersion.add(listing);
            }
        }
        assertTrue(listingsFromVersion.size() > 0);

        //update the version
        //should trigger the cache refresh
        ProductVersion versionToUpdate = versionController.getProductVersionById(3L);
        UpdateVersionsRequest req = new UpdateVersionsRequest();
        versionToUpdate.setVersion("2.0.Updated");
        req.setVersion(versionToUpdate);
        List<Long> idsToUpdate = new ArrayList<Long>();
        idsToUpdate.add(3L);
        req.setVersionIds(idsToUpdate);
        versionController.updateVersion(req);

        //get the cached listings now, should have been updated in the aspect and have
        //the latest version name
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        for(CertifiedProductFlatSearchResult listing : allListingsAfterUpdate) {
            for(CertifiedProductFlatSearchResult listingFromVersion : listingsFromVersion) {
                if(listing.getId().longValue() == listingFromVersion.getId().longValue()) {
                    assertEquals(versionToUpdate.getVersion(), listing.getVersion());
                }
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateVersionDeprecatedRefreshesCache() throws
    EntityCreationException, EntityRetrievalException,
    ValidationException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<CertifiedProductFlatSearchResult> listingsFromVersion =
                new ArrayList<CertifiedProductFlatSearchResult>();

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        for(CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if(listing.getVersion().equals("2.0")) {
                listingsFromVersion.add(listing);
            }
        }
        assertTrue(listingsFromVersion.size() > 0);

        //update the version
        //should trigger the cache refresh
        ProductVersion versionToUpdate = versionController.getProductVersionById(3L);
        UpdateVersionsRequest req = new UpdateVersionsRequest();
        versionToUpdate.setVersion("2.0.Updated");
        req.setVersion(versionToUpdate);
        List<Long> idsToUpdate = new ArrayList<Long>();
        idsToUpdate.add(3L);
        req.setVersionIds(idsToUpdate);
        versionController.updateVersionDeprecated(req);

        //get the cached listings now, should have been updated in the aspect and have
        //the latest version name
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        for(CertifiedProductFlatSearchResult listing : allListingsAfterUpdate) {
            for(CertifiedProductFlatSearchResult listingFromVersion : listingsFromVersion) {
                if(listing.getId().longValue() == listingFromVersion.getId().longValue()) {
                    assertEquals(versionToUpdate.getVersion(), listing.getVersion());
                }
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateAcbNameRefreshesCache() throws
    UpdateCertifiedBodyException, EntityRetrievalException, EntityCreationException,
    JsonProcessingException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<CertifiedProductFlatSearchResult> listingsFromAcb =
                new ArrayList<CertifiedProductFlatSearchResult>();

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        for(CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if(listing.getAcb().equals("InfoGard")) {
                listingsFromAcb.add(listing);
            }
        }
        assertTrue(listingsFromAcb.size() > 0);

        //update the acb name
        //should trigger the cache refresh
        CertificationBody acbToUpdate = acbController.getAcbById(-1L);
        acbToUpdate.setName("InfoGard Updated");
        acbController.updateAcb(acbToUpdate);

        //get the cached listings now, should have been updated in the aspect and have
        //the latest acb name
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        for(CertifiedProductFlatSearchResult updatedListing : allListingsAfterUpdate) {
            for(CertifiedProductFlatSearchResult listingFromAcb : listingsFromAcb) {
                if(updatedListing.getId().longValue() == listingFromAcb.getId().longValue()) {
                    assertEquals(acbToUpdate.getName(), updatedListing.getAcb());
                }
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateAcbNameDeprecatedRefreshesCache() throws
    UpdateCertifiedBodyException, EntityRetrievalException, EntityCreationException,
    JsonProcessingException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<CertifiedProductFlatSearchResult> listingsFromAcb =
                new ArrayList<CertifiedProductFlatSearchResult>();

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        for(CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if(listing.getAcb().equals("InfoGard")) {
                listingsFromAcb.add(listing);
            }
        }
        assertTrue(listingsFromAcb.size() > 0);

        //update the acb name
        //should trigger the cache refresh
        CertificationBody acbToUpdate = acbController.getAcbById(-1L);
        acbToUpdate.setName("InfoGard Updated");
        acbController.updateAcbDeprecated(acbToUpdate);

        //get the cached listings now, should have been updated in the aspect and have
        //the latest acb name
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        for(CertifiedProductFlatSearchResult updatedListing : allListingsAfterUpdate) {
            for(CertifiedProductFlatSearchResult listingFromAcb : listingsFromAcb) {
                if(updatedListing.getId().longValue() == listingFromAcb.getId().longValue()) {
                    assertEquals(acbToUpdate.getName(), updatedListing.getAcb());
                }
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateListingStatusRefreshesCache() throws EntityRetrievalException, EntityCreationException,
    JsonProcessingException, InvalidArgumentsException, MissingReasonException,
    ValidationException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CertifiedProductSearchDetails listingToUpdate = cpdManager.getCertifiedProductDetails(1L);

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        boolean foundListing = false;
        for(CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if(listing.getId().longValue() == listingToUpdate.getId().longValue()) {
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
        for(int i = 0; i < events.size(); i++) {
            CertificationStatusEvent currEvent = events.get(i);
            if(currEvent.getId().longValue() == currentStatus.getId().longValue()) {
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
        for(CertifiedProductFlatSearchResult updatedListing : allListingsAfterUpdate) {
            if(updatedListing.getId().longValue() == listingToUpdate.getId().longValue()) {
                foundListing = true;
                assertEquals("Retired", updatedListing.getCertificationStatus());
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateListingStatusDeprecatedRefreshesCache() throws EntityRetrievalException, EntityCreationException,
    JsonProcessingException, InvalidArgumentsException, MissingReasonException,
    ValidationException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CertifiedProductSearchDetails listingToUpdate = cpdManager.getCertifiedProductDetails(1L);

        //get the cache before this update, should pull the listings and cache them
        List<CertifiedProductFlatSearchResult> allListingsBeforeUpdate = searchManager.search();
        boolean foundListing = false;
        for(CertifiedProductFlatSearchResult listing : allListingsBeforeUpdate) {
            if(listing.getId().longValue() == listingToUpdate.getId().longValue()) {
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
        for(int i = 0; i < events.size(); i++) {
            CertificationStatusEvent currEvent = events.get(i);
            if(currEvent.getId().longValue() == currentStatus.getId().longValue()) {
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
        cpController.updateCertifiedProductDeprecated(updateRequest);

        //get the cached listings now, should have been updated in the aspect and have
        //the latest status value
        List<CertifiedProductFlatSearchResult> allListingsAfterUpdate = searchManager.search();
        foundListing = false;
        for(CertifiedProductFlatSearchResult updatedListing : allListingsAfterUpdate) {
            if(updatedListing.getId().longValue() == listingToUpdate.getId().longValue()) {
                foundListing = true;
                assertEquals("Retired", updatedListing.getCertificationStatus());
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    //TODO: Add/update/remove surveillance and nonconformities
    //TODO: Change MUU number via upload (change to a single listing will get caught 
    //with the update listing aspect
}
