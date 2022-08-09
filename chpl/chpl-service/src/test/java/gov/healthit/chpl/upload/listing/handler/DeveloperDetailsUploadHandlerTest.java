package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperDetailsUploadHandlerTest {
    private static final String HEADER_ROW = "UNIQUE_CHPL_ID__C";
    private static final String LISTING_ROW = "15.02.02.3007.A056.01.00.0.180214";

    private DeveloperDetailsUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new DeveloperDetailsUploadHandler(handlerUtil);
    }

    @Test
    public void parseDeveloperName_NoDeveloperFields_NullDeveloper() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW);
        assertNotNull(listingRecords);

        Developer developer = handler.handle(headingRecord, listingRecords);
        assertNull(developer);
    }

    @Test
    public void parseDeveloper_NameOnly_ReturnsCorrectly() {
        String developerName = "My Developer";
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",VENDOR__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + "," + developerName);
        assertNotNull(listingRecords);

        Developer developer = handler.handle(headingRecord, listingRecords);
        assertNotNull(developer);
        assertNull(developer.getId());
        assertEquals(developerName, developer.getUserEnteredName());
    }

    @Test
    public void parseDeveloper_WebsiteOnly_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",VENDOR_WEBSITE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + ",http://www.test.com");
        assertNotNull(listingRecords);

        Developer developer = handler.handle(headingRecord, listingRecords);
        assertNotNull(developer);
        assertNull(developer.getName());
        assertNotNull(developer.getUserEnteredWebsite());
        assertEquals("http://www.test.com", developer.getUserEnteredWebsite());
    }

    @Test
    public void parseDeveloper_WebsiteEmpty_ParsesEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",VENDOR_WEBSITE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + ",");
        assertNotNull(listingRecords);

        Developer developer = handler.handle(headingRecord, listingRecords);
        assertNotNull(developer);
        assertNotNull(developer.getUserEnteredWebsite());
        assertEquals("", developer.getUserEnteredWebsite());
    }

    @Test
    public void parseDeveloper_SelfDeveloper1_ParsesTrue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",Self-developer").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + ",1");
        assertNotNull(listingRecords);

        Developer developer = handler.handle(headingRecord, listingRecords);
        assertNotNull(developer);
        assertNotNull(developer.getUserEnteredSelfDeveloper());
        assertEquals("1", developer.getUserEnteredSelfDeveloper());
    }

    @Test
    public void parseDeveloper_SelfDeveloper0_ParsesFalse() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",Self-developer").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + ",0");
        assertNotNull(listingRecords);

        Developer developer = handler.handle(headingRecord, listingRecords);
        assertNotNull(developer);
        assertNotNull(developer.getUserEnteredSelfDeveloper());
        assertEquals("0", developer.getUserEnteredSelfDeveloper());
    }

    @Test
    public void parseDeveloper_SelfDeveloperBad_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",Self-developer").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + ",BAD");
        assertNotNull(listingRecords);
        Developer developer = handler.handle(headingRecord, listingRecords);
        assertNull(developer.getSelfDeveloper());
        assertNotNull(developer.getUserEnteredSelfDeveloper());
        assertEquals("BAD", developer.getUserEnteredSelfDeveloper());
    }

    @Test
    public void parseAddress_AllAddressFields_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",VENDOR_STREET_ADDRESS__C,VENDOR_STREET_ADDRESS_2__C,VENDOR_CITY__C,VENDOR_STATE__C,VENDOR_ZIP__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + "," + "111 Rocky Rd.,Suite 100,Baltimore,MD,21205");
        assertNotNull(listingRecords);

        Developer developer = handler.handle(headingRecord, listingRecords);
        assertNotNull(developer);
        Address address = developer.getUserEnteredAddress();
        assertNotNull(address);
        assertEquals("111 Rocky Rd.", address.getLine1());
        assertEquals("Suite 100", address.getLine2());
        assertEquals("Baltimore", address.getCity());
        assertEquals("MD", address.getState());
        assertEquals("21205", address.getZipcode());
        assertNull(address.getCountry());
    }

    @Test
    public void parseContact_AllContactFields_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",VENDOR_EMAIL__C,VENDOR_PHONE__C,VENDOR_CONTACT_NAME__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + "," + "test@ainq.com,333-444-5555,First Last");
        assertNotNull(listingRecords);

        Developer developer = handler.handle(headingRecord, listingRecords);
        assertNotNull(developer);
        PointOfContact contact = developer.getUserEnteredPointOfContact();
        assertNotNull(contact);
        assertEquals("test@ainq.com", contact.getEmail());
        assertEquals("333-444-5555", contact.getPhoneNumber());
        assertEquals("First Last", contact.getFullName());
    }
}
