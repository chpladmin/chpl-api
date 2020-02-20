package old.gov.healthit.chpl.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

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
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class CQMCriterionDaoTest {

    @Autowired
    private CQMCriterionDAO cqmCriterionDAO;

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

        CQMCriterionDTO dto = new CQMCriterionDTO();
        dto.setCmsId("CMS123_V1");
        dto.setCqmCriterionTypeId(1L);
        dto.setCqmDomain("123");
        dto.setCqmVersion("1");
        dto.setCqmVersionId(1L);
        dto.setDescription("Description goes here");
        dto.setLastModifiedDate(new Date());
        dto.setLastModifiedUser(-2L);
        dto.setNqfNumber("NQF123");
        dto.setNumber("CMS123");
        dto.setTitle("Test CQM 1");
        dto.setCreationDate(new Date());
        dto.setDeleted(false);
        dto.setLastModifiedDate(new Date());
        dto.setLastModifiedUser(AuthUtil.getAuditId());
        dto.setRetired(false);

        CQMCriterionDTO result = cqmCriterionDAO.create(dto);
        CQMCriterionDTO check = cqmCriterionDAO.getById(result.getId());

        assertEquals(result.getCreationDate(), check.getCreationDate());
        assertEquals(result.getDeleted(), check.getDeleted());
        assertEquals(result.getId(), check.getId());
        assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
        assertEquals(result.getCmsId(), check.getCmsId());
        assertEquals(result.getCqmCriterionTypeId(), check.getCqmCriterionTypeId());
        assertEquals(result.getCqmDomain(), check.getCqmDomain());
        assertEquals(result.getCqmVersion(), check.getCqmVersion());
        assertEquals(result.getCqmVersionId(), check.getCqmVersionId());
        assertEquals(result.getCreationDate(), check.getCreationDate());
        assertEquals(result.getDescription(), check.getDescription());
        assertEquals(result.getNqfNumber(), check.getNqfNumber());
        assertEquals(result.getNumber(), check.getNumber());
        assertEquals(result.getTitle(), check.getTitle());

        cqmCriterionDAO.delete(result.getId());

        SecurityContextHolder.getContext().setAuthentication(null);

    }

    @Test
    @Transactional
    @Rollback
    public void testUpdate() throws EntityCreationException, EntityRetrievalException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CQMCriterionDTO dto = new CQMCriterionDTO();
        dto.setCmsId("CMS123_V1");
        dto.setCqmCriterionTypeId(1L);
        dto.setCqmDomain("123");
        dto.setCqmVersionId(1L);
        dto.setDescription("Description goes here");
        dto.setLastModifiedDate(new Date());
        dto.setLastModifiedUser(-2L);
        dto.setNqfNumber("NQF123");
        dto.setNumber("CMS123");
        dto.setTitle("Test CQM 1");
        dto.setCreationDate(new Date());
        dto.setDeleted(false);
        dto.setLastModifiedDate(new Date());
        dto.setLastModifiedUser(AuthUtil.getAuditId());
        dto.setRetired(false);

        CQMCriterionDTO result = cqmCriterionDAO.create(dto);

        result.setCmsId("CMS123_V2");
        result.setCqmCriterionTypeId(1L);
        result.setCqmDomain("1234");
        result.setCqmVersionId(1L);
        result.setDescription("Description goes here");
        result.setLastModifiedDate(new Date());
        result.setLastModifiedUser(-2L);
        result.setNqfNumber("NQF123");
        result.setNumber("CMS123");
        result.setTitle("Test CQM 1");
        result.setCreationDate(new Date());
        result.setDeleted(false);
        result.setLastModifiedDate(new Date());
        result.setLastModifiedUser(AuthUtil.getAuditId());

        cqmCriterionDAO.update(result);

        CQMCriterionDTO check = cqmCriterionDAO.getById(result.getId());

        assertEquals(result.getDeleted(), check.getDeleted());
        assertEquals(result.getId(), check.getId());
        assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
        assertEquals(result.getCqmDomain(), check.getCqmDomain());
        assertEquals(result.getCmsId(), check.getCmsId());
        assertEquals(result.getCqmCriterionTypeId(), check.getCqmCriterionTypeId());
        assertEquals(result.getCqmVersion(), check.getCqmVersion());
        assertEquals(result.getCqmVersionId(), check.getCqmVersionId());
        assertEquals(result.getCreationDate(), check.getCreationDate());
        assertEquals(result.getDescription(), check.getDescription());
        assertEquals(result.getNqfNumber(), check.getNqfNumber());
        assertEquals(result.getNumber(), check.getNumber());
        assertEquals(result.getTitle(), check.getTitle());

        cqmCriterionDAO.delete(result.getId());

        SecurityContextHolder.getContext().setAuthentication(null);

    }

    @Test
    @Transactional
    @Rollback
    public void testDelete() throws EntityCreationException, EntityRetrievalException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CQMCriterionDTO dto = new CQMCriterionDTO();
        dto.setCmsId("CMS123_V1");
        dto.setCqmCriterionTypeId(1L);
        dto.setCqmDomain("123");
        dto.setCqmVersion("1");
        dto.setCqmVersionId(1L);
        dto.setDescription("Description goes here");
        dto.setLastModifiedDate(new Date());
        dto.setLastModifiedUser(-2L);
        dto.setNqfNumber("NQF123");
        dto.setNumber("CMS123");
        dto.setTitle("Test CQM 1");
        dto.setCreationDate(new Date());
        dto.setDeleted(false);
        dto.setLastModifiedDate(new Date());
        dto.setLastModifiedUser(AuthUtil.getAuditId());
        dto.setRetired(false);

        CQMCriterionDTO result = cqmCriterionDAO.create(dto);
        CQMCriterionDTO check = cqmCriterionDAO.getById(result.getId());

        assertEquals(result.getDeleted(), check.getDeleted());
        assertEquals(result.getId(), check.getId());
        assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
        assertEquals(result.getCmsId(), check.getCmsId());
        assertEquals(result.getCqmCriterionTypeId(), check.getCqmCriterionTypeId());
        assertEquals(result.getCqmDomain(), check.getCqmDomain());
        assertEquals(result.getCqmVersion(), check.getCqmVersion());
        assertEquals(result.getCqmVersionId(), check.getCqmVersionId());
        assertEquals(result.getCreationDate(), check.getCreationDate());
        assertEquals(result.getDescription(), check.getDescription());
        assertEquals(result.getNqfNumber(), check.getNqfNumber());
        assertEquals(result.getNumber(), check.getNumber());
        assertEquals(result.getTitle(), check.getTitle());

        cqmCriterionDAO.delete(result.getId());

        assertNull(cqmCriterionDAO.getById(result.getId()));

        SecurityContextHolder.getContext().setAuthentication(null);

    }

    @Test
    @Transactional
    public void testFindAll() {

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        assertNotNull(cqmCriterionDAO.findAll());

        SecurityContextHolder.getContext().setAuthentication(null);

    }

    @Test
    @Transactional
    public void testGetById() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CQMCriterionDTO dto = new CQMCriterionDTO();
        dto.setCmsId("CMS123_V1");
        dto.setCqmCriterionTypeId(1L);
        dto.setCqmDomain("123");
        dto.setCqmVersion("1");
        dto.setCqmVersionId(1L);
        dto.setDescription("Description goes here");
        dto.setLastModifiedDate(new Date());
        dto.setLastModifiedUser(-2L);
        dto.setNqfNumber("NQF123");
        dto.setNumber("CMS123");
        dto.setTitle("Test CQM 1");
        dto.setCreationDate(new Date());
        dto.setDeleted(false);
        dto.setLastModifiedDate(new Date());
        dto.setLastModifiedUser(AuthUtil.getAuditId());
        dto.setRetired(false);

        CQMCriterionDTO result = cqmCriterionDAO.create(dto);
        CQMCriterionDTO check = cqmCriterionDAO.getById(result.getId());

        assertEquals(result.getDeleted(), check.getDeleted());
        assertEquals(result.getId(), check.getId());
        assertEquals(result.getLastModifiedUser(), check.getLastModifiedUser());
        assertEquals(result.getCmsId(), check.getCmsId());
        assertEquals(result.getCqmCriterionTypeId(), check.getCqmCriterionTypeId());
        assertEquals(result.getCqmDomain(), check.getCqmDomain());
        assertEquals(result.getCqmVersion(), check.getCqmVersion());
        assertEquals(result.getCqmVersionId(), check.getCqmVersionId());
        assertEquals(result.getCreationDate(), check.getCreationDate());
        assertEquals(result.getDescription(), check.getDescription());
        assertEquals(result.getNqfNumber(), check.getNqfNumber());
        assertEquals(result.getNumber(), check.getNumber());
        assertEquals(result.getTitle(), check.getTitle());

        cqmCriterionDAO.delete(result.getId());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

}
