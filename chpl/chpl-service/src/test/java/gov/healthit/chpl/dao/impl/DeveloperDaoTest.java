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

import gov.healthit.chpl.UnitTestUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.DecertifiedDeveloperDTODeprecated;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.TransparencyAttestationDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
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
public class DeveloperDaoTest extends TestCase {

    @Autowired
    private DeveloperDAO developerDao;

    @Autowired
    private AddressDAO addressDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser authUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        authUser = new JWTAuthenticatedUser();
        authUser.setFullName("Admin");
        authUser.setId(-2L);
        authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Test
    @Transactional
    public void getAllDevelopers() {
        List<DeveloperDTO> results = developerDao.findAll();
        assertNotNull(results);
        assertEquals(9, results.size());
        DeveloperDTO first = results.get(0);
        DeveloperStatusEventDTO status = first.getStatus();
        assertNotNull(status);
        assertNotNull(status.getId());
        assertNotNull(status.getStatus());
        assertNotNull(status.getStatus().getStatusName());
    }

    @Test
    @Transactional
    public void getDeveloperWithAddress() {
        Long developerId = -1L;
        DeveloperDTO developer = null;
        try {
            developer = developerDao.getById(developerId);
        } catch (EntityRetrievalException ex) {
            fail("Could not find developer with id " + developerId);
        }
        assertNotNull(developer);
        assertEquals(-1, developer.getId().longValue());
        assertNotNull(developer.getAddress());
        assertEquals(-1, developer.getAddress().getId().longValue());
        DeveloperStatusEventDTO status = developer.getStatus();
        assertNotNull(status);
        assertNotNull(status.getId());
        assertNotNull(status.getStatus());
        assertNotNull(status.getStatus().getStatusName());
    }

    @Test
    @Transactional
    public void getDeveloperWithoutAddress() {
        Long developerId = -3L;
        DeveloperDTO developer = null;
        try {
            developer = developerDao.getById(developerId);
        } catch (EntityRetrievalException ex) {
            fail("Could not find developer with id " + developerId);
        }
        assertNotNull(developer);
        assertEquals(-3, developer.getId().longValue());
        assertNull(developer.getAddress());
        DeveloperStatusEventDTO status = developer.getStatus();
        assertNotNull(status);
        assertNotNull(status.getId());
        assertNotNull(status.getStatus());
        assertNotNull(status.getStatus().getStatusName());
    }

    @Test
    @Transactional
    @Rollback
    public void createDeveloperWithoutAddress() throws EntityRetrievalException {
        DeveloperDTO developer = new DeveloperDTO();
        developer.setCreationDate(new Date());
        developer.setDeleted(false);
        developer.setLastModifiedDate(new Date());
        developer.setLastModifiedUser(-2L);
        developer.setName("Unit Test Developer!");
        developer.setWebsite("http://www.google.com");

        DeveloperDTO result = null;
        try {
            result = developerDao.create(developer);
        } catch (Exception ex) {
            fail("could not create developer!");
            ex.printStackTrace();
        }

        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(result.getId() > 0L);
        result = developerDao.getById(result.getId());
        assertNull(result.getAddress());
        DeveloperStatusEventDTO status = result.getStatus();
        assertNotNull(status);
        assertNotNull(status.getId());
        assertNotNull(status.getStatus());
        assertNotNull(status.getStatus().getStatusName());
        assertEquals(1, status.getStatus().getId().longValue());
    }

    @Test
    @Transactional
    @Rollback
    public void createDeveloperWithNewAddress() {
        DeveloperDTO developer = new DeveloperDTO();
        developer.setCreationDate(new Date());
        developer.setDeleted(false);
        developer.setLastModifiedDate(new Date());
        developer.setLastModifiedUser(-2L);
        developer.setName("Unit Test Developer!");
        developer.setWebsite("http://www.google.com");

        AddressDTO newAddress = new AddressDTO();
        newAddress.setStreetLineOne("11 Holmehurst Ave");
        newAddress.setCity("Catonsville");
        newAddress.setState("MD");
        newAddress.setZipcode("21228");
        newAddress.setCountry("USA");
        newAddress.setLastModifiedUser(-2L);
        newAddress.setCreationDate(new Date());
        newAddress.setLastModifiedDate(new Date());
        newAddress.setDeleted(false);
        developer.setAddress(newAddress);

        DeveloperDTO result = null;
        try {
            result = developerDao.create(developer);
        } catch (Exception ex) {
            fail("could not create developer!");
            ex.printStackTrace();
        }

        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(result.getId() > 0L);
        assertNotNull(result.getAddress());
        assertNotNull(result.getAddress().getId());
        assertTrue(result.getAddress().getId() > 0L);
    }

