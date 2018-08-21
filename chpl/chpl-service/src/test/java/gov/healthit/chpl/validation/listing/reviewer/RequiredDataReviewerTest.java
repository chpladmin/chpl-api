package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.listing.ListingMockUtil;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class RequiredDataReviewerTest {
    private static final String D_1 = "170.315 (d)(1)";
    private static final String CERT_EDITION_NOT_FOUND_ERROR = 
            "Certification edition is required but was not found.";
    private static final String ATL_NOT_FOUND_ERROR = "Testing lab not found.";
    private static final String CERTID_NOT_FOUND_ERROR = "CHPL certification ID was not found.";
    private static final String CERT_DATE_NOT_FOUND_ERROR = "Certification date was not found.";
    private static final String DEV_NOT_FOUND_ERROR = "A developer is required.";
    private static final String PRODUCT_NOT_FOUND_ERROR = "A product name is required.";
    private static final String VERSION_NOT_FOUND_ERROR = "A product version is required.";
    private static final String STATUS_NOT_FOUND_ERROR = 
            "A certification status must be provided for every listing on the CHPL.";
    private static final String CRITERIA_MISSING_GAP_ERROR_START = "GAP is required for certification";

    @Autowired private ListingMockUtil mockUtil;

    @Spy
    private CertificationResultRules certResultRules;
    
    @Spy
    private MessageSource messageSource;

    @Spy
    private ErrorMessageUtil msgUtil;

    @InjectMocks
    private RequiredDataReviewer requiredDataReivewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CRITERIA_MISSING_GAP_ERROR_START)
        .when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.missingGap"), ArgumentMatchers.anyString());
        Mockito.doReturn(STATUS_NOT_FOUND_ERROR)
        .when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.noStatusProvided"));
        Mockito.doReturn(ATL_NOT_FOUND_ERROR)
        .when(msgUtil).getMessage(
                ArgumentMatchers.eq("atl.notFound"));
        Mockito.when(certResultRules.hasCertOption(
                ArgumentMatchers.anyString(), 
                ArgumentMatchers.eq(CertificationResultRules.GAP)))
        .thenReturn(false);
        Mockito.when(certResultRules.hasCertOption(
                ArgumentMatchers.eq(D_1), 
                ArgumentMatchers.eq(CertificationResultRules.GAP)))
        .thenReturn(true);
    }

    @Test
    public void testValidListing_DoesNotHaveErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        requiredDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertFalse(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertFalse(hasMissingGapError(listing));
    }

    @Test
    public void testNullCertEdition_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setCertificationEdition(null);
        requiredDataReivewer.review(listing);
        assertTrue(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertFalse(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertFalse(hasMissingGapError(listing));
    }

    @Test
    public void testBlankCertEdition_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.getCertificationEdition().put("id", null);
        requiredDataReivewer.review(listing);
        assertTrue(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertFalse(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertFalse(hasMissingGapError(listing));
    }

    @Test
    public void testNullAtl_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setTestingLabs(null);
        requiredDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertTrue(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertFalse(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertFalse(hasMissingGapError(listing));
    }

    @Test
    public void testBlankAtl_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.getTestingLabs().clear();
        requiredDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertTrue(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertFalse(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertFalse(hasMissingGapError(listing));
    }

    @Test
    public void testNullCertificationId_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setAcbCertificationId(null);
        requiredDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertTrue(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertFalse(hasMissingGapError(listing));
    }

    @Test
    public void testBlankCertificationId_HasWarning() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setAcbCertificationId("");
        requiredDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertTrue(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertFalse(hasMissingGapError(listing));
    }

    @Test
    public void testNullCertificationDate_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setCertificationDate(null);
        requiredDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertFalse(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertTrue(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertFalse(hasMissingGapError(listing));
    }

    @Test
    public void testNullDeveloper_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setDeveloper(null);
        requiredDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertFalse(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertTrue(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertFalse(hasMissingGapError(listing));
    }

    @Test
    public void testNullProduct_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setProduct(null);
        requiredDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertFalse(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertTrue(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertFalse(hasMissingGapError(listing));
    }

    @Test
    public void testNullVersion_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setVersion(null);
        requiredDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertFalse(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertTrue(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertFalse(hasMissingGapError(listing));
    }

    @Test
    public void testNullStatus_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.setCertificationEvents(null);
        requiredDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertFalse(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertTrue(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertFalse(hasMissingGapError(listing));
    }

    @Test
    public void testBlankStatus_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.getCertificationEvents().clear();
        requiredDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertFalse(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertTrue(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertFalse(hasMissingGapError(listing));
    }

    @Test
    public void testMissingGap_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        for(CertificationResult certResult : listing.getCertificationResults()) {
            if(certResult.getNumber().equals(D_1)) {
                certResult.setGap(null);
            }
        }
        requiredDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(CERT_EDITION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ATL_NOT_FOUND_ERROR));
        assertFalse(listing.getWarningMessages().contains(CERTID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(CERT_DATE_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(PRODUCT_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(VERSION_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(STATUS_NOT_FOUND_ERROR));
        assertTrue(hasMissingGapError(listing));
    }
    
    private boolean hasMissingGapError(CertifiedProductSearchDetails listing) {
        for(String error : listing.getErrorMessages()) {
            if(error.startsWith(CRITERIA_MISSING_GAP_ERROR_START)) {
                return true;
            }
        }
        return false;
    }
}
