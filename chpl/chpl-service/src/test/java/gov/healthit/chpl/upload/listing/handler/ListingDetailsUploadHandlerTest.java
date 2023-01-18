package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.List;

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
    private static final String HEADER_ROW_BEGIN = "UNIQUE_CHPL_ID__C";
    private static final String LISTING_ROW_BEGIN = "15.02.02.3007.A056.01.00.0.180214";

    private ErrorMessageUtil msgUtil;
    private ListingUploadHandlerUtil handlerUtil;
    private ListingDetailsUploadHandler handler;
    private CertificationDateHandler certDateHandler;
    private CertificationEditionHandler editionHandler;

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
        DeveloperDetailsUploadHandler devHandler = new DeveloperDetailsUploadHandler(handlerUtil);
        certDateHandler = Mockito.mock(CertificationDateHandler.class);
        editionHandler = Mockito.mock(CertificationEditionHandler.class);
        handler = new ListingDetailsUploadHandler(editionHandler,
                certDateHandler,
                devHandler,
                Mockito.mock(TargetedUsersUploadHandler.class),
                Mockito.mock(AccessibilityStandardsUploadHandler.class),
                Mockito.mock(QmsUploadHandler.class), Mockito.mock(IcsUploadHandler.class),
                Mockito.mock(CqmUploadHandler.class),
                Mockito.mock(MeasuresUploadHandler.class),
                Mockito.mock(SedUploadHandler.class),
                Mockito.mock(CertificationResultUploadHandler.class),
                handlerUtil);
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
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(",");
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
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("  ");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getChplProductNumber());
        assertEquals("", listing.getChplProductNumber());
    }

    public void buildListing_ChplProductNumberColumnMissing_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("SOME_OTHER_COLUMN__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("Test");
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

    @Test
    public void buildListing_BooleanValueBad_RetunsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Accessibility Certified").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",JUNK");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNull(listing.getAccessibilityCertified());
        assertNotNull(listing.getAccessibilityCertifiedStr());
        assertEquals("JUNK", listing.getAccessibilityCertifiedStr());
    }

    @Test
    public void buildListing_CertificationDateNoColumnAndEmptyListingField_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("15.02.02.3007.A056.01.00.0.");
        assertNotNull(listingRecords);

        Mockito.when(certDateHandler.handle(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(null);
        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getCertificationDate());
    }

    @Test
    public void buildListing_CertificationDateNoColumn_ReturnsValueFromChplProductNumber() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        Mockito.when(certDateHandler.handle(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(LocalDate.of(2018, Month.FEBRUARY, 14));

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        Calendar cal = Calendar.getInstance();
        cal.set(2018, 1, 14, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(cal.getTime().getTime(), listing.getCertificationDate().longValue());
    }

    @Test
    public void buildListing_CertificationDateColumnValid_ParsesDateValueFromColumn() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFICATION_DATE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",20200909");
        assertNotNull(listingRecords);

        Mockito.when(certDateHandler.handle(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(LocalDate.of(2020, Month.SEPTEMBER, 9));

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getCertificationDate());
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 8, 9, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(cal.getTime().getTime(), listing.getCertificationDate().longValue());
    }

    @Test
    public void buildListing_CertificationDateColumnEmpty_ParsesValueFromChplProductNumber() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFICATION_DATE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",");
        assertNotNull(listingRecords);

        Mockito.when(certDateHandler.handle(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(LocalDate.of(2018, Month.FEBRUARY, 14));

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        Calendar cal = Calendar.getInstance();
        cal.set(2018, 1, 14, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(cal.getTime().getTime(), listing.getCertificationDate().longValue());
    }

    @Test
    public void buildListing_CertificationDateValueBadInChplProductNumberAndDateColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFICATION_DATE__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("14.bl.ah." + ",BADDATE");
        assertNotNull(listingRecords);

        Mockito.when(certDateHandler.handle(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(null);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNull(listing.getCertificationDate());
        assertNotNull(listing.getCertificationDateStr());
        assertEquals("BADDATE", listing.getCertificationDateStr());
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
        assertNotNull(listing.getMandatoryDisclosures());
        assertEquals("http://examplek1.com", listing.getMandatoryDisclosures());
    }

    @Test
    public void buildListing_K1UrlHasWhitespace_ReturnsTrimmed() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",170.523(k)(1) URL").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ", http://examplek1.com  ");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getMandatoryDisclosures());
        assertEquals("http://examplek1.com", listing.getMandatoryDisclosures());
    }

    @Test
    public void buildListing_K1UrlMissing_ReturnsEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",170.523(k)(1) URL").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getMandatoryDisclosures());
        assertEquals("", listing.getMandatoryDisclosures());
    }

    @Test
    public void buildListing_K1UrlNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getMandatoryDisclosures());
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
        assertNull(listing.getProduct().getId());
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
        assertNull(listing.getProduct().getId());
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
        assertNull(listing.getVersion().getId());
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
        assertNull(listing.getVersion().getId());
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
                + LISTING_ROW_BEGIN + ",ICSA Labs");
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

    @Test
    public void buildListing_SedUrlExists_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",SED Report Hyperlink").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",http://examplesed.com");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getSedReportFileLocation());
        assertEquals("http://examplesed.com", listing.getSedReportFileLocation());
    }

    @Test
    public void buildListing_SedUrlHasWhitespace_ReturnsTrimmed() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",SED Report Hyperlink").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ", http://examplesed.com  ");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getSedReportFileLocation());
        assertEquals("http://examplesed.com", listing.getSedReportFileLocation());
    }

    @Test
    public void buildListing_SedUrlMissing_ReturnsEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",SED Report Hyperlink").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getSedReportFileLocation());
        assertEquals("", listing.getSedReportFileLocation());
    }

    @Test
    public void buildListing_SedUrlNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getSedReportFileLocation());
    }

    @Test
    public void buildListing_SedTestingDateNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getSedTestingEndDay());
    }

    @Test
    public void buildListing_SedTestingDateGood_ParsesDateValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Date SED Testing was Concluded").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",20200909");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getSedTestingEndDay());
        assertEquals(LocalDate.parse("2020-09-09"), listing.getSedTestingEndDay());
    }

    @Test
    public void buildListing_SedTestingDateLocalDateValue_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Date SED Testing was Concluded").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",2022-01-01");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing.getSedTestingEndDay());
        assertEquals(LocalDate.parse("2022-01-01"), listing.getSedTestingEndDay());
        assertNotNull(listing.getSedTestingEndDateStr());
        assertEquals("2022-01-01", listing.getSedTestingEndDateStr());
    }

    @Test
    public void buildListing_SedTestingDateEmpty_ParsesNullValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Date SED Testing was Concluded").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getSedTestingEndDay());
    }

    @Test
    public void buildListing_SedTestingDateValueBad_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Date SED Testing was Concluded").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",BADDATE");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNull(listing.getSedTestingEndDay());
        assertNotNull(listing.getSedTestingEndDateStr());
        assertEquals("BADDATE", listing.getSedTestingEndDateStr());
    }

    @Test
    public void buildListing_SedIntendedUsersExists_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Description of the Intended Users").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",Pediatric Nephrology");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getSedIntendedUserDescription());
        assertEquals("Pediatric Nephrology", listing.getSedIntendedUserDescription());
    }

    @Test
    public void buildListing_SedIntendedUsersHasWhitespace_ReturnsTrimmed() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Description of the Intended Users").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ", Pediatric Nephrology ");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getSedIntendedUserDescription());
        assertEquals("Pediatric Nephrology", listing.getSedIntendedUserDescription());
    }

    @Test
    public void buildListing_SedIntendedUsersMissing_ReturnsEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",Description of the Intended Users").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getSedIntendedUserDescription());
        assertEquals("", listing.getSedIntendedUserDescription());
    }

    @Test
    public void buildListing_SedIntendedUsersNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getSedIntendedUserDescription());
    }

    @Test
    public void buildListing_SvapUrlExists_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",SVAP Notice URL").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",http://examplesvap.com");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getSvapNoticeUrl());
        assertEquals("http://examplesvap.com", listing.getSvapNoticeUrl());
    }

    @Test
    public void buildListing_SvapNoticeUrlHasWhitespace_ReturnsTrimmed() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",SVAP Notice URL").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ", http://examplesvap.com  ");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getSvapNoticeUrl());
        assertEquals("http://examplesvap.com", listing.getSvapNoticeUrl());
    }

    @Test
    public void buildListing_SvapNoticeUrlMissing_ReturnsEmptyString() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",SVAP Notice URL").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",");
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getSvapNoticeUrl());
        assertEquals("", listing.getSvapNoticeUrl());
    }

    @Test
    public void buildListing_SvapNoticeUrlNoColumn_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNull(listing.getSvapNoticeUrl());
    }
}
