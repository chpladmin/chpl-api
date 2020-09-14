package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class QmsUploadHandlerTest {
    private static final String HEADER_ROW = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C,Qms Standard,QMS Standard Applicable Criteria,QMS Modification Description";
    private static final String LISTING_ROW_BEGIN = "15.02.02.3007.A056.01.00.0.180214,New";
    private static final String LISTING_ROW_SUBELEMENT_BEGIN = "15.02.02.3007.A056.01.00.0.180214,Subelement";

    private QmsStandardDAO dao;
    private QmsUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        dao = Mockito.mock(QmsStandardDAO.class);
        handler = new QmsUploadHandler(handlerUtil, dao, msgUtil);
    }

    @Test
    public void parseQms_MultipleValidQmsAllFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",ISO 9001,All Criteria,None\n"
                + LISTING_ROW_SUBELEMENT_BEGIN + ",ISO 9002,a1 a2 and a3,Custom");
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("ISO 9001")))
            .thenReturn(buildDto(1L, "ISO 9001"));
        Mockito.when(dao.getByName(ArgumentMatchers.eq("ISO 9002")))
        .thenReturn(buildDto(2L, "ISO 9002"));

        List<CertifiedProductQmsStandard> foundQms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundQms);
        assertEquals(2, foundQms.size());
        foundQms.stream().forEach(qms -> {
            assertNull(qms.getId());
            assertNotNull(qms.getQmsStandardName());
            assertNotNull(qms.getQmsStandardId());
            assertNotNull(qms.getApplicableCriteria());
            assertNotNull(qms.getQmsModification());
            if (qms.getQmsStandardName().equals("ISO 9001")) {
                assertEquals("All Criteria", qms.getApplicableCriteria());
                assertEquals("None", qms.getQmsModification());
            } else if (qms.getQmsStandardName().contentEquals("ISO 9002")) {
                assertEquals("a1 a2 and a3", qms.getApplicableCriteria());
                assertEquals("Custom", qms.getQmsModification());
            } else {
                fail("No QMS Standard with name " + qms.getQmsStandardName() + " should have been found.");
            }
        });
    }


    @Test
    public void parseQms_MultipleValidQmsSomeFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",ISO 9001,,None\n"
                + LISTING_ROW_SUBELEMENT_BEGIN + ",ISO 9002,a1 a2 and a3,");
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("ISO 9001")))
            .thenReturn(buildDto(1L, "ISO 9001"));
        Mockito.when(dao.getByName(ArgumentMatchers.eq("ISO 9002")))
        .thenReturn(buildDto(2L, "ISO 9002"));

        List<CertifiedProductQmsStandard> foundQms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundQms);
        assertEquals(2, foundQms.size());
        foundQms.stream().forEach(qms -> {
            assertNull(qms.getId());
            assertNotNull(qms.getQmsStandardName());
            assertNotNull(qms.getQmsStandardId());
            assertNotNull(qms.getApplicableCriteria());
            assertNotNull(qms.getQmsModification());
            if (qms.getQmsStandardName().equals("ISO 9001")) {
                assertEquals("", qms.getApplicableCriteria());
                assertEquals("None", qms.getQmsModification());
            } else if (qms.getQmsStandardName().contentEquals("ISO 9002")) {
                assertEquals("a1 a2 and a3", qms.getApplicableCriteria());
                assertEquals("", qms.getQmsModification());
            } else {
                fail("No QMS Standard with name " + qms.getQmsStandardName() + " should have been found.");
            }
        });
    }

    @Test
    public void parseQms_SingleValidQmsSomeFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",ISO 9001,,None");
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("ISO 9001")))
            .thenReturn(buildDto(1L, "ISO 9001"));

        List<CertifiedProductQmsStandard> foundQms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundQms);
        assertEquals(1, foundQms.size());
        foundQms.stream().forEach(qms -> {
            assertNull(qms.getId());
            assertNotNull(qms.getQmsStandardName());
            assertNotNull(qms.getQmsStandardId());
            assertNotNull(qms.getApplicableCriteria());
            assertNotNull(qms.getQmsModification());
            assertEquals("ISO 9001", qms.getQmsStandardName());
            assertEquals("", qms.getApplicableCriteria());
            assertEquals("None", qms.getQmsModification());
        });
    }

    @Test
    public void parseQms_SingleInvalidQmsSomeFieldsPopulated_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(HEADER_ROW).get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",ISO 9001,,None");
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("ISO 9001")))
            .thenReturn(null);

        List<CertifiedProductQmsStandard> foundQms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundQms);
        assertEquals(1, foundQms.size());
        foundQms.stream().forEach(qms -> {
            assertNull(qms.getId());
            assertNotNull(qms.getQmsStandardName());
            assertNull(qms.getQmsStandardId());
            assertNotNull(qms.getApplicableCriteria());
            assertNotNull(qms.getQmsModification());
            assertEquals("ISO 9001", qms.getQmsStandardName());
            assertEquals("", qms.getApplicableCriteria());
            assertEquals("None", qms.getQmsModification());
        });
    }

    @Test
    public void parseQms_SingleInvalidQmsUnexpectedHeaderOrder_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "UNIQUE_CHPL_ID__C,RECORD_STATUS__C,QMS Modification Description,QMS Standard Applicable Criteria,Qms Standard").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",None,All Criteria,ISO 9001");
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("ISO 9001")))
            .thenReturn(buildDto(1L, "ISO 9001"));

        List<CertifiedProductQmsStandard> foundQms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundQms);
        assertEquals(1, foundQms.size());
        foundQms.stream().forEach(qms -> {
            assertNull(qms.getId());
            assertNotNull(qms.getQmsStandardName());
            assertNotNull(qms.getQmsStandardId());
            assertNotNull(qms.getApplicableCriteria());
            assertNotNull(qms.getQmsModification());
            assertEquals(1, qms.getQmsStandardId().longValue());
            assertEquals("ISO 9001", qms.getQmsStandardName());
            assertEquals("All Criteria", qms.getApplicableCriteria());
            assertEquals("None", qms.getQmsModification());
        });
    }

    @Test
    public void parseQms_SingleValidQmsCriteriaAndName_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "UNIQUE_CHPL_ID__C,RECORD_STATUS__C,QMS Standard Applicable Criteria,Qms Standard").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",All Criteria,ISO 9001");
        assertNotNull(listingRecords);

        Mockito.when(dao.getByName(ArgumentMatchers.eq("ISO 9001")))
            .thenReturn(buildDto(1L, "ISO 9001"));

        List<CertifiedProductQmsStandard> foundQms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundQms);
        assertEquals(1, foundQms.size());
        foundQms.stream().forEach(qms -> {
            assertNull(qms.getId());
            assertNotNull(qms.getQmsStandardName());
            assertNotNull(qms.getQmsStandardId());
            assertNotNull(qms.getApplicableCriteria());
            assertNull(qms.getQmsModification());
            assertEquals(1, qms.getQmsStandardId().longValue());
            assertEquals("ISO 9001", qms.getQmsStandardName());
            assertEquals("All Criteria", qms.getApplicableCriteria());
        });
    }

    @Test
    public void parseQms_QmsNameMissing_ReturnsCorrectly() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString(
                "UNIQUE_CHPL_ID__C,RECORD_STATUS__C,Qms Modification Description,QMS Standard Applicable Criteria").get(0);
        assertNotNull(headingRecord);
        List<CSVRecord> listingRecords = ListingUploadTestUtil.getRecordsFromString(
                LISTING_ROW_BEGIN + ",Some mods,All Criteria");
        assertNotNull(listingRecords);

        List<CertifiedProductQmsStandard> foundQms = handler.handle(headingRecord, listingRecords);
        assertNotNull(foundQms);
        assertEquals(1, foundQms.size());
        foundQms.stream().forEach(qms -> {
            assertNull(qms.getId());
            assertNull(qms.getQmsStandardName());
            assertNull(qms.getQmsStandardId());
            assertNotNull(qms.getApplicableCriteria());
            assertNotNull(qms.getQmsModification());
            assertEquals("Some mods", qms.getQmsModification());
            assertEquals("All Criteria", qms.getApplicableCriteria());
        });
    }

    private QmsStandardDTO buildDto(Long id, String name) {
        QmsStandardDTO dto = new QmsStandardDTO();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}
