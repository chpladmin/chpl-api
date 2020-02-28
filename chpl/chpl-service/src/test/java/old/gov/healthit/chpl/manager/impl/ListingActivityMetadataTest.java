package old.gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.TransparencyAttestation;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.ActivityMetadataManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class ListingActivityMetadataTest extends TestCase {
    @Autowired
    private ActivityMetadataManager metadataManager;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private CertifiedProductDetailsManager cpdManager;

    @Autowired
    private CertifiedProductManager cpManager;

    @Autowired
    private SurveillanceManager survManager;

    @Autowired
    private CertifiedProductDAO cpDao;

    @Autowired
    private CertificationStatusDAO certStatusDao;

    @Autowired
    private SurveillanceDAO survDao;

    @Autowired
    private FF4j ff4j;

    private static JWTAuthenticatedUser adminUser, acbUser;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setId(3L);
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("testUser3");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));
    }

    @Before
    public void setup() {
        Mockito.doReturn(true).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE);
    }

    @Test
    @Transactional
    public void testGetActivityMetadataForAllListingsLoggedIn() throws JsonParseException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);

        List<ActivityMetadata> metadatas = metadataManager
                .getActivityMetadataByConcept(ActivityConcept.CERTIFIED_PRODUCT, start.getTime(), end.getTime());
        assertEquals(3, metadatas.size());

        for (ActivityMetadata metadata : metadatas) {
            assertNotNull(metadata.getId());
            assertNotNull(metadata.getDate());
            assertNotNull(metadata.getObjectId());
            assertTrue(metadata.getCategories().contains(ActivityCategory.LISTING));
            assertEquals(ActivityConcept.CERTIFIED_PRODUCT, metadata.getConcept());
        }
    }

    @Test
    @Transactional
    public void testGetActivityMetadataForListingLoggedIn() throws JsonParseException, IOException {
        Long objectId = 1L;
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);

        List<ActivityMetadata> metadatas = metadataManager.getActivityMetadataByObject(objectId,
                ActivityConcept.CERTIFIED_PRODUCT, start.getTime(), end.getTime());
        assertEquals(3, metadatas.size());

        for (ActivityMetadata metadata : metadatas) {
            assertNotNull(metadata.getId());
            assertNotNull(metadata.getDate());
            assertEquals(ActivityConcept.CERTIFIED_PRODUCT, metadata.getConcept());
            assertTrue(metadata.getCategories().contains(ActivityCategory.LISTING));
            assertEquals(objectId, metadata.getObjectId());
        }
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testModifyCertificationStatusAndActivityHasStatusCategory() throws EntityRetrievalException,
            ValidationException, IOException, InvalidArgumentsException, EntityCreationException, AccessDeniedException,
            MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.WithdrawnByAcb.getName());
        assertNotNull(stat);
        String reason = "Reason Text";
        Long listingId = 1L;

        CertifiedProductSearchDetails beforeListing = cpdManager.getCertifiedProductDetails(listingId);

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        CertificationStatusEvent statusEvent = new CertificationStatusEvent();
        statusEvent.setEventDate(System.currentTimeMillis());
        statusEvent.setStatus(new CertificationStatus(stat));
        statusEvent.setReason(reason);
        toUpdateListing.getCertificationEvents().add(statusEvent);

        toUpdateListing.setTransparencyAttestation(new TransparencyAttestation("Affirmative"));

        ListingUpdateRequest toUpdate = new ListingUpdateRequest();
        toUpdate.setListing(toUpdateListing);
        toUpdate.setReason("test reason");

        cpManager.update(toUpdate);

        CertifiedProductSearchDetails afterListing = cpdManager.getCertifiedProductDetails(listingId);
        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, listingId, "Updated certification status",
                beforeListing, afterListing);

        Calendar start = getBeginningOfToday();
        Calendar end = getEndOfToday();
        List<ActivityMetadata> metadatas = metadataManager
                .getActivityMetadataByConcept(ActivityConcept.CERTIFIED_PRODUCT, start.getTime(), end.getTime());
        assertEquals(2, metadatas.size());
        ActivityMetadata metadata = metadatas.get(0);
        assertEquals(listingId.longValue(), metadata.getObjectId().longValue());
        assertTrue(metadata.getCategories().contains(ActivityCategory.LISTING));
        assertTrue(metadata.getCategories().contains(ActivityCategory.LISTING_STATUS_CHANGE));
        assertFalse(metadata.getCategories().contains(ActivityCategory.LISTING_UPLOAD));
        assertFalse(metadata.getCategories().contains(ActivityCategory.SURVEILLANCE));
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testAddSurveillanceAndActivityHasSurveillanceCategory()
            throws EntityRetrievalException, EntityCreationException, IOException {
        Long listingId = 1L;

        SecurityContextHolder.getContext().setAuthentication(acbUser);
        addSurveillance(listingId);
        Calendar start = getBeginningOfToday();
        Calendar end = getEndOfToday();
        List<ActivityMetadata> metadatas = metadataManager
                .getActivityMetadataByConcept(ActivityConcept.CERTIFIED_PRODUCT, start.getTime(), end.getTime());
        assertEquals(1, metadatas.size());
        ActivityMetadata metadata = metadatas.get(0);
        assertEquals(listingId.longValue(), metadata.getObjectId().longValue());
        assertTrue(metadata.getCategories().contains(ActivityCategory.LISTING));
        assertTrue(metadata.getCategories().contains(ActivityCategory.SURVEILLANCE));
        assertFalse(metadata.getCategories().contains(ActivityCategory.LISTING_STATUS_CHANGE));
        assertFalse(metadata.getCategories().contains(ActivityCategory.LISTING_UPLOAD));
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testRemoveSurveillanceAndActivityHasSurveillanceCategory()
            throws EntityRetrievalException, EntityCreationException, IOException {
        Long listingId = 10L;

        SecurityContextHolder.getContext().setAuthentication(acbUser);
        CertifiedProductSearchDetails beforeListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(beforeListing.getSurveillance());
        assertEquals(1, beforeListing.getSurveillance().size());
        try {
            survManager.deleteSurveillance(beforeListing.getSurveillance().get(0));
        } catch (Exception e) {
            fail("Could not delete surveillance: " + e.getMessage());
            e.printStackTrace();
        }
        CertifiedProductSearchDetails afterListing = cpdManager.getCertifiedProductDetails(listingId);
        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, listingId, "Deleted surveillance", beforeListing,
                afterListing);

        Calendar start = getBeginningOfToday();
        Calendar end = getEndOfToday();
        List<ActivityMetadata> metadatas = metadataManager
                .getActivityMetadataByConcept(ActivityConcept.CERTIFIED_PRODUCT, start.getTime(), end.getTime());
        assertEquals(1, metadatas.size());
        ActivityMetadata metadata = metadatas.get(0);
        assertEquals(listingId.longValue(), metadata.getObjectId().longValue());
        assertTrue(metadata.getCategories().contains(ActivityCategory.LISTING));
        assertTrue(metadata.getCategories().contains(ActivityCategory.SURVEILLANCE));
        assertFalse(metadata.getCategories().contains(ActivityCategory.LISTING_STATUS_CHANGE));
        assertFalse(metadata.getCategories().contains(ActivityCategory.LISTING_UPLOAD));
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testModifySurveillanceEndDateAndActivityHasSurveillanceCategory()
            throws EntityRetrievalException, EntityCreationException, IOException {
        Long listingId = 1L;
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        addSurveillance(listingId);
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(listing.getSurveillance());
        assertEquals(1, listing.getSurveillance().size());
        try {
            Surveillance surv = listing.getSurveillance().get(0);
            Calendar updatedEndDate = Calendar.getInstance();
            updatedEndDate.add(Calendar.DATE, 5);
            surv.setEndDate(updatedEndDate.getTime());
            survManager.updateSurveillance(surv);
        } catch (Exception e) {
            fail("Could not update surveillance: " + e.getMessage());
            e.printStackTrace();
        }

        Calendar start = getBeginningOfToday();
        Calendar end = getEndOfToday();
        List<ActivityMetadata> metadatas = metadataManager
                .getActivityMetadataByConcept(ActivityConcept.CERTIFIED_PRODUCT, start.getTime(), end.getTime());
        assertEquals(2, metadatas.size());
        for (ActivityMetadata metadata : metadatas) {
            assertEquals(listingId.longValue(), metadata.getObjectId().longValue());
            assertTrue(metadata.getCategories().contains(ActivityCategory.LISTING));
            assertTrue(metadata.getCategories().contains(ActivityCategory.SURVEILLANCE));
            assertFalse(metadata.getCategories().contains(ActivityCategory.LISTING_STATUS_CHANGE));
            assertFalse(metadata.getCategories().contains(ActivityCategory.LISTING_UPLOAD));
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private Long addSurveillance(Long listingId) throws EntityRetrievalException {
        Surveillance surv = new Surveillance();
        CertifiedProductDTO cpDto = cpDao.getById(listingId);
        CertifiedProduct cp = new CertifiedProduct();
        cp.setId(cpDto.getId());
        cp.setChplProductNumber(cp.getChplProductNumber());
        cp.setEdition(cp.getEdition());
        surv.setCertifiedProduct(cp);
        surv.setStartDate(new Date());
        surv.setEndDate(new Date(surv.getStartDate().getTime() + 1000));
        surv.setRandomizedSitesUsed(10);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);
        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);
        surv.getRequirements().add(req);

        Long addedId = null;
        try {
            addedId = survManager.createSurveillance(surv);
        } catch (Exception e) {
            fail("Could not insert surveillance: " + e.getMessage());
            e.printStackTrace();
        }
        return addedId;
    }

    private Calendar getBeginningOfToday() {
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(Calendar.HOUR, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        return start;
    }

    private Calendar getEndOfToday() {
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(Calendar.HOUR, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        return end;
    }
}
