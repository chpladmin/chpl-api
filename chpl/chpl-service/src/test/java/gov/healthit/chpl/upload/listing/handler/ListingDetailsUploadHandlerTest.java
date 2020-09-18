package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ListingDetailsUploadHandlerTest {
    private static final String HEADER_ROW_BEGIN = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C";
    private static final String LISTING_ROW_BEGIN = "15.02.02.3007.A056.01.00.0.180214,New";
    private static final String LISTING_SUBELEMENT_BEGIN = "15.02.02.3007.A056.01.00.0.180214,Subelement";

    private ErrorMessageUtil msgUtil;
    private ListingUploadHandlerUtil handlerUtil;
    private ListingDetailsUploadHandler handler;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.emptyFile"))).thenReturn("Empty file message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("upload.notCSV"))).thenReturn("Not CSV message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.emptyRows"))).thenReturn("Header only message");
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.requiredHeadingNotFound"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("The required heading %s was not found.", i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.upload.invalidBoolean"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("The value %s could not be converted to a yes/no field..", i.getArgument(1), ""));

        handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        DeveloperDetailsUploadHandler devHandler = new DeveloperDetailsUploadHandler(handlerUtil, msgUtil);
        handler = new ListingDetailsUploadHandler(devHandler,
                Mockito.mock(TargetedUsersUploadHandler.class),
                Mockito.mock(AccessibilityStandardsUploadHandler.class),
                Mockito.mock(QmsUploadHandler.class), Mockito.mock(IcsUploadHandler.class),
                Mockito.mock(CqmUploadHandler.class),
                handlerUtil, msgUtil);
    }

    @Test
    public void buildListing_ChplProductNumberExists_ReturnsCorrectChplProductNumber() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getChplProductNumber());
        assertEquals("15.02.02.3007.A056.01.00.0.180214", listing.getChplProductNumber());
    }

    @Test
    public void buildListing_ChplProductNumberEmpty_ReturnsEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(",New");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getChplProductNumber());
        assertEquals("", listing.getChplProductNumber());
    }

    @Test
    public void buildListing_ChplProductNumberWhitespace_TrimsResult() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("  ,New");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getChplProductNumber());
        assertEquals("", listing.getChplProductNumber());
    }

    public void buildListing_ChplProductNumberColumnMissing_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("RECORD_STATUS__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("New");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getChplProductNumber());
    }

    @Test
    public void buildListing_AccessibilityCertifiedNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getAccessibilityCertified());
    }

    @Test
    public void buildListing_BooleanValue0_ReturnsFalseAccessibilityCertified() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Accessibility Certified").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",0");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getAccessibilityCertified());
        assertEquals(Boolean.FALSE, listing.getAccessibilityCertified());
    }

    @Test
    public void buildListing_BooleanValue1_ReturnsTrueAccessibilityCertified() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Accessibility Certified").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",1");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getAccessibilityCertified());
        assertEquals(Boolean.TRUE, listing.getAccessibilityCertified());
    }

    @Test
    public void buildListing_BooleanValueNo_ReturnsFalseAccessibilityCertified() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Accessibility Certified").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",No");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getAccessibilityCertified());
        assertEquals(Boolean.FALSE, listing.getAccessibilityCertified());
    }

    @Test
    public void buildListing_BooleanValueYes_ReturnsTrueAccessibilityCertified() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Accessibility Certified").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",Yes");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getAccessibilityCertified());
        assertEquals(Boolean.TRUE, listing.getAccessibilityCertified());
    }

    @Test
    public void buildListing_BooleanValueEmptyString_ReturnsFalseAccessibilityCertified() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Accessibility Certified").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getAccessibilityCertified());
        assertEquals(Boolean.FALSE, listing.getAccessibilityCertified());
    }

    @Test(expected = ValidationException.class)
    public void buildListing_BooleanValueBad_ThrowsException() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Accessibility Certified").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",JUNK");
        assertNotNull(listingRecords);

        handler.parseAsListing(headingRecord, listingRecords);
    }

    @Test
    public void buildListing_CertificationDateNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getCertificationDate());
    }

    @Test
    public void buildListing_CertificationDateGood_ParsesDateValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFICATION_DATE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",20200909");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getCertificationDate());
    }

    @Test
    public void buildListing_CertificationDateEmpty_ParsesNullValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFICATION_DATE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getCertificationDate());
    }

    @Test(expected = ValidationException.class)
    public void buildListing_CertificationDateValueBad_ThrowsException() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFICATION_DATE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",BADDATE");
        assertNotNull(listingRecords);

        handler.parseAsListing(headingRecord, listingRecords);
    }

    @Test
    public void buildListing_EditionNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getCertificationEdition());
    }

    @Test
    public void buildListing_EditionGood_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERT_YEAR__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",2015");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getCertificationEdition());
        assertEquals("2015",
                listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString());
        assertNull(listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY));
    }

    @Test
    public void buildListing_AcbNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getCertifyingBody());
    }

    @Test
    public void buildListing_AcbGood_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFYING_ACB__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",Drummond Group");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getCertifyingBody());
        assertEquals("Drummond Group",
                listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
        assertNull(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY));
    }

    @Test
    public void buildListing_K1UrlExists_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",170.523(k)(1) URL").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",http://examplek1.com");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getTransparencyAttestationUrl());
        assertEquals("http://examplek1.com", listing.getTransparencyAttestationUrl());
    }

    @Test
    public void buildListing_K1UrlHasWhitespace_ReturnsTrimmed() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",170.523(k)(1) URL").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ", http://examplek1.com  ");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getTransparencyAttestationUrl());
        assertEquals("http://examplek1.com", listing.getTransparencyAttestationUrl());
    }

    @Test
    public void buildListing_K1UrlMissing_ReturnsEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",170.523(k)(1) URL").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getTransparencyAttestationUrl());
        assertEquals("", listing.getTransparencyAttestationUrl());
    }

    @Test
    public void buildListing_K1UrlNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getTransparencyAttestationUrl());
    }

    @Test
    public void buildListing_ProductNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getProduct());
    }

    @Test
    public void parseProduct_NoDeveloper_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",PRODUCT__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",Test Product");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getProduct());
        assertEquals("Test Product", listing.getProduct().getName());
        assertNull(listing.getProduct().getProductId());
    }

    @Test
    public void parseProduct_DeveloperAndProduct_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",VENDOR__C,PRODUCT__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",My Developer,Test Product");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getDeveloper());
        assertNotNull(listing.getProduct());
        assertEquals("Test Product", listing.getProduct().getName());
        assertNull(listing.getProduct().getProductId());
    }

    @Test
    public void buildListing_VersionNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getVersion());
    }

    @Test
    public void parseVersion_NoProduct_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",VERSION__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",v1");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getVersion());
        assertEquals("v1", listing.getVersion().getVersion());
        assertNull(listing.getVersion().getVersionId());
    }

    @Test
    public void parseVersion_DeveloperAndProductAndVersion_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",VENDOR__C,PRODUCT_C,VERSION__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN
                + ",My Developer,Test Product,v1");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getDeveloper());
        assertNotNull(listing.getProduct());
        assertNotNull(listing.getVersion());
        assertEquals("v1", listing.getVersion().getVersion());
        assertNull(listing.getVersion().getVersionId());
    }

    @Test
    public void buildListing_AtlNoColumn_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getTestingLabs());
        assertEquals(0, listing.getTestingLabs().size());
    }

    @Test
    public void buildListing_SingleAtl_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",TESTING_ATL__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",Drummond Group");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getTestingLabs());
        assertEquals(1, listing.getTestingLabs().size());
        CertifiedProductTestingLab parsedAtl = listing.getTestingLabs().get(0);
        assertNull(parsedAtl.getTestingLabId());
        assertEquals("Drummond Group", parsedAtl.getTestingLabName());
    }

    @Test
    public void buildListing_MultipleAtls_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",TESTING_ATL__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",Drummond Group\n"
                + LISTING_SUBELEMENT_BEGIN + ",ICSA Labs");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getTestingLabs());
        assertEquals(2, listing.getTestingLabs().size());
        listing.getTestingLabs().stream().forEach(parsedAtl -> {
            assertNull(parsedAtl.getTestingLabId());
            assertNotNull(parsedAtl.getTestingLabName());
        });
    }
}
