package gov.healthit.chpl.activity.history.explorer;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import gov.healthit.chpl.activity.history.query.ListingActivityQuery;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.JSONUtils;

public class ChplProductNumberChangedActivityExplorerTest {
    private ActivityDAO activityDao;
    private ListingActivityUtil listingActivityUtil = new ListingActivityUtil(null, null);
    private ChplProductNumberChangedActivityExplorer explorer;
    private SimpleDateFormat formatter;
    @Before
    public void setup() {
        formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        activityDao = Mockito.mock(ActivityDAO.class);
        explorer = new ChplProductNumberChangedActivityExplorer(activityDao, listingActivityUtil);
    }

    @Test
    public void getActivities_nullActivityForListing_returnsEmptyList() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(null);
        ListingActivityQuery query = new ListingActivityQuery(1L);
        List<ActivityDTO> foundActivities = explorer.getActivities(query);
        assertNotNull(foundActivities);
        assertEquals(0, foundActivities.size());
    }

    @Test
    public void getActivities_emptyActivityForListing_returnsEmptyList() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(new ArrayList<ActivityDTO>());
        ListingActivityQuery query = new ListingActivityQuery(1L);
        List<ActivityDTO> foundActivities = explorer.getActivities(query);
        assertNotNull(foundActivities);
        assertEquals(0, foundActivities.size());
    }

    @Test
    public void getActivities_oneActivityNoOriginalData_returnsEmptyList() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("test")
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
        ListingActivityQuery query = new ListingActivityQuery(2L);
        List<ActivityDTO> foundActivities = explorer.getActivities(query);
        assertNotNull(foundActivities);
        assertEquals(0, foundActivities.size());
    }

    @Test
    public void getActivities_oneActivityNoNewData_returnsEmptyList() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("test")
                .build());
        ActivityDTO activity = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(listingConfirmActivity)
            .newData(null)
            .build();
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(activity).collect(Collectors.toList()));
        ListingActivityQuery query = new ListingActivityQuery(2L);
        List<ActivityDTO> foundActivities = explorer.getActivities(query);
        assertNotNull(foundActivities);
        assertEquals(0, foundActivities.size());
    }

    @Test
    public void getActivities_oneActivityNoChplNumberChange_returnsEmptyList() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("test")
                .build());
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("test")
                .reportFileLocation("test1")
                .build());
        ActivityDTO activity = ActivityDTO.builder()
            .id(1L)
            .activityDate(formatter.parse("02-01-2020 10:00:00 AM"))
            .originalData(listingConfirmActivity)
            .newData(listingUpdateActivity)
            .build();
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(activity).collect(Collectors.toList()));
        ListingActivityQuery query = new ListingActivityQuery(2L);
        List<ActivityDTO> foundActivities = explorer.getActivities(query);
        assertNotNull(foundActivities);
        assertEquals(0, foundActivities.size());
    }

    @Test
    public void getActivities_twoActivitiesNoChplNumberChange_returnsEmptyList() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("test")
                .build());
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("test")
                .reportFileLocation("test1")
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
        ListingActivityQuery query = new ListingActivityQuery(2L);
        List<ActivityDTO> foundActivities = explorer.getActivities(query);
        assertNotNull(foundActivities);
        assertEquals(0, foundActivities.size());
    }

    @Test
    public void getActivities_twoActivitiesOneHasChplNumberChange_returnsActivity() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("test")
                .build());
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("test2")
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
        ListingActivityQuery query = new ListingActivityQuery(2L);
        List<ActivityDTO> foundActivities = explorer.getActivities(query);
        assertNotNull(foundActivities);
        assertEquals(1, foundActivities.size());
        assertEquals(2L, foundActivities.get(0).getId());
    }

    @Test
    public void getActivities_threeActivitiesTwoHaveChplNumberChange_returnsActivities() throws ParseException, JsonProcessingException {
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("test")
                .build());
        String listingUpdateActivity1 = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("test2")
                .build());
        String listingUpdateActivity2 = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("test3")
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
                .activityDate(formatter.parse("02-02-2020 10:00:00 AM"))
                .originalData(listingUpdateActivity1)
                .newData(listingUpdateActivity2)
                .build();
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(confirmActivity, updateActivity1, updateActivity2).collect(Collectors.toList()));
        ListingActivityQuery query = new ListingActivityQuery(2L);
        List<ActivityDTO> foundActivities = explorer.getActivities(query);
        assertNotNull(foundActivities);
        assertEquals(2, foundActivities.size());
        List<Long> activityIds = foundActivities.stream().map(activity -> activity.getId()).toList();
        assertTrue(activityIds.contains(2L));
        assertTrue(activityIds.contains(3L));
    }
}
