package gov.healthit.chpl.upload.listing;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.DeprecatedUploadTemplateException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ListingManagerUploadTest {
    @SuppressWarnings("checkstyle:linelength")
    private static final String HEADER_2015_V19 = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C,VENDOR__C,PRODUCT__C,VERSION__C,CERT_YEAR__C,ACB_CERTIFICATION_ID__C,CERTIFYING_ACB__C,TESTING_ATL__C,CERTIFICATION_DATE__C,VENDOR_STREET_ADDRESS__C,VENDOR_STATE__C,VENDOR_CITY__C,VENDOR_ZIP__C,VENDOR_WEBSITE__C,Self-developer,VENDOR_EMAIL__C,VENDOR_PHONE__C,VENDOR_CONTACT_NAME__C,Developer-Identified Target Users,QMS Standard,QMS Standard Applicable Criteria,QMS Modification Description,ICS,ICS Source,Accessibility Certified,Accessibility Standard,170.523(k)(1) URL,CQM Number,CQM Version,CQM Criteria,SED Report Hyperlink,Description of the Intended Users,Date SED Testing was Concluded,Participant Identifier,Participant Gender,Participant Age,Participant Education,Participant Occupation/Role,Participant Professional Experience,Participant Computer Experience,Participant Product Experience,Participant Assistive Technology Needs,Task Identifier,Task Description,Task Success - Mean (%),Task Success - Standard Deviation (%),Task Path Deviation - Observed #,Task Path Deviation - Optimal #,Task Time - Mean (seconds),Task Time - Standard Deviation (seconds),Task Time Deviation - Observed Seconds,Task Time Deviation - Optimal Seconds,Task Errors  Mean(%),Task Errors - Standard Deviation (%),Task Rating - Scale Type,Task Rating,Task Rating - Standard Deviation,CRITERIA_170_315_A_1__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_2__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_3__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_4__C,GAP,Privacy and Security Framework,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_5__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_9__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_10__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_12__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_13__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_14__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_15__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_B_1__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_1_Cures__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_2__C,Privacy and Security Framework,Standard Tested Against,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_2_Cures__C,Privacy and Security Framework,Standard Tested Against,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_3_Cures__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_6__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_7__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_B_7_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_B_8__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_8_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_9__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_9_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_10__C,Privacy and Security Framework,Export Documentation,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_C_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_3__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_3_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_4__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_D_1__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_2__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_2_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_3__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_3_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_4__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_5__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_6__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_7__C,GAP,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_8__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_9__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_10__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_10_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_11__C,GAP,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_12__C,Attestation Answer,Documentation URL,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_D_13__C,Attestation Answer,Documentation URL,Use Cases,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_E_1__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_E_1_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_E_2__C,Privacy and Security Framework,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_E_3__C,Privacy and Security Framework,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_3__C,GAP,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_4__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_5__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_5_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_6__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_F_7__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_1__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_2__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_3__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_4__C,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_5__C,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_6__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_6_CURES__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_7__C,Privacy and Security Framework,API Documentation Link,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_G_8__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_9__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_9_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_10__C,Privacy and Security Framework,Standard Tested Against,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_H_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_H_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description";

    private String listing1Csv, listing2Csv, listingNewDeveloperCsv;
    private ErrorMessageUtil msgUtil;
    private ListingUploadManager uploadManager;
    private ListingUploadHandlerUtil uploadUtil;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ListingUploadDao uploadDao;
    private CertificationBodyDAO acbDao;

    @Before
    public void setup() throws InvalidArgumentsException, JsonProcessingException,
        EntityRetrievalException, EntityCreationException, IOException, FileNotFoundException {
        loadFiles();

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        uploadDao = Mockito.mock(ListingUploadDao.class);
        acbDao = Mockito.mock(CertificationBodyDAO.class);
        chplProductNumberUtil = Mockito.mock(ChplProductNumberUtil.class);

        Mockito.when(acbDao.getByName(ArgumentMatchers.anyString())).thenReturn(createAcb());
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.emptyFile"))).thenReturn("Empty file message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.notCSV"))).thenReturn("Not CSV message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.emptyRows"))).thenReturn("Header only message");

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.missingRequiredHeadings"),
                ArgumentMatchers.anyString()))
        .thenAnswer(i -> String.format("Headings with the following values are required but were not found: %s.",
                i.getArgument(1), ""));

        uploadUtil = new ListingUploadHandlerUtil(msgUtil);
        uploadManager = new ListingUploadManager(uploadUtil, chplProductNumberUtil, uploadDao, acbDao, msgUtil);
    }

    @Test(expected = ValidationException.class)
    public void upload_EmptyData_Fails() throws JsonProcessingException, ValidationException,
        InvalidArgumentsException, DeprecatedUploadTemplateException {
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", "".getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test(expected = ValidationException.class)
    public void upload_HeaderOnly_Fails() throws JsonProcessingException, ValidationException,
        InvalidArgumentsException, DeprecatedUploadTemplateException {
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", HEADER_2015_V19.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test(expected = ValidationException.class)
    public void upload_BadContentType_Fails() throws JsonProcessingException, ValidationException,
        InvalidArgumentsException, DeprecatedUploadTemplateException {
        String fileContents = HEADER_2015_V19 + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/plain", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test
    public void uploadV19SingleListing_ValidData_Successful() throws JsonProcessingException, ValidationException,
        InvalidArgumentsException, DeprecatedUploadTemplateException {
        String fileContents = HEADER_2015_V19 + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());

        List<ListingUpload> parsedListings = uploadManager.parseUploadFile(file);
        assertNotNull(parsedListings);
        assertEquals(1, parsedListings.size());
    }

    @Test
    public void uploadV19_DuplicateChplIds_GroupsAsSingleListing() throws JsonProcessingException, ValidationException,
        InvalidArgumentsException, DeprecatedUploadTemplateException {
        String fileContents = HEADER_2015_V19 + "\n" + listing1Csv + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());

        List<ListingUpload> parsedListings = uploadManager.parseUploadFile(file);
        assertNotNull(parsedListings);
        assertEquals(1, parsedListings.size());
    }

    @Test()
    public void uploadV19_DuplicateChplIdsNewDeveloper_GroupsAsSingleListing()
            throws JsonProcessingException, ValidationException,
        InvalidArgumentsException, DeprecatedUploadTemplateException {
        String fileContents = HEADER_2015_V19 + "\n" + listingNewDeveloperCsv + listingNewDeveloperCsv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());

        List<ListingUpload> parsedListings = uploadManager.parseUploadFile(file);
        assertNotNull(parsedListings);
        assertEquals(1, parsedListings.size());
    }

    @Test()
    public void uploadV19MultipleListings_Succeeds() throws JsonProcessingException, ValidationException,
        InvalidArgumentsException, DeprecatedUploadTemplateException {
        String fileContents = HEADER_2015_V19 + "\n" + listing1Csv + "\n"
                + listing2Csv + "\n" + listingNewDeveloperCsv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());

        List<ListingUpload> parsedListings = uploadManager.parseUploadFile(file);
        assertNotNull(parsedListings);
        assertEquals(3, parsedListings.size());
    }

    @Test(expected = ValidationException.class)
    public void uploadSingleListing_MissingDeveloperColumn_Fails() throws JsonProcessingException, ValidationException,
        InvalidArgumentsException, DeprecatedUploadTemplateException {
        String fileContents = HEADER_2015_V19.replace("VENDOR__C", "") + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test(expected = ValidationException.class)
    public void uploadSingleListing_MissingProductColumn_Fails() throws JsonProcessingException, ValidationException,
        InvalidArgumentsException, DeprecatedUploadTemplateException {
        String fileContents = HEADER_2015_V19.replace("PRODUCT__C", "") + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test(expected = ValidationException.class)
    public void uploadSingleListing_MissingVersionColumn_Fails() throws JsonProcessingException, ValidationException,
        InvalidArgumentsException, DeprecatedUploadTemplateException {
        String fileContents = HEADER_2015_V19.replace("VERSION__C", "") + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test(expected = ValidationException.class)
    public void uploadSingleListing_MissingUniqueIdColumn_Fails() throws JsonProcessingException, ValidationException,
        InvalidArgumentsException, DeprecatedUploadTemplateException {
        String fileContents = HEADER_2015_V19.replace("UNIQUE_CHPL_ID__C", "") + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test
    public void uploadSingleListing_MissingAcbColumn_Succeeds() throws JsonProcessingException, ValidationException,
        InvalidArgumentsException, DeprecatedUploadTemplateException {
        Mockito.when(acbDao.getByCode(ArgumentMatchers.anyString())).thenReturn(createAcb());
        Mockito.when(chplProductNumberUtil.getAcbCode(ArgumentMatchers.anyString())).thenReturn("04");

        String fileContents = "UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VERSION__C" + "\n"
                + "15.04.04.2669.MDTB.03.01.1.200707,DEV Name,Prod Name,1.0";
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());

        List<ListingUpload> parsedListings = uploadManager.parseUploadFile(file);
        assertNotNull(parsedListings);
        assertEquals(1, parsedListings.size());
        ListingUpload metadata = parsedListings.get(0);
        assertNotNull(metadata.getAcb());
        assertNotNull(metadata.getAcb().getId());
    }

    @Test
    public void uploadSingleListing_MissingAcbColumnAndJunkInChplId_AcbIsNull()
            throws JsonProcessingException, ValidationException,
        InvalidArgumentsException, DeprecatedUploadTemplateException {
        Mockito.when(acbDao.getByCode(ArgumentMatchers.anyString())).thenReturn(createAcb());
        Mockito.when(chplProductNumberUtil.getAcbCode(ArgumentMatchers.anyString()))
            .thenThrow(ArrayIndexOutOfBoundsException.class);

        String fileContents = "UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VERSION__C" + "\n"
                + "JUNK,DEV Name,Prod Name,1.0";
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());

        List<ListingUpload> parsedListings = uploadManager.parseUploadFile(file);
        assertNotNull(parsedListings);
        assertEquals(1, parsedListings.size());
        ListingUpload metadata = parsedListings.get(0);
        assertNull(metadata.getAcb());
    }

    private CertificationBodyDTO createAcb() {
        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1L);
        dto.setAcbCode("04");
        dto.setName("Test");
        dto.setRetired(false);
        dto.setWebsite("http://www.test.com");
        AddressDTO address = new AddressDTO();
        address.setId(1L);
        address.setStreetLineOne("111 Test Road");
        address.setStreetLineTwo("Suite 4");
        address.setCity("Baltimore");
        address.setState("MD");
        address.setZipcode("21008");
        dto.setAddress(address);
        return dto;
    }

    private void loadFiles() throws IOException, FileNotFoundException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("upload_v19_listing1.csv");
        try {
            listing1Csv = IOUtils.toString(inputStream, "UTF-8");
        } finally {
            inputStream.close();
            IOUtils.closeQuietly(inputStream);
        }
        assertTrue(!StringUtils.isEmpty(listing1Csv));

        inputStream = getClass().getClassLoader().getResourceAsStream("upload_v19_listing2.csv");
        try {
            listing2Csv = IOUtils.toString(inputStream, "UTF-8");
        } finally {
            inputStream.close();
            IOUtils.closeQuietly(inputStream);
        }
        assertTrue(!StringUtils.isEmpty(listing2Csv));

        inputStream = getClass().getClassLoader().getResourceAsStream("upload_v19_listing_newdeveloper.csv");
        try {
            listingNewDeveloperCsv = IOUtils.toString(inputStream, "UTF-8");
        } finally {
            inputStream.close();
            IOUtils.closeQuietly(inputStream);
        }
        assertTrue(!StringUtils.isEmpty(listingNewDeveloperCsv));
    }
}
