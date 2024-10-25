package gov.healthit.chpl.upload.listing.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;

public class CertificationCriterionUploadHandlerTest {
    private CertificationCriterionUploadHandler handler;

    private CertificationCriterion a1, d4;

    @Before
    public void setup() {
        a1 = CertificationCriterion.builder()
                .id(1L)
                .number("170.315 (a)(1)")
                .build();
        d4 = CertificationCriterion.builder()
                .id(2L)
                .number("170.315 (d)(4)")
                .build();

        CertificationCriterionService criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.getAllowedCriterionHeadingsForNewListing())
            .thenReturn(Stream.of("CRITERIA_170_315_A_1__C", "CRITERIA_170_315_D_4__C", "CRITERIA_170_315_D_4_Cures__C").toList());

        criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.getCriterionForHeading(ArgumentMatchers.eq("CRITERIA_170_315_A_1__C")))
            .thenReturn(a1);
        Mockito.when(criteriaService.getCriterionForHeading(ArgumentMatchers.eq("CRITERIA_170_315_D_4__C")))
            .thenReturn(d4);
        Mockito.when(criteriaService.getCriterionForHeading(ArgumentMatchers.eq("CRITERIA_170_315_D_4_C")))
            .thenReturn(d4);
        Mockito.when(criteriaService.getCriterionForHeading(ArgumentMatchers.eq("CRITERIA_170_315_D_4_Cures__C")))
            .thenReturn(d4);
        handler = new CertificationCriterionUploadHandler(criteriaService);
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
        CertificationCriterion parsedCriterion = handler.handle(headingRecord);
        assertNull(parsedCriterion);
    }

    @Test
    public void parseCriterion_ValidCriterionFormatWithSingleActiveMatch_ReturnsCriterion() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_D_4__C").get(0);
        assertNotNull(headingRecord);

        CertificationCriterion parsedCriterion = handler.handle(headingRecord);
        assertNotNull(parsedCriterion);
        assertEquals(d4.getId(), parsedCriterion.getId());
        assertEquals(d4.getNumber(), parsedCriterion.getNumber());
    }

    @Test
    public void parseCriterion_SingleUnderscoreCriterionFormatWithActiveMatch_ReturnsCriterion() {
        CSVRecord headingRecord = ListingUploadTestUtil.getRecordsFromString("CRITERIA_170_315_D_4_C").get(0);
        assertNotNull(headingRecord);

        CertificationCriterion parsedCriterion = handler.handle(headingRecord);
        assertNotNull(parsedCriterion);
        assertEquals(d4.getId(), parsedCriterion.getId());
        assertEquals(d4.getNumber(), parsedCriterion.getNumber());
    }
}
