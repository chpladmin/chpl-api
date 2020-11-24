package gov.healthit.chpl.upload.listing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ListingUploadHandlerUtilTest {
    private static final String HEADER_2015_V19 = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C,VENDOR__C,PRODUCT__C,VERSION__C,CERT_YEAR__C,ACB_CERTIFICATION_ID__C,CERTIFYING_ACB__C,TESTING_ATL__C,CERTIFICATION_DATE__C,VENDOR_STREET_ADDRESS__C,VENDOR_STATE__C,VENDOR_CITY__C,VENDOR_ZIP__C,VENDOR_WEBSITE__C,Self-developer,VENDOR_EMAIL__C,VENDOR_PHONE__C,VENDOR_CONTACT_NAME__C,Developer-Identified Target Users,QMS Standard,QMS Standard Applicable Criteria,QMS Modification Description,ICS,ICS Source,Accessibility Certified,Accessibility Standard,170.523(k)(1) URL,CQM Number,CQM Version,CQM Criteria,SED Report Hyperlink,Description of the Intended Users,Date SED Testing was Concluded,Participant Identifier,Participant Gender,Participant Age,Participant Education,Participant Occupation/Role,Participant Professional Experience,Participant Computer Experience,Participant Product Experience,Participant Assistive Technology Needs,Task Identifier,Task Description,Task Success - Mean (%),Task Success - Standard Deviation (%),Task Path Deviation - Observed #,Task Path Deviation - Optimal #,Task Time - Mean (seconds),Task Time - Standard Deviation (seconds),Task Time Deviation - Observed Seconds,Task Time Deviation - Optimal Seconds,Task Errors  Mean(%),Task Errors - Standard Deviation (%),Task Rating - Scale Type,Task Rating,Task Rating - Standard Deviation,CRITERIA_170_315_A_1__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_2__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_3__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_4__C,GAP,Privacy and Security Framework,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_5__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_9__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_10__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_12__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_13__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_14__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_15__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_B_1__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_1_Cures__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_2__C,Privacy and Security Framework,Standard Tested Against,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_2_Cures__C,Privacy and Security Framework,Standard Tested Against,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_3_Cures__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_6__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_7__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_B_7_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_B_8__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_8_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_9__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_9_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_10__C,Privacy and Security Framework,Export Documentation,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_C_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_3__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_3_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_4__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_D_1__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_2__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_2_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_3__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_3_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_4__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_5__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_6__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_7__C,GAP,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_8__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_9__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_10__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_10_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_11__C,GAP,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_12__C,Attestation Answer,Documentation URL,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_D_13__C,Attestation Answer,Documentation URL,Use Cases,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_E_1__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_E_1_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_E_2__C,Privacy and Security Framework,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_E_3__C,Privacy and Security Framework,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_3__C,GAP,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_4__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_5__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_5_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_6__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_F_7__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_1__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_2__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_3__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_4__C,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_5__C,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_6__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_6_CURES__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_7__C,Privacy and Security Framework,API Documentation Link,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_G_8__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_9__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_9_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_10__C,Privacy and Security Framework,Standard Tested Against,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_H_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_H_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description";
    private static final String HEADER_COMMON_NAMES = "UNIQUE_CHPL_ID__C,VENDOR__C";
    private static final String HEADER_ALT_NAMES = "UNIQUE_CHPL_ID__C,VENDOR_C";
    private static final String HEADER_WITH_SPACES = " UNIQUE_CHPL_ID__C , VENDOR__C ";
    private static final String HEADER_DUPLICATE = "UNIQUE_CHPL_ID__C,UNIQUE_CHPL_ID__C,VENDOR__C";
    private static final String MULTIPLE_ROWS = "UNIQUE_CHPL_ID__C,VENDOR__C\n"
                                                + "15.02.02.3007.A056.01.00.0.180214,DevName";
    private static final String LISTING_ROW = "15.02.02.3007.A056.01.00.0.180214,DevName";

    private String listing1Csv;
    private ErrorMessageUtil msgUtil;
    private ListingUploadHandlerUtil handlerUtil;

    @Before
    public void setup() throws InvalidArgumentsException, JsonProcessingException,
        EntityRetrievalException, EntityCreationException, IOException, FileNotFoundException {
        loadFile();
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.emptyFile"))).thenReturn("Empty file message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.notCSV"))).thenReturn("Not CSV message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.emptyRows"))).thenReturn("Header only message");

        handlerUtil = new ListingUploadHandlerUtil(msgUtil);
    }

    @Test
    public void getHeadingRecordIndex_EmptyData_ReturnsCorrectValue() {
        int index = handlerUtil.getHeadingRecordIndex(new ArrayList<CSVRecord>());
        assertEquals(-1, index);
    }

    @Test
    public void getHeadingRecordIndex_NullData_ReturnsCorrectValue() {
        int index = handlerUtil.getHeadingRecordIndex(null);
        assertEquals(-1, index);
    }

    @Test
    public void getHeadingRecordIndex_HeadingWithCriteriaLevelRows_ReturnsCorrectValue() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_A_1__C");
        int index = handlerUtil.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecordIndex_HeadingOnly_ReturnsCorrectValue() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(HEADER_COMMON_NAMES);
        int index = handlerUtil.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecordIndex_HeadingWithSpaces_ReturnsCorrectValue() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(HEADER_WITH_SPACES);
        int index = handlerUtil.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecordIndex_HeadingOnlyAlternateNames_ReturnsCorrectValue() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(HEADER_ALT_NAMES);
        int index = handlerUtil.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecordIndex_HeadingOnlySingleColumn_ReturnsCorrectValue() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C");
        int index = handlerUtil.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecordIndex_MultiRowData_ReturnsCorrectValue() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(MULTIPLE_ROWS);
        int index = handlerUtil.getHeadingRecordIndex(records);
        assertEquals(0, index);
    }

    @Test
    public void getHeadingRecord_MultiRowData_ReturnsCorrectValue() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(MULTIPLE_ROWS);
        assertEquals(2, records.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(records);
        assertNotNull(heading);
        assertEquals("UNIQUE_CHPL_ID__C", heading.get(0));
    }

    @Test
    public void getStartIndexOfNextCertResult_NoCriteriaColumns_ReturnsNegativeNumber() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(HEADER_COMMON_NAMES);
        assertEquals(1, records.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(records);
        assertNotNull(heading);
        int index = handlerUtil.getNextIndexOfCertificationResult(0, heading);
        assertTrue(index < 0);
    }

    @Test
    public void getStartIndexOfNextCertResult_OnlyCriteriaColumns_ReturnsZero() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_A_1__C");
        assertEquals(1, records.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(records);
        assertNotNull(heading);
        int index = handlerUtil.getNextIndexOfCertificationResult(0, heading);
        assertEquals(0, index);
    }

    @Test
    public void getStartIndexOfNextCertResult_ListingAndCriteriaColumns_ReturnsCorrectIndex() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(
                "UNIQUE_CHPL_ID__C,VENDOR__C,CRITERIA_170_315_A_1__C");
        assertEquals(1, records.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(records);
        assertNotNull(heading);
        int index = handlerUtil.getNextIndexOfCertificationResult(0, heading);
        assertEquals(2, index);
    }

    @Test
    public void getStartIndexOfNextCertResult_CriteriaAndTestDataColumns_ReturnsCorrectIndex() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_A_1__C,"
                + "Test Data,Test data version,Test data alteration description");
        assertEquals(1, records.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(records);
        assertNotNull(heading);
        int index = handlerUtil.getNextIndexOfCertificationResult(0, heading);
        assertEquals(0, index);
    }


    @Test
    public void getStartIndexOfNextCertResult_MultipleCriteriaColumnsNoData_ReturnsCorrectIndices() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_A_1__C,CRITERIA_170_315_A_2__C");
        assertEquals(1, records.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(records);
        assertNotNull(heading);
        int index = handlerUtil.getNextIndexOfCertificationResult(0, heading);
        assertEquals(0, index);
        index = handlerUtil.getNextIndexOfCertificationResult(1, heading);
        assertEquals(1, index);
    }

    @Test
    public void getStartIndexOfNextCertResult_MultipleCriteriaColumnsWithData_ReturnsCorrectIndices() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test tool name,Test tool version,"
                + "CRITERIA_170_315_A_2__C,Privacy and Security Framework,Functionality Tested,Standard Tested Against");
        assertEquals(1, records.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(records);
        assertNotNull(heading);
        int index = handlerUtil.getNextIndexOfCertificationResult(0, heading);
        assertEquals(0, index);
        index = handlerUtil.getNextIndexOfCertificationResult(1, heading);
        assertEquals(3, index);
    }

    @Test
    public void getStartIndexOfNextCertResult_ListingColumnsAtEnd_ReturnsCorrectIndices() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_2__C,Privacy and Security Framework,Functionality Tested,Standard Tested Against,"
                + "UNIQUE_CHPL_ID__C,VENDOR__C");
        assertEquals(1, records.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(records);
        assertNotNull(heading);
        int index = handlerUtil.getNextIndexOfCertificationResult(0, heading);
        assertEquals(0, index);
    }

    @Test
    public void getStartIndexOfNextCertResult_ListingColumnsInMiddle_ReturnsCorrectIndices() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test tool name,UNIQUE_CHPL_ID__C,VENDOR__C,Test tool version,CRITERIA_170_315_A_2__C");
        assertEquals(1, records.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(records);
        assertNotNull(heading);
        int index = handlerUtil.getNextIndexOfCertificationResult(0, heading);
        assertEquals(0, index);
        index = handlerUtil.getNextIndexOfCertificationResult(1, heading);
        assertEquals(5, index);
    }

    @Test
    public void getStartIndexOfNextCertResult_UnknownColumnsBetweenCriteria_ReturnsCorrectIndices() {
        List<CSVRecord> records = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test tool name,JUNK,Test tool version,CRITERIA_170_315_A_2__C");
        assertEquals(1, records.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(records);
        assertNotNull(heading);
        int index = handlerUtil.getNextIndexOfCertificationResult(0, heading);
        assertEquals(0, index);
        index = handlerUtil.getNextIndexOfCertificationResult(1, heading);
        assertEquals(4, index);
    }

    @Test
    public void getCertResultRecords_NoCriteriaColumns_ReturnsNull() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                HEADER_COMMON_NAMES + "\n,,");
        assertEquals(2, certResultRecords.size());
        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords);
        assertNull(parsedCertResultRecords);
    }

    @Test
    public void getCertResultRecords_CriteriaColumnOnly_ReturnsCorrectColumns() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C\n0");
        assertEquals(2, certResultRecords.size());
        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(1, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_1__C", parsedHeading.get(0));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals("0", parsedData.get(0));
    }

    @Test
    public void getCertResultRecords_ListingAndCriteriaColumns_ReturnsCorrectColumns() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "UNIQUE_CHPL_ID__C,VENDOR__C,CRITERIA_170_315_A_1__C\n14.05,dev,0");
        assertEquals(2, certResultRecords.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(1, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_1__C", parsedHeading.get(0));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(1, parsedData.size());
        assertEquals("0", parsedData.get(0));
    }

    @Test
    public void getCertResultRecords_CriteriaAndTestDataColumns_ReturnsCorrectColumns() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test Data,Test data version,Test data alteration description\n"
                + "1,something,v1,test");
        assertEquals(2, certResultRecords.size());
        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(4, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_1__C", parsedHeading.get(0));
        assertEquals("Test Data", parsedHeading.get(1));
        assertEquals("Test data version", parsedHeading.get(2));
        assertEquals("Test data alteration description", parsedHeading.get(3));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(4, parsedData.size());
        assertEquals("1", parsedData.get(0));
        assertEquals("something", parsedData.get(1));
        assertEquals("v1", parsedData.get(2));
        assertEquals("test", parsedData.get(3));
    }

    @Test
    public void getCertResultRecords_CriteriaAndTestDataWithInnerCommaColumns_ReturnsCorrectColumns() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test Data,Test data version,Test data alteration description\n"
                + "1,something,v1,\"i altered this, and i'm not sorry\"");
        assertEquals(2, certResultRecords.size());
        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(4, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_1__C", parsedHeading.get(0));
        assertEquals("Test Data", parsedHeading.get(1));
        assertEquals("Test data version", parsedHeading.get(2));
        assertEquals("Test data alteration description", parsedHeading.get(3));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(4, parsedData.size());
        assertEquals("1", parsedData.get(0));
        assertEquals("something", parsedData.get(1));
        assertEquals("v1", parsedData.get(2));
        assertEquals("i altered this, and i'm not sorry", parsedData.get(3));
    }

    @Test
    public void getCertResultRecords_CriteriaAndTestDataWithInnerLineBreakColumns_ReturnsCorrectColumns() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test Data,Test data version,Test data alteration description\n"
                + "1,something,v1,\"i altered this\nand i'm not sorry\"");
        assertEquals(2, certResultRecords.size());
        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(4, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_1__C", parsedHeading.get(0));
        assertEquals("Test Data", parsedHeading.get(1));
        assertEquals("Test data version", parsedHeading.get(2));
        assertEquals("Test data alteration description", parsedHeading.get(3));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(4, parsedData.size());
        assertEquals("1", parsedData.get(0));
        assertEquals("something", parsedData.get(1));
        assertEquals("v1", parsedData.get(2));
        assertEquals("i altered this\nand i'm not sorry", parsedData.get(3));
    }

    @Test
    public void getCertResultRecords_MultipleCriteriaColumns_ReturnsCorrectColumns() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,CRITERIA_170_315_A_2__C\n0,1");
        assertEquals(2, certResultRecords.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(1, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_1__C", parsedHeading.get(0));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(1, parsedData.size());
        assertEquals("0", parsedData.get(0));

        parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                1, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(1, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_2__C", parsedHeading.get(0));
        parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(1, parsedData.size());
        assertEquals("1", parsedData.get(0));
    }

    @Test
    public void getCertResultRecords_MultipleCriteriaColumnsWithData_ReturnsCorrectColumns() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test tool name,Test tool version,"
                + "CRITERIA_170_315_A_2__C,Privacy and Security Framework,Functionality Tested,Standard Tested Against\n"
                + "1,ttname,ver1,1,Approach 1,func1,std1");
        assertEquals(2, certResultRecords.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(3, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_1__C", parsedHeading.get(0));
        assertEquals("Test tool name", parsedHeading.get(1));
        assertEquals("Test tool version", parsedHeading.get(2));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(3, parsedData.size());
        assertEquals("1", parsedData.get(0));
        assertEquals("ttname", parsedData.get(1));
        assertEquals("ver1", parsedData.get(2));

        parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                1, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(4, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_2__C", parsedHeading.get(0));
        assertEquals("Privacy and Security Framework", parsedHeading.get(1));
        assertEquals("Functionality Tested", parsedHeading.get(2));
        assertEquals("Standard Tested Against", parsedHeading.get(3));
        parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(4, parsedData.size());
        assertEquals("1", parsedData.get(0));
        assertEquals("Approach 1", parsedData.get(1));
        assertEquals("func1", parsedData.get(2));
        assertEquals("std1", parsedData.get(3));
    }

    @Test
    public void getCertResultRecords_ListingColumnsAtEnd_IgnoresListingColumns() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_2__C,Privacy and Security Framework,Functionality Tested,Standard Tested Against,"
                + "UNIQUE_CHPL_ID__C,VENDOR__C\n"
                + "1,Approach 1,func1,std1,chpid,dev");
        assertEquals(2, certResultRecords.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(4, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_2__C", parsedHeading.get(0));
        assertEquals("Privacy and Security Framework", parsedHeading.get(1));
        assertEquals("Functionality Tested", parsedHeading.get(2));
        assertEquals("Standard Tested Against", parsedHeading.get(3));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(4, parsedData.size());
        assertEquals("1", parsedData.get(0));
        assertEquals("Approach 1", parsedData.get(1));
        assertEquals("func1", parsedData.get(2));
        assertEquals("std1", parsedData.get(3));
    }

    @Test
    public void getCertResultRecords_ListingColumnsInMiddle_IgnoresListingColumns() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test tool name,UNIQUE_CHPL_ID__C,VENDOR__C,Test tool version\n"
                + "1,ttname,chplid,dev,v1");
        assertEquals(2, certResultRecords.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(3, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_1__C", parsedHeading.get(0));
        assertEquals("Test tool name", parsedHeading.get(1));
        assertEquals("Test tool version", parsedHeading.get(2));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(3, parsedData.size());
        assertEquals("1", parsedData.get(0));
        assertEquals("ttname", parsedData.get(1));
        assertEquals("v1", parsedData.get(2));
    }

    @Test
    public void getCertResultRecords_UnknownColumnsInMiddle_IgnoresUnknownColumn() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test tool name,JUNK,Test tool version\n"
                + "1,ttname,junkdata,v1");
        assertEquals(2, certResultRecords.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(3, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_1__C", parsedHeading.get(0));
        assertEquals("Test tool name", parsedHeading.get(1));
        assertEquals("Test tool version", parsedHeading.get(2));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(3, parsedData.size());
        assertEquals("1", parsedData.get(0));
        assertEquals("ttname", parsedData.get(1));
        assertEquals("v1", parsedData.get(2));
    }

    @Test
    public void getCertResultRecords_UnknownColumnsAtEnd_IgnoresUnknownColumn() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_2__C,Privacy and Security Framework,Functionality Tested,Standard Tested Against,JUNK\n"
                + "1,Approach 1,func1,std1,junkdata");
        assertEquals(2, certResultRecords.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(4, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_2__C", parsedHeading.get(0));
        assertEquals("Privacy and Security Framework", parsedHeading.get(1));
        assertEquals("Functionality Tested", parsedHeading.get(2));
        assertEquals("Standard Tested Against", parsedHeading.get(3));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(4, parsedData.size());
        assertEquals("1", parsedData.get(0));
        assertEquals("Approach 1", parsedData.get(1));
        assertEquals("func1", parsedData.get(2));
        assertEquals("std1", parsedData.get(3));
    }

    @Test
    public void getCertResultRecords_MultipleDataRows_ParsesCorrectly() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Test tool name,Test tool version\n"
                + "1,ttname,v1\n"
                + ",ttname2,v2\n"
                + ",ttname3,v3");
        assertEquals(4, certResultRecords.size());

        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 4));
        assertNotNull(parsedCertResultRecords);
        assertEquals(4, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(3, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_1__C", parsedHeading.get(0));
        assertEquals("Test tool name", parsedHeading.get(1));
        assertEquals("Test tool version", parsedHeading.get(2));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(3, parsedData.size());
        assertEquals("1", parsedData.get(0));
        assertEquals("ttname", parsedData.get(1));
        assertEquals("v1", parsedData.get(2));
        parsedData = parsedCertResultRecords.get(2);
        assertNotNull(parsedData);
        assertEquals(3, parsedData.size());
        assertEquals("", parsedData.get(0));
        assertEquals("ttname2", parsedData.get(1));
        assertEquals("v2", parsedData.get(2));
        parsedData = parsedCertResultRecords.get(3);
        assertNotNull(parsedData);
        assertEquals(3, parsedData.size());
        assertEquals("", parsedData.get(0));
        assertEquals("ttname3", parsedData.get(1));
        assertEquals("v3", parsedData.get(2));
    }

    @Test
    public void getCertResultRecords_CriteriaWithTaskIdColumn_ReturnsCorrectColumns() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Task Identifier\n1,A1.1");
        assertEquals(2, certResultRecords.size());
        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(2, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_1__C", parsedHeading.get(0));
        assertEquals("Task Identifier", parsedHeading.get(1));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(2, parsedData.size());
        assertEquals("1", parsedData.get(0));
        assertEquals("A1.1", parsedData.get(1));
    }

    @Test
    public void getCertResultRecords_CriteriaWithParticipantIdColumn_ReturnsCorrectColumns() {
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(
                "CRITERIA_170_315_A_1__C,Participant Identifier\n1,A1.1");
        assertEquals(2, certResultRecords.size());
        CSVRecord heading = handlerUtil.getHeadingRecord(certResultRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, certResultRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(2, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_1__C", parsedHeading.get(0));
        assertEquals("Participant Identifier", parsedHeading.get(1));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(2, parsedData.size());
        assertEquals("1", parsedData.get(0));
        assertEquals("A1.1", parsedData.get(1));
    }

    @Test
    public void getCertResultRecords_TaskIdAtListingAndCertResultLevel_ReturnsCorrectCertResult() {
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                "UNIQUE_CHPL_ID__C,VENDOR__C,Task Identifier,CRITERIA_170_315_A_1__C,Task Identifier\n"
                + "14.05.05,developer name,A1.1,1,A1.1");
        assertEquals(2, listingRecords.size());
        CSVRecord heading = handlerUtil.getHeadingRecord(listingRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, listingRecords.subList(1, 2));
        assertNotNull(parsedCertResultRecords);
        assertEquals(2, parsedCertResultRecords.size());

        CSVRecord parsedHeading = parsedCertResultRecords.get(0);
        assertNotNull(parsedHeading);
        assertEquals(2, parsedHeading.size());
        assertEquals("CRITERIA_170_315_A_1__C", parsedHeading.get(0));
        assertEquals("Task Identifier", parsedHeading.get(1));
        CSVRecord parsedData = parsedCertResultRecords.get(1);
        assertNotNull(parsedData);
        assertEquals(2, parsedData.size());
        assertEquals("1", parsedData.get(0));
        assertEquals("A1.1", parsedData.get(1));
    }

    @Test
    public void getCertResultRecords_V19UploadTemplateColumns_ReturnsCorrectCertResult() {
        String fileContents = HEADER_2015_V19 + "\n" + listing1Csv;
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(fileContents);
        assertEquals(18, listingRecords.size());
        CSVRecord heading = handlerUtil.getHeadingRecord(listingRecords);
        assertNotNull(heading);
        List<CSVRecord> parsedCertResultRecords = handlerUtil.getCertificationResultRecordsFromIndex(
                0, heading, listingRecords.subList(1, 18));
        assertNotNull(parsedCertResultRecords);
        assertEquals(18, parsedCertResultRecords.size());
        //A_1 has 17 columns
        assertEquals(17, parsedCertResultRecords.get(0).size());
    }

    @Test(expected = ValidationException.class)
    public void getChplProductNumber_NoChplProductNumberHeading_ThrowsException()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString("PRODUCT__C");
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW);
        handlerUtil.parseRequiredSingleRowField(Headings.UNIQUE_ID, headingRecords.get(0), listingRecords);
    }

    @Test
    public void getChplProductNumber_EmptyData_ReturnsNullValue()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString(HEADER_COMMON_NAMES);
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        String chplProductNumber = handlerUtil.parseRequiredSingleRowField(
                Headings.UNIQUE_ID, headingRecords.get(0), new ArrayList<CSVRecord>());
        assertNull(chplProductNumber);
    }

    @Test
    public void getChplProductNumber_EmptyStringData_ReturnsEmptyValue()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString(HEADER_COMMON_NAMES);
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(",,");
        String chplProductNumber = handlerUtil.parseRequiredSingleRowField(
                Headings.UNIQUE_ID, headingRecords.get(0), listingRecords);
        assertNotNull(chplProductNumber);
        assertEquals("", chplProductNumber);
    }

    @Test
    public void getChplProductNumber_MultiRowData_ReturnsCorrectValue()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString(HEADER_COMMON_NAMES);
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW);
        String chplProductNumber = handlerUtil.parseRequiredSingleRowField(
                Headings.UNIQUE_ID, headingRecords.get(0), listingRecords);
        assertNotNull(chplProductNumber);
        assertEquals("15.02.02.3007.A056.01.00.0.180214", chplProductNumber);
    }

    @Test
    public void getChplProductNumber_ExtraSpacesData_ReturnsTrimmedValue()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString(HEADER_COMMON_NAMES);
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(" extra spaces ,new,dev");
        String chplProductNumber = handlerUtil.parseRequiredSingleRowField(
                Headings.UNIQUE_ID, headingRecords.get(0), listingRecords);
        assertNotNull(chplProductNumber);
        assertEquals("extra spaces", chplProductNumber);
    }

    @Test
    public void getChplProductNumber_ExtraSpacesHeaderAndData_ReturnsTrimmedValue()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString(HEADER_WITH_SPACES);
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(" extra spaces ,new");
        String chplProductNumber = handlerUtil.parseRequiredSingleRowField(
                Headings.UNIQUE_ID, headingRecords.get(0), listingRecords);
        assertNotNull(chplProductNumber);
        assertEquals("extra spaces", chplProductNumber);
    }

    @Test
    public void getChplProductNumber_DuplicateHeaders_ReturnsFirstValue()
            throws IOException {
        List<CSVRecord> headingRecords = ListingUploadTestUtil.getRecordsFromString(HEADER_DUPLICATE);
        assertNotNull(headingRecords);
        assertEquals(1, headingRecords.size());
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("first,second,status");
        String chplProductNumber = handlerUtil.parseRequiredSingleRowField(
                Headings.UNIQUE_ID, headingRecords.get(0), listingRecords);
        assertNotNull(chplProductNumber);
        assertEquals("first", chplProductNumber);
    }


    @Test
    public void parseCriteriaHeading_CuresHeading_ReturnsCorrectNumber() {
        String expectedResult = "170.315 (d)(12)";
        String d12Cures = "CRITERIA_170_315_D_12_Cures__C";
        String parsedNumber = handlerUtil.parseCriteriaNumberFromHeading(d12Cures);
        assertEquals(expectedResult, parsedNumber);
    }

    @Test
    public void parseCriteriaHeading_NonCuresHeading_ReturnsCorrectNumber() {
        String expectedResult = "170.315 (b)(3)";
        String b3 = "CRITERIA_170_315_B_3__C";
        String parsedNumber = handlerUtil.parseCriteriaNumberFromHeading(b3);
        assertEquals(expectedResult, parsedNumber);
    }

    @Test
    public void parseCriteriaHeading_B5AHeading_ReturnsCorrectNumber() {
        String expectedResult = "170.314 (b)(5)(A)";
        String b5A = "CRITERIA_170_314_B_5A__C";
        String parsedNumber = handlerUtil.parseCriteriaNumberFromHeading(b5A);
        assertEquals(expectedResult, parsedNumber);
    }

    @Test
    public void parseCriteriaHeading_InvalidHeading_ReturnsNull() {
        String badHeading = "DOES NOT MATCH";
        String parsedNumber = handlerUtil.parseCriteriaNumberFromHeading(badHeading);
        assertNull(parsedNumber);
    }

    @Test
    public void parseCriteriaHeading_CuresHeading_ReturnsIsCures() {
        String d12Cures = "CRITERIA_170_315_D_12_Cures__C";
        assertTrue(handlerUtil.isCriteriaNumberHeadingCures(d12Cures));
    }

    @Test
    public void parseCriteriaHeading_NonCuresHeading_ReturnsNotCures() {
        String d11 = "CRITERIA_170_315_D_11__C";
        assertFalse(handlerUtil.isCriteriaNumberHeadingCures(d11));
    }

    @Test
    public void parseCriteriaHeading_BadHeading_ReturnsNotCures() {
        String badHeading = "DOES NOT MATCH";
        assertFalse(handlerUtil.isCriteriaNumberHeadingCures(badHeading));
    }

    private void loadFile() throws IOException, FileNotFoundException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("upload_v19_listing1.csv");
        try {
            listing1Csv = IOUtils.toString(inputStream, "UTF-8");
        } finally {
            inputStream.close();
            IOUtils.closeQuietly(inputStream);
        }
        assertTrue(!StringUtils.isEmpty(listing1Csv));
    }
}
