package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityDetails;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.util.JSONUtils;
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
public class ActivityManagerTest extends TestCase {
    @Autowired
    private ActivityManager activityManager;

    private static JWTAuthenticatedUser adminUser, acbUser, atlUser;

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

        atlUser = new JWTAuthenticatedUser();
        atlUser.setFullName("ATL");
        atlUser.setId(4L);
        atlUser.setFriendlyName("User");
        atlUser.setSubjectName("atlUser");
        atlUser.getPermissions().add(new GrantedPermission("ROLE_ATL"));
    }

    @Test
    @Transactional
    @Rollback
    public void testAddActivity() throws EntityCreationException, EntityRetrievalException, IOException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Date startDate = new Date();
        DeveloperDTO developer = new DeveloperDTO();
        developer.setCreationDate(new Date());
        developer.setId(-1L);
        developer.setName("Test");
        developer.setWebsite("www.zombo.com");

        activityManager.addActivity(ActivityConcept.DEVELOPER, developer.getId(), "Test Activity",
                null, developer);
        Date endDate = new Date();
        List<ActivityDetails> events = activityManager.getActivityForObject(ActivityConcept.DEVELOPER,
                -1L, startDate, endDate);

        ActivityDetails event = events.get(events.size() - 1);

        assertEquals(event.getConcept(), ActivityConcept.DEVELOPER);
        assertEquals(event.getOriginalData(), null);
        assertEquals(event.getNewData().toString(), JSONUtils.toJSON(developer));
        assertEquals(event.getActivityObjectId(), developer.getId());
    }

    @Test
    @Transactional
    @Rollback
    public void testAddActivityWithTimeStamp() throws EntityCreationException, EntityRetrievalException, IOException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        DeveloperDTO developer = new DeveloperDTO();
        developer.setCreationDate(new Date());
        developer.setId(-1L);
        developer.setName("Test");
        developer.setWebsite("www.zombo.com");

        Date timestamp = new Date();

        activityManager.addActivity(ActivityConcept.DEVELOPER, developer.getId(), "Test Activity",
                null, developer, timestamp);
        Date endDate = new Date();
        List<ActivityDetails> events = activityManager.getActivityForObject(ActivityConcept.DEVELOPER,
                -1L, timestamp, endDate);

        ActivityDetails event = events.get(events.size() - 1);

        assertEquals(ActivityConcept.DEVELOPER, event.getConcept());
        assertEquals(null, event.getOriginalData());
        assertEquals(JSONUtils.toJSON(developer), event.getNewData().toString());
        assertEquals(developer.getId(), event.getActivityObjectId());
    }

    @Test
    @Transactional
    public void testGetActivityForListingLoggedIn() throws JsonParseException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        ActivityConcept concept = ActivityConcept.CERTIFIED_PRODUCT;
        Long objectId = 1L;
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);

        List<ActivityDetails> events = activityManager.getActivityForObject(concept, objectId, start.getTime(),
                end.getTime());
        assertEquals(3, events.size());

        for (ActivityDetails event : events) {
            assertEquals(concept, event.getConcept());
            assertEquals(objectId, event.getActivityObjectId());
        }

    }

    @Test
    @Transactional
    public void testGetActivityForAcbsAsAdmin() throws JsonParseException, IOException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);
        List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();
        acbs.add(acb);
        List<ActivityDetails> events = activityManager.getAcbActivity(acbs, start.getTime(), end.getTime());
        assertEquals(0, events.size());
    }

    @Test
    @Transactional
    public void testGetActivityForAcbsAsAcbAdmin() throws JsonParseException, IOException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);
        List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();
        acbs.add(acb);
        List<ActivityDetails> events = activityManager.getAcbActivity(acbs, start.getTime(), end.getTime());
        assertEquals(0, events.size());
    }

    @Test(expected = AccessDeniedException.class)
    @Transactional
    public void testGetActivityForAcbsAsAtlAdmin() throws JsonParseException, IOException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(atlUser);
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);
        List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();
        acbs.add(acb);
        activityManager.getAcbActivity(acbs, start.getTime(), end.getTime());
    }

    @Test
    @Transactional
    @Rollback
    public void testGetActivityForObjectDateRange()
            throws EntityCreationException, EntityRetrievalException, IOException {

        Date fiveDaysAgo = new Date(System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000));

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        DeveloperDTO developer = new DeveloperDTO();
        developer.setCreationDate(new Date());
        developer.setId(1L);
        developer.setName("Test");
        developer.setWebsite("www.zombo.com");

        Date timestamp = new Date();

        activityManager.addActivity(ActivityConcept.DEVELOPER, developer.getId(), "Test Activity",
                "Before", "Test", timestamp);
        List<ActivityDetails> events = activityManager.getActivityForObject(ActivityConcept.DEVELOPER,
                developer.getId(), fiveDaysAgo, null);

        assertEquals(1, events.size());
    }

    @Test
    @Transactional
    public void testGetActivityForConceptLastNDays()
            throws EntityCreationException, EntityRetrievalException, JsonParseException, IOException {

        ActivityConcept concept = ActivityConcept.CERTIFIED_PRODUCT;
        Date fiveDaysAgo = new Date(System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000));

        List<ActivityDetails> events = activityManager.getActivityForConcept(concept, fiveDaysAgo, null);
        assertEquals(0, events.size());

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        DeveloperDTO developer = new DeveloperDTO();
        developer.setCreationDate(new Date());
        developer.setId(1L);
        developer.setName("Test");
        developer.setWebsite("www.zombo.com");

        Date timestamp = new Date();

        activityManager.addActivity(ActivityConcept.DEVELOPER, developer.getId(), "Test Activity",
                "Before", "Test", timestamp);

        DeveloperDTO developer2 = new DeveloperDTO();
        developer2.setCreationDate(new Date());
        developer2.setId(2L);
        developer2.setName("Test");
        developer2.setWebsite("www.zombo.com");

        Date timestamp2 = new Date(100);

        activityManager.addActivity(ActivityConcept.DEVELOPER, developer2.getId(), "Test Activity",
                "Before", "Test", timestamp2);

        List<ActivityDetails> events2 = activityManager.getActivityForConcept(ActivityConcept.DEVELOPER,
                fiveDaysAgo, new Date());

        assertEquals(1, events2.size());
    }

    @Test
    @Transactional
    public void testGetActivityForUserInDateRange() throws JsonParseException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<ActivityDetails> eventsForUser = activityManager.getActivityForUserInDateRange(1L, new Date(0), new Date());
        assertEquals(5, eventsForUser.size());
    }

    @Test(expected = AccessDeniedException.class)
    @Transactional
    public void testGetActivityForUserInDateRangeAcbAdmin() throws JsonParseException, IOException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        List<ActivityDetails> eventsForUser = activityManager.getActivityForUserInDateRange(1L, new Date(0), new Date());
        assertEquals(5, eventsForUser.size());
    }

    @Test(expected = AccessDeniedException.class)
    @Transactional
    public void testGetActivityForUserInDateRangeAtlAdmin() throws JsonParseException, IOException {
        SecurityContextHolder.getContext().setAuthentication(atlUser);
        List<ActivityDetails> eventsForUser = activityManager.getActivityForUserInDateRange(1L, new Date(0), new Date());
        assertEquals(5, eventsForUser.size());
    }

    /**
     * Given the API call is made for
     * /activity/user_activities?start=milliValue?end=milliValue When an
     * activity exists for a valid user Then the authenticated user's activity
     * is returned.
     * 
     * @throws JsonParseException
     * @throws IOException
     * @throws UserRetrievalException
     */
    @Test
    @Transactional
    public void testFindAllByUserInDateRange_returnsResult()
            throws JsonParseException, IOException, UserRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Date startDate = new Date(1489550400000L); // 2017-03-15
        Date endDate = new Date(1489723200000L); // 2017-03-17
        // Create a user activity for a user that does not exist
        List<UserActivity> eventsForUser = activityManager.getActivityByUserInDateRange(startDate, endDate);

        List<UserActivity> forUser = new ArrayList<UserActivity>();

        for (UserActivity activity : eventsForUser) {
            if (activity.getUser().getUserId().equals(-2L)) {
                forUser.add(activity);
            }
        }
        assertEquals(1, forUser.get(0).getEvents().size());
    }

}
