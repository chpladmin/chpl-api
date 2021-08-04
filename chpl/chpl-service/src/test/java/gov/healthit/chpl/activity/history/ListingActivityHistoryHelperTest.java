package gov.healthit.chpl.activity.history;

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

import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.JSONUtils;

public class ListingActivityHistoryHelperTest {
    private ActivityDAO activityDao;
    private ListingActivityHistoryHelper historyHelper;
    private SimpleDateFormat formatter;
    @Before
    public void setup() {
        formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        activityDao = Mockito.mock(ActivityDAO.class);
        historyHelper = new ListingActivityHistoryHelper(activityDao);
    }

    @Test
    public void getLastUpdateDateForSvapNoticeUrl_nullActivityForListing_returnsNull() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(null);
        LocalDate ld = historyHelper.getLastUpdateDateForSvapNoticeUrl(CertifiedProductSearchDetails.builder().id(1L).build());
        assertNull(ld);
    }

    @Test
    public void getLastUpdateDateForSvapNoticeUrl_emptyActivityForListing_returnsNull() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(new ArrayList<ActivityDTO>());
        LocalDate ld = historyHelper.getLastUpdateDateForSvapNoticeUrl(CertifiedProductSearchDetails.builder().id(1L).build());
        assertNull(ld);
    }

    @Test
    public void getLastUpdateDateForSvapNoticeUrl_nullCurrentSvapNoticeUrl_returnsNull() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .svapNoticeUrl(null)
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
        LocalDate ld = historyHelper.getLastUpdateDateForSvapNoticeUrl(
                CertifiedProductSearchDetails.builder().id(1L).svapNoticeUrl(null).build());
        assertNull(ld);
    }

    @Test
    public void getLastUpdateDateForSvapNoticeUrl_emptyCurrentSvapNoticeUrl_returnsNull() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .svapNoticeUrl("")
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
        LocalDate ld = historyHelper.getLastUpdateDateForSvapNoticeUrl(
                CertifiedProductSearchDetails.builder().id(1L).svapNoticeUrl("").build());
        assertNull(ld);
    }

    @Test
    public void getLastUpdateDateForSvapNoticeUrl_noActivityWithMatchingSvapNoticeUrl_returnsNull() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .svapNoticeUrl("url1")
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
        LocalDate ld = historyHelper.getLastUpdateDateForSvapNoticeUrl(
                CertifiedProductSearchDetails.builder().id(1L).svapNoticeUrl("url2").build());
        assertNull(ld);
    }

    @Test
    public void getLastUpdateDateForSvapNoticeUrl_confirmActivityWithMatchingSvapNoticeUrl_returnsCorrectDate() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .svapNoticeUrl("url1")
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
        LocalDate ld = historyHelper.getLastUpdateDateForSvapNoticeUrl(
                CertifiedProductSearchDetails.builder().id(1L).svapNoticeUrl("url1").build());
        assertNotNull(ld);
        assertEquals(LocalDate.parse("2020-02-01"), ld);
    }

    @Test
    public void getLastUpdateDateForSvapNoticeUrl_confirmAndEditActivityWithMatchingSvapNoticeUrl_returnsCorrectDate() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .svapNoticeUrl("url1")
                .build());
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .svapNoticeUrl("url2")
                .build());
        ActivityDTO confirmActivity = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(null)
            .newData(listingConfirmActivity)
            .build();
        ActivityDTO updateActivity = ActivityDTO.builder()
                .id(2L)
                .activityDate(formatter.parse("02-02-2020 10:00:00 AM"))
                .originalData(listingConfirmActivity)
                .newData(listingUpdateActivity)
                .build();
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(confirmActivity, updateActivity).collect(Collectors.toList()));
        LocalDate ld = historyHelper.getLastUpdateDateForSvapNoticeUrl(
                CertifiedProductSearchDetails.builder().id(1L).svapNoticeUrl("url2").build());
        assertNotNull(ld);
        assertEquals(LocalDate.parse("2020-02-02"), ld);
    }

    @Test
    public void getLastUpdateDateForSvapNoticeUrl_confirmAndTwoEditActivitiesWithMatchingSvapNoticeUrl_returnsCorrectDate() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .svapNoticeUrl("url1")
                .build());
        String listingUpdateActivity1 = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .svapNoticeUrl("url2")
                .build());
        String listingUpdateActivity2 = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(3L)
                .svapNoticeUrl("url2")
                .otherAcb("other ACB")
                .build());
        ActivityDTO confirmActivity = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(null)
            .newData(listingConfirmActivity)
            .build();
        ActivityDTO updateActivity1 = ActivityDTO.builder()
                .id(2L)
                .activityDate(formatter.parse("02-02-2020 10:00:00 AM"))
                .originalData(listingConfirmActivity)
                .newData(listingUpdateActivity1)
                .build();
        ActivityDTO updateActivity2 = ActivityDTO.builder()
                .id(3L)
                .activityDate(formatter.parse("02-03-2020 10:00:00 AM"))
                .originalData(listingUpdateActivity1)
                .newData(listingUpdateActivity2)
                .build();
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(confirmActivity, updateActivity1, updateActivity2).collect(Collectors.toList()));
        LocalDate ld = historyHelper.getLastUpdateDateForSvapNoticeUrl(
                CertifiedProductSearchDetails.builder().id(1L).svapNoticeUrl("url2").build());
        assertNotNull(ld);
        assertEquals(LocalDate.parse("2020-02-02"), ld);
    }

    @Test
    public void getListingOnDate_nullActivityForListing_returnsNull() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(null);
        CertifiedProductSearchDetails listingOnDate = historyHelper.getListingOnDate(1L, new Date());
        assertNull(listingOnDate);
    }

    @Test
    public void getListingOnDate_emptyActivityForListing_returnsNull() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(new ArrayList<ActivityDTO>());
        CertifiedProductSearchDetails listingOnDate = historyHelper.getListingOnDate(1L, new Date());
        assertNull(listingOnDate);
    }

    @Test
    public void getListingOnDate_listingConfirmedAfterRequestedDate_returnsNull() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder().id(2L).build());
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
        CertifiedProductSearchDetails listingOnDate = historyHelper.getListingOnDate(1L, formatter.parse("01-01-2020 10:00:00 AM"));
        assertNull(listingOnDate);
    }

    @Test
    public void getListingOnDate_listingConfirmedShortlyAfterRequestedDate_returnsNull() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder().id(2L).build());
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
        CertifiedProductSearchDetails listingOnDate = historyHelper.getListingOnDate(1L, formatter.parse("02-01-2020 09:59:59 AM"));
        assertNull(listingOnDate);
    }

    @Test
    public void getListingOnDate_listingConfirmedBeforeRequestedDate_noOtherActivity_returnsListing() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder().id(2L).build());
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
        CertifiedProductSearchDetails listingOnDate = historyHelper.getListingOnDate(1L, formatter.parse("03-01-2020 10:00:00 AM"));
        assertNotNull(listingOnDate);
        assertEquals(2L, listingOnDate.getId());
    }

    @Test
    public void getListingOnDate_listingConfirmedShortlyBeforeRequestedDate_noOtherActivity_returnsListing() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder().id(2L).build());
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
        CertifiedProductSearchDetails listingOnDate = historyHelper.getListingOnDate(1L, formatter.parse("02-01-2020 10:00:01 AM"));
        assertNotNull(listingOnDate);
        assertEquals(2L, listingOnDate.getId());
    }

    @Test
    public void getListingOnDate_multipleActivities_requestListingBeforeConfirm_returnsNull() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder().id(2L).build());
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder().id(2L).chplProductNumber("1234").build());

        ActivityDTO activity1 = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(null)
            .newData(listingConfirmActivity)
            .build();
        ActivityDTO activity2 = ActivityDTO.builder()
                .id(1L)
                .activityDate(formatter.parse("02-03-2020 10:00:00 AM"))
                .originalData(listingConfirmActivity)
                .newData(listingUpdateActivity)
                .build();

        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(activity1, activity2).collect(Collectors.toList()));
        CertifiedProductSearchDetails listingOnDate = historyHelper.getListingOnDate(1L, formatter.parse("01-01-2020 10:00:00 AM"));
        assertNull(listingOnDate);
    }

    @Test
    public void getListingOnDate_multipleActivities_requestListingAfterFirstAcivity_returnsCorrectListing() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder().id(2L).build());
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder().id(2L).chplProductNumber("1234").build());

        ActivityDTO activity1 = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(null)
            .newData(listingConfirmActivity)
            .build();
        ActivityDTO activity2 = ActivityDTO.builder()
                .id(1L)
                .activityDate(formatter.parse("02-03-2020 10:00:00 AM"))
                .originalData(listingConfirmActivity)
                .newData(listingUpdateActivity)
                .build();

        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(activity1, activity2).collect(Collectors.toList()));
        CertifiedProductSearchDetails listingOnDate = historyHelper.getListingOnDate(1L, formatter.parse("02-02-2020 10:00:00 AM"));
        assertNotNull(listingOnDate);
        assertEquals(2L, listingOnDate.getId());
        assertNull(listingOnDate.getChplProductNumber());
    }

    @Test
    public void getListingOnDate_multipleActivities_requestListingAfterSecondAcivity_returnsCorrectListing() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder().id(2L).build());
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder().id(2L).chplProductNumber("1234").build());

        ActivityDTO activity1 = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(null)
            .newData(listingConfirmActivity)
            .build();
        ActivityDTO activity2 = ActivityDTO.builder()
                .id(1L)
                .activityDate(formatter.parse("02-03-2020 10:00:00 AM"))
                .originalData(listingConfirmActivity)
                .newData(listingUpdateActivity)
                .build();

        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(activity1, activity2).collect(Collectors.toList()));
        CertifiedProductSearchDetails listingOnDate = historyHelper.getListingOnDate(1L, formatter.parse("02-04-2020 10:00:00 AM"));
        assertNotNull(listingOnDate);
        assertEquals(2L, listingOnDate.getId());
        assertEquals("1234", listingOnDate.getChplProductNumber());
    }

    @Test
    public void getListingOnDate_multipleActivitiesOutOfOrder_requestListingAfterSecondAcivity_returnsCorrectListing() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder().id(2L).build());
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder().id(2L).chplProductNumber("1234").build());

        ActivityDTO activity1 = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(null)
            .newData(listingConfirmActivity)
            .build();
        ActivityDTO activity2 = ActivityDTO.builder()
                .id(1L)
                .activityDate(formatter.parse("02-03-2020 10:00:00 AM"))
                .originalData(listingConfirmActivity)
                .newData(listingUpdateActivity)
                .build();

        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(activity2, activity1).collect(Collectors.toList()));
        CertifiedProductSearchDetails listingOnDate = historyHelper.getListingOnDate(1L, formatter.parse("02-04-2020 10:00:00 AM"));
        assertNotNull(listingOnDate);
        assertEquals(2L, listingOnDate.getId());
        assertEquals("1234", listingOnDate.getChplProductNumber());
    }
}
