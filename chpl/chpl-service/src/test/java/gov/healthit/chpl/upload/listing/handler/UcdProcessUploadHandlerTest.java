package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class UcdProcessUploadHandlerTest {
    private static final String HEADER_ROW_ALL_UCD_FIELDS = "CRITERIA_170_315_A_1__C,UCD Process Selected,UCD Process Details";

    private UcdProcessUploadHandler handler;

    @Before
    public void setup() {
        CertificationCriterionService criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.getAllowedCriterionHeadingsForNewListing())
            .thenReturn(Stream.of("CRITERIA_170_315_A_1__C", "CRITERIA_170_315_D_4__C", "CRITERIA_170_315_D_4_Cures__C",
                    "CRITERIA_170_315_B_3_Cures__C").toList());
        ListingUploadHeadingUtil uploadHeadingUtil = new ListingUploadHeadingUtil(criteriaService);

        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(uploadHeadingUtil, msgUtil);
        handler = new UcdProcessUploadHandler(handlerUtil);
    }

    @Test
    public void parseUcdProcess_NoUcdProcessColumns_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_A_1__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        List<CertifiedProductUcdProcess> parsedUcdProcesss = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedUcdProcesss);
        assertEquals(0, parsedUcdProcesss.size());
    }

    @Test
    public void parseUcdProcess_UcdProcessAllColumnsNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_UCD_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,,");
        assertNotNull(certResultRecords);

        List<CertifiedProductUcdProcess> parsedUcdProcesss = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedUcdProcesss);
        assertEquals(0, parsedUcdProcesss.size());
    }

    @Test
    public void parseUcdProcess_SingleUcdProcessNameOnlyNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,UCD Process Selected").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        List<CertifiedProductUcdProcess> parsedUcdProcesss = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedUcdProcesss);
        assertEquals(0, parsedUcdProcesss.size());
    }

    @Test
    public void parseUcdProcess_SingleUcdProcessNameOnlyWithData_ReturnsListWithEmptyItems() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,UCD Process Selected").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,UCD Name");
        assertNotNull(certResultRecords);

        List<CertifiedProductUcdProcess> parsedUcdProcesss = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedUcdProcesss);
        assertEquals(1, parsedUcdProcesss.size());
        CertifiedProductUcdProcess ucd = parsedUcdProcesss.get(0);
        assertEquals("UCD Name", ucd.getName());
        assertNull(ucd.getDetails());
        assertNull(ucd.getId());
    }

    @Test
    public void parseUcdProcess_SingleUcdProcessAllColumnsWithData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_UCD_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,NISTIR 7741,NISTIR 7741 was used");
        assertNotNull(certResultRecords);

        List<CertifiedProductUcdProcess> parsedUcdProcesss = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedUcdProcesss);
        assertEquals(1, parsedUcdProcesss.size());
        CertifiedProductUcdProcess ucd = parsedUcdProcesss.get(0);
        assertEquals("NISTIR 7741", ucd.getName());
        assertEquals("NISTIR 7741 was used", ucd.getDetails());
        assertNull(ucd.getId());
    }

    @Test
    public void parseUcdProcess_SingleUcdProcessAndBlankUcdProcess_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_UCD_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,NISTIR 7741,NISTIR 7741 was used\n,,");
        assertNotNull(certResultRecords);

        List<CertifiedProductUcdProcess> parsedUcdProcesss = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedUcdProcesss);
        assertEquals(1, parsedUcdProcesss.size());
        CertifiedProductUcdProcess ucd = parsedUcdProcesss.get(0);
        assertEquals("NISTIR 7741", ucd.getName());
        assertEquals("NISTIR 7741 was used", ucd.getDetails());
        assertNull(ucd.getId());
    }

    @Test
    public void parseUcdProcess_MultipleUcdProcesssAllFieldsPopulated_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_UCD_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "1,NISTIR 7741,NISTIR 7741 was used" + "\n" + ",UCD 2,UCD 2 was used");
        assertNotNull(certResultRecords);

        List<CertifiedProductUcdProcess> parsedUcdProcesss = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedUcdProcesss);
        assertEquals(2, parsedUcdProcesss.size());
        parsedUcdProcesss.stream().forEach(ucd -> {
            assertNull(ucd.getId());
            assertNotNull(ucd.getName());
            assertNotNull(ucd.getDetails());
            if (ucd.getName().equals("NISTIR 7741")) {
                assertEquals("NISTIR 7741 was used", ucd.getDetails());
            } else if (ucd.getName().equals("UCD 2")) {
                assertEquals("UCD 2 was used", ucd.getDetails());
            } else {
                fail("No Ucd Process with name " + ucd.getName() + " should have been found.");
            }
        });
    }

    @Test
    public void parseUcdProcess_SingleUcdProcessUnexpectedHeaderOrder_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,UCD Process Details,UCD Process Selected").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,details,ucd name");
        assertNotNull(certResultRecords);

        List<CertifiedProductUcdProcess> parsedUcdProcesss = handler.handle(headingRecord, certResultRecords);
        assertNotNull(parsedUcdProcesss);
        assertEquals(1, parsedUcdProcesss.size());
        CertifiedProductUcdProcess ucd = parsedUcdProcesss.get(0);
        assertEquals("ucd name", ucd.getName());
        assertEquals("details", ucd.getDetails());
    }
}
