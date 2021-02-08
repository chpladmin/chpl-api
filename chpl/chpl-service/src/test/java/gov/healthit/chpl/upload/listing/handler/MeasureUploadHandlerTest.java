package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class MeasureUploadHandlerTest {
    private static final String HEADER_ROW_ALL_MEASURE_FIELDS = "CRITERIA_170_315_A_1__C,Measure Successfully Tested for G1,Measure Successfully Tested for G2";

    private MeasureUploadHandler handler;

    @Before
    public void setup() {
        CertificationCriterionUploadHandler criterionHandler = Mockito.mock(CertificationCriterionUploadHandler.class);
        Mockito.when(criterionHandler.handle(ArgumentMatchers.any()))
            .thenReturn(buildCriterion(1L, "170.315 (a)(1)", "a title"));
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new MeasureUploadHandler(criterionHandler, handlerUtil);
    }

    @Test
    public void parseMeasures_NoCriteriaOrMeasureColumns_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("14.05.05");
        assertNotNull(certResultRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, certResultRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(0, parsedListingMeasures.size());
    }

    @Test
    public void parseMeasures_NoMeasureColumns_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_A_1__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, certResultRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(0, parsedListingMeasures.size());
    }

    @Test
    public void parseMeasures_G1MeasureHeaderNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Measure Successfully Tested for G1").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, certResultRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(0, parsedListingMeasures.size());
    }

    @Test
    public void parseMeasures_G2MeasureHeaderNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Measure Successfully Tested for G2").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, certResultRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(0, parsedListingMeasures.size());
    }

    @Test
    public void parseMeasures_AllMeasureColumnsNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,,");
        assertNotNull(certResultRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, certResultRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(0, parsedListingMeasures.size());
    }

    @Test
    public void parseMeasures_G1MeasureHeaderWithData_ReturnsMeasuresWithData() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Measure Successfully Tested for G1").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,legacy measure name");
        assertNotNull(certResultRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, certResultRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(1, parsedListingMeasures.size());
        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("G1", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getMeasure());
        assertEquals("legacy measure name", parsedMeasure.getMeasure().getLegacyMacraMeasureValue());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(1, parsedMeasure.getAssociatedCriteria().size());
    }

    @Test
    public void parseMeasures_G2MeasureHeaderWithData_ReturnsMeasuresWithData() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Measure Successfully Tested for G2").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,legacy measure name");
        assertNotNull(certResultRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, certResultRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(1, parsedListingMeasures.size());
        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("G2", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getMeasure());
        assertEquals("legacy measure name", parsedMeasure.getMeasure().getLegacyMacraMeasureValue());
    }

    @Test
    public void parseMeasures_G1AndG2MeasureHeaderWithSingleDataRow_ReturnsMeasuresWithData() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,legacy g1,legacy g2");
        assertNotNull(certResultRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, certResultRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(2, parsedListingMeasures.size());
        for (ListingMeasure parsedMeasure : parsedListingMeasures) {
            assertNotNull(parsedMeasure.getMeasureType());
            if (parsedMeasure.getMeasureType().getName().equals("G1")) {
                assertNotNull(parsedMeasure.getMeasure());
                assertEquals("legacy g1", parsedMeasure.getMeasure().getLegacyMacraMeasureValue());
            } else if (parsedMeasure.getMeasureType().getName().equals("G2")) {
                assertNotNull(parsedMeasure.getMeasure());
                assertEquals("legacy g2", parsedMeasure.getMeasure().getLegacyMacraMeasureValue());
            } else {
                fail("Unexpected measure type");
            }
        }
    }

    @Test
    public void parseMeasures_G1AndG2MeasureHeaderWithMultipleDataRows_ReturnsMeasuresWithData() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,legacy g1,legacy g2\n"
                + ",legacy2 g1,legacy2 g2\n"
                + ",,legacy3 g2");
        assertNotNull(certResultRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, certResultRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(5, parsedListingMeasures.size());
        int g1Count = 0;
        int g2Count = 0;
        for (ListingMeasure parsedMeasure : parsedListingMeasures) {
            assertNotNull(parsedMeasure.getMeasureType());
            if (parsedMeasure.getMeasureType().getName().equals("G1")) {
                g1Count++;
                assertNotNull(parsedMeasure.getMeasure());
                assertNotNull(parsedMeasure.getMeasure().getLegacyMacraMeasureValue());
            } else if (parsedMeasure.getMeasureType().getName().equals("G2")) {
                g2Count++;
                assertNotNull(parsedMeasure.getMeasure());
                assertNotNull(parsedMeasure.getMeasure().getLegacyMacraMeasureValue());
            } else {
                fail("Unexpected measure type");
            }
        }
        assertEquals(2, g1Count);
        assertEquals(3, g2Count);
    }

    private CertificationCriterion buildCriterion(Long id, String number, String title) {
        return CertificationCriterion.builder()
                .id(id)
                .number(number)
                .title(title)
          .build();
    }
}
