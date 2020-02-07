package old.gov.healthit.chpl.manager.impl;

import java.util.Date;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductVersionManager;
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
public class VersionManagerTest extends TestCase {

    @Autowired
    private ProductVersionManager versionManager;
    @Autowired
    private DeveloperManager developerManager;
    @Autowired
    private DeveloperStatusDAO devStatusDao;

    @Autowired
    private FF4j ff4j;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser;

    private static JWTAuthenticatedUser acbUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("ACB User");
        acbUser.setId(-1L);
        acbUser.setFriendlyName("ACB User");
        acbUser.setSubjectName("acb");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.doReturn(true).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK);
    }

    @Test
    @Transactional
    @Rollback
    public void testAllowedToUpdateVersionWithActiveDeveloper()
            throws EntityRetrievalException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        ProductVersionDTO version = versionManager.getById(-1L);
        assertNotNull(version);
        version.setVersion("new version name");
        boolean failed = false;
        try {
            version = versionManager.update(version);
        } catch (EntityCreationException ex) {
            System.out.println(ex.getMessage());
            failed = true;
        }
        assertFalse(failed);
        assertEquals("new version name", version.getVersion());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testAllowedToUpdateVersionWithInactiveDeveloper() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, MissingReasonException, ValidationException {
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

        // try to update version
        ProductVersionDTO version = versionManager.getById(-1L);
        assertNotNull(version);
        version.setVersion("new version name");
        failed = false;
        version = versionManager.update(version);
        assertTrue(version != null);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AccessDeniedException.class)
    @Transactional
    @Rollback
    public void testAcbNotAllowedToUpdateVersionWithInactiveDeveloper() throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, MissingReasonException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);

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

        // try to update version
        ProductVersionDTO version = versionManager.getById(-1L);
        assertNotNull(version);
        version.setVersion("new version name");
        failed = false;
        try {
            version = versionManager.update(version);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

}
