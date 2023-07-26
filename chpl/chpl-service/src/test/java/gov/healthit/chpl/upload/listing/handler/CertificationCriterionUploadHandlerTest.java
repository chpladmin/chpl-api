package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
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
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,Test Data,Test tool name").get(0);
        assertNotNull(headingRecord);
        CertificationCriterion parsedCriterion = handler.handle(headingRecord);
        assertNull(parsedCriterion);
    }

    @Test
    public void parseCriterion_InvalidCriterionFormat_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERION_170_315_R_4").get(0);
        assertNotNull(headingRecord);
        CertificationCriterion parsedCriterion = handler.handle(headingRecord);
        assertNull(parsedCriterion);
    }

    @Test
    public void parseCriterion_ValidCriterionFormatWithNoMatch_ReturnsNull() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_R_4__C").get(0);
        assertNotNull(headingRecord);
        Mockito.when(criterionDao.getAllByNumber(ArgumentMatchers.eq("170.315 (r)(4)")))
            .thenReturn(new ArrayList<CertificationCriterion>());

        CertificationCriterion parsedCriterion = handler.handle(headingRecord);
        assertNull(parsedCriterion);
    }

    @Test
    public void parseCriterion_ValidCriterionFormatWithSingleMatch_ReturnsCriterion() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_D_4__C").get(0);
        assertNotNull(headingRecord);
        List<CertificationCriterion> criterionDtos = new ArrayList<CertificationCriterion>();
        criterionDtos.add(createCriterion(1L, "170.315 (d)(4)", "test title"));
        Mockito.when(criterionDao.getAllByNumber(ArgumentMatchers.eq("170.315 (d)(4)")))
            .thenReturn(criterionDtos);

        CertificationCriterion parsedCriterion = handler.handle(headingRecord);
        assertNotNull(parsedCriterion);
        assertEquals(1L, parsedCriterion.getId());
        assertEquals("170.315 (d)(4)", parsedCriterion.getNumber());
    }

    @Test
    public void parseCriterion_ValidCriterionFormatWithTwoMatches_ReturnsCriterion() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_D_4__C").get(0);
        assertNotNull(headingRecord);
        List<CertificationCriterion> criterionDtos = new ArrayList<CertificationCriterion>();
        criterionDtos.add(createCriterion(1L, "170.315 (d)(4)", "test title"));
        criterionDtos.add(createCriterion(2L, "170.315 (d)(4)", "test title (Cures Update)"));

        Mockito.when(criterionDao.getAllByNumber(ArgumentMatchers.eq("170.315 (d)(4)")))
            .thenReturn(criterionDtos);

        CertificationCriterion parsedCriterion = handler.handle(headingRecord);
        assertNotNull(parsedCriterion);
        assertEquals(1L, parsedCriterion.getId());
        assertEquals("170.315 (d)(4)", parsedCriterion.getNumber());
    }

    @Test
    public void parseCriterion_ValidCuresCriterionColumnWithTwoMatches_ReturnsCriterion() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_D_3_Cures__C").get(0);
        assertNotNull(headingRecord);
        List<CertificationCriterion> criterionDtos = new ArrayList<CertificationCriterion>();
        criterionDtos.add(createCriterion(1L, "170.315 (d)(3)", "test title"));
        criterionDtos.add(createCriterion(2L, "170.315 (d)(3)", "test title (Cures Update)"));
        Mockito.when(criterionDao.getAllByNumber(ArgumentMatchers.eq("170.315 (d)(3)")))
            .thenReturn(criterionDtos);

        CertificationCriterion parsedCriterion = handler.handle(headingRecord);
        assertNotNull(parsedCriterion);
        assertEquals(2L, parsedCriterion.getId());
        assertEquals("170.315 (d)(3)", parsedCriterion.getNumber());
    }

    private CertificationCriterion createCriterion(Long id, String number, String title) {
        return CertificationCriterion.builder()
                .id(id)
                .number(number)
                .title(title)
          .build();
    }
}
