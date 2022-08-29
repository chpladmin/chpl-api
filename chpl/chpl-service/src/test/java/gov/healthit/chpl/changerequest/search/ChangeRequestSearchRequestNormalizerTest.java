package gov.healthit.chpl.changerequest.search;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

public class ChangeRequestSearchRequestNormalizerTest {

    private ChangeRequestSearchRequestNormalizer normalizer;

    @Before
    public void setup() {
        normalizer = new ChangeRequestSearchRequestNormalizer();
    }

    @Test
    public void normalize_emptySearchRequest_doesNotFail() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder().build();
        normalizer.normalize(searchRequest);
        assertNotNull(searchRequest);
    }

    @Test
    public void normalize_developerIdStringValidLong_convertsToLong() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .developerIdString("1")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1L, searchRequest.getDeveloperId());
    }

    @Test
    public void normalize_developerIdStringNotANumber_leavesIdNull() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .developerIdString("test")
                .build();
        normalizer.normalize(searchRequest);

        assertNull(searchRequest.getDeveloperId());
    }

    @Test
    public void normalize_currentTypeNames_trimsCorrectly() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .changeRequestTypeNames(Stream.of("test1 ", " test2 ", "", " ", null, "test3").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getChangeRequestTypeNames().size());
        assertTrue(searchRequest.getChangeRequestTypeNames().contains("test1"));
        assertTrue(searchRequest.getChangeRequestTypeNames().contains("test2"));
        assertTrue(searchRequest.getChangeRequestTypeNames().contains("test3"));
    }

    @Test
    public void normalize_currentStatusChangeDateStart_trimsCorrectly() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .currentStatusChangeDateTimeStart("2022-06-12T11:00:00   ")
                .build();
        normalizer.normalize(searchRequest);
        assertEquals("2022-06-12T11:00:00", searchRequest.getCurrentStatusChangeDateTimeStart());

        searchRequest = ChangeRequestSearchRequest.builder()
                .currentStatusChangeDateTimeStart("    2022-06-12T11:00:00")
                .build();
        normalizer.normalize(searchRequest);
        assertEquals("2022-06-12T11:00:00", searchRequest.getCurrentStatusChangeDateTimeStart());

        searchRequest = ChangeRequestSearchRequest.builder()
                .currentStatusChangeDateTimeStart("    2022-06-12T11:00:00    ")
                .build();
        normalizer.normalize(searchRequest);
        assertEquals("2022-06-12T11:00:00", searchRequest.getCurrentStatusChangeDateTimeStart());

        searchRequest = ChangeRequestSearchRequest.builder()
                .currentStatusChangeDateTimeStart(null)
                .build();
        normalizer.normalize(searchRequest);
        assertNull(searchRequest.getCurrentStatusChangeDateTimeStart());
    }

    @Test
    public void normalize_currentStatusChangeDateEnd_trimsCorrectly() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .currentStatusChangeDateTimeEnd("2022-06-12T11:00:00   ")
                .build();
        normalizer.normalize(searchRequest);
        assertEquals("2022-06-12T11:00:00", searchRequest.getCurrentStatusChangeDateTimeEnd());

        searchRequest = ChangeRequestSearchRequest.builder()
                .currentStatusChangeDateTimeEnd("    2022-06-12T11:00:00")
                .build();
        normalizer.normalize(searchRequest);
        assertEquals("2022-06-12T11:00:00", searchRequest.getCurrentStatusChangeDateTimeEnd());

        searchRequest = ChangeRequestSearchRequest.builder()
                .currentStatusChangeDateTimeEnd("    2022-06-12T11:00:00    ")
                .build();
        normalizer.normalize(searchRequest);
        assertEquals("2022-06-12T11:00:00", searchRequest.getCurrentStatusChangeDateTimeEnd());

        searchRequest = ChangeRequestSearchRequest.builder()
                .currentStatusChangeDateTimeEnd(null)
                .build();
        normalizer.normalize(searchRequest);
        assertNull(searchRequest.getCurrentStatusChangeDateTimeEnd());
    }

    @Test
    public void normalize_submittedDateTimeStart_trimsCorrectly() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .submittedDateTimeStart("2022-06-12T11:00:00   ")
                .build();
        normalizer.normalize(searchRequest);
        assertEquals("2022-06-12T11:00:00", searchRequest.getSubmittedDateTimeStart());

        searchRequest = ChangeRequestSearchRequest.builder()
                .submittedDateTimeStart("    2022-06-12T11:00:00")
                .build();
        normalizer.normalize(searchRequest);
        assertEquals("2022-06-12T11:00:00", searchRequest.getSubmittedDateTimeStart());

        searchRequest = ChangeRequestSearchRequest.builder()
                .submittedDateTimeStart("    2022-06-12T11:00:00    ")
                .build();
        normalizer.normalize(searchRequest);
        assertEquals("2022-06-12T11:00:00", searchRequest.getSubmittedDateTimeStart());

        searchRequest = ChangeRequestSearchRequest.builder()
                .submittedDateTimeStart(null)
                .build();
        normalizer.normalize(searchRequest);
        assertNull(searchRequest.getSubmittedDateTimeStart());
    }

    @Test
    public void normalize_submittedDateTimeEnd_trimsCorrectly() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .submittedDateTimeEnd("2022-06-12T11:00:00   ")
                .build();
        normalizer.normalize(searchRequest);
        assertEquals("2022-06-12T11:00:00", searchRequest.getSubmittedDateTimeEnd());

        searchRequest = ChangeRequestSearchRequest.builder()
                .submittedDateTimeEnd("    2022-06-12T11:00:00")
                .build();
        normalizer.normalize(searchRequest);
        assertEquals("2022-06-12T11:00:00", searchRequest.getSubmittedDateTimeEnd());

        searchRequest = ChangeRequestSearchRequest.builder()
                .submittedDateTimeEnd("    2022-06-12T11:00:00    ")
                .build();
        normalizer.normalize(searchRequest);
        assertEquals("2022-06-12T11:00:00", searchRequest.getSubmittedDateTimeEnd());

        searchRequest = ChangeRequestSearchRequest.builder()
                .submittedDateTimeEnd(null)
                .build();
        normalizer.normalize(searchRequest);
        assertNull(searchRequest.getSubmittedDateTimeEnd());
    }

    @Test
    public void normalize_orderByStringValid_resolvesCorrectly() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .orderByString("DEVELOPER")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.DEVELOPER, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByStringLowercase_resolvesCorrectly() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .orderByString("developer")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.DEVELOPER, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByDveloper_noChanges() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .orderBy(OrderByOption.DEVELOPER)
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.DEVELOPER, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByStringInvalid_setsFieldNull() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .orderByString("NOTVALID")
                .build();
        normalizer.normalize(searchRequest);

        assertNull(searchRequest.getOrderBy());
    }

    @Test
    public void normalize_pageNumberStringValidLong_convertsToLong() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .pageNumberString("1")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getPageNumber());
    }

    @Test
    public void normalize_pageNumberStringNotANumber_leavesDefault() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .pageNumberString("test")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(0, searchRequest.getPageNumber());
    }

    @Test
    public void normalize_pageSizeStringValidLong_convertsToLong() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .pageSizeString("1")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getPageSize());
    }

    @Test
    public void normalize_pageSizeStringNotANumber_leavesDefault() {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .pageSizeString("test")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(ChangeRequestSearchRequest.DEFAULT_PAGE_SIZE, searchRequest.getPageSize());
    }
}
