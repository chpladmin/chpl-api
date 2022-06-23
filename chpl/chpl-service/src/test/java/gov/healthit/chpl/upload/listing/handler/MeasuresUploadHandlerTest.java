package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class MeasuresUploadHandlerTest {
    private static final String HEADER_ROW_ALL_MEASURE_FIELDS = "UNIQUE_CHPL_ID__C,Measure Name,Measure Required Test,Measure Type,Measure Associated Criteria";
    private static final String LISTING_ROW_BEGIN = "15.02.02.3007.A056.01.00.0.180214";

    private MeasuresUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new MeasuresUploadHandler(handlerUtil);
    }

    @Test
    public void parseMeasures_NoMeasureHeadings_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,CRITERIA_170_315_A_1__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",1");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(0, parsedListingMeasures.size());
    }

    @Test
    public void parseMeasures_MeasureHeadingsNoData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",,,,");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(0, parsedListingMeasures.size());
    }

    @Test
    public void parseMeasures_MeasureHeaderWithOneMeasureAndNoName_ReturnsMeasureWithNullName() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",,rt1,g1,crit1");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(1, parsedListingMeasures.size());
        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure.getMeasure());
        assertEquals("", parsedMeasure.getMeasure().getName());
    }

    @Test
    public void parseMeasures_MeasureHeaderWithOneMeasureAndNoRequiredTest_ReturnsMeasureWithNullRequiredTest() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",name,,g1,crit1");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(1, parsedListingMeasures.size());
        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure.getMeasure());
        assertEquals("", parsedMeasure.getMeasure().getRequiredTest());
    }

    @Test
    public void parseMeasures_MeasureHeaderWithOneMeasureAndNoType_ReturnsMeasureWithNullTypeName() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",name,rt1,,crit1");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(1, parsedListingMeasures.size());
        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("", parsedMeasure.getMeasureType().getName());
    }

    @Test
    public void parseMeasures_MeasureHeaderWithOneMeasureAndNoAssociatedCriteria_ReturnsMeasureWithEmptyAssociatedCriteria() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",name,rt1,g1,");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(1, parsedListingMeasures.size());
        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure);
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(0, parsedMeasure.getAssociatedCriteria().size());
    }

    @Test
    public void parseMeasures_MeasureHeaderWithOneMeasureAndOneAssociatedCriterion_ReturnsMeasureWithAllFields() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",name,rt1,g1,170.315 (a)(1)");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(1, parsedListingMeasures.size());
        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure.getMeasure());
        assertEquals("name", parsedMeasure.getMeasure().getName());
        assertEquals("rt1", parsedMeasure.getMeasure().getRequiredTest());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("g1", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(1, parsedMeasure.getAssociatedCriteria().size());
        assertEquals("170.315 (a)(1)", parsedMeasure.getAssociatedCriteria().iterator().next().getNumber());
    }

    @Test
    public void parseMeasures_MeasureHeaderWithOneMeasureAndTwoAssociatedCriteria_ReturnsMeasureWithAllFields() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",name,rt1,g1,170.315 (a)(1);a2");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(1, parsedListingMeasures.size());
        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure.getMeasure());
        assertEquals("name", parsedMeasure.getMeasure().getName());
        assertEquals("rt1", parsedMeasure.getMeasure().getRequiredTest());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("g1", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(2, parsedMeasure.getAssociatedCriteria().size());
        Iterator<CertificationCriterion> assocCriteriaIter = parsedMeasure.getAssociatedCriteria().iterator();
        assertEquals("170.315 (a)(1)", assocCriteriaIter.next().getNumber());
        assertEquals("a2", assocCriteriaIter.next().getNumber());
    }

    @Test
    public void parseMeasures_MeasureHeaderWithMultipleMeasuresVariousData_ReturnsMeasuresCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",name,rt1,g1,170.315 (a)(1);a2\n"
                + LISTING_ROW_BEGIN + ",name2,rt2,g2,a2\n"
                + LISTING_ROW_BEGIN + ",name3,rt2,,");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(3, parsedListingMeasures.size());

        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure.getMeasure());
        assertEquals("name", parsedMeasure.getMeasure().getName());
        assertEquals("rt1", parsedMeasure.getMeasure().getRequiredTest());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("g1", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(2, parsedMeasure.getAssociatedCriteria().size());
        Iterator<CertificationCriterion> assocCriteriaIter = parsedMeasure.getAssociatedCriteria().iterator();
        assertEquals("170.315 (a)(1)", assocCriteriaIter.next().getNumber());
        assertEquals("a2", assocCriteriaIter.next().getNumber());

        parsedMeasure = parsedListingMeasures.get(1);
        assertNotNull(parsedMeasure.getMeasure());
        assertEquals("name2", parsedMeasure.getMeasure().getName());
        assertEquals("rt2", parsedMeasure.getMeasure().getRequiredTest());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("g2", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(1, parsedMeasure.getAssociatedCriteria().size());
        assertEquals("a2", parsedMeasure.getAssociatedCriteria().iterator().next().getNumber());

        parsedMeasure = parsedListingMeasures.get(2);
        assertNotNull(parsedMeasure.getMeasure());
        assertEquals("name3", parsedMeasure.getMeasure().getName());
        assertEquals("rt2", parsedMeasure.getMeasure().getRequiredTest());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(0, parsedMeasure.getAssociatedCriteria().size());
    }

    @Test
    public void parseMeasures_MeasureHeaderWithDuplicateMeasureData_ReturnsDuplicateMeasures() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",name,rt1,g1,170.315 (a)(1)\n"
                + LISTING_ROW_BEGIN + ",name,rt1,g1,170.315 (a)(1)\n"
                + LISTING_ROW_BEGIN + ",name,rt1,g1,170.315 (a)(1)");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(3, parsedListingMeasures.size());

        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure.getMeasure());
        assertEquals("name", parsedMeasure.getMeasure().getName());
        assertEquals("rt1", parsedMeasure.getMeasure().getRequiredTest());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("g1", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(1, parsedMeasure.getAssociatedCriteria().size());
        assertEquals("170.315 (a)(1)", parsedMeasure.getAssociatedCriteria().iterator().next().getNumber());

        parsedMeasure = parsedListingMeasures.get(1);
        assertNotNull(parsedMeasure.getMeasure());
        assertEquals("name", parsedMeasure.getMeasure().getName());
        assertEquals("rt1", parsedMeasure.getMeasure().getRequiredTest());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("g1", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(1, parsedMeasure.getAssociatedCriteria().size());
        assertEquals("170.315 (a)(1)", parsedMeasure.getAssociatedCriteria().iterator().next().getNumber());

        parsedMeasure = parsedListingMeasures.get(2);
        assertNotNull(parsedMeasure.getMeasure());
        assertEquals("name", parsedMeasure.getMeasure().getName());
        assertEquals("rt1", parsedMeasure.getMeasure().getRequiredTest());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("g1", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(1, parsedMeasure.getAssociatedCriteria().size());
        assertEquals("170.315 (a)(1)", parsedMeasure.getAssociatedCriteria().iterator().next().getNumber());
    }
}
