package gov.healthit.chpl.upload.listing.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVRecord;
import org.ff4j.FF4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class IcsUploadHandlerTest {
    private static final String HEADER_ROW_BEGIN = "UNIQUE_CHPL_ID__C";
    private static final String LISTING_ROW_BEGIN = "15.02.02.3007.A056.01.00.0.180214";
    private static final String LISTING_ROW_SUBELEMENT_BEGIN = "15.02.02.3007.A056.01.00.0.180214";

    private IcsUploadHandler handler;

    @BeforeEach
    public void setup() {
        CertificationCriterionService criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.getAllowedCriterionHeadingsForNewListing())
            .thenReturn(Stream.of("CRITERIA_170_315_A_1__C", "CRITERIA_170_315_D_4__C", "CRITERIA_170_315_D_4_Cures__C",
                    "CRITERIA_170_315_B_3_Cures__C").toList());

        FF4j ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.HTI_5_ERD))).thenReturn(false);

        ListingUploadHeadingUtil uploadHeadingUtil = new ListingUploadHeadingUtil(criteriaService, ff4j);

        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(uploadHeadingUtil, msgUtil);
        handler = new IcsUploadHandler(handlerUtil);
    }

    @Test
    public void parseIcs_NoIcsColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNull(parsedIcs);
    }

    @Test
    public void parseIcs_IcsColumnsNoData_ReturnsListWithEmptyItems() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS,ICS Source").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",,");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertFalse(parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(0, parsedIcs.getParents().size());
        assertNotNull(parsedIcs.getChildren());
        assertEquals(0, parsedIcs.getChildren().size());
    }

    @Test
    public void parseIcs_MultipleIcsAllFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS,ICS Source").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",1,CHP-12345\n"
                + LISTING_ROW_SUBELEMENT_BEGIN + ",,CHP-23456");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertEquals(true, parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(2, parsedIcs.getParents().size());
        parsedIcs.getParents().stream().forEach(icsParent -> {
            assertNotNull(icsParent.getChplProductNumber());
            assertTrue(icsParent.getChplProductNumber().equals("CHP-12345")
                    || icsParent.getChplProductNumber().equals("CHP-23456"));
        });
    }

    @Test
    public void parseIcs_MultipleIcsSomeFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS,ICS Source").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",0,\n"
                + LISTING_ROW_SUBELEMENT_BEGIN + ",,");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertEquals(false, parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(0, parsedIcs.getParents().size());
    }

    @Test
    public void parseIcs_SingleIcsAllFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS,ICS Source").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",1,CHP-12345");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertTrue(parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(1, parsedIcs.getParents().size());
        assertEquals("CHP-12345", parsedIcs.getParents().get(0).getChplProductNumber());
    }

    @Test
    public void parseIcs_SingleIcsUnexpectedHeaderOrder_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS Source,ICS").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",CHP-12345,1");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertTrue(parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(1, parsedIcs.getParents().size());
        assertEquals("CHP-12345", parsedIcs.getParents().get(0).getChplProductNumber());
    }

    @Test
    public void parseIcs_SingleNoSourceColumn_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",0");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertFalse(parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(0, parsedIcs.getParents().size());
    }

    @Test
    public void parseIcs_SingleNoIcsColumn_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",ICS Source").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",CHP-12345");
        assertNotNull(listingRecords);

        InheritedCertificationStatus parsedIcs = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedIcs);
        assertNull(parsedIcs.getInherits());
        assertNotNull(parsedIcs.getParents());
        assertEquals(1, parsedIcs.getParents().size());
    }
}
