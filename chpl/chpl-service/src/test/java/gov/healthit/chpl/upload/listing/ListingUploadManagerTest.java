package gov.healthit.chpl.upload.listing;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockMultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ConfirmListingRequest;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.standard.StandardGroupService;
import gov.healthit.chpl.upload.listing.handler.CertificationDateHandler;
import gov.healthit.chpl.upload.listing.handler.ListingDetailsUploadHandler;
import gov.healthit.chpl.upload.listing.normalizer.ListingDetailsNormalizer;
import gov.healthit.chpl.upload.listing.validation.ListingUploadValidator;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ListingUploadManagerTest {
    @SuppressWarnings("checkstyle:linelength")
    private static final String HEADER_2015_V19 = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C,VENDOR__C,PRODUCT__C,VERSION__C,CERT_YEAR__C,ACB_CERTIFICATION_ID__C,CERTIFYING_ACB__C,TESTING_ATL__C,CERTIFICATION_DATE__C,VENDOR_STREET_ADDRESS__C,VENDOR_STATE__C,VENDOR_CITY__C,VENDOR_ZIP__C,VENDOR_WEBSITE__C,Self-developer,VENDOR_EMAIL__C,VENDOR_PHONE__C,VENDOR_CONTACT_NAME__C,Developer-Identified Target Users,QMS Standard,QMS Standard Applicable Criteria,QMS Modification Description,ICS,ICS Source,Accessibility Certified,Accessibility Standard,170.523(k)(1) URL,CQM Number,CQM Version,CQM Criteria,SED Report Hyperlink,Description of the Intended Users,Date SED Testing was Concluded,Participant Identifier,Participant Gender,Participant Age,Participant Education,Participant Occupation/Role,Participant Professional Experience,Participant Computer Experience,Participant Product Experience,Participant Assistive Technology Needs,Task Identifier,Task Description,Task Success - Mean (%),Task Success - Standard Deviation (%),Task Path Deviation - Observed #,Task Path Deviation - Optimal #,Task Time - Mean (seconds),Task Time - Standard Deviation (seconds),Task Time Deviation - Observed Seconds,Task Time Deviation - Optimal Seconds,Task Errors  Mean(%),Task Errors - Standard Deviation (%),Task Rating - Scale Type,Task Rating,Task Rating - Standard Deviation,CRITERIA_170_315_A_1__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_2__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_3__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_4__C,GAP,Privacy and Security Framework,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_5__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_9__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_10__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_12__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_13__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_14__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_15__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_B_1__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_1_Cures__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_2__C,Privacy and Security Framework,Standard Tested Against,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_2_Cures__C,Privacy and Security Framework,Standard Tested Against,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_3_Cures__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_6__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_7__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_B_7_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_B_8__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_8_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_9__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_9_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_10__C,Privacy and Security Framework,Export Documentation,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_C_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_3__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_3_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_4__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_D_1__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_2__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_2_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_3__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_3_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_4__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_5__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_6__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_7__C,GAP,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_8__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_9__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_10__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_10_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_11__C,GAP,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_12__C,Attestation Answer,Documentation URL,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_D_13__C,Attestation Answer,Documentation URL,Use Cases,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_E_1__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_E_1_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_E_2__C,Privacy and Security Framework,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_E_3__C,Privacy and Security Framework,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_3__C,GAP,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_4__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_5__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_5_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_6__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_F_7__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_1__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_2__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_3__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_4__C,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_5__C,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_6__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_6_CURES__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_7__C,Privacy and Security Framework,API Documentation Link,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_G_8__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_9__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_9_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_10__C,Privacy and Security Framework,Standard Tested Against,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_H_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_H_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description";

    private String listing1Csv, listing2Csv, listingNewDeveloperCsv;
    private ErrorMessageUtil msgUtil;
    private ListingUploadManager uploadManager;
    private CertificationDateHandler certDateHandler;
    private ListingUploadHandlerUtil uploadUtil;
    private ChplProductNumberUtil chplProductNumberUtil;
    private CertificationBodyDAO acbDao;
    private ListingUploadDao listingUploadDao;
    private ListingUploadValidator listingUploadValidator;
    private ListingConfirmationManager listingConfirmationManager;

    @Before
    public void setup() throws InvalidArgumentsException, JsonProcessingException,
    EntityRetrievalException, EntityCreationException, IOException, FileNotFoundException {
        loadFiles();

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        acbDao = Mockito.mock(CertificationBodyDAO.class);
        chplProductNumberUtil = Mockito.mock(ChplProductNumberUtil.class);
        certDateHandler = Mockito.mock(CertificationDateHandler.class);
        listingConfirmationManager = Mockito.mock(ListingConfirmationManager.class);
        listingUploadDao = Mockito.mock(ListingUploadDao.class);
        listingUploadValidator = Mockito.mock(ListingUploadValidator.class);
        ListingDetailsNormalizer listingNormalizer = Mockito.mock(ListingDetailsNormalizer.class);

        Mockito.when(acbDao.getByName(ArgumentMatchers.anyString())).thenReturn(createAcb());
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.emptyFile"), ArgumentMatchers.any())).thenReturn("Empty file message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.notCSV"), ArgumentMatchers.any())).thenReturn("Not CSV message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.emptyRows"), ArgumentMatchers.any())).thenReturn("Header only message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.missingRequiredData"), ArgumentMatchers.any()))
                .thenReturn("The following headings require non-empty data in the upload file: %s.");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.missingRequiredHeadings"), ArgumentMatchers.any()))
                .thenReturn("Headings with the following values are required but were not found: %s.");

        Mockito.doNothing().when(listingNormalizer).normalize(ArgumentMatchers.any());

        uploadUtil = new ListingUploadHandlerUtil(msgUtil);
        uploadManager = new ListingUploadManager(Mockito.mock(ListingDetailsUploadHandler.class),
                certDateHandler,
                listingNormalizer,
                listingUploadValidator,
                uploadUtil, chplProductNumberUtil, listingUploadDao, acbDao,
                Mockito.mock(UserDAO.class),
                Mockito.mock(StandardDAO.class),
                listingConfirmationManager,
                Mockito.mock(SchedulerManager.class),
                Mockito.mock(ActivityManager.class), msgUtil,
                Mockito.mock(StandardGroupService.class));
    }

    @Test(expected = ValidationException.class)
    public void upload_EmptyData_Fails() throws ValidationException {
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", "".getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test(expected = ValidationException.class)
    public void upload_HeaderOnly_Fails() throws ValidationException {
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", HEADER_2015_V19.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test(expected = ValidationException.class)
    public void upload_BadContentType_Fails() throws ValidationException {
        String fileContents = HEADER_2015_V19 + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/plain", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test
    public void uploadV19SingleListing_ValidData_Successful() throws ValidationException {
        String fileContents = HEADER_2015_V19 + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());

        List<ListingUpload> parsedListings = uploadManager.parseUploadFile(file);
        assertNotNull(parsedListings);
        assertEquals(1, parsedListings.size());
    }

    @Test
    public void uploadV19SingleListing_NewlinesAfterHeader_Successful() throws ValidationException {
        String fileContents = HEADER_2015_V19 + "\n\n\n\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());

        List<ListingUpload> parsedListings = uploadManager.parseUploadFile(file);
        assertNotNull(parsedListings);
        assertEquals(1, parsedListings.size());
    }

    @Test
    public void uploadV19_DuplicateChplIds_GroupsAsSingleListing() throws ValidationException {
        String fileContents = HEADER_2015_V19 + "\n" + listing1Csv + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());

        List<ListingUpload> parsedListings = uploadManager.parseUploadFile(file);
        assertNotNull(parsedListings);
        assertEquals(1, parsedListings.size());
    }

    @Test()
    public void uploadV19_DuplicateChplIdsNewDeveloper_GroupsAsSingleListing()
            throws ValidationException {
        String fileContents = HEADER_2015_V19 + "\n" + listingNewDeveloperCsv + listingNewDeveloperCsv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());

        List<ListingUpload> parsedListings = uploadManager.parseUploadFile(file);
        assertNotNull(parsedListings);
        assertEquals(1, parsedListings.size());
    }

    @Test
    public void uploadV19MultipleListings_Succeeds() throws ValidationException {
        String fileContents = HEADER_2015_V19 + "\n" + listing1Csv + "\n"
                + listing2Csv + "\n" + listingNewDeveloperCsv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());

        List<ListingUpload> parsedListings = uploadManager.parseUploadFile(file);
        assertNotNull(parsedListings);
        assertEquals(3, parsedListings.size());
    }

    @Test(expected = ValidationException.class)
    public void uploadSingleListing_MissingDeveloperColumn_Fails() throws ValidationException {
        String fileContents = HEADER_2015_V19.replace("VENDOR__C", "") + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test(expected = ValidationException.class)
    public void uploadSingleListing_MissingProductColumn_Fails() throws ValidationException {
        String fileContents = HEADER_2015_V19.replace("PRODUCT__C", "") + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test(expected = ValidationException.class)
    public void uploadSingleListing_MissingVersionColumn_Fails() throws ValidationException {
        String fileContents = HEADER_2015_V19.replace("VERSION__C", "") + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test(expected = ValidationException.class)
    public void uploadSingleListing_MissingUniqueIdColumn_Fails() throws ValidationException {
        String fileContents = HEADER_2015_V19.replace("UNIQUE_CHPL_ID__C", "") + "\n" + listing1Csv;
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test
    public void uploadSingleListing_MissingAcbColumn_Succeeds() throws ValidationException {
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
            throws ValidationException {
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
        assertNull(metadata.getCertificationDate());
        assertEquals("DEV Name", metadata.getDeveloper());
        assertEquals("Prod Name", metadata.getProduct());
        assertEquals("1.0", metadata.getVersion());
    }

    @Test(expected = ValidationException.class)
    public void uploadSingleListing_HasRequiredColumnsMissingChplIdData_ThrowsException()
            throws ValidationException {

        String fileContents = "UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VERSION__C" + "\n"
                + ",DEV Name,Prod Name,1.0";
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test(expected = ValidationException.class)
    public void uploadSingleListing_HasRequiredColumnsMissingDeveloperData_ThrowsException()
            throws ValidationException {

        String fileContents = "UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VERSION__C" + "\n"
                + "15.04.04.2669.MDTB.03.01.1.200707,,Prod Name,1.0";
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test(expected = ValidationException.class)
    public void uploadSingleListing_HasRequiredColumnsMissingProductData_ThrowsException()
            throws ValidationException {

        String fileContents = "UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VERSION__C" + "\n"
                + "15.04.04.2669.MDTB.03.01.1.200707,DEV Name,,1.0";
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test(expected = ValidationException.class)
    public void uploadSingleListing_HasRequiredColumnsMissingVersionData_ThrowsException()
            throws ValidationException {

        String fileContents = "UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VERSION__C" + "\n"
                + "15.04.04.2669.MDTB.03.01.1.200707,DEV Name,Prod Name,";
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());
        uploadManager.parseUploadFile(file);
    }

    @Test
    public void uploadSingleListing_ValidChplId_ParsesAcbAndCertDate()
            throws ValidationException {
        Mockito.when(acbDao.getByCode(ArgumentMatchers.anyString())).thenReturn(createAcb());
        Mockito.when(chplProductNumberUtil.getAcbCode(ArgumentMatchers.anyString()))
            .thenReturn("04");
        Mockito.when(certDateHandler.handle(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(LocalDate.of(2020, Month.JULY, 7));

        String fileContents = "UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VERSION__C" + "\n"
                + "15.04.04.2669.MDTB.03.01.1.200707,DEV Name,Prod Name,1.0";
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());

        List<ListingUpload> parsedListings = uploadManager.parseUploadFile(file);
        assertNotNull(parsedListings);
        assertEquals(1, parsedListings.size());
        ListingUpload metadata = parsedListings.get(0);
        assertNotNull(metadata.getAcb());
        assertEquals(1L, metadata.getAcb().getId());
        assertNotNull(metadata.getCertificationDate());
        assertEquals(LocalDate.of(2020, Month.JULY, 7), metadata.getCertificationDate());
        assertEquals("DEV Name", metadata.getDeveloper());
        assertEquals("Prod Name", metadata.getProduct());
        assertEquals("1.0", metadata.getVersion());
    }

    @Test
    public void uploadSingleListing_ChplIdInFirstColumnOnly_ParsesListing()
            throws ValidationException {
        Mockito.when(acbDao.getByCode(ArgumentMatchers.anyString())).thenReturn(createAcb());
        Mockito.when(chplProductNumberUtil.getAcbCode(ArgumentMatchers.anyString()))
            .thenReturn("04");
        Mockito.when(certDateHandler.handle(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(LocalDate.of(2020, Month.JULY, 7));

        String fileContents = "UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VERSION__C" + "\n"
                + "15.04.04.2669.MDTB.03.01.1.200707,DEV Name,Prod Name,1.0\n"
                + ",,,,\n"
                + ",,,,";
        MockMultipartFile file = new MockMultipartFile("2015_v19.csv", "2015_v19.csv", "text/csv", fileContents.getBytes());

        List<ListingUpload> parsedListings = uploadManager.parseUploadFile(file);
        assertNotNull(parsedListings);
        assertEquals(1, parsedListings.size());
        ListingUpload metadata = parsedListings.get(0);
        assertNotNull(metadata.getAcb());
        assertEquals(1L, metadata.getAcb().getId());
        assertNotNull(metadata.getCertificationDate());
        assertEquals(LocalDate.of(2020, Month.JULY, 7), metadata.getCertificationDate());
        assertEquals("DEV Name", metadata.getDeveloper());
        assertEquals("Prod Name", metadata.getProduct());
        assertEquals("1.0", metadata.getVersion());
    }

    @Test
    public void confirmListing_noWarningsNoAcknowledgment_succeeds()
            throws InvalidArgumentsException, EntityCreationException,
            EntityRetrievalException, JsonProcessingException, ValidationException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .build();
        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                CertifiedProductSearchDetails listing = (CertifiedProductSearchDetails) invocation.getArgument(0);
                return null;
            }
        }).when(listingUploadValidator).review(Mockito.eq(listing));
        Mockito.when(listingUploadDao.isAvailableForProcessing(ArgumentMatchers.eq(listing.getId())))
            .thenReturn(true);
        Mockito.when(listingConfirmationManager.create(ArgumentMatchers.any()))
            .thenReturn(listing);
        uploadManager.confirm(1L, ConfirmListingRequest.builder()
                .acknowledgeWarnings(false)
                .listing(listing)
                .build());
    }

    @Test
    public void confirmListing_noWarningsHasAcknowledgment_succeeds()
            throws InvalidArgumentsException, EntityCreationException,
            EntityRetrievalException, JsonProcessingException, ValidationException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .build();
        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                CertifiedProductSearchDetails listing = (CertifiedProductSearchDetails) invocation.getArgument(0);
                return null;
            }
        }).when(listingUploadValidator).review(Mockito.eq(listing));
        Mockito.when(listingUploadDao.isAvailableForProcessing(ArgumentMatchers.eq(listing.getId())))
            .thenReturn(true);
        Mockito.when(listingConfirmationManager.create(ArgumentMatchers.any()))
            .thenReturn(listing);
        uploadManager.confirm(1L, ConfirmListingRequest.builder()
                .acknowledgeWarnings(true)
                .listing(listing)
                .build());
    }

    @Test(expected = ValidationException.class)
    public void confirmListing_hasWarningsNoAcknowledgment_throwsValidationException()
            throws InvalidArgumentsException, EntityCreationException,
            EntityRetrievalException, JsonProcessingException, ValidationException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .build();
        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                CertifiedProductSearchDetails listing = (CertifiedProductSearchDetails) invocation.getArgument(0);
                listing.addWarningMessage("This is a test warning");
                return null;
            }
        }).when(listingUploadValidator).review(Mockito.eq(listing));
        Mockito.when(listingUploadDao.isAvailableForProcessing(ArgumentMatchers.eq(listing.getId())))
            .thenReturn(true);
        uploadManager.confirm(1L, ConfirmListingRequest.builder()
                .acknowledgeWarnings(false)
                .listing(listing)
                .build());
    }

    @Test
    public void confirmListing_hasWarningsHasAcknowledgment_succeeds()
            throws InvalidArgumentsException, EntityCreationException,
            EntityRetrievalException, JsonProcessingException, ValidationException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .build();
        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                CertifiedProductSearchDetails listing = (CertifiedProductSearchDetails) invocation.getArgument(0);
                listing.addWarningMessage("This is a test warning");
                return null;
            }
        }).when(listingUploadValidator).review(Mockito.eq(listing));
        Mockito.when(listingUploadDao.isAvailableForProcessing(ArgumentMatchers.eq(listing.getId())))
            .thenReturn(true);
        Mockito.when(listingConfirmationManager.create(ArgumentMatchers.any()))
            .thenReturn(listing);
        uploadManager.confirm(1L, ConfirmListingRequest.builder()
                .acknowledgeWarnings(true)
                .listing(listing)
                .build());
    }

    @Test(expected = InvalidArgumentsException.class)
    public void confirmListing_notAvailableForConfirmation_throwsInvalidArgumentsException()
            throws InvalidArgumentsException, EntityCreationException,
            EntityRetrievalException, JsonProcessingException, ValidationException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .build();
        Mockito.when(listingUploadDao.isAvailableForProcessing(ArgumentMatchers.eq(listing.getId())))
            .thenReturn(false);
        uploadManager.confirm(1L, ConfirmListingRequest.builder()
                .acknowledgeWarnings(false)
                .listing(listing)
                .build());
    }

    private CertificationBody createAcb() {
        CertificationBody dto = new CertificationBody();
        dto.setId(1L);
        dto.setAcbCode("04");
        dto.setName("Test");
        dto.setRetired(false);
        dto.setWebsite("http://www.test.com");
        Address address = new Address();
        address.setAddressId(1L);
        address.setLine1("111 Test Road");
        address.setLine2("Suite 4");
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
