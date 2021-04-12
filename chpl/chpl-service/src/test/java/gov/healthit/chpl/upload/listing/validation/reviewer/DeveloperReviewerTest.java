package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperReviewerTest {
    private static final String SYSTEM_AND_USER_DATA_MISMATCH = "The user-entered developer %s field of '%s' does not match the system value of '%s'.";
    private static final String MISSING_DEVELOPER = "A developer is required to be associated with the listing.";
    private static final String MISSING_DEVELOPER_NAME = "A developer name is required.";
    private static final String INVALID_SELF_DEVELOPER = "Self developer value %s is invalid. It must be true or false.";
    private static final String MISSING_SELF_DEVELOPER = "Self developer value is missing.";
    private static final String MISSING_WEBSITE = "Developer website is required.";
    private static final String INVALID_WEBSITE = "Developer website is improperly formed.";
    private static final String MISSING_ADDRESS = "An address is required for the developer.";
    private static final String MISSING_STREET_ADDRESS = "Developer street address is required.";
    private static final String MISSING_CITY = "Developer city is required.";
    private static final String MISSING_STATE = "Developer state is required.";
    private static final String MISSING_ZIP = "Developer zipcode is required.";
    private static final String MISSING_CONTACT = "A contact is required for the developer.";
    private static final String MISSING_EMAIL = "Developer contact email address is required.";
    private static final String MISSING_PHONE_NUMBER = "Developer contact phone number is required.";
    private static final String MISSING_CONTACT_NAME = "Developer contact name is required.";
    private static final String MISSING_STATUS = "The developer must have a current status specified.";
    private static final String INVALID_STATUS = "The developer %s has a status of %s. Certified products belonging to this developer cannot be created until its status returns to Active.";
    private static final String NOT_FOUND = "The developer %s was not found in the system.";

    private ErrorMessageUtil errorMessageUtil;
    private DeveloperReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.developer.userAndSystemMismatch"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(SYSTEM_AND_USER_DATA_MISMATCH, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.missingDeveloper")))
            .thenReturn(MISSING_DEVELOPER);
        Mockito.when(errorMessageUtil.getMessage("developer.nameRequired"))
            .thenReturn(MISSING_DEVELOPER_NAME);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.selfDeveloper.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_SELF_DEVELOPER, i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.selfDeveloper.missing")))
            .thenReturn(MISSING_SELF_DEVELOPER);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.websiteRequired")))
            .thenReturn(MISSING_WEBSITE);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.websiteIsInvalid")))
            .thenReturn(INVALID_WEBSITE);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.addressRequired")))
            .thenReturn(MISSING_ADDRESS);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.address.streetRequired")))
            .thenReturn(MISSING_STREET_ADDRESS);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.address.cityRequired")))
            .thenReturn(MISSING_CITY);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.address.stateRequired")))
            .thenReturn(MISSING_STATE);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.address.zipRequired")))
            .thenReturn(MISSING_ZIP);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.contactRequired")))
            .thenReturn(MISSING_CONTACT);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.contact.emailRequired")))
            .thenReturn(MISSING_EMAIL);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.contact.phoneRequired")))
            .thenReturn(MISSING_PHONE_NUMBER);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.contact.nameRequired")))
            .thenReturn(MISSING_CONTACT_NAME);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("developer.status.noCurrent")))
            .thenReturn(MISSING_STATUS);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.developer.notActive.noCreate"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_STATUS, i.getArgument(1), i.getArgument(2)));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.developer.notFound"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(NOT_FOUND, i.getArgument(1), ""));

        reviewer = new DeveloperReviewer(errorMessageUtil,
                new ChplProductNumberUtil(), new ListingUploadHandlerUtil(errorMessageUtil));
    }

    @Test
    public void review_nullDeveloperLegacyListing_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_DEVELOPER));
    }

    @Test
    public void review_nullDeveloper_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.01.1.210102")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_DEVELOPER));
    }

    @Test
    public void review_newDeveloperWithNullName_hasError() {
        Developer developer = buildNewDeveloper();
        developer.setName(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_DEVELOPER_NAME));
    }

    @Test
    public void review_systemDeveloperWithNullName_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setName(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_DEVELOPER_NAME));
    }

    @Test
    public void review_newDeveloperWithNullSelfDeveloperNullString_hasError() {
        Developer developer = buildNewDeveloper();
        developer.setSelfDeveloper(null);
        developer.setUserEnteredSelfDeveloper(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_SELF_DEVELOPER));
    }

    @Test
    public void review_systemDeveloperWithNullSelfDeveloperNullString_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setSelfDeveloper(null);
        developer.setUserEnteredSelfDeveloper(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_SELF_DEVELOPER));
    }

    @Test
    public void review_newDeveloperWithNullSelfDeveloperEmptyString_hasError() {
        Developer developer = buildNewDeveloper();
        developer.setSelfDeveloper(null);
        developer.setUserEnteredSelfDeveloper("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_SELF_DEVELOPER));
    }

    @Test
    public void review_systemDeveloperWithNullSelfDeveloperEmptyString_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setSelfDeveloper(null);
        developer.setUserEnteredSelfDeveloper("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_SELF_DEVELOPER));
    }

    @Test
    public void review_newDeveloperWithNullSelfDeveloperInvalidString_hasError() {
        Developer developer = buildNewDeveloper();
        developer.setSelfDeveloper(null);
        developer.setUserEnteredSelfDeveloper("Junk");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_SELF_DEVELOPER, "Junk", "")));
    }

    @Test
    public void review_systemDeveloperWithNullSelfDeveloperInvalidString_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setSelfDeveloper(null);
        developer.setUserEnteredSelfDeveloper("Junk");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(INVALID_SELF_DEVELOPER, "Junk", "")));
    }

    @Test
    public void review_newDeveloperWithNullWebsite_hasError() {
        Developer developer = buildNewDeveloper();
        developer.setWebsite(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_WEBSITE));
    }

    @Test
    public void review_systemDeveloperWithNullWebsite_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setWebsite(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_WEBSITE));
    }

    @Test
    public void review_newDeveloperWithEmptyWebsite_hasError() {
        Developer developer = buildNewDeveloper();
        developer.setWebsite("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_WEBSITE));
    }

    @Test
    public void review_systemDeveloperWithEmptyWebsite_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setWebsite("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_WEBSITE));
    }

    @Test
    public void review_newDeveloperWithMalformedWebsite_hasError() {
        Developer developer = buildNewDeveloper();
        developer.setWebsite("junk");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(INVALID_WEBSITE));
    }

    @Test
    public void review_systemDeveloperWithMalformedWebsite_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setWebsite("junk");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(INVALID_WEBSITE));
    }

    @Test
    public void review_newDeveloperWithMalformedWebsiteWithNewline_hasError() {
        Developer developer = buildNewDeveloper();
        developer.setWebsite("ju\r\nnk");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(INVALID_WEBSITE));
    }

    @Test
    public void review_systemDeveloperWithMalformedWebsiteWithNewline_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setWebsite("ju\r\nnk");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(INVALID_WEBSITE));
    }

    @Test
    public void review_newDeveloperWithNullAddress_hasError() {
        Developer developer = buildNewDeveloper();
        developer.setAddress(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ADDRESS));
    }

    @Test
    public void review_sysemDeveloperWithNullAddress_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setAddress(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_ADDRESS));
    }

    @Test
    public void review_newDeveloperWithNullStreetAddress_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getAddress().setLine1(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STREET_ADDRESS));
    }

    @Test
    public void review_systemDeveloperWithNullStreetAddress_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getAddress().setLine1(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_STREET_ADDRESS));
    }

    @Test
    public void review_newDeveloperWithEmptyStreetAddress_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getAddress().setLine1("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STREET_ADDRESS));
    }

    @Test
    public void review_systemDeveloperWithEmptyStreetAddress_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getAddress().setLine1("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_STREET_ADDRESS));
    }

    @Test
    public void review_newDeveloperWithNullCity_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getAddress().setCity(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CITY));
    }

    @Test
    public void review_systemDeveloperWithNullCity_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getAddress().setCity(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_CITY));
    }

    @Test
    public void review_newDeveloperWithEmptyCity_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getAddress().setCity("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CITY));
    }

    @Test
    public void review_systemDeveloperWithEmptyCity_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getAddress().setCity("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_CITY));
    }

    @Test
    public void review_newDeveloperWithNullState_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getAddress().setState(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STATE));
    }

    @Test
    public void review_systemDeveloperWithNullState_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getAddress().setState(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_STATE));
    }

    @Test
    public void review_newDeveloperWithEmptyState_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getAddress().setState("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STATE));
    }

    @Test
    public void review_systemDeveloperWithEmptyState_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getAddress().setState("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_STATE));
    }

    @Test
    public void review_newDeveloperWithNullZipcode_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getAddress().setZipcode(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ZIP));
    }

    @Test
    public void review_systemDeveloperWithNullZipcode_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getAddress().setZipcode(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_ZIP));
    }

    @Test
    public void review_newDeveloperWithEmptyZipcode_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getAddress().setZipcode("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ZIP));
    }

    @Test
    public void review_systemDeveloperWithEmptyZipcode_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getAddress().setZipcode("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_ZIP));
    }

    @Test
    public void review_newDeveloperWithNullContact_hasError() {
        Developer developer = buildNewDeveloper();
        developer.setContact(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CONTACT));
    }

    @Test
    public void review_systemDeveloperWithNullContact_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setContact(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_CONTACT));
    }

    @Test
    public void review_newDeveloperWithNullContactName_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getContact().setFullName(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CONTACT_NAME));
    }

    @Test
    public void review_systemDeveloperWithNullContactName_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getContact().setFullName(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_CONTACT_NAME));
    }

    @Test
    public void review_newDeveloperWithEmptyContactName_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getContact().setFullName("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CONTACT_NAME));
    }

    @Test
    public void review_systemDeveloperWithEmptyContactName_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getContact().setFullName("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_CONTACT_NAME));
    }

    @Test
    public void review_newDeveloperWithNullEmail_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getContact().setEmail(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_EMAIL));
    }

    @Test
    public void review_systemDeveloperWithNullEmail_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getContact().setEmail(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_EMAIL));
    }

    @Test
    public void review_newDeveloperWithEmptyEmail_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getContact().setEmail("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_EMAIL));
    }

    @Test
    public void review_systemDeveloperWithEmptyEmail_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getContact().setEmail("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_EMAIL));
    }

    @Test
    public void review_newDeveloperWithNullPhoneNumber_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getContact().setPhoneNumber(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_PHONE_NUMBER));
    }

    @Test
    public void review_systemDeveloperWithNullPhoneNumber_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getContact().setPhoneNumber(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_PHONE_NUMBER));
    }

    @Test
    public void review_newDeveloperWithEmptyPhoneNumber_hasError() {
        Developer developer = buildNewDeveloper();
        developer.getContact().setPhoneNumber("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_PHONE_NUMBER));
    }

    @Test
    public void review_systemDeveloperWithEmptyPhoneNumber_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.getContact().setPhoneNumber("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_PHONE_NUMBER));
    }

    @Test
    public void review_newDeveloperWithNoCurrentStatus_noError() {
        Developer developer = buildNewDeveloper();
        developer.setStatus(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_systemDeveloperWithNoCurrentStatus_hasError() {
        Developer developer = buildSystemDeveloper();
        developer.setStatus(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STATUS));
    }

    @Test
    public void review_newDeveloperWithNullStatus_noError() {
        Developer developer = buildNewDeveloper();
        developer.getStatus().setStatus(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_systemDeveloperWithNullStatus_hasError() {
        Developer developer = buildSystemDeveloper();
        developer.getStatus().setStatus(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STATUS));
    }

    @Test
    public void review_newDeveloperWithEmptyStatusName_noError() {
        Developer developer = buildNewDeveloper();
        developer.getStatus().setStatus("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_systemDeveloperWithEmptyStatusName_hasError() {
        Developer developer = buildSystemDeveloper();
        developer.getStatus().setStatus("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STATUS));
    }

    @Test
    public void review_systemDeveloperWithUnderOncBanStatusName_hasError() {
        Developer developer = buildSystemDeveloper();
        developer.getStatus().setStatus(DeveloperStatusType.UnderCertificationBanByOnc.getName());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_STATUS, "Test Name", DeveloperStatusType.UnderCertificationBanByOnc.getName())));
    }

    @Test
    public void review_systemDeveloperWithSuspendedStatusName_hasError() {
        Developer developer = buildSystemDeveloper();
        developer.getStatus().setStatus(DeveloperStatusType.SuspendedByOnc.getName());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_STATUS, "Test Name", DeveloperStatusType.SuspendedByOnc.getName())));
    }

    @Test
    public void review_newDeveloperWithAllFields_noError() {
        Developer developer = buildNewDeveloper();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(developer)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_systemDeveloperWithAllFields_noError() {
        Developer developer = buildSystemDeveloper();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_systemDeveloperNameDoesNotMatchEnteredDeveloperName_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setUserEnteredName("Other Name");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(SYSTEM_AND_USER_DATA_MISMATCH, "name", "Other Name", "Test Name")));
    }

    @Test
    public void review_systemDeveloperWebsiteDoesNotMatchEnteredDeveloperWebsite_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setUserEnteredWebsite("http://www.test2.com");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(SYSTEM_AND_USER_DATA_MISMATCH, "website",
                "http://www.test2.com", "http://www.test.com")));
    }

    @Test
    public void review_systemSelfDeveloperValidAndDoesNotMatchEnteredSelfDeveloper_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setUserEnteredSelfDeveloper("blah");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
    }

    @Test
    public void review_systemSelfDeveloperNullAndDoesNotMatchEnteredSelfDeveloper_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setSelfDeveloper(null);
        developer.setUserEnteredSelfDeveloper("blah");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
    }

    @Test
    public void review_systemSelfDeveloperTrueAndDoesNotMatchEnteredSelfDeveloper_hasWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setUserEnteredSelfDeveloper("0");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
    }

    @Test
    public void review_systemSelfDeveloperValidAndDoesMatchEnteredSelfDeveloper_noWarning() {
        Developer developer = buildSystemDeveloper();
        developer.setUserEnteredSelfDeveloper("1");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_systemDeveloperDoesNotExist_hasError() {
        Developer developer = Developer.builder()
                .name("test dev")
                .website("http://www.test.com")
                .address(Address.builder()
                        .line1("test")
                        .city("test")
                        .state("test")
                        .zipcode("12345")
                        .build())
                .selfDeveloper(true)
                .contact(PointOfContact.builder()
                        .fullName("test")
                        .email("test@test.com")
                        .phoneNumber("123-456-7890")
                        .build())
                .userEnteredName("test dev")
                .userEnteredWebsite("http://www.test.com")
                .userEnteredAddress(Address.builder()
                        .line1("test")
                        .city("test")
                        .state("test")
                        .zipcode("12345")
                        .build())
                .userEnteredSelfDeveloper("true")
                .userEnteredPointOfContact(PointOfContact.builder()
                        .fullName("test")
                        .email("test@test.com")
                        .phoneNumber("123-456-7890")
                        .build())
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(NOT_FOUND, "test dev", "")));
    }

    private Developer buildNewDeveloper() {
        return buildDeveloper(null, null);
    }

    private Developer buildSystemDeveloper() {
        return buildDeveloper(1L, "1234");
    }

    private Developer buildDeveloper(Long id, String code) {
        return Developer.builder()
                .developerId(id)
                .developerCode(code)
                .name("Test Name")
                .selfDeveloper(true)
                .userEnteredSelfDeveloper("1")
                .website("http://www.test.com")
                .address(Address.builder()
                        .line1("test")
                        .city("test")
                        .state("test")
                        .zipcode("12345")
                        .build())
                .contact(PointOfContact.builder()
                        .fullName("test")
                        .email("test@test.com")
                        .phoneNumber("123-456-7890")
                        .build())
                .status(DeveloperStatus.builder()
                        .status(DeveloperStatusType.Active.getName())
                        .build())
                .build();
    }
}
