package gov.healthit.chpl.listener;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.UnitTestUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityVersionDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ProductVersionManager;
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
public class VersionTest extends TestCase {

    @Autowired
    private QuestionableActivityDAO qaDao;
    @Autowired
    private ProductVersionManager versionManager;

    private static JWTAuthenticatedUser adminUser;

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

    @Test
    @Transactional
    @Rollback(true)
    public void testUpdateVersion() throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        ProductVersionDTO version = versionManager.getById(1L);
        version.setVersion("NEW VERSION NAME");
        versionManager.update(version);
        Date afterActivity = new Date();

        List<QuestionableActivityVersionDTO> activities = qaDao.findVersionActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        QuestionableActivityVersionDTO activity = activities.get(0);
        assertEquals(1, activity.getVersionId().longValue());
        assertEquals("1.0.0", activity.getBefore());
        assertEquals("NEW VERSION NAME", activity.getAfter());
        assertEquals(QuestionableActivityTriggerConcept.VERSION_NAME_EDITED.getName(), activity.getTrigger().getName());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testUpdateNothing() throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        ProductVersionDTO version = versionManager.getById(1L);
        versionManager.update(version);
        Date afterActivity = new Date();

        List<QuestionableActivityVersionDTO> activities = qaDao.findVersionActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(0, activities.size());

        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
