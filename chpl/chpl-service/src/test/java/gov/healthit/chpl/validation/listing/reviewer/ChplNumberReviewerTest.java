package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ListingMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class ChplNumberReviewerTest {
    private static final String PRODUCT_CODE_ERROR = "The product code is required and must be "
            + ChplProductNumberUtil.PRODUCT_CODE_LENGTH
            + " characters in length containing only the characters A-Z, a-z, 0-9, and _.";
    private static final String VERSION_CODE_ERROR = "The version code is required and must be "
            + ChplProductNumberUtil.VERSION_CODE_LENGTH
            + " characters in length containing only the characters A-Z, a-z, 0-9, and _.";
    private static final String ICS_CODE_ERROR = "The ICS code is required and must be "
            + ChplProductNumberUtil.ICS_CODE_LENGTH
            + " characters in length with a value between 00-99. "
            + "If you have exceeded the maximum inheritance level of 99, please contact the CHPL team for further assistance.";
    private static final String ADDSOFT_CODE_ERROR = "The additional software code is required and must be "
            + ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_LENGTH
            + " character in length containing only the characters 0 or 1.";
    private static final String CERTDATE_CODE_ERROR = "The certified date code is required and must be "
            + ChplProductNumberUtil.CERTIFIED_DATE_CODE_LENGTH
            + " characters in length containing only the characters 0-9.";
    private static final String ICS_CODE_0_HAS_PARENTS_ERROR =
            "ICS Code is 00, which means this Listing must not inherit from any other Listings";
    private static final String ICS_CODE_FALSE_HAS_ICS_ERROR =
            "The unique id indicates the product does not have ICS but the value for Inherited Certification Status is true.";
    private static final String ICS_CODE_TRUE_NO_ICS_ERROR =
            "The unique id indicates the product does have ICS but the value for Inherited Certification Status is false.";
    private static final String DUPLICATE_CHPLID_ERROR_END = "one already exists with this value.";

    private ListingMockUtil mockUtil = new ListingMockUtil();

    @Autowired
    private MessageSource messageSource;

    @Spy
    private ChplProductNumberUtil chplProductNumberUtil;

    @Spy
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    @Spy
    private CertificationResultManager certificationResultManager;

    private ChplNumberReviewer chplNumberReviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        chplNumberReviewer = new ChplNumberReviewer(certificationResultManager, chplProductNumberUtil, msgUtil);

        Mockito.doReturn(PRODUCT_CODE_ERROR)
                .when(msgUtil).getMessage(
                        ArgumentMatchers.eq("listing.badProductCodeChars"), ArgumentMatchers.anyInt());
        Mockito.doReturn(VERSION_CODE_ERROR)
                .when(msgUtil).getMessage(
                        ArgumentMatchers.eq("listing.badVersionCodeChars"), ArgumentMatchers.anyInt());
        Mockito.doReturn(ICS_CODE_ERROR)
                .when(msgUtil).getMessage(
                        ArgumentMatchers.eq("listing.badIcsCodeChars"), ArgumentMatchers.anyInt());
        Mockito.doReturn(ADDSOFT_CODE_ERROR)
                .when(msgUtil).getMessage(
                        ArgumentMatchers.eq("listing.badAdditionalSoftwareCodeChars"), ArgumentMatchers.anyInt());
        Mockito.doReturn(CERTDATE_CODE_ERROR)
                .when(msgUtil).getMessage(
                        ArgumentMatchers.eq("listing.badCertifiedDateCodeChars"), ArgumentMatchers.anyInt());
        Mockito.doReturn(ICS_CODE_0_HAS_PARENTS_ERROR)
                .when(msgUtil).getMessage(
                        ArgumentMatchers.eq("listing.ics00"));
        Mockito.doReturn(ICS_CODE_FALSE_HAS_ICS_ERROR)
                .when(msgUtil).getMessage(
                        ArgumentMatchers.eq("listing.icsCodeFalseValueTrue"));
        Mockito.doReturn(ICS_CODE_TRUE_NO_ICS_ERROR)
        .when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.icsCodeTrueValueFalse"));
        Mockito.doReturn(DUPLICATE_CHPLID_ERROR_END)
        .when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.chplProductNumber.systemChangedNotUnique"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        Mockito.doReturn(true).when(chplProductNumberUtil).isUnique(ArgumentMatchers.anyString());
        Mockito.doReturn(false).when(certificationResultManager)
                .getCertifiedProductHasAdditionalSoftware(ArgumentMatchers.anyLong());
    }

    @Test
    public void testValidChplProductNumber_DoesNotHaveErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
    }

    @Test
    public void testBadProductCodeLength_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setChplProductNumber(mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.PRODUCT_CODE_INDEX, "012"));
        chplNumberReviewer.review(listing);
        assertTrue(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
    }

    @Test
    public void testBadProductCodeCharacter_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setChplProductNumber(mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.PRODUCT_CODE_INDEX, "0!23"));
        chplNumberReviewer.review(listing);
        assertTrue(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
    }

    @Test
    public void testBadVersionCodeLength_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setChplProductNumber(mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.VERSION_CODE_INDEX, "012"));
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertTrue(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
    }

    @Test
    public void testBadVersionCodeCharacter_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setChplProductNumber(mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.VERSION_CODE_INDEX, "0!"));
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertTrue(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
    }

    @Test
    public void testBadIcsCodeLength_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setChplProductNumber(mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "1"));
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertTrue(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
    }

    @Test
    public void testBadIcsCodeCharacter_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setChplProductNumber(mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "0Y"));
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertTrue(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
    }

    @Test
    public void testHasIcsTrueButNoParents_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        // our listing doesn't have ICS so change it to true
        listing.setChplProductNumber(mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "01"));
        listing.getIcs().setInherits(Boolean.FALSE);
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertTrue(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
    }

    @Test
    public void testHasIcsFalseButHasParents_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.getIcs().setInherits(Boolean.TRUE);
        CertifiedProduct parentListing = new CertifiedProduct();
        String parentUniqueId = mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "00");
        parentListing.setChplProductNumber(parentUniqueId);
        parentListing.setEdition("2015");
        parentListing.setId(0L);
        listing.getIcs().getParents().add(parentListing);
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertTrue(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertTrue(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
    }

    @Test
    public void testBadAdditionalSoftwareCodeLength_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setChplProductNumber(mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_INDEX, "00"));
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertTrue(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
    }

    @Test
    public void testBadAdditionalSoftwareCodeCharacter_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setChplProductNumber(mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_INDEX, "Y"));
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertTrue(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
    }

    @Test
    public void testBadCertifiedDateCodeLength_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setChplProductNumber(mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.CERTIFIED_DATE_CODE_INDEX, "20150701"));
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertTrue(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
    }

    @Test
    public void testBadCertifiedDateCodeCharacter_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setChplProductNumber(mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.CERTIFIED_DATE_CODE_INDEX, "15JUL1"));
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertTrue(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
    }

    @Test
    public void testAdditionalSoftwareCodeChanged_IsNotDuplicate_HasNoErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        // the mock listing does not have additional software;
        // add some to a criteria that was met.
        boolean addedSoftwareToOneCert = false;
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (!addedSoftwareToOneCert && cert.isSuccess()) {
                CertificationResultAdditionalSoftware addSoft = new CertificationResultAdditionalSoftware();
                addSoft.setCertificationResultId(cert.getId());
                addSoft.setId(1L);
                addSoft.setName("Microsoft Windows");
                addSoft.setVersion("8");
                cert.getAdditionalSoftware().add(addSoft);
                addedSoftwareToOneCert = true;
            }
        }
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
        assertFalse(hasDuplicateIdError(listing));
    }

    @Test
    public void testAdditionalSoftwareCodeChanged_IsDuplicate_HasError() {
        Mockito.doReturn(false).when(chplProductNumberUtil).isUnique(ArgumentMatchers.anyString());

        Mockito.doReturn(true)
                .when(certificationResultManager).getCertifiedProductHasAdditionalSoftware(ArgumentMatchers.anyLong());

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        // the mock listing does not have additional software;
        // add some to a criteria that was met.
        boolean addedSoftwareToOneCert = false;
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (!addedSoftwareToOneCert && cert.isSuccess()) {
                CertificationResultAdditionalSoftware addSoft = new CertificationResultAdditionalSoftware();
                addSoft.setCertificationResultId(cert.getId());
                addSoft.setId(1L);
                addSoft.setName("Microsoft Windows");
                addSoft.setVersion("8");
                cert.getAdditionalSoftware().add(addSoft);
                addedSoftwareToOneCert = true;
            }
        }
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
        assertTrue(hasDuplicateIdError(listing));
    }

    @Test
    public void testCertificationDateChanged_IsNotDuplicate_HasNoErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setCertificationDate(System.currentTimeMillis());
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
        assertFalse(hasDuplicateIdError(listing));
    }

    @Test
    public void testCertificationDateChanged_IsDuplicate_HasError() {
        Mockito.doReturn(false).when(chplProductNumberUtil).isUnique(ArgumentMatchers.anyString());

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setCertificationDate(System.currentTimeMillis());
        chplNumberReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(PRODUCT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ADDSOFT_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERTDATE_CODE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_0_HAS_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_FALSE_HAS_ICS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_CODE_TRUE_NO_ICS_ERROR));
        assertTrue(hasDuplicateIdError(listing));
    }

    private boolean hasDuplicateIdError(CertifiedProductSearchDetails listing) {
        for (String error : listing.getErrorMessages()) {
            if (error.endsWith(DUPLICATE_CHPLID_ERROR_END)) {
                return true;
            }
        }
        return false;
    }
}
