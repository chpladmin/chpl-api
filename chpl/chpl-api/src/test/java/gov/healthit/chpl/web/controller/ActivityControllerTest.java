package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
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
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.domain.activity.ActivityDetails;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class ActivityControllerTest {
    private static JWTAuthenticatedUser adminUser, acbUser, atlUser;

    @Autowired
    Environment env;
    @Autowired
    ActivityController activityController;

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

        acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setId(3L);
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("testUser3");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));

        atlUser = new JWTAuthenticatedUser();
        atlUser.setFullName("ATL");
        atlUser.setId(3L);
        atlUser.setFriendlyName("User");
        atlUser.setSubjectName("atlUser");
        atlUser.getPermissions().add(new GrantedPermission("ROLE_ATL"));
    }

    /**
     * Tests that listActivity returns results for a Certified Product.
     * @throws IOException
     * @throws ValidationException
     */
    @Transactional
    @Test
    public void test_listActivity()
            throws EntityRetrievalException, EntityCreationException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        // Note: certification_criterion_id="59" has testTool="true", number
        // 170.315 (h)(1) and title "Direct Project"
        Long cpId = 1L; // this CP has ics_code = "1" & associated
                        // certification_result_id = 8 with
                        // certification_criterion_id="59"
        Long cpId2 = 10L; // this CP has ics_code = "0" & associated
                          // certification_result_id = 9 with
                          // certification_criterion_id="59"

        List<ActivityDetails> cpActivityEvents = activityController.activityForCertifiedProductById(cpId, null, null);
        assertEquals(4, cpActivityEvents.size());

        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
        List<ActivityDetails> cpActivityEventsInDates = activityController.activityForCertifiedProductById(cpId,
                start.getTimeInMillis(), end.getTimeInMillis());
        assertEquals(3, cpActivityEventsInDates.size());

        List<ActivityDetails> cp2ActivityEvents = activityController.activityForCertifiedProductById(cpId2, null, null);
        assertEquals(0, cp2ActivityEvents.size());
    }

    /**
     * GIVEN a user is looking at activity WHEN they try to search for more than
     * configurable days (set to 60) THEN they should not be able to make the
     * call.
     * @throws IOException
     * @throws ValidationException
     */
    @Transactional
    @Test(expected = IllegalArgumentException.class)
    public void test_dateValidation_outOfRangeThrowsException()
            throws EntityRetrievalException, EntityCreationException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        activityController.activityForACBs(0L, cal.getTimeInMillis());
    }

    /**
     * GIVEN a user is looking at activity WHEN they try to search for a date
     * range within the configurable days (set to 60) THEN they should be able
     * to make the call.
     * @throws IOException
     * @throws ValidationException
     */
    @Transactional
    @Test
    public void test_dateValidation_insideRangeDoesNotThrowException()
            throws EntityRetrievalException, EntityCreationException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Calendar calEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Integer maxActivityRangeInDays = Integer.getInteger(env.getProperty("maxActivityRangeInDays"),
                ActivityController.DEFAULT_MAX_ACTIVITY_RANGE_DAYS);
        calStart.add(Calendar.DATE, -maxActivityRangeInDays + 1);
        activityController.activityForACBs(calStart.getTimeInMillis(), calEnd.getTimeInMillis());
    }

    /**
     * GIVEN a user is looking at activity WHEN they try to search for a date
     * range equal to the max number of days (set to 60) THEN they should be
     * able to make the call.
     * @throws IOException
     * @throws ValidationException
     */
    @Transactional
    @Test
    public void test_dateValidation_allowsMaxActivityRangeDays()
            throws EntityRetrievalException, EntityCreationException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Calendar calEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Integer maxActivityRangeInDays = Integer.getInteger(env.getProperty("maxActivityRangeInDays"),
                ActivityController.DEFAULT_MAX_ACTIVITY_RANGE_DAYS);
        calStart.add(Calendar.DATE, -maxActivityRangeInDays);
        activityController.activityForACBs(calStart.getTimeInMillis(), calEnd.getTimeInMillis());
    }

    /**
     * GIVEN a user is looking at activity WHEN they try to search for a date
     * range greater than the max number of days (set to 60) THEN they should
     * not be able to make the call.
     * @throws IOException
     * @throws ValidationException
     */
    @Transactional
    @Test(expected = IllegalArgumentException.class)
    public void test_dateValidation_ExceptionForMaxDateRangePlusOneDays()
            throws EntityRetrievalException, EntityCreationException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Calendar calEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calStart.add(Calendar.DATE, (ActivityController.DEFAULT_MAX_ACTIVITY_RANGE_DAYS + 1) * -1);
        activityController.activityForACBs(calStart.getTimeInMillis(), calEnd.getTimeInMillis());
    }

    /**
     * GIVEN a user is looking at activity WHEN they try to search for a date
     * range with the start date > end date THEN they should not be able to make
     * the call.
     * @throws IOException
     * @throws ValidationException
     */
    @Transactional
    @Test(expected = IllegalArgumentException.class)
    public void test_dateValidation_startDateAfterEndDate()
            throws EntityRetrievalException, EntityCreationException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Calendar calEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calStart.add(Calendar.DATE, 20);
        activityController.activityForACBs(calStart.getTimeInMillis(), calEnd.getTimeInMillis());
    }

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void testGetAcbActivityWithBadId() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        activityController.activityForACBById(-100L, null, null);
    }

    @Transactional
    @Test
    public void testGetAcbActivityAsAdmin() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<ActivityDetails> userActivity = activityController.activityForACBById(-1L, null, null);
        assertEquals(0, userActivity.size());
    }

    @Transactional
    @Test
    public void testGetAcbActivityAsAcbUser() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        List<ActivityDetails> userActivity = activityController.activityForACBById(-1L, null, null);
        assertEquals(0, userActivity.size());
    }

    @Transactional
    @Test(expected = AccessDeniedException.class)
    public void testGetAcbActivityAsAtlUser() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(atlUser);
        activityController.activityForACBById(-2L, null, null);
    }

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void testGetAnnouncementActivityWithBadId()
            throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        activityController.activityForAnnouncementById(-100L, null, null);
    }

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void testGetAtlActivityWithBadId() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        activityController.activityForATLById(-100L, null, null);
    }

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void testGetListingActivityWithBadId() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
        activityController.activityForCertifiedProductById(-100L, null, null);
    }

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void testGetDeveloperActivityWithBadId() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
        activityController.activityForDeveloperById(-100L, null, null);
    }

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void testGetPendingListingActivityWithBadId()
            throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
        activityController.activityForPendingCertifiedProductById(-100L, null, null);
    }

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void testGetProductActivityWithBadId() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
        activityController.activityForProducts(-100L, start.getTimeInMillis(), end.getTimeInMillis());
    }

    @Transactional
    @Test(expected = UserRetrievalException.class)
    public void testGetUserActivityWithBadId() throws UserRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
        activityController.activityForUsers(-100L, start.getTimeInMillis(), end.getTimeInMillis());
    }

    @Transactional
    @Test(expected = UserRetrievalException.class)
    public void testGetUserActivitesWithBadId() throws UserRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        activityController.activityByUser(-100L, null, null);
    }

    @Transactional
    @Test
    public void testGetActivityByUsersAsAdmin() throws UserRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<UserActivity> userActivity = activityController.activityByUser(1504224000000L, 1504424000000L);
        assertEquals(0, userActivity.size());
    }

    @Transactional
    @Test(expected = AccessDeniedException.class)
    public void testGetActivityByUsersAsAcbAdmin() throws UserRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        activityController.activityByUser(1504224000000L, 1504424000000L);
    }

    @Transactional
    @Test(expected = AccessDeniedException.class)
    public void testGetActivityByUsersAsAtlAdmin() throws UserRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        activityController.activityByUser(1504224000000L, 1504424000000L);
    }

    @Transactional
    @Test
    public void testGetActivityByUserIdAsAdmin() throws UserRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        List<ActivityDetails> userActivity = activityController.activityByUser(1L, null, null);
        assertEquals(5, userActivity.size());
    }

    @Transactional
    @Test(expected = AccessDeniedException.class)
    public void testGetActivityByUserIdAsAcbAdmin() throws UserRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        List<ActivityDetails> userActivity = activityController.activityByUser(1L, null, null);
        assertEquals(5, userActivity.size());
    }

    @Transactional
    @Test(expected = AccessDeniedException.class)
    public void testGetActivityByUserIdAsAtlAdmin() throws UserRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(atlUser);
        List<ActivityDetails> userActivity = activityController.activityByUser(1L, null, null);
        assertEquals(5, userActivity.size());
    }

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void testGetVersionActivityWithBadId() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
        activityController.activityForVersions(-100L, start.getTimeInMillis(), end.getTimeInMillis());
    }

    @Test
    public void testDateRangeComparisonWorksAcrossDaylightSavings() {
        // two dates where daylight savings falls between them
        Long startDate = 1505620800000L; // 9/17/17
        Long endDate = 1510808400000L; // 11/16/17

        LocalDate startDateUtc = Instant.ofEpochMilli(startDate).atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate endDateUtc = Instant.ofEpochMilli(endDate).atZone(ZoneId.of("UTC")).toLocalDate();

        System.out.println("Start: " + startDateUtc);
        System.out.println("End: " + endDateUtc);
        if (startDateUtc.isAfter(endDateUtc)) {
            fail("End date is not before start date.");
        }

        Integer maxActivityRangeInDays = Integer.getInteger(env.getProperty("maxActivityRangeInDays"), 60);

        endDateUtc = endDateUtc.minusDays(maxActivityRangeInDays);
        if (startDateUtc.isBefore(endDateUtc)) {
            System.out.println("Start: " + startDateUtc);
            System.out.println("End minus " + maxActivityRangeInDays + " days: " + endDateUtc);
            fail("Date range is not more than 60 days apart");
        }

        System.out.println("End minus " + maxActivityRangeInDays + " days: " + endDateUtc);
    }
}
