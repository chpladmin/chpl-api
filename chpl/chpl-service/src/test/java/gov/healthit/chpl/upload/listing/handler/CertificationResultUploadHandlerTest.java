package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.criteriaattribute.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CertificationResultUploadHandlerTest {
    private static final String HEADER_ROW_BEGIN = "CRITERIA_170_315_B_3_Cures__C";

    private ErrorMessageUtil msgUtil;
    private ListingUploadHandlerUtil handlerUtil;
    private CertificationResultUploadHandler handler;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("certResult.upload.invalidBoolean"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("The value %s could not be converted to a yes/no field..", i.getArgument(1), ""));

        handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new CertificationResultUploadHandler(
                Mockito.mock(CertificationCriterionUploadHandler.class),
                Mockito.mock(AdditionalSoftwareUploadHandler.class),
                Mockito.mock(ConformanceMethodUploadHandler.class),
                Mockito.mock(TestToolUploadHandler.class),
                Mockito.mock(TestDataUploadHandler.class),
                handlerUtil);
    }

    @Test
    public void buildCertResult_SuccessFieldEmpty_ReturnsFalse() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString(" ");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.isSuccess());
        assertFalse(certResult.isSuccess());
    }

    @Test
    public void buildCertResult_SuccessField1_ReturnsTrue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.isSuccess());
        assertTrue(certResult.isSuccess());
    }

    @Test
    public void buildCertResult_SuccessField0_ReturnsFalse() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("0");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.isSuccess());
        assertFalse(certResult.isSuccess());
    }

    @Test
    public void buildCertResult_SuccessFieldDuplicate_ReturnsFirstValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + "," + HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("0,1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.isSuccess());
        assertFalse(certResult.isSuccess());
    }

    @Test
    public void buildCertResult_SuccessFieldInvalidBoolean_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("JUNK");
        assertNotNull(certResultRecords);
        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNull(certResult.isSuccess());
        assertNotNull(certResult.getSuccessStr());
        assertEquals("JUNK", certResult.getSuccessStr());
    }

    @Test
    public void buildCertResult_GapNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNull(certResult.isGap());
    }

    @Test
    public void buildCertResult_GapBooleanValue0_ReturnsFalse() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",GAP").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,0");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.isGap());
        assertFalse(certResult.isGap());
    }

    @Test
    public void buildCertResult_GapBooleanValue1_ReturnsTrue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",GAP").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.isGap());
        assertTrue(certResult.isGap());
    }

    @Test
    public void buildCertResult_GapBooleanValueNo_ReturnsFalse() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",GAP").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,No");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.isGap());
        assertFalse(certResult.isGap());
    }

    @Test
    public void buildCertResult_GapBooleanValueYes_ReturnsTrue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",GAP").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Yes");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.isGap());
        assertTrue(certResult.isGap());
    }

    @Test
    public void buildCertResult_GapValueDuplicate_ReturnsFirstValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",GAP,GAP").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Yes,0");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.isGap());
        assertTrue(certResult.isGap());
    }

    @Test
    public void buildCertResult_GapEmptyString_ReturnsFalse() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",GAP").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.isGap());
        assertFalse(certResult.isGap());
    }

    @Test
    public void buildCertResult_GapInvalidBoolean_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",GAP").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,JUNK");
        assertNotNull(certResultRecords);
        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNull(certResult.isGap());
        assertNotNull(certResult.getGapStr());
        assertEquals("JUNK", certResult.getGapStr());
    }

    @Test
    public void buildCertResult_AdditionalSoftwareNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNull(certResult.getHasAdditionalSoftware());
    }

    @Test
    public void buildCertResult_AdditionalSoftwareBooleanValue0_ReturnsFalse() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Additional Software").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,0");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getHasAdditionalSoftware());
        assertFalse(certResult.getHasAdditionalSoftware());
    }

    @Test
    public void buildCertResult_AdditionalSoftwareBooleanValue1_ReturnsTrue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Additional Software").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getHasAdditionalSoftware());
        assertTrue(certResult.getHasAdditionalSoftware());
    }

    @Test
    public void buildCertResult_AdditionalSoftwareBooleanValueNo_ReturnsFalse() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Additional Software").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,No");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getHasAdditionalSoftware());
        assertFalse(certResult.getHasAdditionalSoftware());
    }

    @Test
    public void buildCertResult_AdditionalSoftwareBooleanValueYes_ReturnsTrue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Additional Software").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Yes");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getHasAdditionalSoftware());
        assertTrue(certResult.getHasAdditionalSoftware());
    }

    @Test
    public void buildCertResult_AdditionalSoftwareValueDuplicate_ReturnsFirstValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Additional Software,Additional Software").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Yes,0");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getHasAdditionalSoftware());
        assertTrue(certResult.getHasAdditionalSoftware());
    }

    @Test
    public void buildCertResult_AdditionalSoftwareEmptyString_ReturnsFalse() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Additional Software").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getHasAdditionalSoftware());
        assertFalse(certResult.getHasAdditionalSoftware());
    }

    @Test
    public void buildCertResult_AdditionalSoftwareInvalidBoolean_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Additional Software").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,JUNK");
        assertNotNull(certResultRecords);
        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNull(certResult.getHasAdditionalSoftware());
        assertNotNull(certResult.getHasAdditionalSoftwareStr());
        assertEquals("JUNK", certResult.getHasAdditionalSoftwareStr());
    }

    @Test
    public void buildCertResult_functionalityTestedNoColumn_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getFunctionalitiesTested());
        assertEquals(0, certResult.getFunctionalitiesTested().size());
    }

    @Test
    public void buildCertResult_functionalityTestedEmptyData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Functionality Tested").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getFunctionalitiesTested());
        assertEquals(0, certResult.getFunctionalitiesTested().size());
    }

    @Test
    public void buildCertResult_functionalityTestedWithData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Functionality Tested").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,func");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getFunctionalitiesTested());
        assertEquals(1, certResult.getFunctionalitiesTested().size());
        CertificationResultFunctionalityTested func = certResult.getFunctionalitiesTested().get(0);
        assertNotNull(func);
        assertNotNull(func.getFunctionalityTested().getValue());
        assertEquals("func", func.getFunctionalityTested().getValue());
    }

    @Test
    public void buildCertResult_functionalityTestedMultipleRowsWithData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Functionality Tested").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,func\n,func2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getFunctionalitiesTested());
        assertEquals(2, certResult.getFunctionalitiesTested().size());
        CertificationResultFunctionalityTested func = certResult.getFunctionalitiesTested().get(0);
        assertNotNull(func);
        assertNotNull(func.getFunctionalityTested().getValue());
        assertEquals("func", func.getFunctionalityTested().getValue());
        func = certResult.getFunctionalitiesTested().get(1);
        assertNotNull(func);
        assertNotNull(func.getFunctionalityTested().getValue());
        assertEquals("func2", func.getFunctionalityTested().getValue());
    }

    @Test
    public void buildCertResult_functionalityTestedDuplicateData_ParsesFirstValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Functionality Tested,Functionality Tested").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,func,func2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getFunctionalitiesTested());
        assertEquals(1, certResult.getFunctionalitiesTested().size());
        CertificationResultFunctionalityTested func = certResult.getFunctionalitiesTested().get(0);
        assertNotNull(func);
        assertNotNull(func.getFunctionalityTested().getValue());
        assertEquals("func", func.getFunctionalityTested().getValue());
    }

    @Test
    public void buildCertResult_TestStandardNoColumn_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestStandards());
        assertEquals(0, certResult.getTestStandards().size());
    }

    @Test
    public void buildCertResult_TestStandardEmptyData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Standard Tested Against").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestStandards());
        assertEquals(0, certResult.getTestStandards().size());
        assertNotNull(certResult.getOptionalStandards());
        assertEquals(0, certResult.getOptionalStandards().size());
    }

    @Test
    public void buildCertResult_TestStandardWithData_ParsesAsOptionalStandard() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Standard Tested Against").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,std");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestStandards());
        assertEquals(0, certResult.getTestStandards().size());
        assertNotNull(certResult.getOptionalStandards());
        assertEquals(1, certResult.getOptionalStandards().size());
        CertificationResultOptionalStandard std = certResult.getOptionalStandards().get(0);
        assertNotNull(std);
        assertNotNull(std.getCitation());
        assertEquals("std", std.getCitation());
    }

    @Test
    public void buildCertResult_TestStandardDuplicateData_ParsesFirstValueAsOptionalStandard() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Standard Tested Against,Standard Tested Against").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,std,std2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestStandards());
        assertEquals(0, certResult.getTestStandards().size());
        assertNotNull(certResult.getOptionalStandards());
        CertificationResultOptionalStandard std = certResult.getOptionalStandards().get(0);
        assertNotNull(std);
        assertNotNull(std.getCitation());
        assertEquals("std", std.getCitation());
    }

    @Test
    public void buildCertResult_TestStandardMultipleRowsWithData_ParsesAsOptionalStandard() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Standard Tested Against").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,std\n,std2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestStandards());
        assertEquals(0, certResult.getTestStandards().size());
        assertNotNull(certResult.getOptionalStandards());
        assertEquals(2, certResult.getOptionalStandards().size());
        CertificationResultOptionalStandard std = certResult.getOptionalStandards().get(0);
        assertNotNull(std);
        assertNotNull(std.getCitation());
        assertEquals("std", std.getCitation());
        std = certResult.getOptionalStandards().get(1);
        assertNotNull(std);
        assertNotNull(std.getCitation());
        assertEquals("std2", std.getCitation());
    }

    @Test
    public void buildCertResult_OptionalStandardEmptyData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Optional Standard").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getOptionalStandards());
        assertEquals(0, certResult.getOptionalStandards().size());
        assertNotNull(certResult.getTestStandards());
        assertEquals(0, certResult.getTestStandards().size());
    }

    @Test
    public void buildCertResult_OptionalStandardWithData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Optional Standard").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,std");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestStandards());
        assertEquals(0, certResult.getTestStandards().size());
        assertNotNull(certResult.getOptionalStandards());
        assertEquals(1, certResult.getOptionalStandards().size());
        CertificationResultOptionalStandard std = certResult.getOptionalStandards().get(0);
        assertNotNull(std);
        assertNotNull(std.getCitation());
        assertEquals("std", std.getCitation());
    }

    @Test
    public void buildCertResult_OptionalStandardDuplicateData_ParsesFirstValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Optional Standard,Optional Standard").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,std,std2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestStandards());
        assertEquals(0, certResult.getTestStandards().size());
        assertNotNull(certResult.getOptionalStandards());
        CertificationResultOptionalStandard std = certResult.getOptionalStandards().get(0);
        assertNotNull(std);
        assertNotNull(std.getCitation());
        assertEquals("std", std.getCitation());
    }

    @Test
    public void buildCertResult_OptionalStandardMultipleRowsWithData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Optional Standard").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,std\n,std2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestStandards());
        assertEquals(0, certResult.getTestStandards().size());
        assertNotNull(certResult.getOptionalStandards());
        assertEquals(2, certResult.getOptionalStandards().size());
        CertificationResultOptionalStandard std = certResult.getOptionalStandards().get(0);
        assertNotNull(std);
        assertNotNull(std.getCitation());
        assertEquals("std", std.getCitation());
        std = certResult.getOptionalStandards().get(1);
        assertNotNull(std);
        assertNotNull(std.getCitation());
        assertEquals("std2", std.getCitation());
    }

    @Test
    public void buildCertResult_SvapsEmptyData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Regulatory Text Citation").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getSvaps());
        assertEquals(0, certResult.getSvaps().size());
    }

    @Test
    public void buildCertResult_SvapWithData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Regulatory Text Citation").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,svap1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getSvaps());
        assertEquals(1, certResult.getSvaps().size());
        CertificationResultSvap svap = certResult.getSvaps().get(0);
        assertNotNull(svap);
        assertNotNull(svap.getRegulatoryTextCitation());
        assertEquals("svap1", svap.getRegulatoryTextCitation());
    }

    @Test
    public void buildCertResult_SvapDuplicateData_ParsesFirstValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Regulatory Text Citation,Regulatory Text Citation").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,svap1,svap2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getSvaps());
        assertEquals(1, certResult.getSvaps().size());
        CertificationResultSvap svap = certResult.getSvaps().get(0);
        assertNotNull(svap);
        assertNotNull(svap.getRegulatoryTextCitation());
        assertEquals("svap1", svap.getRegulatoryTextCitation());
    }

    @Test
    public void buildCertResult_SvapMultipleRowsWithData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Regulatory Text Citation").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,svap1\n,svap2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getSvaps());
        assertEquals(2, certResult.getSvaps().size());
        CertificationResultSvap svap = certResult.getSvaps().get(0);
        assertNotNull(svap);
        assertNotNull(svap.getRegulatoryTextCitation());
        assertEquals("svap1", svap.getRegulatoryTextCitation());
        svap = certResult.getSvaps().get(1);
        assertNotNull(svap);
        assertNotNull(svap.getRegulatoryTextCitation());
        assertEquals("svap2", svap.getRegulatoryTextCitation());
    }

    @Test
    public void buildCertResult_PrivacySecurityNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNull(certResult.getPrivacySecurityFramework());
    }

    @Test
    public void buildCertResult_PrivacySecurityNoData_ReturnsEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Privacy and Security Framework").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getPrivacySecurityFramework());
        assertEquals("", certResult.getPrivacySecurityFramework());
    }

    @Test
    public void buildCertResult_PrivacySecurityGood_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Privacy and Security Framework").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Approach 1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getPrivacySecurityFramework());
        assertEquals("Approach 1", certResult.getPrivacySecurityFramework());
    }

    @Test
    public void buildCertResult_PrivacySecurityDuplicate_ReturnsFirstValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Privacy and Security Framework,Privacy and Security Framework").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Approach 1,Approach 2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getPrivacySecurityFramework());
        assertEquals("Approach 1", certResult.getPrivacySecurityFramework());
    }

    @Test
    public void buildCertResult_PrivacySecurityExtraWhitespace_ReturnsTrimmedValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Privacy and Security Framework").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,  Approach 1 ");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getPrivacySecurityFramework());
        assertEquals("Approach 1", certResult.getPrivacySecurityFramework());
    }

    @Test
    public void buildCertResult_ExportDocumentationNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNull(certResult.getExportDocumentation());
    }

    @Test
    public void buildCertResult_ExportDocumentationNoData_ReturnsEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Export Documentation").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getExportDocumentation());
        assertEquals("", certResult.getExportDocumentation());
    }

    @Test
    public void buildCertResult_ExportDocumentationGood_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Export Documentation").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Something");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getExportDocumentation());
        assertEquals("Something", certResult.getExportDocumentation());
    }

    @Test
    public void buildCertResult_ExportDocumentationDuplicate_ReturnsFirstValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Export Documentation,Export Documentation").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Something,Something2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getExportDocumentation());
        assertEquals("Something", certResult.getExportDocumentation());
    }

    @Test
    public void buildCertResult_ExportDocumentationExtraWhitespace_ReturnsTrimmedValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Export Documentation").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,  Something ");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getExportDocumentation());
        assertEquals("Something", certResult.getExportDocumentation());
    }

    @Test
    public void buildCertResult_DocumentationUrlNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNull(certResult.getDocumentationUrl());
    }

    @Test
    public void buildCertResult_DocumentationUrlNoData_ReturnsEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Documentation URL").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getDocumentationUrl());
        assertEquals("", certResult.getDocumentationUrl());
    }

    @Test
    public void buildCertResult_DocumentationUrlGood_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Documentation URL").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Something");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getDocumentationUrl());
        assertEquals("Something", certResult.getDocumentationUrl());
    }

    @Test
    public void buildCertResult_DocumentationUrlDuplicate_ReturnsFirstValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Documentation URL,Documentation URL").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Something,Something2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getDocumentationUrl());
        assertEquals("Something", certResult.getDocumentationUrl());
    }

    @Test
    public void buildCertResult_DocumentationUrlExtraWhitespace_ReturnsTrimmedValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Documentation URL").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,  Something ");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getDocumentationUrl());
        assertEquals("Something", certResult.getDocumentationUrl());
    }

    @Test
    public void buildCertResult_UseCasesNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNull(certResult.getUseCases());
    }

    @Test
    public void buildCertResult_UseCasesNoData_ReturnsEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Use Cases").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getUseCases());
        assertEquals("", certResult.getUseCases());
    }

    @Test
    public void buildCertResult_UseCasesGood_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Use Cases").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Something");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getUseCases());
        assertEquals("Something", certResult.getUseCases());
    }

    @Test
    public void buildCertResult_UseCasesDuplicate_ReturnsFirstValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Use Cases,Use Cases").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Something,Something2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getUseCases());
        assertEquals("Something", certResult.getUseCases());
    }

    @Test
    public void buildCertResult_UseCasesExtraWhitespace_ReturnsTrimmedValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Use Cases").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,  Something ");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getUseCases());
        assertEquals("Something", certResult.getUseCases());
    }

    @Test
    public void buildCertResult_ApiDocumentationNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNull(certResult.getApiDocumentation());
    }

    @Test
    public void buildCertResult_ApiDocumentationNoData_ReturnsEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",API Documentation Link").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getApiDocumentation());
        assertEquals("", certResult.getApiDocumentation());
    }

    @Test
    public void buildCertResult_ApiDocumentationGood_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",API Documentation Link").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Something");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getApiDocumentation());
        assertEquals("Something", certResult.getApiDocumentation());
    }

    @Test
    public void buildCertResult_ApiDocumentationDuplicate_ReturnsFirstValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",API Documentation Link,API Documentation Link").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Something,Something2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getApiDocumentation());
        assertEquals("Something", certResult.getApiDocumentation());
    }

    @Test
    public void buildCertResult_ApiDocumentationExtraWhitespace_ReturnsTrimmedValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",API Documentation Link").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,  Something ");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getApiDocumentation());
        assertEquals("Something", certResult.getApiDocumentation());
    }

    @Test
    public void buildCertResult_ServiceBaseUrlListNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNull(certResult.getServiceBaseUrlList());
    }

    @Test
    public void buildCertResult_ServiceBaseUrlListNoData_ReturnsEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Service Base URL List").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getServiceBaseUrlList());
        assertEquals("", certResult.getServiceBaseUrlList());
    }

    @Test
    public void buildCertResult_ServiceBaseUrlListGood_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Service Base URL List").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Something");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getServiceBaseUrlList());
        assertEquals("Something", certResult.getServiceBaseUrlList());
    }

    @Test
    public void buildCertResult_ServiceBaseUrlListDuplicate_ReturnsFirstValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Service Base URL List,Service Base URL List").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Something,Something2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getServiceBaseUrlList());
        assertEquals("Something", certResult.getServiceBaseUrlList());
    }

    @Test
    public void buildCertResult_ServiceBaseUrlListExtraWhitespace_ReturnsTrimmedValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Service Base URL List").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,  Something ");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getServiceBaseUrlList());
        assertEquals("Something", certResult.getServiceBaseUrlList());
    }

    @Test
    public void buildCertResult_AttesttionAnswerNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNull(certResult.getAttestationAnswer());
    }

    @Test
    public void buildCertResult_AttesttionAnswerBooleanValue0_ReturnsFalse() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Attestation Answer").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,0");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getAttestationAnswer());
        assertFalse(certResult.getAttestationAnswer());
    }

    @Test
    public void buildCertResult_AttesttionAnswerBooleanValue1_ReturnsTrue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Attestation Answer").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getAttestationAnswer());
        assertTrue(certResult.getAttestationAnswer());
    }

    @Test
    public void buildCertResult_AttesttionAnswerBooleanValueNo_ReturnsFalse() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Attestation Answer").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,No");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getAttestationAnswer());
        assertFalse(certResult.getAttestationAnswer());
    }

    @Test
    public void buildCertResult_AttesttionAnswerBooleanValueYes_ReturnsTrue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Attestation Answer").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Yes");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getAttestationAnswer());
        assertTrue(certResult.getAttestationAnswer());
    }

    @Test
    public void buildCertResult_AttesttionAnswerDuplicate_ReturnsFirstValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Attestation Answer,Attestation Answer").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,Yes,No");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getAttestationAnswer());
        assertTrue(certResult.getAttestationAnswer());
    }

    @Test
    public void buildCertResult_AttesttionAnswerEmptyString_ReturnsFalse() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Attestation Answer").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getAttestationAnswer());
        assertFalse(certResult.getAttestationAnswer());
    }

    @Test
    public void buildCertResult_AttesttionAnswerInvalidBoolean_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Attestation Answer").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,JUNK");
        assertNotNull(certResultRecords);
        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNull(certResult.getAttestationAnswer());
        assertNotNull(certResult.getAttestationAnswerStr());
        assertEquals("JUNK", certResult.getAttestationAnswerStr());
    }

}
