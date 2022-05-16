package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestProcedure;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class FieldLengthReviewerTest {
    private static final String FIELD_TOO_LONG = "You have exceeded the max length, %s characters, for the %s. You will need to correct this error before you can confirm. Current value: '%s'";

    private ErrorMessageUtil errorMessageUtil;
    private MessageSource messageSource;
    private FieldLengthReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        messageSource = Mockito.mock(MessageSource.class);

        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.developerName"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.developerName.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "developer name", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.productName"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.productName.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "product name", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.productVersion"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.productVersion.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "product version", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.qmsStandard"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("255");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.qmsStandard.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "qms standard name", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.accessibilityStandard"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("500");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.accessibilityStandard.maxlength"),
            ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "accessibility standard name", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.targetedUser"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("300");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.targetedUser.maxlength"),
            ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "targeted user", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.acbCertificationId"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("250");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.acbCertificationId.maxlength"),
            ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "ACB certification id", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.170523k1Url"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("1024");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.170523k1Url.maxlength"),
            ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "Mandatory Disclosures", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.apiDocumentationLink"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("1024");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.apiDocumentationLink.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(String.format(FIELD_TOO_LONG, "1024", "api documentation link", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.exportDocumentationLink"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("1024");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.exportDocumentationLink.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(String.format(FIELD_TOO_LONG, "1024", "export documentation link", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.documentationUrlLink"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("1024");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.documentationUrlLink.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(String.format(FIELD_TOO_LONG, "1024", "documentation URL link", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.useCasesLink"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("1024");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.useCasesLink.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(String.format(FIELD_TOO_LONG, "1024", "use cases link", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.serviceBaseUrlListLink"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("1024");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.serviceBaseUrlListLink.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(String.format(FIELD_TOO_LONG, "1024", "service base url list link", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.testToolVersion"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.testToolVersion.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(String.format(FIELD_TOO_LONG, "20", "test tool version", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.testDataVersion"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.testDataVersion.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(String.format(FIELD_TOO_LONG, "20", "test data version", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.testProcedureVersion"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.testProcedureVersion.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(String.format(FIELD_TOO_LONG, "20", "test procedure version", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.sedReportHyperlink"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.sedReportHyperlink.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(String.format(FIELD_TOO_LONG, "20", "SED Report Hyperlink", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.ucdProcessName"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.ucdProcessName.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(String.format(FIELD_TOO_LONG, "20", "UCD Process Name", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.taskIdentifier"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.taskIdentifier.maxlength"),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(String.format(FIELD_TOO_LONG, "20", "Task Identifier", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.taskRatingScale"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.taskRatingScale.maxlength"),
            ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "Task Rating Scale", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.participantIdentifier"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.participantIdentifier.maxlength"),
            ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "Participant Identifier", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.participantGender"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.participantGender.maxlength"),
            ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "Participant Gender", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.participantOccupation"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.participantOccupation.maxlength"),
            ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "Participant Occupation", "placeholder"));
        Mockito.when(messageSource.getMessage(ArgumentMatchers.eq("maxLength.participantAssistiveTechnology"), ArgumentMatchers.isNull(), ArgumentMatchers.any()))
            .thenReturn("20");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.participantAssistiveTechnology.maxlength"),
            ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(String.format(FIELD_TOO_LONG, "20", "Participant Assistive Technology Needs", "placeholder"));

        reviewer = new FieldLengthReviewer(errorMessageUtil, messageSource);
    }

    @Test
    public void review_nullDeveloperName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .name(null)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyDeveloperName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .name("")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortDeveloperName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .name("test")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longDeveloperName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .name("testtesttesttesttesttest")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "developer name", "placeholder")));
    }

    @Test
    public void review_nullProductName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .product(Product.builder()
                        .name(null)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyProductName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .product(Product.builder()
                        .name("")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortProductName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .product(Product.builder()
                        .name("test")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longProductName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .product(Product.builder()
                        .name("testtesttesttesttesttesttest")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "product name", "placeholder")));
    }

    @Test
    public void review_nullVersionName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .version(ProductVersion.builder()
                        .version(null)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyVersionName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .version(ProductVersion.builder()
                        .version("")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortVersionName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .version(ProductVersion.builder()
                        .version("01")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longVersionName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .version(ProductVersion.builder()
                        .version("1234567890.0987654321")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "product version", "placeholder")));
    }

    @Test
    public void review_nullQmsStandards_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setQmsStandards(null);

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyQmsStandards_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(new ArrayList<CertifiedProductQmsStandard>())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortQmsStandardName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .qmsStandardName("short name")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longQmsStandardName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .qmsStandardName(createStringLongerThan(255, "a"))
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "qms standard name", "placeholder")));
    }

    @Test
    public void review_oneShortAndOneLongQmsStandardName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .qmsStandardName(createStringLongerThan(255, "a"))
                        .build())
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .qmsStandardName("short name")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "qms standard name", "placeholder")));
    }

    @Test
    public void review_nullAccessibilityStandards_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setAccessibilityStandards(null);

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyAccessibilityStandards_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandards(new ArrayList<CertifiedProductAccessibilityStandard>())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortAccessibilityStandardName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandard(CertifiedProductAccessibilityStandard.builder()
                        .accessibilityStandardName("short name")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longAccessibilityStandardName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandard(CertifiedProductAccessibilityStandard.builder()
                        .accessibilityStandardName(createStringLongerThan(500, "a"))
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "accessibility standard name", "placeholder")));
    }

    @Test
    public void review_oneShortAndOneLongAccessibilityStandardName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandard(CertifiedProductAccessibilityStandard.builder()
                        .accessibilityStandardName(createStringLongerThan(500, "a"))
                        .build())
                .accessibilityStandard(CertifiedProductAccessibilityStandard.builder()
                        .accessibilityStandardName("short name")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "accessibility standard name", "placeholder")));
    }

    @Test
    public void review_nullTargetedUsers_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setTargetedUsers(null);

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTargetedUsers_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .targetedUsers(new ArrayList<CertifiedProductTargetedUser>())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortTargetedUserName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .targetedUser(CertifiedProductTargetedUser.builder()
                        .targetedUserName("short name")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longTargetedUserName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .targetedUser(CertifiedProductTargetedUser.builder()
                        .targetedUserName(createStringLongerThan(300, "a"))
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "targeted user", "placeholder")));
    }

    @Test
    public void review_oneShortAndOneLongTargetedUserName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .targetedUser(CertifiedProductTargetedUser.builder()
                        .targetedUserName(createStringLongerThan(300, "a"))
                        .build())
                .targetedUser(CertifiedProductTargetedUser.builder()
                        .targetedUserName("short name")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "targeted user", "placeholder")));
    }

    @Test
    public void review_nullAcbCertificationId_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .acbCertificationId(null)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyAcbCertificationId_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .acbCertificationId("")
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortAcbCertificationId_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .acbCertificationId("short name")
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longAcbCertificationId_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .acbCertificationId(createStringLongerThan(250, "h"))
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "ACB certification id", "placeholder")));
    }

    @Test
    public void review_nullTransparencyAttestationUrl_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .mandatoryDisclosures(null)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTransparencyAttestationUrl_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .mandatoryDisclosures("")
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortTransparencyAttestationUrl_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .mandatoryDisclosures("short name")
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longTransparencyAttestationUrl_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .mandatoryDisclosures(createStringLongerThan(1024, "h"))
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "Mandatory Disclosures", "placeholder")));
    }

    @Test
    public void review_nullTestTools_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestToolsUsed(null);
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestTools_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testToolsUsed(new ArrayList<CertificationResultTestTool>())
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortTestToolVersion_noError() {
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testToolName("a name")
                .testToolId(1L)
                .testToolVersion("1.1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testToolsUsed(testTools)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longTestToolVersion_hasError() {
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testToolName("a name")
                .testToolId(1L)
                .testToolVersion(createStringLongerThan(20, "1"))
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testToolsUsed(testTools)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "test tool version", "placeholder")));
    }

    @Test
    public void review_nullTestData_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestDataUsed(null);
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestData_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testDataUsed(new ArrayList<CertificationResultTestData>())
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortTestDataVersion_noError() {
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(1L)
                        .name("a name")
                        .build())
                .version("1.1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testDataUsed(testData)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longTestDataVersion_hasError() {
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(1L)
                        .name("a name")
                        .build())
                .version(createStringLongerThan(20, "A"))
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testDataUsed(testData)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "test data version", "placeholder")));
    }

    @Test
    public void review_nullTestProcedure_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestProcedures(null);
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestProcedures_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testProcedures(new ArrayList<CertificationResultTestProcedure>())
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortTestProcedureVersion_noError() {
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
        testProcedures.add(CertificationResultTestProcedure.builder()
                .testProcedure(TestProcedure.builder()
                        .id(1L)
                        .name("a name")
                        .build())
                .testProcedureVersion("1.1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testProcedures(testProcedures)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longTestProcedureVersion_hasError() {
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
        testProcedures.add(CertificationResultTestProcedure.builder()
                .testProcedure(TestProcedure.builder()
                        .id(1L)
                        .name("a name")
                        .build())
                .testProcedureVersion(createStringLongerThan(20, "A"))
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testProcedures(testProcedures)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "test procedure version", "placeholder")));
    }

    @Test
    public void review_nullSedReportHyperlink_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sedReportFileLocation(null)
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptySedReportHyperlink_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sedReportFileLocation("")
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortSedReportHyperlink_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sedReportFileLocation("shorturl")
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longSedReportHyperlink_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sedReportFileLocation(createStringLongerThan(20, "A"))
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "SED Report Hyperlink", "placeholder")));
    }

    @Test
    public void review_nullUcdProcesses_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().setUcdProcesses(null);
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyUcdProcesses_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortUcdProcessName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getUcdProcesses().add(UcdProcess.builder()
                .name("name")
                .details("some details")
                .build());
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longUcdProcessName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getUcdProcesses().add(UcdProcess.builder()
                .name(createStringLongerThan(20, "A"))
                .details("some details")
                .build());
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "UCD Process Name", "placeholder")));
    }

    @Test
    public void review_nullTestTasks_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().setTestTasks(null);
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestTasks_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortTestTaskUniqueId_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder()
                .uniqueId("1A")
                .build());
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longTestTaskUniqueId_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder()
                .uniqueId(createStringLongerThan(20, "A"))
                .build());
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "Task Identifier", "placeholder")));
    }

    @Test
    public void review_shortTestTaskRatingScale_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder() .build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder()
                .taskRatingScale("TEST")
                .build());
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longTestTaskRatingScale_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder()
                .taskRatingScale(createStringLongerThan(20, "A"))
                .build());
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "Task Rating Scale", "placeholder")));
    }

    @Test
    public void review_nullTestParticipants_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder()
                .uniqueId("1A")
                .build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(null);
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestParticipants_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder()
                        .build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder()
                .uniqueId("1A")
                .build());
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_shortTestTaskParticipantUniqueId_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().uniqueId("1A").build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(TestParticipant.builder()
                .uniqueId("1P")
                .build()).collect(Collectors.toSet()));
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longTestParticipantUniqueId_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().uniqueId("1A").build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(TestParticipant.builder()
                .uniqueId(createStringLongerThan(20, "A"))
                .build()).collect(Collectors.toSet()));
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "Participant Identifier", "placeholder")));
    }

    @Test
    public void review_shortTestTaskParticipantGender_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().uniqueId("1A").build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(TestParticipant.builder()
                .gender("F")
                .build()).collect(Collectors.toSet()));
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longTestParticipantGender_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().uniqueId("1A").build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(TestParticipant.builder()
                .gender(createStringLongerThan(20, "A"))
                .build()).collect(Collectors.toSet()));
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "Participant Gender", "placeholder")));
    }

    @Test
    public void review_shortTestTaskParticipantOccupation_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().uniqueId("1A").build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(TestParticipant.builder()
                .occupation("Teacher")
                .build()).collect(Collectors.toSet()));
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longTestParticipantOccupation_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().uniqueId("1A").build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(TestParticipant.builder()
                .occupation(createStringLongerThan(20, "A"))
                .build()).collect(Collectors.toSet()));
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "Participant Occupation", "placeholder")));
    }

    @Test
    public void review_shortTestTaskParticipantAssistiveTechnologyNeeds_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().uniqueId("1A").build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(TestParticipant.builder()
                .assistiveTechnologyNeeds("screen reader")
                .build()).collect(Collectors.toSet()));
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_longTestParticipantAssistiveTechnologyNeeds_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder().build())
                .build();
        listing.getSed().getTestTasks().add(TestTask.builder().uniqueId("1A").build());
        listing.getSed().getTestTasks().get(0).setTestParticipants(Stream.of(TestParticipant.builder()
                .assistiveTechnologyNeeds(createStringLongerThan(20, "A"))
                .build()).collect(Collectors.toSet()));
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(FIELD_TOO_LONG, "20", "Participant Assistive Technology Needs", "placeholder")));
    }

    private String createStringLongerThan(int minLength, String charToUse) {
        StringBuffer buf = new StringBuffer();
        int charCount = 0;
        while (charCount <= minLength) {
            buf.append(charToUse);
            charCount = buf.length();
        }
        return buf.toString();
    }
}
