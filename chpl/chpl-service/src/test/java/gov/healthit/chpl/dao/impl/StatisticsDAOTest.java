package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

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
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.dao.statistics.DeveloperStatisticsDAO;
import gov.healthit.chpl.dao.statistics.ListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
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
public class StatisticsDAOTest extends TestCase {
    @Autowired
    private DeveloperStatisticsDAO developerStatisticsDao;
    @Autowired
    private ListingStatisticsDAO listingStatisticsDao;
    @Autowired
    private SurveillanceStatisticsDAO surveillanceStatisticsDao;
    @Autowired
    private SurveillanceDAO surveillanceDao;
    @Autowired
    private CertifiedProductSearchResultDAO cpDetailsDao;

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

    /**
     * Test getTotalDeveloperActivity.
     * Given that getTotalDevelopers(DateRange) is called
     * When the start date is set to the beginning of time and endDate is set to now()
     * Then all non-deleted developers are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalDevelopersFilterDateRangeAllDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        Long totalDevelopers = developerStatisticsDao.getTotalDevelopers(dateRange);
        assertNotNull(totalDevelopers);
        assertEquals(9L, totalDevelopers.longValue());
    }

    /**
     * Test of getDevelopers.
     * Given that getTotalDevelopers(DateRange) is called
     * When the start date is set to the beginning of time and endDate is set to now()
     * Then all non-deleted developers are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalDevelopersFilterDateRangeEndDateFiltersResults() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, 0, 1, 0, 0);
        DateRange dateRange = new DateRange(new Date(0), new Date(cal.getTimeInMillis()));
        Long totalDevelopers = developerStatisticsDao.getTotalDevelopers(dateRange);
        assertNotNull(totalDevelopers);
        assertEquals(4L, totalDevelopers.longValue());
    }

    /**
     * Given that getTotalDevelopers(DateRange) is called When the start date is
     * set to the beginning of time and endDate is set to now() Then all
     * non-deleted developers are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalDevelopers_filterDateRange_startDateFiltersResults() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, 0, 1, 0, 0);
        DateRange dateRange = new DateRange(new Date(cal.getTimeInMillis()), new Date());
        Long totalDevelopers = developerStatisticsDao.getTotalDevelopers(dateRange);
        assertNotNull(totalDevelopers);
        assertEquals(9L, totalDevelopers.longValue());
    }

    /**
     * Given that getTotalDevelopers(DateRange) is called Given that a deleted
     * developer exists with a lastModifiedDate in 2017 When the start date is
     * set to the beginning of time and endDate is set to 12/31/2016 Then the
     * deleted developer with lastModifiedDate in 2017 is included in the count
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalDevelopers_filterDateRange_lastModifiedDateFiltersResults() {
        Calendar calEnd2016 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calEnd2016.set(2017, 0, 1, 0, 0);
        Calendar calStart2016 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calStart2016.set(2016, 0, 1, 0, 0);
        DateRange dateRange = new DateRange(new Date(calStart2016.getTimeInMillis()), new Date(
                calEnd2016.getTimeInMillis()));
        Long totalDevelopers = developerStatisticsDao.getTotalDevelopers(dateRange);
        assertNotNull(totalDevelopers);
        assertEquals(7L, totalDevelopers.longValue());
    }

    /**
     * Given that getTotalDevelopersWith2014Listings(DateRange) is called When
     * the start date is set to the beginning of time and endDate is set to
     * now() Then all non-deleted developers with 2014 listings are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalDevelopersWith2014Listings_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        Long totalDevelopers2014Listings = developerStatisticsDao.getTotalDevelopersWithListingsByEditionAndStatus(
                dateRange, "2014", null);
        assertNotNull(totalDevelopers2014Listings);
        assertEquals(1L, totalDevelopers2014Listings.longValue());
    }

    /**
     * Given that getTotalDevelopersWith2015Listings(DateRange) is called When
     * the start date is set to the beginning of time and endDate is set to
     * now() Then all non-deleted developers with 2015 listings are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalDevelopersWith2015Listings_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        Long totalDevelopers2015Listings = developerStatisticsDao.getTotalDevelopersWithListingsByEditionAndStatus(
                dateRange, "2015", null);
        assertNotNull(totalDevelopers2015Listings);
        assertEquals(4L, totalDevelopers2015Listings.longValue());
    }

    /**
     * Given that getTotalCertifiedProducts(DateRange) is called When the start
     * date is set to the beginning of time and endDate is set to now() Then all
     * non-deleted CPs are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalCertifiedProducts_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        Long totalCertifiedProducts = listingStatisticsDao.getTotalUniqueProductsByEditionAndStatus(dateRange, null,
                null);
        assertNotNull(totalCertifiedProducts);
        assertEquals(8L, totalCertifiedProducts.longValue());
    }

    /**
     * Given that getTotalCPsActive2014Listings(DateRange) is called When the
     * start date is set to the beginning of time and endDate is set to now()
     * Then all non-deleted CPs with active 2014 listings are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalCPsActive2014Listings_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        Long totalActive2014Listings = listingStatisticsDao.getTotalUniqueProductsByEditionAndStatus(dateRange, "2014",
                activeStatuses);
        assertNotNull(totalActive2014Listings);
        assertEquals(1L, totalActive2014Listings.longValue());
    }

    /**
     * Given that getTotalCPsActive2015Listings(DateRange) is called When the
     * start date is set to the beginning of time and endDate is set to now()
     * Then all non-deleted CPs with active 2015 listings are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalCPsActive2015Listings_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        Long totalActive2015Listings = listingStatisticsDao.getTotalUniqueProductsByEditionAndStatus(dateRange, "2015",
                activeStatuses);
        assertNotNull(totalActive2015Listings);
        assertEquals(1L, totalActive2015Listings.longValue());
    }

    /**
     * Given that getTotalCPsActiveListings(DateRange) is called When the start
     * date is set to the beginning of time and endDate is set to now() Then all
     * non-deleted CPs with active listings are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalCPsActiveListings_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        Long totalActiveCPs = listingStatisticsDao.getTotalUniqueProductsByEditionAndStatus(dateRange, null,
                activeStatuses);
        assertNotNull(totalActiveCPs);
        assertEquals(3L, totalActiveCPs.longValue());
    }

    /**
     * Given that getTotalListings(DateRange) is called When the start date is
     * set to the beginning of time and endDate is set to now() Then all
     * listings are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalListings_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        Long totalListings = listingStatisticsDao.getTotalListingsByEditionAndStatus(dateRange, null, null);
        assertNotNull(totalListings);
        assertEquals(18L, totalListings.longValue());
    }

    @Test
    @Transactional(readOnly = true)
    public void getTotalListings_Current() {
        Long totalListings = listingStatisticsDao.getTotalListingsByEditionAndStatus(null, null, null);
        assertNotNull(totalListings);
        assertEquals(18L, totalListings.longValue());
    }

    @Test
    @Transactional(readOnly = true)
    public void getTotalListings_filterDates_beforeListingDeleted() {
        Calendar endDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        endDate.set(2016, 7, 19, 0, 0, 0);
        DateRange dateRange = new DateRange(new Date(0), endDate.getTime());
        Long totalListings = listingStatisticsDao.getTotalListingsByEditionAndStatus(dateRange, null, null);
        assertNotNull(totalListings);
        assertEquals(19L, totalListings.longValue());
    }

    @Test
    @Transactional(readOnly = true)
    public void getTotalListings_filterDates_afterListingDeleted() {
        Calendar startDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        startDate.set(2016, 9, 1, 0, 0, 0);
        DateRange dateRange = new DateRange(startDate.getTime(), new Date());
        Long totalListings = listingStatisticsDao.getTotalListingsByEditionAndStatus(dateRange, null, null);
        assertNotNull(totalListings);
        assertEquals(18L, totalListings.longValue());
    }

    /**
     * Given that getTotalActive2014Listings(DateRange) is called When the start
     * date is set to the beginning of time and endDate is set to now() Then all
     * 2014 active listings are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalActive2014Listings_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long totalActive2014Listings = listingStatisticsDao.getTotalListingsByEditionAndStatus(dateRange, "2014",
                activeStatuses);
        assertNotNull(totalActive2014Listings);
        assertEquals(2L, totalActive2014Listings.longValue());
    }

    /**
     * Given that getTotalActiveListingsByCertifiedBody(DateRange) is called,
     * when the start date is set to the beginning of time and endDate is set to
     * now(), then counts for all active listings for each CertifiedBody are
     * returned.
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalActiveListingsByCertifiedBodyFilterDateRangeAllDates() {
        final int expectedListings = 4;
        DateRange dateRange = new DateRange(new Date(0), new Date());
        List<CertifiedBodyStatistics> cbStats = listingStatisticsDao.getTotalActiveListingsByCertifiedBody(dateRange);
        assertNotNull(cbStats);
        assertEquals(expectedListings, cbStats.size());
    }

    /**
     * Given that getTotalActive2015Listings(DateRange) is called When the start
     * date is set to the beginning of time and endDate is set to now() Then all
     * 2015 active listings are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalActive2015Listings_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long totalActive2015Listings = listingStatisticsDao.getTotalListingsByEditionAndStatus(dateRange, "2015",
                activeStatuses);
        assertNotNull(totalActive2015Listings);
        assertEquals(3L, totalActive2015Listings.longValue());
    }

    /**
     * Given that getTotal2014Listings(DateRange) is called When the start date
     * is set to the beginning of time and endDate is set to now() Then all 2014
     * listings are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotal2014Listings_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        Long total2014Listings = listingStatisticsDao.getTotalListingsByEditionAndStatus(dateRange, "2014", null);
        assertNotNull(total2014Listings);
        assertEquals(3L, total2014Listings.longValue());
    }

    /**
     * Given that getTotal2015Listings(DateRange) is called When the start date
     * is set to the beginning of time and endDate is set to now() Then all 2015
     * listings are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotal2015Listings_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        Long total2015Listings = listingStatisticsDao.getTotalListingsByEditionAndStatus(dateRange, "2015", null);
        assertNotNull(total2015Listings);
        assertEquals(12L, total2015Listings.longValue());
    }

    /**
     * Given that getTotal2011Listings(DateRange) is called When the start date
     * is set to the beginning of time and endDate is set to now() Then all 2011
     * listings are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotal2011Listings_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        Long total2011Listings = listingStatisticsDao.getTotalListingsByEditionAndStatus(dateRange, "2011", null);
        assertNotNull(total2011Listings);
        assertEquals(3L, total2011Listings.longValue());
    }

    /**
     * Given that getTotalSurveillanceActivities(DateRange) is called When the
     * start date is set to the beginning of time and endDate is set to now()
     * Then all surveillance activities are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalSurveillanceActivities_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        Long totalSurveillanceActivities = surveillanceStatisticsDao.getTotalSurveillanceActivities(dateRange);
        assertNotNull(totalSurveillanceActivities);
        assertEquals(4L, totalSurveillanceActivities.longValue());
    }

    /**
     * Given that getTotalOpenSurveillanceActivities(DateRange) is called When
     * the start date is set to the beginning of time and endDate is set to
     * now() Then all open surveillance activities are returned
     */
    @Test
    @Transactional(readOnly = true)
    @Rollback
    public void getTotalOpenSurveillanceActivities_filterDateRange_allDates() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Date beginningOfTime = new Date(0);
        Date currentTime = new Date();

