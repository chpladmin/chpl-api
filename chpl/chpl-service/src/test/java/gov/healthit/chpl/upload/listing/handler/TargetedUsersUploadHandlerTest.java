package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TargetedUsersUploadHandlerTest {
    private static final String HEADER_ROW = "UNIQUE_CHPL_ID__C,Developer-Identified Target Users";
    private static final String LISTING_ROW = "15.02.02.3007.A056.01.00.0.180214,Pediatrics";
    private static final String LISTING_ROWS = "15.02.02.3007.A056.01.00.0.180214,User 1\n"
            + "15.02.02.3007.A056.01.00.0.180214,User 2";

    private TargetedUsersUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new TargetedUsersUploadHandler(handlerUtil);
    }

    @Test
    public void parseUsers_NoUsersColumn_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("14.0.0");
        assertNotNull(listingRecords);

        List<CertifiedProductTargetedUser> foundTargetedUsers = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundTargetedUsers);
        assertEquals(0, foundTargetedUsers.size());
    }

    @Test
    public void parseUsers_UsersColumnNoData_ReturnsListWithEmptyItem() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("14.0.0,");
        assertNotNull(listingRecords);

        List<CertifiedProductTargetedUser> foundTargetedUsers = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundTargetedUsers);
        assertEquals(0, foundTargetedUsers.size());
    }


    @Test
    public void parseUsers_MultipleUsers_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROWS);
        assertNotNull(listingRecords);

        List<CertifiedProductTargetedUser> foundTargetedUsers = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundTargetedUsers);
        assertEquals(2, foundTargetedUsers.size());
        foundTargetedUsers.stream().forEach(tu -> {
            assertNull(tu.getId());
            assertNull(tu.getTargetedUserId());
            assertNotNull(tu.getTargetedUserName());
        });
    }

    @Test
    public void parseUsers_SingleUser_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW);
        assertNotNull(listingRecords);

        List<CertifiedProductTargetedUser> foundTargetedUsers = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundTargetedUsers);
        assertEquals(1, foundTargetedUsers.size());
        foundTargetedUsers.stream().forEach(tu -> {
            assertNull(tu.getId());
            assertNull(tu.getTargetedUserId());
            assertNotNull(tu.getTargetedUserName());
            assertEquals("Pediatrics", tu.getTargetedUserName());
        });
    }

    @Test
    public void parseUsers_UserWithWhitespace_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                "15.02.02.3007.A056.01.00.0.180214,  Test ");
        assertNotNull(listingRecords);

        List<CertifiedProductTargetedUser> foundTargetedUsers = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundTargetedUsers);
        assertEquals(1, foundTargetedUsers.size());
        foundTargetedUsers.stream().forEach(tu -> {
            assertNull(tu.getId());
            assertNull(tu.getTargetedUserId());
            assertNotNull(tu.getTargetedUserName());
            assertEquals("Test", tu.getTargetedUserName());
        });
    }
}