    @Test
    @Transactional
    @Rollback
    public void createDeveloperWithExistingAddress() {
        DeveloperDTO developer = new DeveloperDTO();
        developer.setCreationDate(new Date());
        developer.setDeleted(false);
        developer.setLastModifiedDate(new Date());
        developer.setLastModifiedUser(UnitTestUtil.ADMIN_ID);
        developer.setName("Unit Test Developer!");
        developer.setWebsite("http://www.google.com");

        try {
            AddressDTO existingAddress = addressDao.getById(-1L);
            existingAddress.setCountry("Russia");
            developer.setAddress(existingAddress);
        } catch (EntityRetrievalException ex) {
            fail("could not find existing address to set on developer");
        }

        DeveloperDTO result = null;
        try {
            result = developerDao.create(developer);
        } catch (Exception ex) {
            fail("could not create developer!");
            ex.printStackTrace();
        }

        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(result.getId() > 0L);
        assertNotNull(result.getAddress());
        assertNotNull(result.getAddress().getId());
        assertTrue(result.getAddress().getId() == -1L);
        assertEquals("Russia", result.getAddress().getCountry());
    }

    @Test
    @Transactional
    @Rollback
    public void updateDeveloper() {
        SecurityContextHolder.getContext().setAuthentication(authUser);
        DeveloperDTO developer = developerDao.findAll().get(0);
        developer.setName("UPDATED NAME");

        DeveloperDTO result = null;
        try {
            result = developerDao.update(developer);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("could not update developer!");
        }
        assertNotNull(result);

        try {
            DeveloperDTO updatedDeveloper = developerDao.getById(developer.getId());
            assertEquals("UPDATED NAME", updatedDeveloper.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("could not find developer!");
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void createDeveloperAcbMap() {
        SecurityContextHolder.getContext().setAuthentication(authUser);
        DeveloperDTO developer = developerDao.findAll().get(0);

        DeveloperACBMapDTO dto = new DeveloperACBMapDTO();
        dto.setAcbId(-8L);
        dto.setDeveloperId(developer.getId());
        dto.setTransparencyAttestation(new TransparencyAttestationDTO("N/A"));
        DeveloperACBMapDTO createdMapping = developerDao.createTransparencyMapping(dto);

        assertNotNull(createdMapping);

        dto = developerDao.getTransparencyMapping(developer.getId(), -8L);
        assertNotNull(dto);
        assertEquals("N/A", dto.getTransparencyAttestation().getTransparencyAttestation());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    /**
     * Given the CHPL is accepting search requests When I call the REST API's /decertified/developers Then
     * DeveloperDAOImpl.getCertifiedDevelopers() returns List<DeveloperDecertifiedDTO> Then the returned list contains
     * no duplicates (unique developerName for each row) Then the returned list contains the proper array of ONC_ACBs
     * Then the returned list contains the sum of all of the developer's CP.numMeaningfulUse
     */
    @Test
    @Transactional
    public void getDecertifiedDevelopers() {
        List<DecertifiedDeveloperDTODeprecated> dtoList = new ArrayList<DecertifiedDeveloperDTODeprecated>();
        dtoList = developerDao.getDecertifiedDevelopers();
        assertTrue(dtoList.size() == 1);
        assertTrue(dtoList.get(0).getDeveloperId() == -10L || dtoList.get(0).getDeveloperId() == -11L);
        assertTrue(dtoList.get(0).getNumMeaningfulUse() == 66 || dtoList.get(0).getNumMeaningfulUse() == 73);
        assertTrue(dtoList.get(0).getDeveloperStatus()
                .equals(String.valueOf(DeveloperStatusType.UnderCertificationBanByOnc)));
        assertTrue(dtoList.get(0).getAcbIdList().contains(-6L));
        assertTrue(dtoList.get(0).getAcbIdList().contains(-5L));
        assertTrue(dtoList.get(0).getAcbIdList().contains(-4L));
        assertTrue(dtoList.get(0).getAcbIdList().contains(-6L));
        assertTrue(dtoList.get(0).getAcbIdList().contains(-5L));
        assertTrue(dtoList.get(0).getAcbIdList().contains(-4L));
    }

}
