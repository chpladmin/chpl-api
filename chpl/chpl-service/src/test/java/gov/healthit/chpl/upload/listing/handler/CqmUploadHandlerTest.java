package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CqmUploadHandlerTest {
    private static final String HEADER_ROW_BEGIN = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C";
    private static final String HEADER_ROW = HEADER_ROW_BEGIN + ",CQM Number,CQM Version,CQM Criteria";
    private static final String LISTING_ROW_BEGIN = "15.02.02.3007.A056.01.00.0.180214,New";
    private static final String LISTING_ROW_SUBELEMENT_BEGIN = "15.02.02.3007.A056.01.00.0.180214,Subelement";

    private CqmUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new CqmUploadHandler(handlerUtil);
    }

    @Test
    public void parseCqm_NoCqmColumns_ReturnsEmptyList() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,RECORD_STATUS__C").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN);
        assertNotNull(listingRecords);

        List<CQMResultDetails> parsedCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedCqms);
        assertEquals(0, parsedCqms.size());
    }

    @Test
    public void parseCqm_CqmColumnsNoData_ReturnsListWithEmptyItems() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(LISTING_ROW_BEGIN + ",,,");
        assertNotNull(listingRecords);

        List<CQMResultDetails> parsedCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(parsedCqms);
        assertEquals(1, parsedCqms.size());
        CQMResultDetails cqm = parsedCqms.get(0);
        assertEquals("", cqm.getNumber());
        assertNull(cqm.getCmsId());
        assertNull(cqm.getDescription());
        assertNull(cqm.getDomain());
        assertNull(cqm.getNqfNumber());
        assertNull(cqm.getTitle());
        assertNull(cqm.getTypeId());
        assertNotNull(cqm.getAllVersions());
        assertEquals(0, cqm.getAllVersions().size());
        assertNotNull(cqm.getSuccessVersions());
        assertEquals(0, cqm.getSuccessVersions().size());
        assertNotNull(cqm.getCriteria());
        assertEquals(0, cqm.getCriteria().size());
    }

    @Test
    public void parseCqm_SingleCqm_Whitespace_AllFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ", 61 , v0 , c1 ");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("61", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        assertEquals(1, cqm.getSuccessVersions().size());
        assertEquals("v0", cqm.getSuccessVersions().iterator().next());
        assertNotNull(cqm.getCriteria());
        assertEquals(1, cqm.getCriteria().size());
        assertEquals("c1", cqm.getCriteria().get(0).getCertificationNumber());
    }

    @Test
    public void parseCqm_SingleCqm_SemicolonSeparatedVersionList_AllFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",61,v0;v1;v2,c1");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("61", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        assertEquals(3, cqm.getSuccessVersions().size());
        int i = 0;
        Iterator<String> versionIter = cqm.getSuccessVersions().iterator();
        while (versionIter.hasNext()) {
            String version = versionIter.next();
            switch (i) {
                case 0:
                    assertEquals("v0", version);
                    break;
                case 1:
                    assertEquals("v1", version);
                    break;
                case 2:
                    assertEquals("v2", version);
                    break;
                default:
                    fail("Unexpected index " + i);
            }
            i++;
        }
        assertNotNull(cqm.getCriteria());
        assertEquals(1, cqm.getCriteria().size());
        assertEquals("c1", cqm.getCriteria().get(0).getCertificationNumber());
    }

    @Test
    public void parseCqm_SingleCqm_CommaSeparatedVersionList_AllFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",61,\"v0,v1,v2\",c1");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("61", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        assertEquals(3, cqm.getSuccessVersions().size());
        int i = 0;
        Iterator<String> versionIter = cqm.getSuccessVersions().iterator();
        while (versionIter.hasNext()) {
            String version = versionIter.next();
            switch (i) {
                case 0:
                    assertEquals("v0", version);
                    break;
                case 1:
                    assertEquals("v1", version);
                    break;
                case 2:
                    assertEquals("v2", version);
                    break;
                default:
                    fail("Unexpected index " + i);
            }
            i++;
        }
        assertNotNull(cqm.getCriteria());
        assertEquals(1, cqm.getCriteria().size());
        assertEquals("c1", cqm.getCriteria().get(0).getCertificationNumber());
    }

    @Test
    public void parseCqm_SingleCqm_SemicolonSeparatedCriteriaList_AllFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",61,v3,c1;c2;c3;c4");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("61", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        String cqmVersion = cqm.getSuccessVersions().iterator().next();
        assertNotNull(cqmVersion);
        assertEquals("v3", cqmVersion);
        assertNotNull(cqm.getCriteria());
        assertEquals(4, cqm.getCriteria().size());
        for (int i = 0; i < cqm.getCriteria().size(); i++) {
            CQMResultCertification cert = cqm.getCriteria().get(i);
            switch (i) {
            case 0:
                assertEquals("c1", cert.getCertificationNumber());
                break;
            case 1:
                assertEquals("c2", cert.getCertificationNumber());
                break;
            case 2:
                assertEquals("c3", cert.getCertificationNumber());
                break;
            case 3:
                assertEquals("c4", cert.getCertificationNumber());
                break;
            default:
                fail("Unknown index " + i);
            }
            assertNull(cert.getCriterion());
        }
    }

    @Test
    public void parseCqm_SingleCqm_CommaSeparatedCriteriaList_AllFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",61,v3,\"c1,c2,c3,c4\"");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("61", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        String cqmVersion = cqm.getSuccessVersions().iterator().next();
        assertNotNull(cqmVersion);
        assertEquals("v3", cqmVersion);
        assertNotNull(cqm.getCriteria());
        assertEquals(4, cqm.getCriteria().size());
        for (int i = 0; i < cqm.getCriteria().size(); i++) {
            CQMResultCertification cert = cqm.getCriteria().get(i);
            switch (i) {
            case 0:
                assertEquals("c1", cert.getCertificationNumber());
                break;
            case 1:
                assertEquals("c2", cert.getCertificationNumber());
                break;
            case 2:
                assertEquals("c3", cert.getCertificationNumber());
                break;
            case 3:
                assertEquals("c4", cert.getCertificationNumber());
                break;
            default:
                fail("Unknown index " + i);
            }
            assertNull(cert.getCriterion());
        }
    }

    @Test
    public void parseCqm_SingleCqm_NumberOnlyPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",61,,");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("61", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        assertEquals(0, cqm.getSuccessVersions().size());
        assertNotNull(cqm.getCriteria());
        assertEquals(0, cqm.getCriteria().size());
    }

    @Test
    public void parseCqm_SingleCqm_NumberAndVersionPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",61,v3,");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("61", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        String cqmVersion = cqm.getSuccessVersions().iterator().next();
        assertNotNull(cqmVersion);
        assertEquals("v3", cqmVersion);
        assertNotNull(cqm.getCriteria());
        assertEquals(0, cqm.getCriteria().size());
    }

    @Test
    public void parseCqm_SingleCqm_VersionOnlyPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",,v3,");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        String cqmVersion = cqm.getSuccessVersions().iterator().next();
        assertNotNull(cqmVersion);
        assertEquals("v3", cqmVersion);
        assertNotNull(cqm.getCriteria());
        assertEquals(0, cqm.getCriteria().size());
    }

    @Test
    public void parseCqm_SingleCqm_VersionAndCrtieriaPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",,v3,c1;c3");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        String cqmVersion = cqm.getSuccessVersions().iterator().next();
        assertNotNull(cqmVersion);
        assertEquals("v3", cqmVersion);
        assertNotNull(cqm.getCriteria());
        assertEquals(2, cqm.getCriteria().size());
        for (int i = 0; i < cqm.getCriteria().size(); i++) {
            CQMResultCertification cert = cqm.getCriteria().get(i);
            switch (i) {
            case 0:
                assertEquals("c1", cert.getCertificationNumber());
                break;
            case 1:
                assertEquals("c3", cert.getCertificationNumber());
                break;
            default:
                fail("Unknown index " + i);
            }
            assertNull(cert.getCriterion());
        }
    }

    @Test
    public void parseCqm_SingleCqm_CrtieriaOnlyPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",,,c1;c3");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        assertEquals(0, cqm.getSuccessVersions().size());
        assertNotNull(cqm.getCriteria());
        assertEquals(2, cqm.getCriteria().size());
        for (int i = 0; i < cqm.getCriteria().size(); i++) {
            CQMResultCertification cert = cqm.getCriteria().get(i);
            switch (i) {
            case 0:
                assertEquals("c1", cert.getCertificationNumber());
                break;
            case 1:
                assertEquals("c3", cert.getCertificationNumber());
                break;
            default:
                fail("Unknown index " + i);
            }
            assertNull(cert.getCriterion());
        }
    }

    @Test
    public void parseCqm_SingleCqm_NumberAndCrtieriaPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",61,,c1;c3");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("61", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        assertEquals(0, cqm.getSuccessVersions().size());
        assertNotNull(cqm.getCriteria());
        assertEquals(2, cqm.getCriteria().size());
        for (int i = 0; i < cqm.getCriteria().size(); i++) {
            CQMResultCertification cert = cqm.getCriteria().get(i);
            switch (i) {
            case 0:
                assertEquals("c1", cert.getCertificationNumber());
                break;
            case 1:
                assertEquals("c3", cert.getCertificationNumber());
                break;
            default:
                fail("Unknown index " + i);
            }
            assertNull(cert.getCriterion());
        }
    }

    @Test
    public void parseCqm_SingleCqm_NumberColumnOnlyExists_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",CQM Number").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",61");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("61", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        assertEquals(0, cqm.getSuccessVersions().size());
        assertNotNull(cqm.getCriteria());
        assertEquals(0, cqm.getCriteria().size());
    }

    @Test
    public void parseCqm_SingleCqm_VersionColumnOnlyExists_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",CQM Version").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",v3");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNull(cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        assertEquals(1, cqm.getSuccessVersions().size());
        assertEquals("v3", cqm.getSuccessVersions().iterator().next());
        assertNotNull(cqm.getCriteria());
        assertEquals(0, cqm.getCriteria().size());
    }

    @Test
    public void parseCqm_SingleCqm_CriteriaColumnOnlyExists_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",CQM Criteria").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",c2;c3;c4");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNull(cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        assertEquals(0, cqm.getSuccessVersions().size());
        assertNotNull(cqm.getCriteria());
        assertEquals(3, cqm.getCriteria().size());
        for (int i = 0; i < cqm.getCriteria().size(); i++) {
            CQMResultCertification cert = cqm.getCriteria().get(i);
            switch (i) {
            case 0:
                assertEquals("c2", cert.getCertificationNumber());
                break;
            case 1:
                assertEquals("c3", cert.getCertificationNumber());
                break;
            case 2:
                assertEquals("c4", cert.getCertificationNumber());
                break;
            default:
                fail("Unknown index " + i);
            }
            assertNull(cert.getCriterion());
        }
    }

    @Test
    public void parseCqm_SingleCqm_NumberAndVersionColumnsOnlyExist_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",CQM Number,CQM Version").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",61,v3");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("61", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        assertEquals(1, cqm.getSuccessVersions().size());
        assertEquals("v3", cqm.getSuccessVersions().iterator().next());
        assertNotNull(cqm.getCriteria());
        assertEquals(0, cqm.getCriteria().size());
    }

    @Test
    public void parseCqm_SingleCqm_NumberAndCriteriaColumnsOnlyExist_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",CQM Number,CQM Criteria").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",61,c1;c2");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNotNull(cqm.getNumber());
        assertEquals("61", cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        assertEquals(0, cqm.getSuccessVersions().size());
        assertNotNull(cqm.getCriteria());
        assertEquals(2, cqm.getCriteria().size());
        for (int i = 0; i < cqm.getCriteria().size(); i++) {
            CQMResultCertification cert = cqm.getCriteria().get(i);
            switch (i) {
            case 0:
                assertEquals("c1", cert.getCertificationNumber());
                break;
            case 1:
                assertEquals("c2", cert.getCertificationNumber());
                break;
            default:
                fail("Unknown index " + i);
            }
            assertNull(cert.getCriterion());
        }
    }

    @Test
    public void parseCqm_SingleCqm_VersionAndCriteriaColumnsOnlyExist_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                HEADER_ROW_BEGIN + ",CQM Version,CQM Criteria").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",v3,c1;c2");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(1, foundCqms.size());
        CQMResultDetails cqm = foundCqms.get(0);
        assertNull(cqm.getNumber());
        assertNotNull(cqm.getSuccessVersions());
        assertEquals(1, cqm.getSuccessVersions().size());
        assertEquals("v3", cqm.getSuccessVersions().iterator().next());
        assertNotNull(cqm.getCriteria());
        assertEquals(2, cqm.getCriteria().size());
        for (int i = 0; i < cqm.getCriteria().size(); i++) {
            CQMResultCertification cert = cqm.getCriteria().get(i);
            switch (i) {
            case 0:
                assertEquals("c1", cert.getCertificationNumber());
                break;
            case 1:
                assertEquals("c2", cert.getCertificationNumber());
                break;
            default:
                fail("Unknown index " + i);
            }
            assertNull(cert.getCriterion());
        }
    }

    @Test
    public void parseCqms_MultipleCqms_AllFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",59,v1,c1;c2\n"
                + LISTING_ROW_SUBELEMENT_BEGIN + ",61,v3,c3");
        assertNotNull(listingRecords);

        List<CQMResultDetails> foundCqms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundCqms);
        assertEquals(2, foundCqms.size());
        foundCqms.stream().forEach(cqm -> {
            assertNull(cqm.getId());
            assertNotNull(cqm.getNumber());
            if (cqm.getNumber().equals("59")) {
                assertEquals("v1", cqm.getSuccessVersions().iterator().next());
                assertEquals(2, cqm.getCriteria().size());
                assertEquals("c1", cqm.getCriteria().get(0).getCertificationNumber());
                assertEquals("c2", cqm.getCriteria().get(1).getCertificationNumber());
            } else if (cqm.getNumber().equals("61")) {
                assertEquals("v3", cqm.getSuccessVersions().iterator().next());
                assertEquals(1, cqm.getCriteria().size());
                assertEquals("c3", cqm.getCriteria().get(0).getCertificationNumber());
            } else {
                fail("No CQM with number " + cqm.getNumber() + " should have been found.");
            }
        });
    }
}
