package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class MeasuresUploadHandlerTest {
    private static final String HEADER_ROW_ALL_MEASURE_FIELDS = "UNIQUE_CHPL_ID__C,Measure Domain,Measure Required Test,Measure Type,Measure Associated Criteria";
    private static final String LISTING_ROW_BEGIN = "15.02.02.3007.A056.01.00.0.180214";

    private MeasuresUploadHandler handler;

    @Before
    public void setup() {
        CertificationCriterionService criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.getAllowedCriterionHeadingsForNewListing())
            .thenReturn(Stream.of("CRITERIA_170_315_A_1__C", "CRITERIA_170_315_D_4__C", "CRITERIA_170_315_D_4_Cures__C",
                    "CRITERIA_170_315_B_3_Cures__C").toList());
        ListingUploadHeadingUtil uploadHeadingUtil = new ListingUploadHeadingUtil(criteriaService);

        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(uploadHeadingUtil, msgUtil);
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
    public void parseMeasures_MeasureHeaderWithOneMeasureAndNoDomain_ReturnsMeasureWithEmptyDomainName() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",,rt1,g1,crit1");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(1, parsedListingMeasures.size());
        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure.getMeasure());
        assertNotNull(parsedMeasure.getMeasure().getDomain());
        assertEquals("", parsedMeasure.getMeasure().getDomain().getName());
    }

    @Test
    public void parseMeasures_MeasureHeaderWithOneMeasureAndNoRequiredTest_ReturnsMeasureWithEmptyAbbreviation() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",EC,,g1,crit1");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(1, parsedListingMeasures.size());
        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure.getMeasure());
        assertEquals("", parsedMeasure.getMeasure().getAbbreviation());
    }

    @Test
    public void parseMeasures_MeasureHeaderWithOneMeasureAndNoType_ReturnsMeasureWithEmptyTypeName() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",EC,rt1,,crit1");
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
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",EC,rt1,g1,");
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
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",EC,RT1,G1,170.315 (a)(1)");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(1, parsedListingMeasures.size());
        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure.getMeasure());
        assertNotNull(parsedMeasure.getMeasure().getDomain());
        assertEquals("EC", parsedMeasure.getMeasure().getDomain().getName());
        assertEquals("RT1", parsedMeasure.getMeasure().getAbbreviation());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("G1", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(1, parsedMeasure.getAssociatedCriteria().size());
        assertEquals("170.315 (a)(1)", parsedMeasure.getAssociatedCriteria().iterator().next().getNumber());
    }

    @Test
    public void parseMeasures_MeasureHeaderWithOneMeasureAndTwoAssociatedCriteria_ReturnsMeasureWithAllFields() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_ALL_MEASURE_FIELDS).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",EP,RT1,G1,170.315 (a)(1);a2");
        assertNotNull(listingRecords);

        List<ListingMeasure> parsedListingMeasures = handler.parseAsMeasures(headingRecord, listingRecords);
        assertNotNull(parsedListingMeasures);
        assertEquals(1, parsedListingMeasures.size());
        ListingMeasure parsedMeasure = parsedListingMeasures.get(0);
        assertNotNull(parsedMeasure.getMeasure());
        assertNotNull(parsedMeasure.getMeasure().getDomain());
        assertEquals("EP", parsedMeasure.getMeasure().getDomain().getName());
        assertEquals("RT1", parsedMeasure.getMeasure().getAbbreviation());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("G1", parsedMeasure.getMeasureType().getName());
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
        assertNotNull(parsedMeasure.getMeasure().getDomain());
        assertEquals("name", parsedMeasure.getMeasure().getDomain().getName());
        assertEquals("rt1", parsedMeasure.getMeasure().getAbbreviation());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("g1", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(2, parsedMeasure.getAssociatedCriteria().size());
        Iterator<CertificationCriterion> assocCriteriaIter = parsedMeasure.getAssociatedCriteria().iterator();
        assertEquals("170.315 (a)(1)", assocCriteriaIter.next().getNumber());
        assertEquals("a2", assocCriteriaIter.next().getNumber());

        parsedMeasure = parsedListingMeasures.get(1);
        assertNotNull(parsedMeasure.getMeasure());
        assertNotNull(parsedMeasure.getMeasure().getDomain());
        assertEquals("name2", parsedMeasure.getMeasure().getDomain().getName());
        assertEquals("rt2", parsedMeasure.getMeasure().getAbbreviation());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("g2", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(1, parsedMeasure.getAssociatedCriteria().size());
        assertEquals("a2", parsedMeasure.getAssociatedCriteria().iterator().next().getNumber());

        parsedMeasure = parsedListingMeasures.get(2);
        assertNotNull(parsedMeasure.getMeasure());
        assertNotNull(parsedMeasure.getMeasure().getDomain());
        assertEquals("name3", parsedMeasure.getMeasure().getDomain().getName());
        assertEquals("rt2", parsedMeasure.getMeasure().getAbbreviation());
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
        assertNotNull(parsedMeasure.getMeasure().getDomain());
        assertEquals("name", parsedMeasure.getMeasure().getDomain().getName());
        assertEquals("rt1", parsedMeasure.getMeasure().getAbbreviation());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("g1", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(1, parsedMeasure.getAssociatedCriteria().size());
        assertEquals("170.315 (a)(1)", parsedMeasure.getAssociatedCriteria().iterator().next().getNumber());

        parsedMeasure = parsedListingMeasures.get(1);
        assertNotNull(parsedMeasure.getMeasure());
        assertNotNull(parsedMeasure.getMeasure().getDomain());
        assertEquals("name", parsedMeasure.getMeasure().getDomain().getName());
        assertEquals("rt1", parsedMeasure.getMeasure().getAbbreviation());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("g1", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(1, parsedMeasure.getAssociatedCriteria().size());
        assertEquals("170.315 (a)(1)", parsedMeasure.getAssociatedCriteria().iterator().next().getNumber());

        parsedMeasure = parsedListingMeasures.get(2);
        assertNotNull(parsedMeasure.getMeasure());
        assertNotNull(parsedMeasure.getMeasure().getDomain());
        assertEquals("name", parsedMeasure.getMeasure().getDomain().getName());
        assertEquals("rt1", parsedMeasure.getMeasure().getAbbreviation());
        assertNotNull(parsedMeasure.getMeasureType());
        assertEquals("g1", parsedMeasure.getMeasureType().getName());
        assertNotNull(parsedMeasure.getAssociatedCriteria());
        assertEquals(1, parsedMeasure.getAssociatedCriteria().size());
        assertEquals("170.315 (a)(1)", parsedMeasure.getAssociatedCriteria().iterator().next().getNumber());
    }
}
