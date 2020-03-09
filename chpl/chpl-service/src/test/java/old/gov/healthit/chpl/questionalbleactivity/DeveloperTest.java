package old.gov.healthit.chpl.questionalbleactivity;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
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
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DeveloperManager;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class DeveloperTest extends TestCase {

    @Autowired
    private QuestionableActivityDAO qaDao;
    @Autowired
    private DeveloperManager devManager;
    private static JWTAuthenticatedUser adminUser;

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
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateName() throws EntityCreationException, EntityRetrievalException,
            JsonProcessingException, MissingReasonException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        DeveloperDTO developer = devManager.getById(-1L);
        developer.setName("NEW DEVELOPER NAME");
        devManager.update(developer, false);
        Date afterActivity = new Date();

        List<QuestionableActivityDeveloperDTO> activities = qaDao.findDeveloperActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        QuestionableActivityDeveloperDTO activity = activities.get(0);
        assertEquals(-1, activity.getDeveloperId().longValue());
        assertEquals("Test Developer 1", activity.getBefore());
        assertEquals("NEW DEVELOPER NAME", activity.getAfter());
        assertEquals(QuestionableActivityTriggerConcept.DEVELOPER_NAME_EDITED.getName(),
                activity.getTrigger().getName());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateCurrentStatus() throws EntityCreationException, EntityRetrievalException,
            JsonProcessingException, MissingReasonException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        DeveloperDTO developer = devManager.getById(-1L);
        DeveloperStatusEventDTO newCurrStatus = new DeveloperStatusEventDTO();
        newCurrStatus.setDeveloperId(developer.getId());
        Calendar statusDate = new GregorianCalendar(2017, 9, 1);
        newCurrStatus.setStatusDate(statusDate.getTime());
        DeveloperStatusDTO status = new DeveloperStatusDTO();
        status.setId(2L);
        status.setStatusName(DeveloperStatusType.SuspendedByOnc.toString());
        newCurrStatus.setStatus(status);
        developer.getStatusEvents().add(newCurrStatus);
        devManager.update(developer, false);
        Date afterActivity = new Date();

        List<QuestionableActivityDeveloperDTO> activities = qaDao.findDeveloperActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        int numFound = 0;
        for (QuestionableActivityDeveloperDTO activity : activities) {
            if (activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED.getName())) {
                numFound++;
                assertEquals(-1, activity.getDeveloperId().longValue());
                assertEquals(DeveloperStatusType.Active.toString(), activity.getBefore());
                assertEquals(DeveloperStatusType.SuspendedByOnc.toString(), activity.getAfter());
            } else if (activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_ADDED.getName())) {
                numFound++;
                assertEquals(-1, activity.getDeveloperId().longValue());
                assertNull(activity.getBefore());
                assertEquals("Suspended by ONC (2017-10-01)", activity.getAfter());
            }
        }
        assertEquals(2, numFound);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testRemoveStatusHistory() throws EntityCreationException, EntityRetrievalException,
            JsonProcessingException, MissingReasonException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        DeveloperDTO developer = devManager.getById(-3L);
        Iterator<DeveloperStatusEventDTO> iter = developer.getStatusEvents().iterator();
        while (iter.hasNext()) {
            DeveloperStatusEventDTO status = iter.next();
            if (status.getStatus().getStatusName().equals(
                    DeveloperStatusType.SuspendedByOnc.toString())) {
                iter.remove();
            }
        }
        devManager.update(developer, false);
        Date afterActivity = new Date();

        List<QuestionableActivityDeveloperDTO> activities = qaDao.findDeveloperActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        int numFound = 0;
        for (QuestionableActivityDeveloperDTO activity : activities) {
            if (activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED.getName())) {
                numFound++;
                assertEquals(-3, activity.getDeveloperId().longValue());
                assertEquals(DeveloperStatusType.SuspendedByOnc.toString(), activity.getBefore());
                assertEquals(DeveloperStatusType.Active.toString(), activity.getAfter());
            } else if (activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED.getName())) {
                numFound++;
                assertEquals(-3, activity.getDeveloperId().longValue());
                assertEquals("Suspended by ONC (2015-08-21)", activity.getBefore());
                assertNull(activity.getAfter());
            }
        }
        assertEquals(2, numFound);
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
