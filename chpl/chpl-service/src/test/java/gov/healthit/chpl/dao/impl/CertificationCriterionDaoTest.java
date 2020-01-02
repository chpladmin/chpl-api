package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.exception.EntityCreationException;
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
public class CertificationCriterionDaoTest extends TestCase {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private CertificationCriterionDAO certificationCriterionDAO;

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
    @Transactional
    @Rollback
    public void testCreate() throws EntityCreationException, EntityRetrievalException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CertificationCriterionDTO dto = new CertificationCriterionDTO();
        dto.setAutomatedMeasureCapable(true);
        dto.setAutomatedNumeratorCapable(true);
        dto.setCertificationEditionId(1L);
        dto.setCreationDate(new Date());
        dto.setDeleted(false);
        dto.setDescription("Test");
        // dto.setId(null);
        dto.setLastModifiedDate(new Date());
        dto.setLastModifiedUser(-1L);
        dto.setNumber("CERT123");
        // dto.setParentCriterionId(null);
        dto.setRequiresSed(false);
        dto.setTitle("Test Cert Criterion");
        dto.setRemoved(false);

        CertificationCriterionDTO result = create(dto);
        CertificationCriterionDTO check = certificationCriterionDAO.getById(result.getId());

        assertEquals(result.getAutomatedMeasureCapable(), check.getAutomatedMeasureCapable());
        assertEquals(result.getAutomatedNumeratorCapable(), check.getAutomatedMeasureCapable());
        assertEquals(result.getCertificationEditionId(), check.getCertificationEditionId());
        assertEquals(result.getCreationDate(), check.getCreationDate());
        assertEquals(result.getDeleted(), check.getDeleted());
        assertEquals(result.getDescription(), check.getDescription());
        assertEquals(result.getId(), check.getId());
        assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
        assertEquals(result.getNumber(), check.getNumber());
        // assertEquals(result.getParentCriterionId(),
        // result.getParentCriterionId());
        assertEquals(result.getRequiresSed(), result.getRequiresSed());
        assertEquals(result.getTitle(), result.getTitle());

        delete(result.getId());

