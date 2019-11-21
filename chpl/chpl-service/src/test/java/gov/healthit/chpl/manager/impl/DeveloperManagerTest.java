package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.domain.DecertifiedDeveloperResult;
import gov.healthit.chpl.domain.DeveloperTransparency;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import junit.framework.TestCase;

@ActiveProfiles({
    "Ff4jMock"
})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class,
        gov.healthit.chpl.Ff4jTestConfiguration.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class DeveloperManagerTest extends TestCase {

    @Autowired
    private DeveloperManager developerManager;
    @Autowired
    private ProductManager productManager;
    @Autowired
    private DeveloperStatusDAO devStatusDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Autowired
    private FF4j ff4j;

    private static JWTAuthenticatedUser adminUser;
    private static JWTAuthenticatedUser testUser3;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

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
        Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)).thenReturn(false);
    }

    @Test
    @Transactional
    public void testGetDeveloperAsAdmin() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        DeveloperDTO developer = developerManager.getById(-1L);
        assertNotNull(developer);
        assertNotNull(developer.getTransparencyAttestationMappings());
        assertTrue(developer.getTransparencyAttestationMappings().size() > 0);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Ignore //ignoring until transparency attestation mappings are fixed
    public void testGetDeveloperAsAcbAdmin() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(testUser3);
        DeveloperDTO developer = developerManager.getById(-1L);
        assertNotNull(developer);
        assertNotNull(developer.getTransparencyAttestationMappings());
        for (DeveloperACBMapDTO attMap : developer.getTransparencyAttestationMappings()) {
            if (attMap.getDeveloperId().longValue() == developer.getId().longValue()) {
                assertEquals("Affirmative", attMap.getTransparencyAttestation());
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    public void testGetAllDevelopersIncludingDeletedAsAdmin() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<DeveloperDTO> developers = developerManager.getAllIncludingDeleted();
        assertNotNull(developers);
        assertEquals(12, developers.size());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    public void testGetAllDevelopersIncludingDeletedUncredentialed() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(null);
        List<DeveloperDTO> developers = null;
        boolean failed = false;
        try {
            developers = developerManager.getAllIncludingDeleted();
        } catch (Exception ex) {
            // should fail
            failed = true;
        }
        assertNull(developers);
        assertTrue(failed);
    }

    @Test
    @Transactional
    public void testGetDeveloperCollection() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(null);
        List<DeveloperTransparency> developers = developerManager.getDeveloperCollection();
        assertNotNull(developers);
        assertEquals(9, developers.size());
    }

    @Test
    @Transactional
    @Rollback
    public void testDeveloperStatusChangeAllowedByAdmin()
            throws EntityRetrievalException, JsonProcessingException, MissingReasonException, EntityCreationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
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
        } catch (ValidationException ex) {
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
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testMergeDeveloper_productOwnershipHistoryAdded() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<Long> idsToMerge = new ArrayList<Long>();
        idsToMerge.add(-1L);
        idsToMerge.add(-4L);

        DeveloperDTO toCreate = createDeveloper();
        DeveloperDTO merged = null;
        try {
            merged = developerManager.merge(idsToMerge, toCreate);
        } catch (EntityCreationException | JsonProcessingException | EntityRetrievalException
                | ValidationException ex) {
            fail(ex.getMessage());
        }

        assertNotNull(merged);
        assertNotNull(merged.getId());

        try {
            ProductDTO affectedProduct = productManager.getById(-3L);
            assertNotNull(affectedProduct);
            assertNotNull(affectedProduct.getOwnerHistory());
            assertEquals(1, affectedProduct.getOwnerHistory().size());
            assertEquals(-1, affectedProduct.getOwnerHistory().get(0).getDeveloper().getId().longValue());
        } catch (EntityRetrievalException ex) {
            fail(ex.getMessage());
        }

        try {
            ProductDTO affectedProduct = productManager.getById(-1L);
            assertNotNull(affectedProduct);
            assertNotNull(affectedProduct.getOwnerHistory());
            assertEquals(2, affectedProduct.getOwnerHistory().size());
            int expectedDevsCount = 0;
            for (ProductOwnerDTO owner : affectedProduct.getOwnerHistory()) {
                if (owner.getDeveloper() != null && owner.getDeveloper().getId().longValue() == -1) {
                    expectedDevsCount++;
                } else if (owner.getDeveloper() != null && owner.getDeveloper().getId().longValue() == -2) {
                    expectedDevsCount++;
                }
            }
            assertEquals(2, expectedDevsCount);
        } catch (EntityRetrievalException ex) {
            fail(ex.getMessage());
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = ValidationException.class)
    @Transactional
    @Rollback
    public void testMergeDeveloperDuplicateChplProductNumberValidationError()
            throws JsonProcessingException, EntityRetrievalException, EntityCreationException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<Long> idsToMerge = new ArrayList<Long>();
        idsToMerge.add(-1L);
        idsToMerge.add(-2L);

        DeveloperDTO toCreate = new DeveloperDTO();
        toCreate.setName("dev name");

        DeveloperDTO merged = null;
        merged = developerManager.merge(idsToMerge, toCreate);

    }

    @Test(expected = AccessDeniedException.class)
    @Transactional
    @Rollback(true)
    public void testNoUpdatesAllowedByNonAdminIfDeveloperIsNotActive() throws EntityRetrievalException,
            JsonProcessingException, MissingReasonException, ValidationException, EntityCreationException {
        SecurityContextHolder.getContext().setAuthentication(testUser3);
        DeveloperDTO developer = developerManager.getById(-3L);
        assertNotNull(developer);
        DeveloperStatusEventDTO status = developer.getStatus();
        assertNotNull(status);
        assertNotNull(status.getId());
        assertNotNull(status.getStatus());
        assertNotNull(status.getStatus().getStatusName());
        assertNotSame(DeveloperStatusType.Active.toString(), status.getStatus().getStatusName());

        developer.setName("UPDATE THIS NAME");
        boolean failed = false;
        try {
            developer = developerManager.update(developer, false);
            assertTrue(failed);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testSplitDeveloper_productOwnershipHistoryAdded() throws ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Long developerIdToSplit = -1L;
        List<Long> productIdsToMove = new ArrayList<Long>();
        productIdsToMove.add(-1L);

        DeveloperDTO toCreate = createDeveloper();
        DeveloperDTO createdDev = null;
        try {
            createdDev = developerManager.split(developerManager.getById(developerIdToSplit), toCreate,
                    productIdsToMove);
        } catch (EntityCreationException | JsonProcessingException | EntityRetrievalException ex) {
            fail(ex.getMessage());
        }

        assertNotNull(createdDev);
        assertNotNull(createdDev.getId());

        try {
            ProductDTO movedProduct = productManager.getById(-1L);
            assertNotNull(movedProduct);
            assertNotNull(movedProduct.getOwnerHistory());
            assertEquals(2, movedProduct.getOwnerHistory().size());
            boolean foundOldOwner = false;
            for (ProductOwnerDTO owner : movedProduct.getOwnerHistory()) {
                if (owner.getDeveloper() != null
                        && owner.getDeveloper().getId().longValue() == developerIdToSplit.longValue()) {
                    foundOldOwner = true;
                }
            }
            assertTrue(foundOldOwner);
        } catch (EntityRetrievalException ex) {
            fail(ex.getMessage());
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testDeveloperStatusChangeNotAllowedByNonAdmin()
            throws EntityRetrievalException, JsonProcessingException, MissingReasonException, EntityCreationException {
        SecurityContextHolder.getContext().setAuthentication(testUser3);
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
            developerManager.update(developer, false);
        } catch (ValidationException ex) {
            System.out.println(ex.getMessage());
            failed = true;
        }
        assertTrue(failed);

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    /**
     * Given the CHPL is accepting search requests When I call the REST API's
     * /decertified/developers, the controller calls the
     * developerManager.getDecertifiedDevelopers() Then the manager returns a
     * list of DeveloperDecertifiedDTO with expected results.
     *
     * @throws EntityRetrievalException
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testGetDecertifiedDevelopers() throws EntityRetrievalException {
        List<DecertifiedDeveloperResult> results = developerManager.getDecertifiedDevelopers();
        assertEquals(1, results.size());
    }

    private DeveloperDTO createDeveloper() {
        DeveloperDTO developer = new DeveloperDTO();
        developer.setName("dev name");
        List<DeveloperStatusEventDTO> statusEvents = new ArrayList<DeveloperStatusEventDTO>();
        DeveloperStatusEventDTO statusEvent = new DeveloperStatusEventDTO();
        DeveloperStatusDTO status = new DeveloperStatusDTO();
        status.setId(1L);
        status.setStatusName(DeveloperStatusType.Active.name());
        statusEvent.setStatus(status);
        statusEvent.setStatusDate(new Date());
        statusEvents.add(statusEvent);
        developer.setStatusEvents(statusEvents);
        AddressDTO address = new AddressDTO();
        address.setStreetLineOne("111 Test Road");
        address.setCity("Baltimore");
        address.setState("MD");
        address.setZipcode("21000");
        address.setCountry("USA");
        developer.setAddress(address);
        ContactDTO contact = new ContactDTO();
        contact.setEmail("test@test.com");
        contact.setFriendlyName("Test");
        contact.setFullName("test test");
        contact.setPhoneNumber("123-456-7890");
        developer.setContact(contact);
        return developer;
    }
}
