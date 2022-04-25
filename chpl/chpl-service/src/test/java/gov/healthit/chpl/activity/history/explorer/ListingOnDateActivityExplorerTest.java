package gov.healthit.chpl.activity.history.explorer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.activity.history.query.ListingOnDateActivityQuery;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;

public class ListingOnDateActivityExplorerTest {
    private ActivityDAO activityDao;
    private ListingOnDateActivityExplorer explorer;
    private SimpleDateFormat formatter;
    @Before
    public void setup() {
        formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        activityDao = Mockito.mock(ActivityDAO.class);
        explorer = new ListingOnDateActivityExplorer(activityDao);
    }

    @Test
    public void getActivity_nullActivityForListing_returnsNull() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(null);
        ListingOnDateActivityQuery query = ListingOnDateActivityQuery.builder()
                .listingId(1L)
                .day(LocalDate.parse("2020-01-01"))
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNull(foundActivity);
    }

    @Test
    public void getActivity_emptyActivityForListing_returnsNull() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(new ArrayList<ActivityDTO>());
        ListingOnDateActivityQuery query = ListingOnDateActivityQuery.builder()
                .listingId(1L)
                .day(LocalDate.parse("2020-01-01"))
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNull(foundActivity);
    }

    @Test
    public void getActivity_oneActivityForListingBeforeDate_returnsThatActivity() throws ParseException {
        List<ActivityDTO> activities = new ArrayList<ActivityDTO>();
        activities.add(ActivityDTO.builder()
                .id(1L)
                .activityDate(formatter.parse("2019-12-01"))
                .build());

        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(activities);
        ListingOnDateActivityQuery query = ListingOnDateActivityQuery.builder()
                .listingId(1L)
                .day(LocalDate.parse("2020-01-01"))
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNotNull(foundActivity);
        assertEquals(1L, foundActivity.getId());
    }

    @Test
    public void getActivity_oneActivityForListingAfterDate_returnsNull() throws ParseException {
        List<ActivityDTO> activities = new ArrayList<ActivityDTO>();
        activities.add(ActivityDTO.builder()
                .id(1L)
                .activityDate(formatter.parse("2020-01-02"))
                .build());

        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(activities);
        ListingOnDateActivityQuery query = ListingOnDateActivityQuery.builder()
                .listingId(1L)
                .day(LocalDate.parse("2020-01-01"))
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNull(foundActivity);
    }

    @Test
    public void getActivity_twoActivitiesForListingAfterDate_returnsNull() throws ParseException {
        List<ActivityDTO> activities = new ArrayList<ActivityDTO>();
        activities.add(ActivityDTO.builder()
                .id(1L)
                .activityDate(formatter.parse("2020-01-02"))
                .build());
        activities.add(ActivityDTO.builder()
                .id(2L)
                .activityDate(formatter.parse("2020-02-02"))
                .build());

        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(activities);
        ListingOnDateActivityQuery query = ListingOnDateActivityQuery.builder()
                .listingId(1L)
                .day(LocalDate.parse("2020-01-01"))
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNull(foundActivity);
    }

    @Test
    public void getActivity_twoActivitiesForListingBeforeDate_returnsActivityClosestToDate() throws ParseException {
        List<ActivityDTO> activities = new ArrayList<ActivityDTO>();
        activities.add(ActivityDTO.builder()
                .id(1L)
                .activityDate(formatter.parse("2019-12-01"))
                .build());
        activities.add(ActivityDTO.builder()
                .id(2L)
                .activityDate(formatter.parse("2019-12-02"))
                .build());

        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(activities);
        ListingOnDateActivityQuery query = ListingOnDateActivityQuery.builder()
                .listingId(1L)
                .day(LocalDate.parse("2020-01-01"))
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNotNull(foundActivity);
        assertEquals(2L, foundActivity.getId());
    }

    @Test
    public void getActivity_twoActivitiesForListingBeforeDateOneAfterDate_returnsActivityClosestToDate() throws ParseException {
        List<ActivityDTO> activities = new ArrayList<ActivityDTO>();
        activities.add(ActivityDTO.builder()
                .id(1L)
                .activityDate(formatter.parse("2019-12-01"))
                .build());
        activities.add(ActivityDTO.builder()
                .id(2L)
                .activityDate(formatter.parse("2019-12-02"))
                .build());
        activities.add(ActivityDTO.builder()
                .id(3L)
                .activityDate(formatter.parse("2020-01-02"))
                .build());

        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(activities);
        ListingOnDateActivityQuery query = ListingOnDateActivityQuery.builder()
                .listingId(1L)
                .day(LocalDate.parse("2020-01-01"))
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNotNull(foundActivity);
        assertEquals(2L, foundActivity.getId());
    }
}
