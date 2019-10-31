package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestProcedure;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;
import junit.framework.TestCase;

@ActiveProfiles({
        "ListingValidatorMock", "Ff4jMock"
})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class,
        gov.healthit.chpl.ListingValidatorFactoryConfiguration.class,
        gov.healthit.chpl.Ff4jTestConfiguration.class,
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductManagerTest extends TestCase {

    @Autowired
    private DeveloperManager devManager;

    @Autowired
    private CertificationStatusDAO certStatusDao;

    @Autowired
    private CertifiedProductManager cpManager;

    @Autowired
    private CertifiedProductDetailsManager cpdManager;

    @Autowired
    private ListingValidatorFactory validatorFactory;

    @Autowired
    private FF4j ff4j;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser;
    private static JWTAuthenticatedUser testUser3;

    private static final long ADMIN_ID = -2L;
    private static final long USER_ID = 3L;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(ADMIN_ID);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        testUser3 = new JWTAuthenticatedUser();
        testUser3.setFullName("Test");
        testUser3.setId(USER_ID);
        testUser3.setFriendlyName("User3");
        testUser3.setSubjectName("testUser3");
        testUser3.getPermissions().add(new GrantedPermission("ROLE_ACB"));
    }

    @Before
    public void setup() {
        Mockito.when(validatorFactory.getValidator(ArgumentMatchers.any(CertifiedProductSearchDetails.class)))
                .thenReturn(null);

        Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)).thenReturn(true);

    }

    @SuppressWarnings("unchecked")
    @Test
    @Transactional
    public void testIcsFamilyTree() throws EntityRetrievalException {
        final long listingId = 5L;
        final int expectedFamilySize = 4;

        List<IcsFamilyTreeNode> tree = cpManager.getIcsFamilyTree(listingId);
        assertNotNull(tree);
        assertEquals(expectedFamilySize, tree.size());
    }

    @Test
    @Transactional
    public void testGet2015CertifiedProduct() throws EntityRetrievalException {
        final long listingId = 5L;
        final int expectedParticipants = 10;

        CertifiedProductSearchDetails details = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(details);
        List<CertificationResult> certs = details.getCertificationResults();
        assertNotNull(certs);
        assertEquals(2, certs.size());

        assertNotNull(details.getSed());
        List<TestTask> testTasks = details.getSed().getTestTasks();
        assertEquals(1, testTasks.size());
        TestTask task = testTasks.get(0);

        boolean foundExpectedCert = false;
        for (CertificationCriterion criteria : task.getCriteria()) {
            if (criteria.getNumber().equals("170.315 (a)(1)")) {
                foundExpectedCert = true;
            }
        }
        assertTrue(foundExpectedCert);
        assertNotNull(task.getTestParticipants());
        assertEquals(expectedParticipants, task.getTestParticipants().size());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testAdminUserChangeStatusToSuspendedByOnc() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.SuspendedByOnc.getName());
        assertNotNull(stat);
        Long acbId = 1L;
        Long listingId = 1L;
        updateListingStatus(acbId, listingId, stat, null);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        assertNotNull(updatedListing.getCertificationEvents());
        assertTrue(updatedListing.getCertificationEvents().size() > 0);
        assertNotNull(updatedListing.getCurrentStatus());
        assertNotNull(updatedListing.getCurrentStatus().getStatus());
        assertEquals(CertificationStatusType.SuspendedByOnc.getName(),
                updatedListing.getCurrentStatus().getStatus().getName());

        DeveloperDTO dev = devManager.getById(-1L);
        assertNotNull(dev);
        DeveloperStatusEventDTO status = dev.getStatus();
        assertNotNull(status);
        assertNotNull(status.getId());
        assertNotNull(status.getStatus());
        assertNotNull(status.getStatus().getStatusName());
        assertEquals(DeveloperStatusType.SuspendedByOnc.toString(), status.getStatus().getStatusName());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testAdminUserChangeStatusToWithdrawnByAcbWithReason() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.WithdrawnByAcb.getName());
        assertNotNull(stat);
        String reason = "Reason Text";
        Long acbId = 1L;
        Long listingId = 1L;
        updateListingStatus(acbId, listingId, stat, reason);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        assertNotNull(updatedListing.getCertificationEvents());
        assertTrue(updatedListing.getCertificationEvents().size() > 0);
        assertNotNull(updatedListing.getCurrentStatus());
        assertNotNull(updatedListing.getCurrentStatus().getStatus());
        assertNotNull(updatedListing.getCurrentStatus().getReason());
        assertEquals(reason, updatedListing.getCurrentStatus().getReason());
        assertEquals(CertificationStatusType.WithdrawnByAcb.getName(),
                updatedListing.getCurrentStatus().getStatus().getName());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testAdminUserSeesStatusChangeReason() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.WithdrawnByAcb.getName());
        assertNotNull(stat);
        String reason = "Reason Text";
        Long acbId = 1L;
        Long listingId = 1L;
        updateListingStatus(acbId, listingId, stat, reason);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getCurrentStatus().getReason());
        assertEquals(reason, updatedListing.getCurrentStatus().getReason());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testAcbUserSeesStatusChangeReason() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.WithdrawnByAcb.getName());
        assertNotNull(stat);
        String reason = "Reason Text";
        Long acbId = 1L;
        Long listingId = 1L;
        updateListingStatus(acbId, listingId, stat, reason);

        SecurityContextHolder.getContext().setAuthentication(testUser3);
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getCurrentStatus().getReason());
        assertEquals(reason, updatedListing.getCurrentStatus().getReason());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testNonLoggedInUserSeesStatusChangeReason() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.WithdrawnByAcb.getName());
        assertNotNull(stat);
        String reason = "Reason Text";
        Long acbId = 1L;
        Long listingId = 1L;
        updateListingStatus(acbId, listingId, stat, reason);

        SecurityContextHolder.getContext().setAuthentication(null);
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNull(updatedListing.getCurrentStatus().getReason());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testNonAdminUserNotAllowedToChangeStatusToSuspendedByOnc() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(testUser3);
        CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.SuspendedByOnc.getName());
        assertNotNull(stat);
        Long acbId = 1L;
        Long listingId = 1L;

        boolean success = true;
        try {
            updateListingStatus(acbId, listingId, stat, null);
        } catch (AccessDeniedException adEx) {
            success = false;
        }
        assertFalse(success);

        DeveloperDTO dev = devManager.getById(-1L);
        assertNotNull(dev);
        DeveloperStatusEventDTO status = dev.getStatus();
        assertNotNull(status);
        assertNotNull(status.getId());
        assertNotNull(status.getStatus());
        assertNotNull(status.getStatus().getStatusName());
        assertEquals(DeveloperStatusType.Active.toString(), status.getStatus().getStatusName());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testNonAdminUserNotAllowedToChangeStatusToWithdrawnByDeveloperUnderReview()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(testUser3);
        CertificationStatusDTO stat = certStatusDao
                .getByStatusName(CertificationStatusType.WithdrawnByDeveloperUnderReview.getName());
        assertNotNull(stat);
        Long acbId = 1L;
        Long listingId = 1L;

        boolean success = true;
        try {
            updateListingStatus(acbId, listingId, stat, null);
        } catch (AccessDeniedException adEx) {
            success = false;
        }
        assertFalse(success);

        DeveloperDTO dev = devManager.getById(-1L);
        assertNotNull(dev);
        DeveloperStatusEventDTO status = dev.getStatus();
        assertNotNull(status);
        assertNotNull(status.getId());
        assertNotNull(status.getStatus());
        assertNotNull(status.getStatus().getStatusName());
        assertEquals(DeveloperStatusType.Active.toString(), status.getStatus().getStatusName());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testAdminUserChangeStatusToWithdrawnByDeveloperUnderReviewWithDeveloperBan()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertificationStatusDTO stat = certStatusDao
                .getByStatusName(CertificationStatusType.WithdrawnByDeveloperUnderReview.getName());
        assertNotNull(stat);
        Long acbId = 1L;
        Long listingId = 1L;

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        CertificationStatusEvent statusEvent = new CertificationStatusEvent();
        statusEvent.setEventDate(System.currentTimeMillis());
        statusEvent.setStatus(new CertificationStatus(stat));
        updatedListing.getCertificationEvents().add(statusEvent);

        ListingUpdateRequest toUpdate = new ListingUpdateRequest();
        toUpdate.setListing(updatedListing);
        toUpdate.setReason("test reason");
        cpManager.update(toUpdate);

        DeveloperDTO dev = devManager.getById(-1L);
        assertNotNull(dev);
        DeveloperStatusEventDTO status = dev.getStatus();
        assertNotNull(status);
        assertNotNull(status.getId());
        assertNotNull(status.getStatus());
        assertNotNull(status.getStatus().getStatusName());
        assertEquals(DeveloperStatusType.Active.toString(), status.getStatus().getStatusName());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testAdminUserChangeStatusToWithdrawnByDeveloperUnderReviewWithoutDeveloperBan()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException,
            MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertificationStatusDTO stat = certStatusDao
                .getByStatusName(CertificationStatusType.WithdrawnByDeveloperUnderReview.getName());
        assertNotNull(stat);

        DeveloperDTO beforeDev = devManager.getById(-1L);
        assertNotNull(beforeDev);
        DeveloperStatusEventDTO beforeStatus = beforeDev.getStatus();
        assertNotNull(beforeStatus);
        assertNotNull(beforeStatus.getId());

        Long acbId = 1L;
        Long listingId = 1L;
        updateListingStatus(acbId, listingId, stat, "test reason");

        DeveloperDTO afterDev = devManager.getById(-1L);
        assertNotNull(afterDev);
        DeveloperStatusEventDTO afterStatus = afterDev.getStatus();
        assertNotNull(afterStatus);
        assertNotNull(afterStatus.getId());
        assertEquals(beforeStatus.getId().longValue(), afterStatus.getId().longValue());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testAdminUserChangeStatusToTerminatedByOnc() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.TerminatedByOnc.getName());
        assertNotNull(stat);
        final Long acbId = 1L;
        final Long listingId = 1L;
        updateListingStatus(acbId, listingId, stat, null);

        DeveloperDTO dev = devManager.getById(-1L);
        assertNotNull(dev);
        DeveloperStatusEventDTO status = dev.getStatus();
        assertNotNull(status);
        assertNotNull(status.getId());
        assertNotNull(status.getStatus());
        assertNotNull(status.getStatus().getStatusName());
        assertEquals(DeveloperStatusType.UnderCertificationBanByOnc.toString(), status.getStatus().getStatusName());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testNonAdminUserNotAllowedToChangeStatusToTerminatedByOnc() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(testUser3);
        CertificationStatusDTO stat = certStatusDao.getByStatusName(CertificationStatusType.TerminatedByOnc.getName());
        assertNotNull(stat);

        final Long acbId = 1L;
        final Long listingId = 1L;

        boolean success = true;
        try {
            updateListingStatus(acbId, listingId, stat, null);
        } catch (AccessDeniedException adEx) {
            success = false;
        }
        assertFalse(success);

        DeveloperDTO dev = devManager.getById(-1L);
        assertNotNull(dev);
        DeveloperStatusEventDTO status = dev.getStatus();
        assertNotNull(status);
        assertNotNull(status.getId());
        assertNotNull(status.getStatus());
        assertNotNull(status.getStatus().getStatusName());
        assertEquals(DeveloperStatusType.Active.toString(), status.getStatus().getStatusName());
    }

    /*********************
     * QMS Standard crud tests.
     * 
     * @throws MissingReasonException
     * @throws AccessDeniedException
     *************************/
    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddExistingQmsStandard() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long qmsToAdd = 1L;
        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
        int origQmsLength = existingListing.getQmsStandards().size();
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        CertifiedProductQmsStandard std = new CertifiedProductQmsStandard();
        std.setQmsStandardId(qmsToAdd);
        std.setQmsStandardName("21 CFR Part 820");
        std.setQmsModification("None");
        std.setApplicableCriteria("All");
        updatedListing.getQmsStandards().add(std);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getQmsStandards());
        assertEquals(origQmsLength + 1, updatedListing.getQmsStandards().size());
        boolean foundAddedQms = false;
        for (CertifiedProductQmsStandard qms : updatedListing.getQmsStandards()) {
            if (qms.getQmsStandardId().longValue() == qmsToAdd.longValue()) {
                foundAddedQms = true;
            }
        }
        assertTrue(foundAddedQms);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddNonExistingQmsStandard() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
        int origQmsLength = existingListing.getQmsStandards().size();
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        CertifiedProductQmsStandard std = new CertifiedProductQmsStandard();
        String stdName = "NEW QMS STANDARD";
        String stdModification = "None";
        String stdCriteria = "ALL";
        std.setQmsStandardName(stdName);
        std.setQmsModification(stdModification);
        std.setApplicableCriteria(stdCriteria);
        updatedListing.getQmsStandards().add(std);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getQmsStandards());
        assertEquals(origQmsLength + 1, updatedListing.getQmsStandards().size());
        CertifiedProductQmsStandard added = updatedListing.getQmsStandards().get(0);
        assertNotNull(added.getQmsStandardId());
        assertNotNull(added.getId());
        assertEquals(stdName, added.getQmsStandardName());
        assertEquals(stdModification, added.getQmsModification());
        assertEquals(stdCriteria, added.getApplicableCriteria());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testDeleteQmsStandard() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long qmsToAdd = 1L;

        // add a qms
        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
        int origQmsLength = existingListing.getQmsStandards().size();
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        CertifiedProductQmsStandard std = new CertifiedProductQmsStandard();
        std.setQmsStandardId(qmsToAdd);
        std.setQmsModification("None");
        std.setApplicableCriteria("All");
        updatedListing.getQmsStandards().add(std);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        // remove the qms
        CertifiedProductSearchDetails listingWithQms = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(listingWithQms.getQmsStandards());
        assertEquals(origQmsLength + 1, listingWithQms.getQmsStandards().size());

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        updatedListing.getQmsStandards().clear();
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getQmsStandards());
        assertEquals(origQmsLength, updatedListing.getQmsStandards().size());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testUpdateQmsModification()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long qmsToAdd = 1L;

        // add a qms
        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
        int origQmsLength = existingListing.getQmsStandards().size();
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        CertifiedProductQmsStandard std = new CertifiedProductQmsStandard();
        std.setQmsStandardId(qmsToAdd);
        std.setQmsModification("None");
        std.setApplicableCriteria("All");
        updatedListing.getQmsStandards().add(std);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        // update the qms
        CertifiedProductSearchDetails listingWithQms = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(listingWithQms.getQmsStandards());
        assertEquals(origQmsLength + 1, listingWithQms.getQmsStandards().size());

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        String newMod = "I modified a lot of stuff";
        updatedListing.getQmsStandards().get(0).setQmsModification(newMod);
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getQmsStandards());
        assertEquals(origQmsLength + 1, updatedListing.getQmsStandards().size());
        assertEquals(newMod, updatedListing.getQmsStandards().get(0).getQmsModification());
    }

    /*********************
     * Targeted User crud tests.
     * 
     * @throws MissingReasonException
     * @throws AccessDeniedException
     *************************/
    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddExistingTargetedUser() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long targetedUserToAdd = -1L;
        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
        int origTuLength = existingListing.getTargetedUsers().size();
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        CertifiedProductTargetedUser tu = new CertifiedProductTargetedUser();
        tu.setTargetedUserId(targetedUserToAdd);
        updatedListing.getTargetedUsers().add(tu);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getTargetedUsers());
        assertEquals(origTuLength + 1, updatedListing.getTargetedUsers().size());
        boolean foundAddedTu = false;
        for (CertifiedProductTargetedUser updatedTu : updatedListing.getTargetedUsers()) {
            if (updatedTu.getTargetedUserId().longValue() == targetedUserToAdd.longValue()) {
                foundAddedTu = true;
            }
        }
        assertTrue(foundAddedTu);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddNonExistingTargetedUser() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
        int origTuLength = existingListing.getTargetedUsers().size();
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        CertifiedProductTargetedUser tu = new CertifiedProductTargetedUser();
        String newTuName = "Physical Therapy";
        tu.setTargetedUserName(newTuName);
        updatedListing.getTargetedUsers().add(tu);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getTargetedUsers());
        assertEquals(origTuLength + 1, updatedListing.getTargetedUsers().size());

        CertifiedProductTargetedUser added = updatedListing.getTargetedUsers().get(0);
        assertNotNull(added.getTargetedUserId());
        assertNotNull(added.getId());
        assertEquals(newTuName, added.getTargetedUserName());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testDeleteTargetedUser()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long targetedUserToAdd = -1L;
        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
        int origTuLength = existingListing.getTargetedUsers().size();
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        CertifiedProductTargetedUser tu = new CertifiedProductTargetedUser();
        tu.setTargetedUserId(targetedUserToAdd);
        updatedListing.getTargetedUsers().add(tu);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        // remove the targeted user
        assertNotNull(updatedListing.getTargetedUsers());
        assertEquals(origTuLength + 1, updatedListing.getTargetedUsers().size());

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        updatedListing.getTargetedUsers().clear();
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getTargetedUsers());
        assertEquals(origTuLength, updatedListing.getTargetedUsers().size());
    }

    /*********************
     * Accessibility Standard crud tests.
     * 
     * @throws MissingReasonException
     * @throws AccessDeniedException
     *************************/

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddExistingAccessibilityStandard()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long accStdIdToAdd = 1L;
        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
        int origAccStdLength = existingListing.getAccessibilityStandards().size();
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        CertifiedProductAccessibilityStandard accStd = new CertifiedProductAccessibilityStandard();
        accStd.setAccessibilityStandardId(accStdIdToAdd);
        updatedListing.getAccessibilityStandards().add(accStd);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getAccessibilityStandards());
        assertEquals(origAccStdLength + 1, updatedListing.getAccessibilityStandards().size());
        boolean foundAddedStd = false;
        for (CertifiedProductAccessibilityStandard updatedStd : updatedListing.getAccessibilityStandards()) {
            if (updatedStd.getAccessibilityStandardId().longValue() == accStdIdToAdd.longValue()) {
                foundAddedStd = true;
            }
        }
        assertTrue(foundAddedStd);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddNonExistingAccessibilityStandard()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
        int origStdLength = existingListing.getAccessibilityStandards().size();
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        CertifiedProductAccessibilityStandard std = new CertifiedProductAccessibilityStandard();
        String newStdName = "NEW STANDARD";
        std.setAccessibilityStandardName(newStdName);
        updatedListing.getAccessibilityStandards().add(std);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getAccessibilityStandards());
        assertEquals(origStdLength + 1, updatedListing.getAccessibilityStandards().size());

        CertifiedProductAccessibilityStandard added = updatedListing.getAccessibilityStandards().get(0);
        assertNotNull(added.getAccessibilityStandardName());
        assertNotNull(added.getId());
        assertEquals(newStdName, added.getAccessibilityStandardName());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testDeleteAccessibilityStandard()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long accStdIdToAdd = 1L;
        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
        int origAccStdLength = existingListing.getAccessibilityStandards().size();
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        CertifiedProductAccessibilityStandard accStd = new CertifiedProductAccessibilityStandard();
        accStd.setAccessibilityStandardId(accStdIdToAdd);
        updatedListing.getAccessibilityStandards().add(accStd);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        // remove the accessibility standard
        assertNotNull(updatedListing.getAccessibilityStandards());
        assertEquals(origAccStdLength + 1, updatedListing.getAccessibilityStandards().size());

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        updatedListing.getAccessibilityStandards().clear();
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getAccessibilityStandards());
        assertEquals(origAccStdLength, updatedListing.getAccessibilityStandards().size());
    }

    /*********************
     * Meaningful Use crud tests
     *************************/
    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddMeaningfulUseHistoryEntry() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, InvalidArgumentsException, IOException,
            ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long now = System.currentTimeMillis();
        final Long newMuuCount = 60L;

        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
        int origMuuCount = existingListing.getMeaningfulUseUserHistory().size();
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        MeaningfulUseUser newMuu = new MeaningfulUseUser();
        newMuu.setMuuCount(newMuuCount);
        newMuu.setMuuDate(now);
        updatedListing.getMeaningfulUseUserHistory().add(newMuu);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getMeaningfulUseUserHistory());
        assertEquals(origMuuCount + 1, updatedListing.getMeaningfulUseUserHistory().size());
        boolean foundAddedMuu = false;
        for (MeaningfulUseUser muu : updatedListing.getMeaningfulUseUserHistory()) {
            if (muu.getMuuCount().longValue() == newMuuCount.longValue()
                    && muu.getMuuDate().longValue() == now.longValue()) {
                foundAddedMuu = true;
            }
        }
        assertTrue(foundAddedMuu);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testDeleteMeaningfulUseUser()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long now = System.currentTimeMillis();
        final Long newMuuCount = 60L;

        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
        int origMuuCount = existingListing.getMeaningfulUseUserHistory().size();
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        MeaningfulUseUser newMuu = new MeaningfulUseUser();
        newMuu.setMuuCount(newMuuCount);
        newMuu.setMuuDate(now);
        updatedListing.getMeaningfulUseUserHistory().add(newMuu);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        // remove the muu
        assertNotNull(updatedListing.getMeaningfulUseUserHistory());
        assertEquals(origMuuCount + 1, updatedListing.getMeaningfulUseUserHistory().size());

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        updatedListing.getMeaningfulUseUserHistory().clear();
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing.getMeaningfulUseUserHistory());
        assertEquals(0, updatedListing.getMeaningfulUseUserHistory().size());
    }

    /*********************
     * Certification Result add and remove tests
     *************************/
    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testUpdateCertificationResultSuccess()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 1L;
        final Long certIdToUpdate = 4L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        // update one that is currently false to be true
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                cert.setSuccess(Boolean.TRUE);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertTrue("Expect certification success to be true", cert.isSuccess());
            }
        }
        assertTrue(foundCert);
    }

    /*********************
     * Certification Result additional software tests.
     *************************/

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddCertificationResultAdditionalSoftware()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 1L;
        final Long certIdToUpdate = 2L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultAdditionalSoftware softwareToAdd = new CertificationResultAdditionalSoftware();
                softwareToAdd.setJustification("you need it");
                softwareToAdd.setName("Microsoft Windows");
                softwareToAdd.setVersion("2000");
                cert.getAdditionalSoftware().add(softwareToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(1, cert.getAdditionalSoftware().size());
                CertificationResultAdditionalSoftware added = cert.getAdditionalSoftware().get(0);
                assertEquals("you need it", added.getJustification());
                assertNull(added.getCertifiedProductNumber());
                assertNull(added.getCertifiedProductId());
                assertEquals("Microsoft Windows", added.getName());
                assertEquals("2000", added.getVersion());
            }
        }
        assertTrue(foundCert);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddCertificationResultAdditionalSoftwareAsCertifiedProduct()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 1L;
        final Long certIdToUpdate = 2L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultAdditionalSoftware softwareToAdd = new CertificationResultAdditionalSoftware();
                softwareToAdd.setCertifiedProductId(2L);
                cert.getAdditionalSoftware().add(softwareToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(1, cert.getAdditionalSoftware().size());
                CertificationResultAdditionalSoftware added = cert.getAdditionalSoftware().get(0);
                assertNull(added.getJustification());
                assertNotNull(added.getCertifiedProductNumber());
                assertNotNull(added.getCertifiedProductId());
                assertEquals(2, added.getCertifiedProductId().longValue());
                assertNull(added.getName());
                assertNull(added.getVersion());
            }
        }
        assertTrue(foundCert);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddCertificationResultAdditionalSoftwareWithGroupings()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 1L;
        final Long certIdToUpdate = 2L;
        final int expectedSwCount = 3;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultAdditionalSoftware softwareToAdd1 = new CertificationResultAdditionalSoftware();
                softwareToAdd1.setJustification("you need it");
                softwareToAdd1.setName("Microsoft Windows");
                softwareToAdd1.setVersion("2000");
                softwareToAdd1.setGrouping("A");
                cert.getAdditionalSoftware().add(softwareToAdd1);

                CertificationResultAdditionalSoftware softwareToAdd2 = new CertificationResultAdditionalSoftware();
                softwareToAdd2.setCertifiedProductId(2L);
                softwareToAdd2.setGrouping("A");
                cert.getAdditionalSoftware().add(softwareToAdd2);

                CertificationResultAdditionalSoftware softwareToAdd3 = new CertificationResultAdditionalSoftware();
                softwareToAdd3.setCertifiedProductId(3L);
                softwareToAdd3.setGrouping("B");
                cert.getAdditionalSoftware().add(softwareToAdd3);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(expectedSwCount, cert.getAdditionalSoftware().size());
            }
        }
        assertTrue(foundCert);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testUpdateCertificationResultAdditionalSoftwareJustification()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 1L;
        final Long certIdToUpdate = 2L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultAdditionalSoftware softwareToAdd = new CertificationResultAdditionalSoftware();
                softwareToAdd.setJustification("you need it");
                softwareToAdd.setName("Microsoft Windows");
                softwareToAdd.setVersion("2000");
                cert.getAdditionalSoftware().add(softwareToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        // now update the justification
        toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultAdditionalSoftware softwareToUpdate = cert.getAdditionalSoftware().get(0);
                softwareToUpdate.setJustification("updated justification");
            }
        }
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(1, cert.getAdditionalSoftware().size());
                CertificationResultAdditionalSoftware added = cert.getAdditionalSoftware().get(0);
                assertEquals("updated justification", added.getJustification());
                assertNull(added.getCertifiedProductNumber());
                assertNull(added.getCertifiedProductId());
                assertEquals("Microsoft Windows", added.getName());
                assertEquals("2000", added.getVersion());
            }
        }
        assertTrue(foundCert);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testRemoveCertificationResultAdditionalSoftware()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 1L;
        final Long certIdToUpdate = 1L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                assertEquals(2, cert.getAdditionalSoftware().size());
                cert.getAdditionalSoftware().remove(0);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(1, cert.getAdditionalSoftware().size());
            }
        }
        assertTrue(foundCert);
    }

    /*********************
     * Certification Result macra measure tests.
     *************************/

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddCertificationResultMacraMeasure()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long certIdToUpdate = 7L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                MacraMeasure measure = new MacraMeasure();
                measure.setId(1L);
                cert.getG1MacraMeasures().add(measure);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);

        try {
            cpManager.update(updateRequest);
        } catch (ValidationException e) {
            // do nothing for now
        }

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(1, cert.getG1MacraMeasures().size());
                MacraMeasure measure = cert.getG1MacraMeasures().get(0);
                assertEquals(1, measure.getId().longValue());
            }
        }
        assertTrue(foundCert);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testRemoveCertificationResultMacraMeasure()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long certIdToUpdate = 7L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                MacraMeasure measure = new MacraMeasure();
                measure.setId(1L);
                cert.getG1MacraMeasures().add(measure);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        // remove the measure
        toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                cert.getG1MacraMeasures().clear();
            }
        }
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(0, cert.getG1MacraMeasures().size());
            }
        }
        assertTrue(foundCert);
    }

    /*********************
     * Certification Result UCD process tests.
     *************************/

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddCertificationResultUcdProcess()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 1L;
        final Long certIdToUpdate = 2L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(toUpdateListing.getSed());

        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                UcdProcess ucdToAdd = new UcdProcess();
                ucdToAdd.setId(1L);
                CertificationCriterion criteria = new CertificationCriterion();
                criteria.setNumber(cert.getNumber());
                ucdToAdd.getCriteria().add(criteria);
                toUpdateListing.getSed().getUcdProcesses().add(ucdToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        assertNotNull(updatedListing.getSed());
        assertNotNull(updatedListing.getSed().getUcdProcesses());
        assertEquals(1, updatedListing.getSed().getUcdProcesses().size());

        UcdProcess ucd = updatedListing.getSed().getUcdProcesses().get(0);
        assertNotNull(ucd.getCriteria());
        assertEquals(2, ucd.getCriteria().size());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testUpdateCertificationResultUcdProcessDetails()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 1L;
        final Long certIdToUpdate = 2L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                UcdProcess ucdToAdd = new UcdProcess();
                ucdToAdd.setId(1L);
                ucdToAdd.setDetails("Some details");
                CertificationCriterion criteria = new CertificationCriterion();
                criteria.setNumber(cert.getNumber());
                ucdToAdd.getCriteria().add(criteria);
                toUpdateListing.getSed().getUcdProcesses().add(ucdToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        // now update the details
        toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (UcdProcess ucd : toUpdateListing.getSed().getUcdProcesses()) {
            ucd.setDetails("NEW DETAILS");
        }
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        assertNotNull(updatedListing.getSed());
        assertNotNull(updatedListing.getSed().getUcdProcesses());
        assertEquals(1, updatedListing.getSed().getUcdProcesses().size());
        UcdProcess ucd = updatedListing.getSed().getUcdProcesses().get(0);
        assertEquals("NEW DETAILS", ucd.getDetails());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testRemoveCertificationResultUcdProcess()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 1L;
        final Long certIdToUpdate = 2L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                UcdProcess ucdToAdd = new UcdProcess();
                ucdToAdd.setId(1L);
                ucdToAdd.setDetails("Some details");
                CertificationCriterion criteria = new CertificationCriterion();
                criteria.setNumber(cert.getNumber());
                ucdToAdd.getCriteria().add(criteria);
                toUpdateListing.getSed().getUcdProcesses().add(ucdToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        // remove the ucd
        toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        toUpdateListing.getSed().getUcdProcesses().clear();
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        assertNotNull(updatedListing.getSed());
        assertNotNull(updatedListing.getSed().getUcdProcesses());
        assertEquals(0, updatedListing.getSed().getUcdProcesses().size());
    }

    /*********************
     * Certification Result test standard tests.
     *************************/

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddExistingCertificationResultTestStandard()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 1L;
        final Long certIdToUpdate = 2L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestStandard tsToAdd = new CertificationResultTestStandard();
                tsToAdd.setTestStandardId(1L);
                cert.getTestStandards().add(tsToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(1, cert.getTestStandards().size());
                CertificationResultTestStandard added = cert.getTestStandards().get(0);
                assertEquals(1, added.getTestStandardId().longValue());
            }
        }
        assertTrue(foundCert);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddNewCertificationResultTestStandard()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 1L;
        final Long certIdToUpdate = 2L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestStandard tsToAdd = new CertificationResultTestStandard();
                tsToAdd.setTestStandardName("test test standard");
                tsToAdd.setTestStandardDescription("a very good standard");
                cert.getTestStandards().add(tsToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(1, cert.getTestStandards().size());
                CertificationResultTestStandard added = cert.getTestStandards().get(0);
                assertNotNull(added.getTestStandardId());
                assertEquals("test test standard", added.getTestStandardName());
                assertEquals("a very good standard", added.getTestStandardDescription());
            }
        }
        assertTrue(foundCert);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testRemoveCertificationResultTestStandard()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 1L;
        final Long certIdToUpdate = 2L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestStandard tsToAdd = new CertificationResultTestStandard();
                tsToAdd.setTestStandardId(1L);
                cert.getTestStandards().add(tsToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        // remove the ucd
        toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                cert.getTestStandards().clear();
            }
        }
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("test reason");
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(0, cert.getTestStandards().size());
            }
        }
        assertTrue(foundCert);
    }

    /*********************
     * Certification Result test tool tests
     *************************/

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddCertificationResultTestTool()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long certIdToUpdate = 11L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestTool toolToAdd = new CertificationResultTestTool();
                toolToAdd.setTestToolId(1L);
                toolToAdd.setTestToolVersion("Version");
                cert.getTestToolsUsed().add(toolToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(1, cert.getTestToolsUsed().size());
                CertificationResultTestTool added = cert.getTestToolsUsed().get(0);
                assertEquals(1, added.getTestToolId().longValue());
                assertEquals("Version", added.getTestToolVersion());
            }
        }
        assertTrue(foundCert);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testUpdateCertificationResultTestTool()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long certIdToUpdate = 11L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestTool toolToAdd = new CertificationResultTestTool();
                toolToAdd.setTestToolId(1L);
                toolToAdd.setTestToolVersion("Version 1");
                cert.getTestToolsUsed().add(toolToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        // update a tool
        toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                for (CertificationResultTestTool tool : cert.getTestToolsUsed()) {
                    if (tool.getTestToolId() == 1L) {
                        tool.setTestToolVersion("Version 2");
                    }
                }
            }
        }
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(1, cert.getTestToolsUsed().size());
                CertificationResultTestTool updated = cert.getTestToolsUsed().get(0);
                assertEquals(1, updated.getTestToolId().longValue());
                assertEquals("Version 2", updated.getTestToolVersion());
            }
        }
        assertTrue(foundCert);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testRemoveCertificationResultTestTool()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long certIdToUpdate = 11L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestTool toolToAdd = new CertificationResultTestTool();
                toolToAdd.setTestToolId(1L);
                cert.getTestToolsUsed().add(toolToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        // remove the ucd
        toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                cert.getTestToolsUsed().clear();
            }
        }
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(0, cert.getTestToolsUsed().size());
            }
        }
        assertTrue(foundCert);
    }

    /*********************
     * Certification Result test data tests
     *************************/

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddCertificationResultTestData()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long certIdToUpdate = 11L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestData tdToAdd = new CertificationResultTestData();
                TestData testData = new TestData();
                testData.setId(1L);
                tdToAdd.setTestData(testData);
                tdToAdd.setAlteration("altered");
                tdToAdd.setVersion("1.0.0");
                cert.getTestDataUsed().add(tdToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(1, cert.getTestDataUsed().size());
                CertificationResultTestData added = cert.getTestDataUsed().get(0);
                assertNotNull(added.getTestData());
                assertNotNull(added.getTestData().getId());
                assertEquals(1, added.getTestData().getId().longValue());
                assertEquals("altered", added.getAlteration());
                assertEquals("1.0.0", added.getVersion());
            }
        }
        assertTrue(foundCert);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testUpdateCertificationResultTestDataAlteration()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long certIdToUpdate = 11L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestData tdToAdd = new CertificationResultTestData();
                TestData testData = new TestData();
                testData.setId(1L);
                tdToAdd.setTestData(testData);
                tdToAdd.setAlteration("altered");
                tdToAdd.setVersion("1.0.0");
                cert.getTestDataUsed().add(tdToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        // now update the details
        toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestData tdToUpdate = cert.getTestDataUsed().get(0);
                tdToUpdate.setAlteration("NEW ALTERATION");
            }
        }
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(1, cert.getTestDataUsed().size());
                CertificationResultTestData updated = cert.getTestDataUsed().get(0);
                assertEquals("NEW ALTERATION", updated.getAlteration());
            }
        }
        assertTrue(foundCert);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testRemoveCertificationResultTestData()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long certIdToUpdate = 11L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestData tdToAdd = new CertificationResultTestData();
                TestData testData = new TestData();
                testData.setId(1L);
                tdToAdd.setTestData(testData);
                tdToAdd.setAlteration("altered");
                tdToAdd.setVersion("1.0.0");
                cert.getTestDataUsed().add(tdToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        // remove the ucd
        toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                cert.getTestDataUsed().clear();
            }
        }
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(0, cert.getTestDataUsed().size());
            }
        }
        assertTrue(foundCert);
    }

    /*********************
     * Certification Result test procedure tests
     *************************/

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddNewCertificationResultTestProcedure()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long certIdToUpdate = 11L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestProcedure procToAdd = new CertificationResultTestProcedure();
                TestProcedure testProcedure = new TestProcedure();
                testProcedure.setId(1L);
                procToAdd.setTestProcedure(testProcedure);
                procToAdd.setTestProcedureVersion("1.1.1");
                cert.getTestProcedures().add(procToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(1, cert.getTestProcedures().size());
                CertificationResultTestProcedure added = cert.getTestProcedures().get(0);
                assertNotNull(added.getTestProcedure());
                assertNotNull(added.getTestProcedure().getId());
                assertEquals(1, added.getTestProcedure().getId().longValue());
                assertEquals("1.1.1", added.getTestProcedureVersion());
            }
        }
        assertTrue(foundCert);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testRemoveCertificationResultTestProcedure()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long certIdToUpdate = 11L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestProcedure procToAdd = new CertificationResultTestProcedure();
                TestProcedure testProcedure = new TestProcedure();
                testProcedure.setId(1L);
                procToAdd.setTestProcedure(testProcedure);
                procToAdd.setTestProcedureVersion("1.1.1");
                cert.getTestProcedures().add(procToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        // remove the proc
        toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                cert.getTestProcedures().clear();
            }
        }
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(0, cert.getTestProcedures().size());
            }
        }
        assertTrue(foundCert);
    }

    /*********************
     * Certification Result test functionality tests
     *************************/

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddNewCertificationResultTestFunctionality()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long certIdToUpdate = 11L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestFunctionality funcToAdd = new CertificationResultTestFunctionality();
                funcToAdd.setTestFunctionalityId(2L);
                cert.getTestFunctionality().add(funcToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(1, cert.getTestFunctionality().size());
                CertificationResultTestFunctionality added = cert.getTestFunctionality().get(0);
                assertEquals(2, added.getTestFunctionalityId().longValue());
            }
        }
        assertTrue(foundCert);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testRemoveCertificationResultTestFunctionality()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;
        final Long certIdToUpdate = 11L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                CertificationResultTestFunctionality funcToAdd = new CertificationResultTestFunctionality();
                funcToAdd.setTestFunctionalityId(2L);
                cert.getTestFunctionality().add(funcToAdd);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        // remove the proc
        toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CertificationResult cert : toUpdateListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                cert.getTestFunctionality().clear();
            }
        }
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCert = false;
        for (CertificationResult cert : updatedListing.getCertificationResults()) {
            if (cert.getId().longValue() == certIdToUpdate.longValue()) {
                foundCert = true;
                assertEquals(0, cert.getTestFunctionality().size());
            }
        }
        assertTrue(foundCert);
    }

    /*********************
     * CQM tests.
     *************************/

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddCqm() throws EntityRetrievalException, EntityCreationException,
            JsonProcessingException, InvalidArgumentsException, IOException, ValidationException, AccessDeniedException,
            MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = -1L;
        final Long listingId = 5L;
        final String cqmToUpdate = "CMS163";

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        boolean updatedCqm = false;
        for (CQMResultDetails cqm : toUpdateListing.getCqmResults()) {
            if (cqm.getCmsId().equals(cqmToUpdate)) {
                assertFalse(cqm.isSuccess());
                assertTrue(cqm.getSuccessVersions() == null || cqm.getSuccessVersions().size() == 0);
                cqm.setSuccess(true);
                cqm.getSuccessVersions().add("v3");
                cqm.getSuccessVersions().add("v4");
                updatedCqm = true;
            }
        }
        assertTrue(updatedCqm);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCqm = false;
        for (CQMResultDetails cqm : updatedListing.getCqmResults()) {
            if (cqm.getCmsId().equals(cqmToUpdate)) {
                foundCqm = true;
                assertTrue("Expect CQM success to be true", cqm.isSuccess());
                assertNotNull(cqm.getSuccessVersions());
                assertEquals(2, cqm.getSuccessVersions().size());
            }
        }
        assertTrue(foundCqm);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testUpdateCqmAddCriteria() throws EntityRetrievalException, EntityCreationException,
            JsonProcessingException, InvalidArgumentsException, IOException, ValidationException, AccessDeniedException,
            MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = -1L;
        final Long listingId = 5L;
        final String cqmToUpdate = "CMS163";

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        boolean updatedCqm = false;
        for (CQMResultDetails cqm : toUpdateListing.getCqmResults()) {
            if (cqm.getCmsId().equals(cqmToUpdate)) {
                assertFalse(cqm.isSuccess());
                assertTrue(cqm.getSuccessVersions() == null || cqm.getSuccessVersions().size() == 0);
                cqm.setSuccess(true);
                cqm.getSuccessVersions().add("v3");
                updatedCqm = true;
            }
        }
        assertTrue(updatedCqm);
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails listingWithCqmAndCriteria = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(listingWithCqmAndCriteria);
        for (CQMResultDetails cqm : listingWithCqmAndCriteria.getCqmResults()) {
            if (cqm.getCmsId().equals(cqmToUpdate)) {
                CQMResultCertification cqmCert = new CQMResultCertification();
                cqmCert.setCertificationId(25L);
                cqmCert.setCertificationNumber("170.315 (c)(1)");
                cqm.getCriteria().add(cqmCert);
            }
        }
        updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listingWithCqmAndCriteria);
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCqm = false;
        for (CQMResultDetails cqm : updatedListing.getCqmResults()) {
            if (cqm.getCmsId().equals(cqmToUpdate)) {
                foundCqm = true;
                assertNotNull(cqm.getCriteria());
                assertEquals(1, cqm.getCriteria().size());
                assertEquals("170.315 (c)(1)", cqm.getCriteria().get(0).getCertificationNumber());
            }
        }
        assertTrue(foundCqm);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testRemoveCqm() throws EntityRetrievalException, EntityCreationException,
            JsonProcessingException, InvalidArgumentsException, IOException, ValidationException, AccessDeniedException,
            MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = -1L;
        final Long listingId = 2L;

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        for (CQMResultDetails cqm : toUpdateListing.getCqmResults()) {
            if (cqm.isSuccess() == Boolean.TRUE) {
                cqm.setSuccess(Boolean.FALSE);
                cqm.getSuccessVersions().clear();
            }
        }

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        updateRequest.setReason("This is the reason");
        cpManager.update(updateRequest);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        assertNotNull(updatedListing);
        boolean foundCqm = false;
        for (CQMResultDetails cqm : updatedListing.getCqmResults()) {
            foundCqm = true;
            assertFalse("Expect CQM success to be false", cqm.isSuccess());
            assertTrue(cqm.getSuccessVersions() == null || cqm.getSuccessVersions().size() == 0);
        }
        assertTrue(foundCqm);
    }

    /*********************
     * Certification Result test participant tests.
     *************************/

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testAddTestTask() throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;

        TestTask toAdd = new TestTask();
        toAdd.setId(-700L);
        toAdd.setDescription("description of the task");
        toAdd.setTaskErrors("6.5");
        toAdd.setTaskErrorsStddev("4.5");
        toAdd.setTaskPathDeviationObserved("123");
        toAdd.setTaskPathDeviationOptimal("0");
        toAdd.setTaskRating("5.0");
        toAdd.setTaskRatingScale("Likert");
        toAdd.setTaskRatingStddev("1.5");
        toAdd.setTaskSuccessAverage("95.1");
        toAdd.setTaskSuccessStddev("90.0");
        toAdd.setTaskTimeAvg("15");
        toAdd.setTaskTimeStddev("5");
        toAdd.setTaskTimeDeviationObservedAvg("10");
        toAdd.setTaskTimeDeviationOptimalAvg("1");
        CertificationCriterion crit = new CertificationCriterion();
        crit.setId(1L);
        crit.setNumber("170.315 (a)(1)");
        toAdd.getCriteria().add(crit);
        for (int i = 0; i < 10; i++) {
            TestParticipant tp = new TestParticipant();
            tp.setId((i + 1) * -1000L);
            tp.setAgeRangeId(1L);
            tp.setAgeRange("0-9");
            tp.setAssistiveTechnologyNeeds("some needs");
            tp.setComputerExperienceMonths("5");
            tp.setEducationTypeId(1L);
            tp.setEducationTypeName("No high school degree");
            tp.setGender("Female");
            tp.setOccupation("Doctor");
            tp.setProductExperienceMonths("4");
            tp.setProfessionalExperienceMonths("65");
            toAdd.getTestParticipants().add(tp);
        }

        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);
        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        List<TestTask> certTasks = updatedListing.getSed().getTestTasks();
        long origNumTasks = certTasks.size();
        certTasks.add(toAdd);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        existingListing = cpdManager.getCertifiedProductDetails(listingId);
        assertEquals(origNumTasks + 1, existingListing.getSed().getTestTasks().size());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testUpdateEducationForOneParticipant()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long acbId = 1L;
        final Long listingId = 5L;

        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        List<TestTask> certTasks = updatedListing.getSed().getTestTasks();
        TestTask certTask = certTasks.get(0);
        Collection<TestParticipant> taskParts = certTask.getTestParticipants();

        TestParticipant firstPart = taskParts.iterator().next();
        final long changedParticipantId = firstPart.getId();
        firstPart.setEducationTypeId(1L);
        firstPart.setEducationTypeName("No high school degree");

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        existingListing = cpdManager.getCertifiedProductDetails(listingId);
        certTasks = existingListing.getSed().getTestTasks();
        certTask = certTasks.get(0);
        taskParts = certTask.getTestParticipants();
        boolean changedParticipantExists = false;
        for (TestParticipant part : taskParts) {
            if (changedParticipantId == part.getId().longValue()) {
                changedParticipantExists = true;
                assertNotNull(part.getEducationTypeId());
                assertEquals(1, part.getEducationTypeId().longValue());
            }
        }
        assertTrue(changedParticipantExists);
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testUpdateOccupationForAllParticipants()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long listingId = 5L;
        final Long acbId = -1L;
        final Long certResultId = 7L;

        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        List<CertificationResult> certs = updatedListing.getCertificationResults();
        CertificationResult certToUpdate = null;
        for (CertificationResult cert : certs) {
            if (cert.getId().equals(certResultId)) {
                certToUpdate = cert;
            }
        }
        List<TestTask> certTasks = updatedListing.getSed().getTestTasks();
        TestTask certTask = certTasks.get(0);
        Collection<TestParticipant> taskParts = certTask.getTestParticipants();

        for (TestParticipant part : taskParts) {
            part.setOccupation("Teacher");
        }

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        existingListing = cpdManager.getCertifiedProductDetails(listingId);
        certs = existingListing.getCertificationResults();
        for (CertificationResult cert : certs) {
            if (cert.getId().equals(certResultId)) {
                certToUpdate = cert;
            }
        }
        certTasks = existingListing.getSed().getTestTasks();
        certTask = certTasks.get(0);
        taskParts = certTask.getTestParticipants();
        assertEquals(10, taskParts.size());
        for (TestParticipant part : taskParts) {
            assertEquals("Teacher", part.getOccupation());
        }
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testUpdateEducationForAllParticipants()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long listingId = 5L;
        final Long acbId = -1L;
        final Long certResultId = 7L;
        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        List<CertificationResult> certs = updatedListing.getCertificationResults();
        CertificationResult certToUpdate = null;
        for (CertificationResult cert : certs) {
            if (cert.getId().equals(certResultId)) {
                certToUpdate = cert;
            }
        }

        List<TestTask> certTasks = updatedListing.getSed().getTestTasks();
        TestTask certTask = certTasks.get(0);
        Collection<TestParticipant> taskParts = certTask.getTestParticipants();

        for (TestParticipant part : taskParts) {
            part.setEducationTypeId(2L);
            part.setEducationTypeName("High school graduate, diploma or the equivalent (for example: GED)");
        }

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        cpManager.update(updateRequest);

        existingListing = cpdManager.getCertifiedProductDetails(listingId);
        certs = existingListing.getCertificationResults();
        for (CertificationResult cert : certs) {
            if (cert.getId().equals(certResultId)) {
                certToUpdate = cert;
            }
        }
        certTasks = existingListing.getSed().getTestTasks();
        certTask = certTasks.get(0);
        taskParts = certTask.getTestParticipants();
        for (TestParticipant part : taskParts) {
            assertNotNull(part.getEducationTypeId());
            assertEquals(2, part.getEducationTypeId().longValue());
        }
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback
    public void testUpdateAgeRangeForAllParticipants()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long listingId = 5L;
        final Long acbId = -1L;
        final Long certResultId = 7L;

        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);

        CertifiedProductSearchDetails toUpdateListing = cpdManager.getCertifiedProductDetails(listingId);
        List<CertificationResult> certs = toUpdateListing.getCertificationResults();
        CertificationResult certToUpdate = null;
        for (CertificationResult cert : certs) {
            if (cert.getId().equals(certResultId)) {
                certToUpdate = cert;
            }
        }
        List<TestTask> certTasks = toUpdateListing.getSed().getTestTasks();
        TestTask certTask = certTasks.get(0);
        Collection<TestParticipant> taskParts = certTask.getTestParticipants();

        for (TestParticipant part : taskParts) {
            part.setAgeRangeId(4L);
            part.setAgeRange("30-39");
        }

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(toUpdateListing);
        cpManager.update(updateRequest);

        existingListing = cpdManager.getCertifiedProductDetails(listingId);
        certs = existingListing.getCertificationResults();
        CertificationResult updatedCert = null;
        for (CertificationResult cert : certs) {
            if (cert.getId().longValue() == certToUpdate.getId().longValue()) {
                updatedCert = cert;
            }
        }
        assertNotNull(updatedCert);
        certTasks = existingListing.getSed().getTestTasks();
        certTask = certTasks.get(0);
        taskParts = certTask.getTestParticipants();
        assertEquals(10, taskParts.size());
        for (TestParticipant part : taskParts) {
            assertEquals(4, part.getAgeRangeId().longValue());
            assertEquals("30-39", part.getAgeRange());
        }
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testUpdateG1MacraMeasures()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long listingId = 3L;
        final Long acbId = -1L;

        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        List<CertificationResult> certs = updatedListing.getCertificationResults();
        assertNotNull(certs);
        assertEquals(1, certs.size());
        CertificationResult cert = certs.get(0);
        assertNotNull(cert);
        List<MacraMeasure> measures = cert.getG1MacraMeasures();
        assertNotNull(measures);
        assertEquals(1, measures.size());
        MacraMeasure measure = measures.get(0);
        assertNotNull(measure);
        MacraMeasure newMeasure = new MacraMeasure();
        newMeasure.setId(2L);
        cert.getG1MacraMeasures().set(0, newMeasure);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        updateRequest.setReason("This is the reason");
        cpManager.update(updateRequest);

        existingListing = cpdManager.getCertifiedProductDetails(listingId);
        certs = existingListing.getCertificationResults();
        assertNotNull(certs);
        assertEquals(1, certs.size());
        cert = certs.get(0);
        assertNotNull(cert);
        measures = cert.getG1MacraMeasures();
        assertNotNull(measures);
        assertEquals(1, measures.size());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testAddG2Measure() throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long listingId = 3L;
        final Long acbId = -1L;

        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        List<CertificationResult> certs = updatedListing.getCertificationResults();
        assertNotNull(certs);
        assertEquals(1, certs.size());
        CertificationResult cert = certs.get(0);
        assertNotNull(cert);

        MacraMeasure newMeasure = new MacraMeasure();
        newMeasure.setId(1L);
        cert.getG2MacraMeasures().add(newMeasure);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        updateRequest.setReason("This is the reason");
        cpManager.update(updateRequest);

        existingListing = cpdManager.getCertifiedProductDetails(listingId);
        certs = existingListing.getCertificationResults();
        assertNotNull(certs);
        assertEquals(1, certs.size());
        cert = certs.get(0);
        assertNotNull(cert);
        List<MacraMeasure> measures = cert.getG2MacraMeasures();
        assertNotNull(measures);
        assertEquals(1, measures.size());
        MacraMeasure measure = measures.get(0);
        assertEquals(1L, measure.getId().longValue());
    }

    @Test
    @Transactional(readOnly = false)
    @Rollback(true)
    public void testDeleteG1MacraMeasure()
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException,
            InvalidArgumentsException, IOException, ValidationException, AccessDeniedException, MissingReasonException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final Long listingId = 3L;
        final Long acbId = -1L;

        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(listingId);

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        List<CertificationResult> certs = updatedListing.getCertificationResults();
        assertNotNull(certs);
        assertEquals(1, certs.size());
        CertificationResult cert = certs.get(0);
        assertNotNull(cert);
        cert.getG1MacraMeasures().clear();

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(updatedListing);
        updateRequest.setReason("This is the reason");
        cpManager.update(updateRequest);

        existingListing = cpdManager.getCertifiedProductDetails(listingId);
        certs = existingListing.getCertificationResults();
        assertNotNull(certs);
        assertEquals(1, certs.size());
        cert = certs.get(0);
        assertNotNull(cert);
        List<MacraMeasure> measures = cert.getG1MacraMeasures();
        assertNotNull(measures);
        assertEquals(0, measures.size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void getListingsOwnedByProduct() throws EntityRetrievalException {
        List<CertifiedProductDetailsDTO> listings = cpManager.getByProduct(-1L);
        assertNotNull(listings);
        assertEquals(6, listings.size());
    }

    private void updateListingStatus(final Long acbId, final Long listingId,
            final CertificationStatusDTO stat, final String reason)
            throws EntityRetrievalException, EntityCreationException,
            JsonProcessingException, InvalidArgumentsException, IOException, ValidationException, AccessDeniedException,
            MissingReasonException {

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetails(listingId);
        CertificationStatusEvent statusEvent = new CertificationStatusEvent();
        statusEvent.setEventDate(System.currentTimeMillis());
        statusEvent.setStatus(new CertificationStatus(stat));
        statusEvent.setReason(reason);
        updatedListing.getCertificationEvents().add(statusEvent);

        ListingUpdateRequest toUpdate = new ListingUpdateRequest();
        toUpdate.setListing(updatedListing);
        toUpdate.setReason("test reason");
        cpManager.update(toUpdate);
    }
}
