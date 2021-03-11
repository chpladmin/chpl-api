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
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperReviewerTest {
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

    private ErrorMessageUtil errorMessageUtil;
    private DeveloperReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

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
        reviewer = new DeveloperReviewer(errorMessageUtil);
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
    public void review_developerWithNullName_hasError() {
        Developer developer = buildValidDeveloper();
        developer.setName(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_DEVELOPER_NAME));
    }

    @Test
    public void review_developerWithNullSelfDeveloperNullString_hasError() {
        Developer developer = buildValidDeveloper();
        developer.setSelfDeveloper(null);
        developer.setSelfDeveloperStr(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_SELF_DEVELOPER));
    }

    @Test
    public void review_developerWithNullSelfDeveloperEmptyString_hasError() {
        Developer developer = buildValidDeveloper();
        developer.setSelfDeveloper(null);
        developer.setSelfDeveloperStr("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_SELF_DEVELOPER));
    }

    @Test
    public void review_developerWithNullSelfDeveloperInvalidString_hasError() {
        Developer developer = buildValidDeveloper();
        developer.setSelfDeveloper(null);
        developer.setSelfDeveloperStr("Junk");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_SELF_DEVELOPER, "Junk", "")));
    }

    @Test
    public void review_developerWithNullWebsite_hasError() {
        Developer developer = buildValidDeveloper();
        developer.setWebsite(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_WEBSITE));
    }

    @Test
    public void review_developerWithEmptyWebsite_hasError() {
        Developer developer = buildValidDeveloper();
        developer.setWebsite("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_WEBSITE));
    }

    @Test
    public void review_developerWithMalformedWebsite_hasError() {
        Developer developer = buildValidDeveloper();
        developer.setWebsite("junk");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(INVALID_WEBSITE));
    }

    @Test
    public void review_developerWithMalformedWebsiteWithNewline_hasError() {
        Developer developer = buildValidDeveloper();
        developer.setWebsite("ju\r\nnk");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(INVALID_WEBSITE));
    }

    @Test
    public void review_developerWithNullAddress_hasError() {
        Developer developer = buildValidDeveloper();
        developer.setAddress(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ADDRESS));
    }

    @Test
    public void review_developerWithNullStreetAddress_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getAddress().setLine1(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STREET_ADDRESS));
    }

    @Test
    public void review_developerWithEmptyStreetAddress_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getAddress().setLine1("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STREET_ADDRESS));
    }

    @Test
    public void review_developerWithNullCity_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getAddress().setCity(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CITY));
    }

    @Test
    public void review_developerWithEmptyCity_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getAddress().setCity("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CITY));
    }

    @Test
    public void review_developerWithNullState_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getAddress().setState(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STATE));
    }

    @Test
    public void review_developerWithEmptyState_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getAddress().setState("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STATE));
    }

    @Test
    public void review_developerWithNullZipcode_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getAddress().setZipcode(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ZIP));
    }

    @Test
    public void review_developerWithEmptyZipcode_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getAddress().setZipcode("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ZIP));
    }

    @Test
    public void review_developerWithNullContact_hasError() {
        Developer developer = buildValidDeveloper();
        developer.setContact(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CONTACT));
    }

    @Test
    public void review_developerWithNullContactName_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getContact().setFullName(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CONTACT_NAME));
    }

    @Test
    public void review_developerWithEmptyContactName_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getContact().setFullName("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CONTACT_NAME));
    }

    @Test
    public void review_developerWithNullEmail_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getContact().setEmail(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_EMAIL));
    }

    @Test
    public void review_developerWithEmptyEmail_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getContact().setEmail("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_EMAIL));
    }

    @Test
    public void review_developerWithNullPhoneNumber_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getContact().setPhoneNumber(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_PHONE_NUMBER));
    }

    @Test
    public void review_developerWithEmptyPhoneNumber_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getContact().setPhoneNumber("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_PHONE_NUMBER));
    }

    @Test
    public void review_developerWithNoCurrentStatus_hasError() {
        Developer developer = buildValidDeveloper();
        developer.setStatus(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STATUS));
    }

    @Test
    public void review_developerWithNullStatusName_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getStatus().setStatus(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STATUS));
    }

    @Test
    public void review_developerWithEmptyStatusName_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getStatus().setStatus("");
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_STATUS));
    }

    @Test
    public void review_developerWithUnderOncBanStatusName_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getStatus().setStatus(DeveloperStatusType.UnderCertificationBanByOnc.getName());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_STATUS, "Test Name", DeveloperStatusType.UnderCertificationBanByOnc.getName())));
    }

    @Test
    public void review_developerWithSuspendedStatusName_hasError() {
        Developer developer = buildValidDeveloper();
        developer.getStatus().setStatus(DeveloperStatusType.SuspendedByOnc.getName());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_STATUS, "Test Name", DeveloperStatusType.SuspendedByOnc.getName())));
    }

    @Test
    public void review_developerWithAllFields_noError() {
        Developer developer = buildValidDeveloper();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(developer)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    private Developer buildValidDeveloper() {
        return Developer.builder()
                .name("Test Name")
                .selfDeveloper(true)
                .selfDeveloperStr("1")
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
