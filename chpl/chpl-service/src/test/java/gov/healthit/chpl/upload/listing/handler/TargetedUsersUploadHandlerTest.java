package gov.healthit.chpl.upload.listing.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVRecord;
import org.ff4j.FF4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.targeteduser.CertifiedProductTargetedUser;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TargetedUsersUploadHandlerTest {
    private static final String HEADER_ROW = "UNIQUE_CHPL_ID__C,Developer-Identified Target Users";
    private static final String LISTING_ROW = "15.02.02.3007.A056.01.00.0.180214,Pediatrics";
    private static final String LISTING_ROWS = "15.02.02.3007.A056.01.00.0.180214,User 1\n"
            + "15.02.02.3007.A056.01.00.0.180214,User 2";

    private TargetedUsersUploadHandler handler;

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
