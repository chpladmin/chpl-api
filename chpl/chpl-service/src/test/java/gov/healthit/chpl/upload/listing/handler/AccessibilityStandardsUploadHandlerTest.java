package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class AccessibilityStandardsUploadHandlerTest {
    private static final String HEADER_ROW = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C,Accessibility Standard";
    private static final String LISTING_ROW = "15.02.02.3007.A056.01.00.0.180214,New,170.204(a)(1)";
    private static final String LISTING_ROWS = "15.02.02.3007.A056.01.00.0.180214,New,170.204(a)(1)\n"
            + "15.02.02.3007.A056.01.00.0.180214,Subelement,IEE 802.11";

    private AccessibilityStandardDAO dao;
    private AccessibilityStandardsUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        dao = Mockito.mock(AccessibilityStandardDAO.class);
        handler = new AccessibilityStandardsUploadHandler(handlerUtil, dao, msgUtil);
    }

    @Test
    public void parseStandards_MultipleValidStandards_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROWS);
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("170.204(a)(1)")))
            .thenReturn(buildDto(1L, "170.204(a)(1)"));
        Mockito.when(dao.getByName(ArgumentMatchers.eq("IEE 802.11")))
        .thenReturn(buildDto(2L, "IEE 802.11"));

        List<CertifiedProductAccessibilityStandard> foundStandards = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundStandards);
        assertEquals(2, foundStandards.size());
        foundStandards.stream().forEach(std -> {
            assertNull(std.getId());
            assertNotNull(std.getAccessibilityStandardId());
            assertNotNull(std.getAccessibilityStandardName());
        });
    }

    @Test
    public void parseStandards_SingleValidStandard_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW);
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("170.204(a)(1)")))
            .thenReturn(buildDto(1L, "170.204(a)(1)"));

        List<CertifiedProductAccessibilityStandard> foundStandards = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundStandards);
        assertEquals(1, foundStandards.size());
        foundStandards.stream().forEach(std -> {
            assertNull(std.getId());
            assertNotNull(std.getAccessibilityStandardId());
            assertEquals(1, std.getAccessibilityStandardId().longValue());
            assertNotNull(std.getAccessibilityStandardName());
            assertEquals("170.204(a)(1)", std.getAccessibilityStandardName());
        });
    }

    @Test
    public void parseStandards_SingleInvalidStandard_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW);
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("170.204(a)(1)"))).thenReturn(null);

        List<CertifiedProductAccessibilityStandard> foundStandards = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundStandards);
        assertEquals(1, foundStandards.size());
        foundStandards.stream().forEach(std -> {
            assertNull(std.getId());
            assertNull(std.getAccessibilityStandardId());
            assertNotNull(std.getAccessibilityStandardName());
            assertEquals("170.204(a)(1)", std.getAccessibilityStandardName());
        });

    }

    @Test
    public void parseStandards_StandardWithWhitespace_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                "15.02.02.3007.A056.01.00.0.180214,New, 170.204(a)(1) ");
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("170.204(a)(1)")))
            .thenReturn(buildDto(1L, "170.204(a)(1)"));

        List<CertifiedProductAccessibilityStandard> foundStandards = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundStandards);
        assertEquals(1, foundStandards.size());
        foundStandards.stream().forEach(std -> {
            assertNull(std.getId());
            assertNotNull(std.getAccessibilityStandardId());
            assertEquals(1, std.getAccessibilityStandardId().longValue());
            assertNotNull(std.getAccessibilityStandardName());
            assertEquals("170.204(a)(1)", std.getAccessibilityStandardName());
        });
    }

    private AccessibilityStandardDTO buildDto(Long id, String name) {
        AccessibilityStandardDTO dto = new AccessibilityStandardDTO();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}
