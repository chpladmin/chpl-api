package gov.healthit.chpl.activity.history.explorer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.activity.history.query.RealWorldTestingEligibilityQuery;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.JSONUtils;

public class RealWorldTestingEligibilityActivityExplorerTest {
    private ActivityDAO activityDao;
    private RealWorldTestingEligibilityActivityExplorer explorer;
    private SimpleDateFormat formatter;
    @Before
    public void setup() {
        formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        activityDao = Mockito.mock(ActivityDAO.class);
        explorer = new RealWorldTestingEligibilityActivityExplorer(activityDao);
    }

    @Test
    public void getActivityForRwtEligibility_nullAsOfDateAndNullActivityForListing_returnsNull() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(null);
        RealWorldTestingEligibilityQuery query = RealWorldTestingEligibilityQuery.builder()
                .listingId(1L)
                .asOfDate(null)
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNull(foundActivity);
    }

    @Test
    public void getActivityForRwtEligibility_nullAsOfDateAndEmptyActivityForListing_returnsNull() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(new ArrayList<ActivityDTO>());
        RealWorldTestingEligibilityQuery query = RealWorldTestingEligibilityQuery.builder()
                .listingId(1L)
                .asOfDate(null)
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNull(foundActivity);
    }

    @Test
    public void getActivityForRwtEligibility_nullAsOfDateAndOneActivityForListing_returnsOldestActivity()
            throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .build());
        ActivityDTO activity = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(null)
            .newData(listingConfirmActivity)
            .build();
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(activity).collect(Collectors.toList()));
        RealWorldTestingEligibilityQuery query = RealWorldTestingEligibilityQuery.builder()
                .listingId(1L)
                .asOfDate(null)
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNotNull(foundActivity);
        assertEquals(1L, foundActivity.getId());
    }

    @Test
    public void getActivityForRwtEligibility_nullAsOfDateAndTwoActivitiesForListing_returnsOldestActivity()
            throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .build());
        ActivityDTO confirmActivity = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(null)
            .newData(listingConfirmActivity)
            .build();
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .rwtPlansUrl("test")
                .build());
        ActivityDTO updateActivity = ActivityDTO.builder()
            .id(2L)
            .activityDate(formatter.parse("02-02-2020 10:00:00 AM"))
            .originalData(listingConfirmActivity)
            .newData(listingUpdateActivity)
            .build();
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(updateActivity, confirmActivity).collect(Collectors.toList()));
        RealWorldTestingEligibilityQuery query = RealWorldTestingEligibilityQuery.builder()
                .listingId(1L)
                .asOfDate(null)
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNotNull(foundActivity);
        assertEquals(1L, foundActivity.getId());
    }

    @Test
    public void getActivityForRwtEligibility_AsOfDateBeforeOneActivityForListing_returnsNull()
            throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .build());
        ActivityDTO confirmActivity = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(null)
            .newData(listingConfirmActivity)
            .build();
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(confirmActivity).collect(Collectors.toList()));
        RealWorldTestingEligibilityQuery query = RealWorldTestingEligibilityQuery.builder()
                .listingId(1L)
                .asOfDate(LocalDate.parse("2020-01-31"))
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNull(foundActivity);
    }

    @Test
    public void getActivityForRwtEligibility_AsOfDateBeforeTwoActivitiesForListing_returnsNull()
            throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .build());
        ActivityDTO confirmActivity = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(null)
            .newData(listingConfirmActivity)
            .build();
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .rwtPlansUrl("test")
                .build());
        ActivityDTO updateActivity = ActivityDTO.builder()
            .id(2L)
            .activityDate(formatter.parse("02-03-2020 10:00:00 AM"))
            .originalData(listingConfirmActivity)
            .newData(listingUpdateActivity)
            .build();
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(updateActivity, confirmActivity).collect(Collectors.toList()));
        RealWorldTestingEligibilityQuery query = RealWorldTestingEligibilityQuery.builder()
                .listingId(1L)
                .asOfDate(LocalDate.parse("2020-01-31"))
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNull(foundActivity);
    }

    @Test
    public void getActivityForRwtEligibility_AsOfDateAfterOneActivityForListing_returnsActivity()
            throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .build());
        ActivityDTO confirmActivity = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(null)
            .newData(listingConfirmActivity)
            .build();
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(confirmActivity).collect(Collectors.toList()));
        RealWorldTestingEligibilityQuery query = RealWorldTestingEligibilityQuery.builder()
                .listingId(1L)
                .asOfDate(LocalDate.parse("2020-02-05"))
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNotNull(foundActivity);
        assertEquals(1L, foundActivity.getId());
    }

    @Test
    public void getActivityForRwtEligibility_AsOfDateBetweenTwoActivitiesForListing_returnsEarlierActivity()
            throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .build());
        ActivityDTO confirmActivity = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(null)
            .newData(listingConfirmActivity)
            .build();
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .rwtPlansUrl("test")
                .build());
        ActivityDTO updateActivity = ActivityDTO.builder()
            .id(2L)
            .activityDate(formatter.parse("02-03-2020 10:00:00 AM"))
            .originalData(listingConfirmActivity)
            .newData(listingUpdateActivity)
            .build();
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(updateActivity, confirmActivity).collect(Collectors.toList()));
        RealWorldTestingEligibilityQuery query = RealWorldTestingEligibilityQuery.builder()
                .listingId(1L)
                .asOfDate(LocalDate.parse("2020-02-02"))
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNotNull(foundActivity);
        assertEquals(1L, foundActivity.getId());
    }

    @Test
    public void getActivityForRwtEligibility_AsOfDateAfterTwoActivitiesForListing_returnsLatestActivity()
            throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .build());
        ActivityDTO confirmActivity = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(null)
            .newData(listingConfirmActivity)
            .build();
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .rwtPlansUrl("test")
                .build());
        ActivityDTO updateActivity = ActivityDTO.builder()
            .id(2L)
            .activityDate(formatter.parse("02-03-2020 10:00:00 AM"))
            .originalData(listingConfirmActivity)
            .newData(listingUpdateActivity)
            .build();
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(updateActivity, confirmActivity).collect(Collectors.toList()));
        RealWorldTestingEligibilityQuery query = RealWorldTestingEligibilityQuery.builder()
                .listingId(1L)
                .asOfDate(LocalDate.parse("2020-02-04"))
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNotNull(foundActivity);
        assertEquals(2L, foundActivity.getId());
    }
}
