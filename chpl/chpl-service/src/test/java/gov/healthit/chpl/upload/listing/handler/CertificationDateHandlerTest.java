package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CertificationDateHandlerTest {
    private static final String HEADER_ROW_BEGIN = "UNIQUE_CHPL_ID__C";
    private static final String LISTING_ROW_BEGIN = "15.02.02.3007.A056.01.00.0.180214";

    private ErrorMessageUtil msgUtil;
    private ListingUploadHandlerUtil handlerUtil;
    private ChplProductNumberUtil chplProductNumberUtil;
    private CertificationDateHandler handler;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        chplProductNumberUtil = new ChplProductNumberUtil();
        handler = new CertificationDateHandler(handlerUtil, chplProductNumberUtil);
    }

    @Test
    public void parseCertDate_CertificationDateNoColumnAndEmptyListingField_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("15.02.02.3007.A056.01.00.0.");
        assertNotNull(listingRecords);

        LocalDate certDate = handler.handle(headingRecord, listingRecords);
        assertNull(certDate);
    }

    @Test
    public void parseCertDate_CertificationDateNoColumn_ReturnsValueFromChplProductNumber() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        LocalDate certDate = handler.handle(headingRecord, listingRecords);
        assertNotNull(certDate);
        assertEquals(LocalDate.of(2018, Month.FEBRUARY, 14), certDate);
    }

    @Test
    public void parseCertDate_CertificationDateColumnValid_ParsesDateValueFromColumn() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFICATION_DATE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",20200909");
        assertNotNull(listingRecords);

        LocalDate certDate = handler.handle(headingRecord, listingRecords);
        assertNotNull(certDate);
        assertEquals(LocalDate.of(2020, Month.SEPTEMBER, 9), certDate);
    }

    @Test
    public void parseCertDate_CertificationDateColumnEmpty_ParsesValueFromChplProductNumber() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFICATION_DATE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",");
        assertNotNull(listingRecords);

        LocalDate certDate = handler.handle(headingRecord, listingRecords);
        assertNotNull(certDate);
        assertEquals(LocalDate.of(2018, Month.FEBRUARY, 14), certDate);
    }

    @Test
    public void parseCertDate_CertificationDateValueBadInChplProductNumberAndDateColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFICATION_DATE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("14.ba.d." + ",BADDATE");
        assertNotNull(listingRecords);

        LocalDate certDate = handler.handle(headingRecord, listingRecords);
        assertNull(certDate);
    }
}
