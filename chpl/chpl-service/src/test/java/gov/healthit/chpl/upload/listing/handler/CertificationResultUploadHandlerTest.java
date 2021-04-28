package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
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
                Mockito.mock(TestProcedureUploadHandler.class),
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
    public void buildCertResult_TestFunctionalityNoColumn_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestFunctionality());
        assertEquals(0, certResult.getTestFunctionality().size());
    }

    @Test
    public void buildCertResult_TestFunctionalityEmptyData_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Functionality Tested").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestFunctionality());
        assertEquals(0, certResult.getTestFunctionality().size());
    }

    @Test
    public void buildCertResult_TestFunctionalityWithData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Functionality Tested").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,func");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestFunctionality());
        assertEquals(1, certResult.getTestFunctionality().size());
        CertificationResultTestFunctionality func = certResult.getTestFunctionality().get(0);
        assertNotNull(func);
        assertNotNull(func.getName());
        assertEquals("func", func.getName());
    }

    @Test
    public void buildCertResult_TestFunctionalityMultipleRowsWithData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",Functionality Tested").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,func\n,func2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestFunctionality());
        assertEquals(2, certResult.getTestFunctionality().size());
        CertificationResultTestFunctionality func = certResult.getTestFunctionality().get(0);
        assertNotNull(func);
        assertNotNull(func.getName());
        assertEquals("func", func.getName());
        func = certResult.getTestFunctionality().get(1);
        assertNotNull(func);
        assertNotNull(func.getName());
        assertEquals("func2", func.getName());
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
    }

    @Test
    public void buildCertResult_TestStandardWithData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Standard Tested Against").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,std");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestStandards());
        assertEquals(1, certResult.getTestStandards().size());
        CertificationResultTestStandard std = certResult.getTestStandards().get(0);
        assertNotNull(std);
        assertNotNull(std.getTestStandardName());
        assertEquals("std", std.getTestStandardName());
    }

    @Test
    public void buildCertResult_TestStandardMultipleRowsWithData_ParsesCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",Standard Tested Against").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> certResultRecords = ListingUploadTestUtil.getRecordsFromString("1,std\n,std2");
        assertNotNull(certResultRecords);

        CertificationResult certResult = handler.parseAsCertificationResult(headingRecord, certResultRecords);
        assertNotNull(certResult);
        assertNotNull(certResult.getTestStandards());
        assertEquals(2, certResult.getTestStandards().size());
        CertificationResultTestStandard std = certResult.getTestStandards().get(0);
        assertNotNull(std);
        assertNotNull(std.getTestStandardName());
        assertEquals("std", std.getTestStandardName());
        std = certResult.getTestStandards().get(1);
        assertNotNull(std);
        assertNotNull(std.getTestStandardName());
        assertEquals("std2", std.getTestStandardName());
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
    public void buildCertResult_PrivacySecurityExtraWhitepalce_ReturnsTrimmedValue() {
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
    public void buildCertResult_ExportDocumentationExtraWhitepalce_ReturnsTrimmedValue() {
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
    public void buildCertResult_DocumentationUrlExtraWhitepalce_ReturnsTrimmedValue() {
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
    public void buildCertResult_UseCasesExtraWhitepalce_ReturnsTrimmedValue() {
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
    public void buildCertResult_ApiDocumentationExtraWhitepalce_ReturnsTrimmedValue() {
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
    public void buildCertResult_ServiceBaseUrlListExtraWhitepalce_ReturnsTrimmedValue() {
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
