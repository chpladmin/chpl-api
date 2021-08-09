package gov.healthit.chpl.activity.history;

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

import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.util.JSONUtils;

public class CertificationResultSvapActivityHistoryHelperTest {
    private ActivityDAO activityDao;
    private CertificationResultSvapActivityHistoryHelper historyHelper;
    private SimpleDateFormat formatter;
    @Before
    public void setup() {
        formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        activityDao = Mockito.mock(ActivityDAO.class);
        historyHelper = new CertificationResultSvapActivityHistoryHelper(activityDao);
    }

    @Test
    public void getActivityWhenCertificationResultHasSvap_nullActivityForListing_returnsNull() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(null);
        ActivityDTO activity = historyHelper.getActivityWhenCertificationResultHasSvap(
                CertifiedProductSearchDetails.builder().id(1L).build(),
                CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").title("").build(),
                Svap.builder().svapId(1L).regulatoryTextCitation("stuff").build());
        assertNull(activity);
    }

    @Test
    public void getActivityWhenCertificationResultHasSvap_emptyActivityForListing_returnsNull() {
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(new ArrayList<ActivityDTO>());
        ActivityDTO activity = historyHelper.getActivityWhenCertificationResultHasSvap(
                CertifiedProductSearchDetails.builder().id(1L).build(),
                CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").title("").build(),
                Svap.builder().svapId(1L).regulatoryTextCitation("stuff").build());
        assertNull(activity);
    }

    @Test
    public void getActivityWhenCertificationResultHasSvap_noActivityWithMatchingSvaps_returnsNull() throws ParseException, JsonProcessingException {
        CertificationCriterion criterion = CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").title("").build();
        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .certificationResults(Stream.of(
                        CertificationResult.builder()
                            .id(1L)
                            .success(true)
                            .criterion(criterion)
                            .build())
                        .collect(Collectors.toList()))
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

        ActivityDTO foundActivity = historyHelper.getActivityWhenCertificationResultHasSvap(
                CertifiedProductSearchDetails.builder().id(1L).build(),
                criterion,
                Svap.builder().svapId(1L).regulatoryTextCitation("stuff").build());
        assertNull(foundActivity);
    }

    @Test
    public void getActivityWhenCertificationResultHasSvap_noActivityWithMatchingCriterion_returnsNull() throws ParseException, JsonProcessingException {
        CertificationCriterion a1 = CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").title("").build();
        CertificationCriterion a2 = CertificationCriterion.builder().id(2L).number("170.315 (a)(2)").title("").build();
        Svap svap = Svap.builder().svapId(1L).regulatoryTextCitation("stuff").build();

        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .certificationResults(Stream.of(
                        CertificationResult.builder()
                            .id(1L)
                            .success(true)
                            .criterion(a1)
                            .svap(CertificationResultSvap.builder()
                                    .svapId(1L)
                                    .regulatoryTextCitation("stuff")
                                    .build())
                            .build())
                        .collect(Collectors.toList()))
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

        ActivityDTO foundActivity = historyHelper.getActivityWhenCertificationResultHasSvap(
                CertifiedProductSearchDetails.builder().id(1L).build(),
                a2,
                svap);
        assertNull(foundActivity);
    }

    @Test
    public void getActivityWhenCertificationResultHasSvap_confirmActivityHasMatchingCriterion_returnsActivity() throws ParseException, JsonProcessingException {
        CertificationCriterion a1 = CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").title("").build();
        Svap svap = Svap.builder().svapId(1L).regulatoryTextCitation("stuff").build();

        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .certificationResults(Stream.of(
                        CertificationResult.builder()
                            .id(1L)
                            .success(true)
                            .criterion(a1)
                            .svap(CertificationResultSvap.builder()
                                    .svapId(1L)
                                    .regulatoryTextCitation("stuff")
                                    .build())
                            .build())
                        .collect(Collectors.toList()))
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

        ActivityDTO foundActivity = historyHelper.getActivityWhenCertificationResultHasSvap(
                CertifiedProductSearchDetails.builder().id(1L).build(),
                a1,
                svap);
        assertNotNull(foundActivity);
        assertEquals(1L, foundActivity.getId());
    }

    @Test
    public void getActivityWhenCertificationResultHasSvap_editActivityAttestsToCriterionAndAddsSvap_returnsActivity() throws ParseException, JsonProcessingException {
        CertificationCriterion a1 = CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").title("").build();
        Svap svap = Svap.builder().svapId(1L).regulatoryTextCitation("stuff").build();

        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .certificationResults(Stream.of(
                        CertificationResult.builder()
                            .id(1L)
                            .success(false)
                            .criterion(a1)
                            .build())
                        .collect(Collectors.toList()))
                .build());
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .certificationResults(Stream.of(
                        CertificationResult.builder()
                            .id(1L)
                            .success(true)
                            .criterion(a1)
                            .svap(CertificationResultSvap.builder()
                                    .svapId(1L)
                                    .regulatoryTextCitation("stuff")
                                    .build())
                            .build())
                        .collect(Collectors.toList()))
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

        ActivityDTO foundActivity = historyHelper.getActivityWhenCertificationResultHasSvap(
                CertifiedProductSearchDetails.builder().id(1L).build(),
                a1,
                svap);
        assertNotNull(foundActivity);
        assertEquals(2L, foundActivity.getId());
    }

    @Test
    public void getActivityWhenCertificationResultHasSvap_editActivityAddsSvapOnly_returnsActivity() throws ParseException, JsonProcessingException {
        CertificationCriterion a1 = CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").title("").build();
        Svap svap = Svap.builder().svapId(1L).regulatoryTextCitation("stuff").build();

        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .certificationResults(Stream.of(
                        CertificationResult.builder()
                            .id(1L)
                            .success(true)
                            .criterion(a1)
                            .build())
                        .collect(Collectors.toList()))
                .build());
        String listingUpdateActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .certificationResults(Stream.of(
                        CertificationResult.builder()
                            .id(1L)
                            .success(true)
                            .criterion(a1)
                            .svap(CertificationResultSvap.builder()
                                    .svapId(1L)
                                    .regulatoryTextCitation("stuff")
                                    .build())
                            .build())
                        .collect(Collectors.toList()))
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

        ActivityDTO foundActivity = historyHelper.getActivityWhenCertificationResultHasSvap(
                CertifiedProductSearchDetails.builder().id(1L).build(),
                a1,
                svap);
        assertNotNull(foundActivity);
        assertEquals(2L, foundActivity.getId());
    }

    @Test
    public void getActivityWhenCertificationResultHasSvap_multipleEditActivities_returnsActivity() throws ParseException, JsonProcessingException {
        CertificationCriterion a1 = CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").title("").build();
        Svap svap = Svap.builder().svapId(1L).regulatoryTextCitation("stuff").build();

        String listingConfirmActivity = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .certificationResults(Stream.of(
                        CertificationResult.builder()
                            .id(1L)
                            .success(true)
                            .criterion(a1)
                            .build())
                        .collect(Collectors.toList()))
                .build());
        String listingUpdateActivity1 = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .certificationResults(Stream.of(
                        CertificationResult.builder()
                            .id(1L)
                            .success(true)
                            .criterion(a1)
                            .svap(CertificationResultSvap.builder()
                                    .svapId(2L)
                                    .regulatoryTextCitation("stuff2")
                                    .build())
                            .build())
                        .collect(Collectors.toList()))
                .build());
        String listingUpdateActivity2 = JSONUtils.toJSON(CertifiedProductSearchDetails.builder()
                .id(2L)
                .certificationResults(Stream.of(
                        CertificationResult.builder()
                            .id(1L)
                            .success(true)
                            .criterion(a1)
                            .svap(CertificationResultSvap.builder()
                                    .svapId(1L)
                                    .regulatoryTextCitation("stuff")
                                    .build())
                            .build())
                        .collect(Collectors.toList()))
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
                .activityDate(formatter.parse("02-02-2020 11:00:00 AM"))
                .originalData(listingUpdateActivity1)
                .newData(listingUpdateActivity2)
                .build();
        Mockito.when(activityDao.findByObjectId(ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(ActivityConcept.CERTIFIED_PRODUCT),
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
        .thenReturn(Stream.of(confirmActivity, updateActivity1, updateActivity2).collect(Collectors.toList()));

        ActivityDTO foundActivity = historyHelper.getActivityWhenCertificationResultHasSvap(
                CertifiedProductSearchDetails.builder().id(1L).build(),
                a1,
                svap);
        assertNotNull(foundActivity);
        assertEquals(3L, foundActivity.getId());
    }
}
