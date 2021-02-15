package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CertificationEditionHandlerTest {
    private static final String HEADER_ROW_BEGIN = "UNIQUE_CHPL_ID__C";
    private static final String LISTING_ROW_BEGIN = "15.02.02.3007.A056.01.00.0.180214";

    private ErrorMessageUtil msgUtil;
    private ListingUploadHandlerUtil handlerUtil;
    private ChplProductNumberUtil chplProductNumberUtil;
    private CertificationEditionHandler handler;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        chplProductNumberUtil = new ChplProductNumberUtil();
        handler = new CertificationEditionHandler(handlerUtil, chplProductNumberUtil);
    }

    @Test
    public void parseEdition_NoEditionColumnNoChplProductNumber_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("NOEDITION");
        assertNotNull(listingRecords);

        Map<String, Object> edition = handler.handle(headingRecord, listingRecords);
        assertNull(edition);
    }

    @Test
    public void parseEdition_BlankEditionColumnNoChplProductNumber_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERT_YEAR__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("NOEDITION,");
        assertNotNull(listingRecords);

        Map<String, Object> edition = handler.handle(headingRecord, listingRecords);
        assertNull(edition);
    }

    @Test
    public void parseEdition_BlankEditionColumnValidChplProductNumber_ReturnsValueFromChplProductNumber() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERT_YEAR__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",");
        assertNotNull(listingRecords);

        Map<String, Object> edition = handler.handle(headingRecord, listingRecords);
        assertNotNull(edition);
        assertEquals("2015", edition.get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString());
        assertNull(edition.get(CertifiedProductSearchDetails.EDITION_ID_KEY));
    }

    @Test
    public void parseEdition_NoEditionColumnValidChplProductNumber_ReturnsEditionFromChplProductNumber() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        Map<String, Object> edition = handler.handle(headingRecord, listingRecords);
        assertNotNull(edition);
        assertEquals("2015", edition.get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString());
        assertNull(edition.get(CertifiedProductSearchDetails.EDITION_ID_KEY));
    }

    @Test
    public void parseEdition_EditionColumnAndValidChplProductNumber_ReturnsEditionFromColumn() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERT_YEAR__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",2014");
        assertNotNull(listingRecords);

        Map<String, Object> edition = handler.handle(headingRecord, listingRecords);
        assertNotNull(edition);
        assertEquals("2014",
                edition.get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString());
        assertNull(edition.get(CertifiedProductSearchDetails.EDITION_ID_KEY));
    }

    @Test
    public void buildListing_EditionColumnInvalidChplProductNumber_ReturnsEditionFromColumn() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERT_YEAR__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("BADNUMBER,2015");
        assertNotNull(listingRecords);

        Map<String, Object> edition = handler.handle(headingRecord, listingRecords);
        assertNotNull(edition);
        assertEquals("2015",
                edition.get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString());
        assertNull(edition.get(CertifiedProductSearchDetails.EDITION_ID_KEY));
    }
}
