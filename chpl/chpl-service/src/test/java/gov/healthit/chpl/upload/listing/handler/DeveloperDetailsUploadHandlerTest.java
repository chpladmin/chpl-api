package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperDetailsUploadHandlerTest {
    private static final String HEADER_ROW = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C";
    private static final String LISTING_ROW = "15.02.02.3007.A056.01.00.0.180214,New";

    private DeveloperDAO dao;
    private DeveloperDetailsUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        dao = Mockito.mock(DeveloperDAO.class);
        handler = new DeveloperDetailsUploadHandler(handlerUtil, dao, msgUtil);
    }

    @Test
    public void parseDeveloperName_ExistingDeveloper_ReturnsCorrectly() {
        String developerName = "My Developer";
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",VENDOR__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + "," + developerName);
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq(developerName)))
            .thenReturn(buildDto(1L, developerName));

        try {
            Developer developer = handler.handle(headingRecord, listingRecords);
            assertNotNull(developer);
            assertEquals(1L, developer.getDeveloperId());
            assertEquals(developerName, developer.getName());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseDeveloperName_NewDeveloper_ReturnsCorrectly() {
        String developerName = "My Developer";
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",VENDOR__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + "," + developerName);
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq(developerName))).thenReturn(null);

        try {
            Developer developer = handler.handle(headingRecord, listingRecords);
            assertNotNull(developer);
            assertNull(developer.getDeveloperId());
            assertEquals(developerName, developer.getName());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseDeveloperWebsite_HasData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",VENDOR_WEBSITE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + ",http://www.test.com");
        assertNotNull(listingRecords);

        try {
            Developer developer = handler.handle(headingRecord, listingRecords);
            assertNotNull(developer);
            assertNotNull(developer.getWebsite());
            assertEquals("http://www.test.com", developer.getWebsite());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseDeveloperWebsite_MissingData_ParsesEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",VENDOR_WEBSITE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + ",");
        assertNotNull(listingRecords);

        try {
            Developer developer = handler.handle(headingRecord, listingRecords);
            assertNotNull(developer);
            assertNotNull(developer.getWebsite());
            assertEquals("", developer.getWebsite());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseSelfDeveloper_TrueData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",Self-developer").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + ",1");
        assertNotNull(listingRecords);

        try {
            Developer developer = handler.handle(headingRecord, listingRecords);
            assertNotNull(developer);
            assertNotNull(developer.getSelfDeveloper());
            assertEquals(Boolean.TRUE, developer.getSelfDeveloper());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseSelfDeveloper_FalseData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",Self-developer").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + ",0");
        assertNotNull(listingRecords);

        try {
            Developer developer = handler.handle(headingRecord, listingRecords);
            assertNotNull(developer);
            assertNotNull(developer.getSelfDeveloper());
            assertEquals(Boolean.FALSE, developer.getSelfDeveloper());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test(expected = ValidationException.class)
    public void parseSelfDeveloper_BadData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",Self-developer").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + ",BAD");
        assertNotNull(listingRecords);
        handler.handle(headingRecord, listingRecords);
    }

    @Test
    public void parseDeveloperAddress_AllAddressFields_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",VENDOR_STREET_ADDRESS__C,VENDOR_CITY__C,VENDOR_STATE__C,VENDOR_ZIP__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + "," + "111 Rocky Rd.,Baltimore,MD,21205");
        assertNotNull(listingRecords);

        try {
            Developer developer = handler.handle(headingRecord, listingRecords);
            assertNotNull(developer);
            Address address = developer.getAddress();
            assertNotNull(address);
            assertEquals("111 Rocky Rd.", address.getLine1());
            assertEquals("Baltimore", address.getCity());
            assertEquals("MD", address.getState());
            assertEquals("21205", address.getZipcode());
            assertNull(address.getCountry());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseDeveloperContact_AllContactFields_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW + ",VENDOR_EMAIL__C,VENDOR_PHONE__C,VENDOR_CONTACT_NAME__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW + "," + "test@ainq.com,333-444-5555,First Last");
        assertNotNull(listingRecords);

        try {
            Developer developer = handler.handle(headingRecord, listingRecords);
            assertNotNull(developer);
            Contact contact = developer.getContact();
            assertNotNull(contact);
            assertEquals("test@ainq.com", contact.getEmail());
            assertEquals("333-444-5555", contact.getPhoneNumber());
            assertEquals("First Last", contact.getFullName());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private DeveloperDTO buildDto(Long id, String name) {
        DeveloperDTO dto = new DeveloperDTO();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}
