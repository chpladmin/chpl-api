package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TargetedUsersUploadHandlerTest {
    private static final String HEADER_ROW = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C,Developer-Identified Target Users";
    private static final String LISTING_ROW = "15.02.02.3007.A056.01.00.0.180214,New,Pediatrics";
    private static final String LISTING_ROWS = "15.02.02.3007.A056.01.00.0.180214,New,User 1\n"
            + "15.02.02.3007.A056.01.00.0.180214,Subelement,User 2";

    private TargetedUserDAO dao;
    private TargetedUsersUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        dao = Mockito.mock(TargetedUserDAO.class);
        handler = new TargetedUsersUploadHandler(handlerUtil, dao, msgUtil);
    }

    @Test
    public void parseUsers_MultipleValidUsers_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROWS);
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("User 1")))
            .thenReturn(buildDto(1L, "User 1"));
        Mockito.when(dao.getByName(ArgumentMatchers.eq("User 2")))
        .thenReturn(buildDto(2L, "User 2"));

        try {
            List<CertifiedProductTargetedUser> foundTargetedUsers = handler.handle(headingRecord, listingRecords);
            assertNotNull(foundTargetedUsers);
            assertEquals(2, foundTargetedUsers.size());
            foundTargetedUsers.stream().forEach(tu -> {
                assertNull(tu.getId());
                assertNotNull(tu.getTargetedUserId());
                assertNotNull(tu.getTargetedUserName());
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseUsers_OneValidOneInvalidUser_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROWS);
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("User 1")))
            .thenReturn(buildDto(1L, "User 1"));
        Mockito.when(dao.getByName(ArgumentMatchers.eq("User 2")))
        .thenReturn(null);

        try {
            List<CertifiedProductTargetedUser> foundTargetedUsers = handler.handle(headingRecord, listingRecords);
            assertNotNull(foundTargetedUsers);
            assertEquals(2, foundTargetedUsers.size());
            foundTargetedUsers.stream().forEach(tu -> {
                assertNull(tu.getId());
                assertNotNull(tu.getTargetedUserName());
                if (tu.getTargetedUserId() != null) {
                    assertEquals(1, tu.getTargetedUserId().longValue());
                } else {
                    assertEquals("User 2", tu.getTargetedUserName());
                }
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseUsers_SingleValidUser_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW);
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("Pediatrics")))
            .thenReturn(buildDto(1L, "Pediatrics"));

        try {
            List<CertifiedProductTargetedUser> foundTargetedUsers = handler.handle(headingRecord, listingRecords);
            assertNotNull(foundTargetedUsers);
            assertEquals(1, foundTargetedUsers.size());
            foundTargetedUsers.stream().forEach(tu -> {
                assertNull(tu.getId());
                assertNotNull(tu.getTargetedUserId());
                assertEquals(1, tu.getTargetedUserId().longValue());
                assertNotNull(tu.getTargetedUserName());
                assertEquals("Pediatrics", tu.getTargetedUserName());
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseUsers_SingleInvalidUser_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW);
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("Pediatrics"))).thenReturn(null);

        try {
            List<CertifiedProductTargetedUser> foundTargetedUsers = handler.handle(headingRecord, listingRecords);
            assertNotNull(foundTargetedUsers);
            assertEquals(1, foundTargetedUsers.size());
            foundTargetedUsers.stream().forEach(tu -> {
                assertNull(tu.getId());
                assertNull(tu.getTargetedUserId());
                assertNotNull(tu.getTargetedUserName());
                assertEquals("Pediatrics", tu.getTargetedUserName());
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseUsers_UserWithWhitespace_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                "15.02.02.3007.A056.01.00.0.180214,New,  Test ");
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("Test")))
            .thenReturn(buildDto(1L, "Test"));

        try {
            List<CertifiedProductTargetedUser> foundTargetedUsers = handler.handle(headingRecord, listingRecords);
            assertNotNull(foundTargetedUsers);
            assertEquals(1, foundTargetedUsers.size());
            foundTargetedUsers.stream().forEach(tu -> {
                assertNull(tu.getId());
                assertNotNull(tu.getTargetedUserId());
                assertEquals(1, tu.getTargetedUserId().longValue());
                assertNotNull(tu.getTargetedUserName());
                assertEquals("Test", tu.getTargetedUserName());
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private TargetedUserDTO buildDto(Long id, String name) {
        TargetedUserDTO dto = new TargetedUserDTO();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}
