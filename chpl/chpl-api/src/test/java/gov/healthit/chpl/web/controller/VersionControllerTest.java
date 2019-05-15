package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
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

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.UnitTestUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.SplitVersionsRequest;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.web.controller.results.SplitVersionResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class VersionControllerTest {
    private static JWTAuthenticatedUser adminUser;

    @Autowired
    private Environment env;

    @Autowired
    @InjectMocks
    private ProductVersionController versionController;

    @Autowired
    private CertifiedProductDAO cpDao;

    @Spy
    private FF4j ff4j;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(UnitTestUtil.ADMIN_ID);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(true).when(ff4j).check(FeatureList.BETTER_SPLIT);
    }

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void testGetVersionByBadId() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        versionController.getProductVersionById(-100L);
    }

    @Transactional
    @Rollback(true)
    @Test
    public void testSplitVersion() throws EntityRetrievalException, JsonProcessingException,
    InvalidArgumentsException, EntityCreationException {
        Long versionIdToSplit = -3L;
        Long newVersionListingId = 2L;
        Long oldVersionListingId = 3L;
        SplitVersionsRequest request = new SplitVersionsRequest();
        request.setNewVersionCode("30");
        request.setNewVersionVersion("3.1-1");

        ProductVersion versionToSplit = versionController.getProductVersionById(versionIdToSplit);
        request.setOldVersion(versionToSplit);

        CertifiedProduct oldVersionListing = new CertifiedProduct();
        oldVersionListing.setId(oldVersionListingId);
        List<CertifiedProduct> oldVersionListings = new ArrayList<CertifiedProduct>();
        oldVersionListings.add(oldVersionListing);
        request.setOldListings(oldVersionListings);

        CertifiedProduct newVersionListing = new CertifiedProduct();
        newVersionListing.setId(newVersionListingId);
        List<CertifiedProduct> newVersionListings = new ArrayList<CertifiedProduct>();
        newVersionListings.add(newVersionListing);
        request.setNewListings(newVersionListings);

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        ResponseEntity<SplitVersionResponse> responseEntity = versionController.splitVersion(versionIdToSplit, request);
        SecurityContextHolder.getContext().setAuthentication(null);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        //make sure the old listing still has old verison id and new listing has new version id
        SplitVersionResponse response = responseEntity.getBody();
        assertNotNull(response.getNewVersion().getVersionId());
        CertifiedProductDTO movedListing = cpDao.getById(newVersionListingId);
        assertEquals(response.getNewVersion().getVersionId(), movedListing.getProductVersionId());

        CertifiedProductDTO remainingListing = cpDao.getById(oldVersionListingId);
        assertEquals(versionIdToSplit, remainingListing.getProductVersionId());
    }

    @Transactional
    @Rollback(true)
    @Test(expected = EntityRetrievalException.class)
    public void testSplitVersion_OldVersionDoesNotExist() throws EntityRetrievalException, JsonProcessingException,
    InvalidArgumentsException, EntityCreationException {
        Long versionIdToSplit = -999L;
        Long newVersionListingId = 2L;
        Long oldVersionListingId = 3L;
        SplitVersionsRequest request = new SplitVersionsRequest();
        request.setNewVersionCode("30");
        request.setNewVersionVersion("3.1-1");

        ProductVersion versionToSplit = new ProductVersion();
        versionToSplit.setVersionId(versionIdToSplit);
        request.setOldVersion(versionToSplit);

        CertifiedProduct oldVersionListing = new CertifiedProduct();
        oldVersionListing.setId(oldVersionListingId);
        List<CertifiedProduct> oldVersionListings = new ArrayList<CertifiedProduct>();
        oldVersionListings.add(oldVersionListing);
        request.setOldListings(oldVersionListings);

        CertifiedProduct newVersionListing = new CertifiedProduct();
        newVersionListing.setId(newVersionListingId);
        List<CertifiedProduct> newVersionListings = new ArrayList<CertifiedProduct>();
        newVersionListings.add(newVersionListing);
        request.setNewListings(newVersionListings);

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        ResponseEntity<SplitVersionResponse> responseEntity = versionController.splitVersion(versionIdToSplit, request);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Transactional
    @Rollback(true)
    @Test(expected = EntityCreationException.class)
    public void testSplitVersion_ListingNotPartOfOldVersion() throws EntityRetrievalException, JsonProcessingException,
    InvalidArgumentsException, EntityCreationException {
        Long versionIdToSplit = -3L;
        Long newVersionListingId = 6L;
        Long oldVersionListingId = 3L; //not part of versionIdToSplit
        SplitVersionsRequest request = new SplitVersionsRequest();
        request.setNewVersionCode("30");
        request.setNewVersionVersion("3.1-1");

        ProductVersion versionToSplit = versionController.getProductVersionById(versionIdToSplit);
        request.setOldVersion(versionToSplit);

        CertifiedProduct oldVersionListing = new CertifiedProduct();
        oldVersionListing.setId(oldVersionListingId);
        List<CertifiedProduct> oldVersionListings = new ArrayList<CertifiedProduct>();
        oldVersionListings.add(oldVersionListing);
        request.setOldListings(oldVersionListings);

        CertifiedProduct newVersionListing = new CertifiedProduct();
        newVersionListing.setId(newVersionListingId);
        List<CertifiedProduct> newVersionListings = new ArrayList<CertifiedProduct>();
        newVersionListings.add(newVersionListing);
        request.setNewListings(newVersionListings);

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        ResponseEntity<SplitVersionResponse> responseEntity = versionController.splitVersion(versionIdToSplit, request);
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