        Surveillance surv = new Surveillance();
        surv.setAuthority("ROLE_ADMIN");
        CertifiedProduct cp = null;
        try {
            cp = new CertifiedProduct(cpDetailsDao.getById(1L));
        } catch (EntityRetrievalException e) {
            e.printStackTrace();
        }
        surv.setCertifiedProduct(cp);
        Set<SurveillanceRequirement> reqs = new HashSet<SurveillanceRequirement>();
        SurveillanceRequirement aReq = new SurveillanceRequirement();
        aReq.setId(1L);
        aReq.setRequirement("some req");
        SurveillanceResultType result = new SurveillanceResultType();
        result.setId(2L);
        result.setName("No Non-Conformity");
        // reqs.add
        aReq.setResult(result);
        SurveillanceRequirementType type = new SurveillanceRequirementType();
        type.setId(3L);
        type.setName("Other Requirement");
        aReq.setType(type);
        reqs.add(aReq);
        surv.setRequirements(reqs);
        //start date is one day ago
        surv.setStartDate(new Date(currentTime.getTime() - (1000 * 60 * 60 * 24)));
        surv.setEndDate(null);
        SurveillanceType survType = new SurveillanceType();
        survType.setId(1L);
        survType.setName("Reactive");
        surv.setType(survType);
        Long insertedSurvId = null;
        try {
            insertedSurvId = surveillanceDao.insertSurveillance(surv);
        } catch (UserPermissionRetrievalException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

        try {
            SurveillanceEntity insertedSurv = surveillanceDao.getSurveillanceById(insertedSurvId);
            assertNotNull(insertedSurv);
        } catch (EntityRetrievalException ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        }
        //adding a minute to the searched date range to account
        //for clock difference on ahrq's dev server and dev db server.
        DateRange dateRange = new DateRange(beginningOfTime, new Date(currentTime.getTime() + (1000 * 60)));
        Long totalOpenSurveillanceActivities = surveillanceStatisticsDao.getTotalOpenSurveillanceActivities(dateRange);
        assertNotNull(totalOpenSurveillanceActivities);
        assertEquals(1L, totalOpenSurveillanceActivities.longValue());
    }

