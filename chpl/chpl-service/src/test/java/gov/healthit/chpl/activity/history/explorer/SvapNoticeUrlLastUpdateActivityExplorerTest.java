package gov.healthit.chpl.activity.history.explorer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.query.SvapNoticeUrlLastUpdateActivityQuery;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.JSONUtils;

public class SvapNoticeUrlLastUpdateActivityExplorerTest {
    private ActivityDAO activityDao;
    private ListingActivityUtil listingActivityUtil = new ListingActivityUtil(null, null);
    private SvapNoticeUrlLastUpdateActivityExplorer explorer;
    private SimpleDateFormat formatter;
    @Before
    public void setup() {
        formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        activityDao = Mockito.mock(ActivityDAO.class);
        explorer = new SvapNoticeUrlLastUpdateActivityExplorer(activityDao, listingActivityUtil);
    }

    @Test
    public void getActivityForLastUpdateToSvapNoticeUrl_nullActivityForListing_returnsNull() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(null);
        SvapNoticeUrlLastUpdateActivityQuery query = SvapNoticeUrlLastUpdateActivityQuery.builder()
                .listingId(1L)
                .svapNoticeUrl(null)
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNull(foundActivity);
    }

    @Test
    public void getActivityForLastUpdateToSvapNoticeUrl_emptyActivityForListing_returnsNull() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(new ArrayList<ActivityDTO>());
        SvapNoticeUrlLastUpdateActivityQuery query = SvapNoticeUrlLastUpdateActivityQuery.builder()
                .listingId(1L)
                .svapNoticeUrl(null)
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNull(foundActivity);
    }

    @Test
    public void getActivityForLastUpdateToSvapNoticeUrl_nullCurrentSvapNoticeUrl_returnsNull() throws ParseException, JsonProcessingException {
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
        SvapNoticeUrlLastUpdateActivityQuery query = SvapNoticeUrlLastUpdateActivityQuery.builder()
                .listingId(1L)
                .svapNoticeUrl(null)
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNull(foundActivity);
    }

    @Test
    public void getActivityForLastUpdateToSvapNoticeUrl_emptyCurrentSvapNoticeUrl_returnsNull() throws ParseException, JsonProcessingException {
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
        SvapNoticeUrlLastUpdateActivityQuery query = SvapNoticeUrlLastUpdateActivityQuery.builder()
                .listingId(1L)
                .svapNoticeUrl("")
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNull(foundActivity);
    }

    @Test
    public void getActivityForLastUpdateToSvapNoticeUrl_noActivityWithMatchingSvapNoticeUrl_returnsNull() throws ParseException, JsonProcessingException {
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
        SvapNoticeUrlLastUpdateActivityQuery query = SvapNoticeUrlLastUpdateActivityQuery.builder()
                .listingId(1L)
                .svapNoticeUrl("url2")
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNull(foundActivity);
    }

    @Test
    public void getActivityForLastUpdateToSvapNoticeUrl_confirmActivityWithMatchingSvapNoticeUrl_returnsCorrectDate() throws ParseException, JsonProcessingException {
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
        SvapNoticeUrlLastUpdateActivityQuery query = SvapNoticeUrlLastUpdateActivityQuery.builder()
                .listingId(1L)
                .svapNoticeUrl("url1")
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNotNull(foundActivity);
        assertEquals(1L, foundActivity.getId());
    }

    @Test
    public void getActivityForLastUpdateToSvapNoticeUrl_confirmAndEditActivityWithMatchingSvapNoticeUrl_returnsCorrectDate() throws ParseException, JsonProcessingException {
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
        SvapNoticeUrlLastUpdateActivityQuery query = SvapNoticeUrlLastUpdateActivityQuery.builder()
                .listingId(1L)
                .svapNoticeUrl("url2")
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNotNull(foundActivity);
        assertEquals(2L, foundActivity.getId());
    }

    @Test
    public void getActivityForLastUpdateToSvapNoticeUrl_confirmAndTwoEditActivitiesWithMatchingSvapNoticeUrl_returnsCorrectDate() throws ParseException, JsonProcessingException {
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
        SvapNoticeUrlLastUpdateActivityQuery query = SvapNoticeUrlLastUpdateActivityQuery.builder()
                .listingId(1L)
                .svapNoticeUrl("url2")
                .build();
        ActivityDTO foundActivity = explorer.getActivity(query);
        assertNotNull(foundActivity);
        assertEquals(2L, foundActivity.getId());
    }
}