        SecurityContextHolder.getContext().setAuthentication(null);

    }

    @Test
    @Transactional
    @Rollback
    public void testUpdate() throws EntityRetrievalException, EntityCreationException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CertificationCriterionDTO dto = new CertificationCriterionDTO();
        dto.setAutomatedMeasureCapable(true);
        dto.setAutomatedNumeratorCapable(true);
        dto.setCertificationEditionId(1L);
        dto.setCreationDate(new Date());
        dto.setDeleted(false);
        dto.setDescription("Test");
        dto.setId(null);
        dto.setLastModifiedDate(new Date());
        dto.setLastModifiedUser(-1L);
        dto.setNumber("CERT123");
        // dto.setParentCriterionId(null);
        dto.setRequiresSed(false);
        dto.setTitle("Test Cert Criterion");
        dto.setRemoved(false);

        CertificationCriterionDTO result = certificationCriterionDAO.create(dto);

        dto.setAutomatedMeasureCapable(false);
        dto.setAutomatedNumeratorCapable(false);
        dto.setCertificationEditionId(2L);
        dto.setCreationDate(new Date());
        dto.setDeleted(false);
        dto.setDescription("Test 1");
        dto.setId(null);
        dto.setLastModifiedDate(new Date());
        dto.setLastModifiedUser(-2L);
        result.setNumber("CERT124");
        // dto.setParentCriterionId(null);
        dto.setRequiresSed(true);
        dto.setTitle("Test Cert Criterion 1");

        certificationCriterionDAO.update(result);

        CertificationCriterionDTO check = certificationCriterionDAO.getById(result.getId());

        assertEquals(result.getAutomatedMeasureCapable(), check.getAutomatedMeasureCapable());
        assertEquals(result.getAutomatedNumeratorCapable(), check.getAutomatedMeasureCapable());
        assertEquals(result.getCertificationEditionId(), check.getCertificationEditionId());
        assertEquals(result.getCreationDate(), check.getCreationDate());
        assertEquals(result.getDeleted(), check.getDeleted());
        assertEquals(result.getDescription(), check.getDescription());
        assertEquals(result.getId(), check.getId());
        assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
        assertEquals(result.getNumber(), check.getNumber());
        // assertEquals(result.getParentCriterionId(),
        // result.getParentCriterionId());
        assertEquals(result.getRequiresSed(), result.getRequiresSed());
        assertEquals(result.getTitle(), result.getTitle());

        certificationCriterionDAO.delete(result.getId());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testDelete() throws EntityCreationException, EntityRetrievalException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CertificationCriterionDTO dto = new CertificationCriterionDTO();
        dto.setAutomatedMeasureCapable(true);
        dto.setAutomatedNumeratorCapable(true);
        dto.setCertificationEditionId(1L);
        dto.setCreationDate(new Date());
        dto.setDeleted(false);
        dto.setDescription("Test");
        dto.setId(null);
        dto.setLastModifiedDate(new Date());
        dto.setLastModifiedUser(-1L);
        dto.setNumber("CERT123");
        // dto.setParentCriterionId(null);
        dto.setRequiresSed(false);
        dto.setTitle("Test Cert Criterion");
        dto.setRemoved(false);

        CertificationCriterionDTO result = certificationCriterionDAO.create(dto);
        CertificationCriterionDTO check = certificationCriterionDAO.getById(result.getId());

        assertEquals(result.getAutomatedMeasureCapable(), check.getAutomatedMeasureCapable());
        assertEquals(result.getAutomatedNumeratorCapable(), check.getAutomatedMeasureCapable());
        assertEquals(result.getCertificationEditionId(), check.getCertificationEditionId());
        assertEquals(result.getCreationDate(), check.getCreationDate());
        assertEquals(result.getDeleted(), check.getDeleted());
        assertEquals(result.getDescription(), check.getDescription());
        assertEquals(result.getId(), check.getId());
        assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
        assertEquals(result.getNumber(), check.getNumber());
        // assertEquals(result.getParentCriterionId(),
        // result.getParentCriterionId());
        assertEquals(result.getRequiresSed(), result.getRequiresSed());
        assertEquals(result.getTitle(), result.getTitle());

        certificationCriterionDAO.delete(result.getId());

        assertNull(certificationCriterionDAO.getById(result.getId()));

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    public void testFindAll() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<CertificationCriterionDTO> dtos = certificationCriterionDAO.findAll();
        assertNotNull(dtos);
        assertTrue(dtos.size() > 0);
        SecurityContextHolder.getContext().setAuthentication(null);

    }

    @Test
    @Transactional
    public void testFindByEdition() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<CertificationCriterionDTO> dtos = certificationCriterionDAO.findByCertificationEditionYear("2014");
        assertNotNull(dtos);
        assertTrue(dtos.size() > 0);
        SecurityContextHolder.getContext().setAuthentication(null);

    }

    @Test
    @Transactional
    public void testGetById() throws EntityRetrievalException, EntityCreationException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CertificationCriterionDTO dto = new CertificationCriterionDTO();
        dto.setAutomatedMeasureCapable(true);
        dto.setAutomatedNumeratorCapable(true);
        dto.setCertificationEditionId(1L);
        dto.setCreationDate(new Date());
        dto.setDeleted(false);
        dto.setDescription("Test");
        dto.setId(null);
        dto.setLastModifiedDate(new Date());
        dto.setLastModifiedUser(-1L);
        dto.setNumber("CERT123");
        // dto.setParentCriterionId(null);
        dto.setRequiresSed(false);
        dto.setTitle("Test Cert Criterion");
        dto.setRemoved(false);

        CertificationCriterionDTO result = certificationCriterionDAO.create(dto);
        CertificationCriterionDTO check = certificationCriterionDAO.getById(result.getId());

        assertEquals(result.getAutomatedMeasureCapable(), check.getAutomatedMeasureCapable());
        assertEquals(result.getAutomatedNumeratorCapable(), check.getAutomatedMeasureCapable());
        assertEquals(result.getCertificationEditionId(), check.getCertificationEditionId());
        assertEquals(result.getCreationDate(), check.getCreationDate());
        assertEquals(result.getDeleted(), check.getDeleted());
        assertEquals(result.getDescription(), check.getDescription());
        assertEquals(result.getId(), check.getId());
        assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
        assertEquals(result.getNumber(), check.getNumber());
        // assertEquals(result.getParentCriterionId(),
        // result.getParentCriterionId());
        assertEquals(result.getRequiresSed(), result.getRequiresSed());
        assertEquals(result.getTitle(), result.getTitle());

        certificationCriterionDAO.delete(result.getId());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Transactional
    public CertificationCriterionDTO create(CertificationCriterionDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        CertificationCriterionDTO result = certificationCriterionDAO.create(dto);
        return result;
    }

    @Transactional
    public CertificationCriterionDTO update(CertificationCriterionDTO dto)
            throws EntityRetrievalException, EntityCreationException {
        CertificationCriterionDTO result = certificationCriterionDAO.update(dto);
        return result;
    }

    @Transactional
    public void delete(Long id) throws EntityRetrievalException, EntityCreationException {
        certificationCriterionDAO.delete(id);
    }

    /**
     * Tests that getAllEntities() gets all non-deleted certification criterion
     * that are associated with a certified product Must have (TestTool.retired
     * = true AND CP.ics_code = true) OR (TestTool.retired = false) AND
     * CertCriterion.deleted = false.
     * @throws EntityRetrievalException
     * @throws EntityCreationException
     */
    @Test
    @Transactional
    public void testGetAllEntities() throws EntityRetrievalException, EntityCreationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        System.out.println("Running getAllEntities() test");
        List<CertificationCriterionDTO> certCritDTOs = certificationCriterionDAO.findAll();
        assertEquals(164, certCritDTOs.size());

        List<Long> certCritIdList = new ArrayList<Long>();
        for (CertificationCriterionDTO dto : certCritDTOs) {
            certCritIdList.add(dto.getId());
            assertFalse(dto.getDeleted());
        }
    }
}