    /**
     * Given that getTotalClosedSurveillanceActivities(DateRange) is called When
     * the start date is set to the beginning of time and endDate is set to
     * now() Then all closed surveillance activities are returned.
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalClosedSurveillanceActivities_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        Long totalClosedSurveillanceActivities = surveillanceStatisticsDao
                .getTotalClosedSurveillanceActivities(dateRange);
        assertNotNull(totalClosedSurveillanceActivities);
        assertEquals(4L, totalClosedSurveillanceActivities.longValue());
    }

    /**
     * Given that getTotalOpenNonconformities(DateRange) is called When the
     * start date is set to the beginning of time and endDate is set to now()
     * Then all open surveillance nonconformities are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalOpenNonConformities_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        Long totalOpenSurveillanceNonconformities = surveillanceStatisticsDao.getTotalOpenNonconformities(dateRange);
        assertNotNull(totalOpenSurveillanceNonconformities);
        assertEquals(3L, totalOpenSurveillanceNonconformities.longValue());
    }

    /**
     * Given that getTotalClosedNonconformities(DateRange) is called When the
     * start date is set to the beginning of time and endDate is set to now()
     * Then all closed surveillance nonconformities are returned
     */
    @Test
    @Transactional(readOnly = true)
    public void getTotalClosedNonconformities_filterDateRange_allDates() {
        DateRange dateRange = new DateRange(new Date(0), new Date());
        Long totalClosedSurveillanceNonconformities = surveillanceStatisticsDao
                .getTotalClosedNonconformities(dateRange);
        assertNotNull(totalClosedSurveillanceNonconformities);
        assertEquals(1L, totalClosedSurveillanceNonconformities.longValue());
    }

}
