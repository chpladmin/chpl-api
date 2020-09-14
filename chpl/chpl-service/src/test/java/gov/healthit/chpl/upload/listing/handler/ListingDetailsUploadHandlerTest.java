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

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
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
    private CertificationEditionDAO editionDao;
    private CertificationBodyDAO acbDao;
    private DeveloperDAO devDao;
    private ProductDAO productDao;
    private ProductVersionDAO versionDao;
    private TestingLabDAO atlDao;

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

        editionDao = Mockito.mock(CertificationEditionDAO.class);
        acbDao = Mockito.mock(CertificationBodyDAO.class);
        productDao = Mockito.mock(ProductDAO.class);
        devDao = Mockito.mock(DeveloperDAO.class);
        versionDao = Mockito.mock(ProductVersionDAO.class);
        atlDao = Mockito.mock(TestingLabDAO.class);

        handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        DeveloperDetailsUploadHandler devHandler = new DeveloperDetailsUploadHandler(handlerUtil, devDao, msgUtil);
        handler = new ListingDetailsUploadHandler(devHandler,
                Mockito.mock(TargetedUsersUploadHandler.class),
                Mockito.mock(AccessibilityStandardsUploadHandler.class),
                Mockito.mock(QmsUploadHandler.class),
                editionDao, acbDao, atlDao, productDao, versionDao,
                handlerUtil, msgUtil);
    }

    @Test
    public void buildListing_GoodData_ReturnsCorrectChplProductNumber() {
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

    @Test(expected = ValidationException.class)
    public void buildListing_ChplProductNumberColumnMissing_ThrowsException() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("RECORD_STATUS__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString("New");
        assertNotNull(listingRecords);

        handler.parseAsListing(headingRecord, listingRecords);
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
    public void buildListing_BooleanValueNo_ReturnsTrueAccessibilityCertified() {
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
    public void buildListing_EditionGood_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERT_YEAR__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",2015");
        assertNotNull(listingRecords);

        CertificationEditionDTO editionDto = new CertificationEditionDTO();
        editionDto.setId(1L);
        editionDto.setYear("2015");
        Mockito.when(editionDao.getByYear("2015")).thenReturn(editionDto);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getCertificationEdition());
        assertEquals(editionDto.getYear(),
                listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString());
        assertEquals(editionDto.getId().toString(),
                listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY).toString());
    }

    @Test
    public void buildListing_EditionBad_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERT_YEAR__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",2017");
        assertNotNull(listingRecords);

        Mockito.when(editionDao.getByYear("2017")).thenReturn(null);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getCertificationEdition());
        assertEquals("2017",
                listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString());
        assertNull(listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY));
    }

    @Test
    public void buildListing_AcbGood_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFYING_ACB__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",Drummond Group");
        assertNotNull(listingRecords);

        CertificationBodyDTO acbDto = new CertificationBodyDTO();
        acbDto.setId(1L);
        acbDto.setName("Drummond Group");
        Mockito.when(acbDao.getByName("Drummond Group")).thenReturn(acbDto);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getCertifyingBody());
        assertEquals(acbDto.getName(),
                listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
        assertEquals(acbDto.getId().toString(),
                listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString());
    }

    @Test
    public void buildListing_AcbBad_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",CERTIFYING_ACB__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",Bogus ACB");
        assertNotNull(listingRecords);

        Mockito.when(acbDao.getByName("Bogus ACB")).thenReturn(null);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getCertifyingBody());
        assertEquals("Bogus ACB",
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
    public void parseProduct_DeveloperFoundNewProduct_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",VENDOR__C,PRODUCT__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",My Developer,Test Product");
        assertNotNull(listingRecords);

        DeveloperDTO devDto = new DeveloperDTO();
        devDto.setId(1L);
        devDto.setName("My Developer");
        Mockito.when(devDao.getByName("My Developer")).thenReturn(devDto);
        Mockito.when(productDao.getByDeveloperAndName(devDto.getId(), "Test Product")).thenReturn(null);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getDeveloper());
        assertNotNull(listing.getProduct());
        assertEquals("Test Product", listing.getProduct().getName());
        assertNull(listing.getProduct().getProductId());
    }

    @Test
    public void parseProduct_DeveloperFoundProductFound_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",VENDOR__C,PRODUCT__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",My Developer,Test Product");
        assertNotNull(listingRecords);

        DeveloperDTO devDto = new DeveloperDTO();
        devDto.setId(1L);
        devDto.setName("My Developer");
        Mockito.when(devDao.getByName("My Developer")).thenReturn(devDto);
        ProductDTO prodDto = new ProductDTO();
        prodDto.setId(2L);
        prodDto.setName("Test Product");
        Mockito.when(productDao.getByDeveloperAndName(devDto.getId(), "Test Product")).thenReturn(prodDto);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getDeveloper());
        assertNotNull(listing.getProduct());
        assertEquals(prodDto.getName(), listing.getProduct().getName());
        assertNotNull(listing.getProduct().getProductId());
        assertEquals(prodDto.getId().longValue(), listing.getProduct().getProductId().longValue());
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
    public void parseVersion_FoundProductNewVersion_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",VENDOR__C,PRODUCT_C,VERSION__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN
                + ",My Developer,Test Product,v1");
        assertNotNull(listingRecords);

        DeveloperDTO devDto = new DeveloperDTO();
        devDto.setId(1L);
        devDto.setName("My Developer");
        Mockito.when(devDao.getByName("My Developer")).thenReturn(devDto);
        ProductDTO prodDto = new ProductDTO();
        prodDto.setId(2L);
        prodDto.setName("Test Product");
        Mockito.when(productDao.getByDeveloperAndName(devDto.getId(), "Test Product")).thenReturn(prodDto);
        Mockito.when(versionDao.getByProductAndVersion(prodDto.getId(), "v1")).thenReturn(null);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getProduct());
        assertNotNull(listing.getVersion());
        assertEquals("v1", listing.getVersion().getVersion());
        assertNull(listing.getVersion().getVersionId());
    }

    @Test
    public void parseVersion_FoundProductFoundVersion_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN
                + ",VENDOR__C,PRODUCT_C,VERSION__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN
                + ",My Developer,Test Product,v1");
        assertNotNull(listingRecords);

        DeveloperDTO devDto = new DeveloperDTO();
        devDto.setId(1L);
        devDto.setName("My Developer");
        Mockito.when(devDao.getByName("My Developer")).thenReturn(devDto);
        ProductDTO prodDto = new ProductDTO();
        prodDto.setId(2L);
        prodDto.setName("Test Product");
        Mockito.when(productDao.getByDeveloperAndName(devDto.getId(), "Test Product")).thenReturn(prodDto);
        ProductVersionDTO versionDto = new ProductVersionDTO();
        versionDto.setId(3L);
        versionDto.setVersion("v1");
        Mockito.when(versionDao.getByProductAndVersion(prodDto.getId(), "v1")).thenReturn(versionDto);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getProduct());
        assertNotNull(listing.getVersion());
        assertEquals(versionDto.getVersion(), listing.getVersion().getVersion());
        assertNotNull(listing.getVersion().getVersionId());
        assertEquals(versionDto.getId().longValue(), listing.getVersion().getVersionId().longValue());
    }

    @Test
    public void buildListing_AtlSingleGood_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",TESTING_ATL__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",Drummond Group");
        assertNotNull(listingRecords);

        TestingLabDTO atlDto = new TestingLabDTO();
        atlDto.setId(1L);
        atlDto.setName("Drummond Group");
        Mockito.when(atlDao.getByName("Drummond Group")).thenReturn(atlDto);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getTestingLabs());
        assertEquals(1, listing.getTestingLabs().size());
        CertifiedProductTestingLab parsedAtl = listing.getTestingLabs().get(0);
        assertEquals(atlDto.getId().longValue(), parsedAtl.getTestingLabId());
        assertEquals(atlDto.getName(), parsedAtl.getTestingLabName());
    }

    @Test
    public void buildListing_AtlMultipleGood_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",TESTING_ATL__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",Drummond Group\n"
                + LISTING_SUBELEMENT_BEGIN + ",ICSA Labs");
        assertNotNull(listingRecords);

        TestingLabDTO drummondDto = new TestingLabDTO();
        drummondDto.setId(1L);
        drummondDto.setName("Drummond Group");
        Mockito.when(atlDao.getByName("Drummond Group")).thenReturn(drummondDto);

        TestingLabDTO icsaDto = new TestingLabDTO();
        icsaDto.setId(2L);
        icsaDto.setName("ICSA labs");
        Mockito.when(atlDao.getByName("ICSA Labs")).thenReturn(icsaDto);

        CertifiedProductSearchDetails listing = handler.parseAsListing(headingRecord, listingRecords);
        assertNotNull(listing);
        assertNotNull(listing.getTestingLabs());
        assertEquals(2, listing.getTestingLabs().size());
        listing.getTestingLabs().stream().forEach(parsedAtl -> {
            assertNotNull(parsedAtl.getTestingLabId());
            assertNotNull(parsedAtl.getTestingLabName());
        });
    }

    @Test
    public void buildListing_AtlMultipleBad_ReturnsCorrectValue() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW_BEGIN + ",TESTING_ATL__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",Drummond Group\n"
                + LISTING_SUBELEMENT_BEGIN + ",ICSA Labs");
        assertNotNull(listingRecords);

        Mockito.when(atlDao.getByName(ArgumentMatchers.anyString())).thenReturn(null);

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
