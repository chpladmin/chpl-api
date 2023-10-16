package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CertificationCriterionUploadHandlerTest {
    private CertificationCriterionDAO criterionDao;
    private CertificationCriterionUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        criterionDao = Mockito.mock(CertificationCriterionDAO.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new CertificationCriterionUploadHandler(criterionDao, handlerUtil);
    }

    @Test
    public void parseCriterion_NoCriterionColumn_ReturnsNull() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();

        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,Test Data,Test tool name").get(0);
        assertNotNull(headingRecord);
        CertificationCriterion parsedCriterion = handler.handle(headingRecord, listing);
        assertNull(parsedCriterion);
    }

    @Test
    public void parseCriterion_InvalidCriterionFormat_ReturnsNull() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();

        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERION_170_315_R_4").get(0);
        assertNotNull(headingRecord);
        CertificationCriterion parsedCriterion = handler.handle(headingRecord, listing);
        assertNull(parsedCriterion);
    }

    @Test
    public void parseCriterion_ValidCriterionFormatWithNoMatch_ReturnsNull() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2023-01-01")))
                .build();

        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_R_4__C").get(0);
        assertNotNull(headingRecord);
        Mockito.when(criterionDao.getAllByNumber(ArgumentMatchers.eq("170.315 (r)(4)")))
            .thenReturn(new ArrayList<CertificationCriterion>());

        CertificationCriterion parsedCriterion = handler.handle(headingRecord, listing);
        assertNull(parsedCriterion);
    }

    @Test
    public void parseCriterion_ValidCriterionFormatWithSingleActiveMatch_ReturnsCriterion() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2023-01-01")))
                .build();

        List<CertificationCriterion> matchingCriteria = new ArrayList<CertificationCriterion>();
        matchingCriteria.add(createCriterion(1L, "170.315 (d)(4)", "test title", "2022-06-01", null));
        Mockito.when(criterionDao.getAllByNumber(ArgumentMatchers.eq("170.315 (d)(4)")))
            .thenReturn(matchingCriteria);

        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_D_4__C").get(0);
        assertNotNull(headingRecord);

        CertificationCriterion parsedCriterion = handler.handle(headingRecord, listing);
        assertNotNull(parsedCriterion);
        assertEquals(1L, parsedCriterion.getId());
        assertEquals("170.315 (d)(4)", parsedCriterion.getNumber());
    }

    @Test
    public void parseCriterion_ValidCriterionFormatWithSingleMatchInactive_ReturnsCriterion() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2023-01-01")))
                .build();

        List<CertificationCriterion> matchingCriteria = new ArrayList<CertificationCriterion>();
        matchingCriteria.add(createCriterion(1L, "170.315 (d)(4)", "test title", "2022-06-01", "2022-12-31"));
        Mockito.when(criterionDao.getAllByNumber(ArgumentMatchers.eq("170.315 (d)(4)")))
            .thenReturn(matchingCriteria);

        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_D_4__C").get(0);
        assertNotNull(headingRecord);

        CertificationCriterion parsedCriterion = handler.handle(headingRecord, listing);
        assertNotNull(parsedCriterion);
        assertEquals(1L, parsedCriterion.getId());
        assertEquals("170.315 (d)(4)", parsedCriterion.getNumber());
    }

    @Test
    public void parseCriterion_ValidCriterionFormatWithTwoMatches_ReturnsOriginalCriterionActiveOnListingCertDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2023-01-01")))
                .build();

        CertificationCriterion origCriterion = createCriterion(1L, "170.315 (d)(4)", "test title", "2022-01-01", "2023-05-31");
        CertificationCriterion curesCriterion = createCriterion(2L, "170.315 (d)(4)", "test title (Cures Update)", "2023-06-01", null);
        List<CertificationCriterion> criteria = Stream.of(origCriterion, curesCriterion).toList();
        Mockito.when(criterionDao.getAllByNumber(ArgumentMatchers.eq("170.315 (d)(4)")))
            .thenReturn(criteria);

        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_D_4__C").get(0);
        assertNotNull(headingRecord);

        CertificationCriterion parsedCriterion = handler.handle(headingRecord, listing);
        assertNotNull(parsedCriterion);
        assertEquals(1L, parsedCriterion.getId());
        assertEquals("170.315 (d)(4)", parsedCriterion.getNumber());
    }

    @Test
    public void parseCriterion_ValidCriterionFormatWithTwoMatches_ReturnsCuresCriterionActiveOnListingCertDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2023-07-01")))
                .build();

        CertificationCriterion origCriterion = createCriterion(1L, "170.315 (d)(4)", "test title", "2022-01-01", "2023-05-31");
        CertificationCriterion curesCriterion = createCriterion(2L, "170.315 (d)(4)", "test title (Cures Update)", "2023-06-01", null);
        List<CertificationCriterion> criteria = Stream.of(origCriterion, curesCriterion).toList();
        Mockito.when(criterionDao.getAllByNumber(ArgumentMatchers.eq("170.315 (d)(4)")))
            .thenReturn(criteria);

        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_D_4__C").get(0);
        assertNotNull(headingRecord);

        CertificationCriterion parsedCriterion = handler.handle(headingRecord, listing);
        assertNotNull(parsedCriterion);
        assertEquals(2L, parsedCriterion.getId());
        assertEquals("170.315 (d)(4)", parsedCriterion.getNumber());
    }

    @Test
    public void parseCriterion_ValidCuresCriterionColumnWithTwoMatches_ReturnsCuresCriterion() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2022-07-01")))
                .build();

        CertificationCriterion origCriterion = createCriterion(1L, "170.315 (d)(4)", "test title", "2022-01-01", "2023-05-31");
        CertificationCriterion curesCriterion = createCriterion(2L, "170.315 (d)(4)", "test title (Cures Update)", "2023-06-01", null);
        List<CertificationCriterion> criteria = Stream.of(origCriterion, curesCriterion).toList();
        Mockito.when(criterionDao.getAllByNumber(ArgumentMatchers.eq("170.315 (d)(4)")))
            .thenReturn(criteria);

        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_D_4_Cures__C").get(0);
        assertNotNull(headingRecord);

        CertificationCriterion parsedCriterion = handler.handle(headingRecord, listing);
        assertNotNull(parsedCriterion);
        assertEquals(2L, parsedCriterion.getId());
        assertEquals("170.315 (d)(4)", parsedCriterion.getNumber());
    }

    private CertificationCriterion createCriterion(Long id, String number, String title, String startDay, String endDay) {
        return CertificationCriterion.builder()
                .id(id)
                .number(number)
                .title(title)
                .startDay(!StringUtils.isEmpty(startDay) ? LocalDate.parse(startDay) : null)
                .endDay(!StringUtils.isEmpty(endDay) ? LocalDate.parse(endDay) : null)
          .build();
    }
}
