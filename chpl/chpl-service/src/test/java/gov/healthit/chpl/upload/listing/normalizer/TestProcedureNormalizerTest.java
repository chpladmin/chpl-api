package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestProcedure;
import gov.healthit.chpl.dto.TestProcedureDTO;

public class TestProcedureNormalizerTest {

    private TestProcedureDAO testProcedureDao;
    private TestProcedureNormalizer normalizer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        testProcedureDao = Mockito.mock(TestProcedureDAO.class);
        List<TestProcedureDTO> allowedTestProcedures = new ArrayList<TestProcedureDTO>();
        allowedTestProcedures.add(TestProcedureDTO.builder()
                .id(1L)
                .name(TestProcedureDTO.DEFAULT_TEST_PROCEDURE)
                .build());
        allowedTestProcedures.add(TestProcedureDTO.builder()
                .id(2L)
                .name("Another test procedure")
                .build());
        Mockito.when(testProcedureDao.getByCriterionId(ArgumentMatchers.anyLong()))
            .thenReturn(allowedTestProcedures);
        normalizer = new TestProcedureNormalizer(testProcedureDao);
    }

    @Test
    public void normalize_nullTestProcedures_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestProcedures(null);
        normalizer.normalize(listing);
        assertNull(listing.getCertificationResults().get(0).getTestProcedures());
    }

    @Test
    public void normalize_emptyTestProcedures_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testProcedures(new ArrayList<CertificationResultTestProcedure>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults().get(0).getTestProcedures());
        assertEquals(0, listing.getCertificationResults().get(0).getTestProcedures().size());
    }

    @Test
    public void normalize_testProcedureFound_fillsInId() {
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
        testProcedures.add(CertificationResultTestProcedure.builder()
                .testProcedure(TestProcedure.builder()
                        .name("Another test procedure")
                        .build())
                .testProcedureVersion("1.1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (b)(1)")
                                .build())
                        .testProcedures(testProcedures)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestProcedures().size());
        assertEquals(2L, listing.getCertificationResults().get(0).getTestProcedures().get(0).getTestProcedure().getId());
    }

    @Test
    public void normalize_testProcedureVersionExistsAndTestProcedureNull_fillsInIdAndNameForDefault() {
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
        testProcedures.add(CertificationResultTestProcedure.builder()
                .testProcedureVersion("1.1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (b)(1)")
                                .build())
                        .testProcedures(testProcedures)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestProcedures().size());
        assertNotNull(listing.getCertificationResults().get(0).getTestProcedures().get(0).getTestProcedure());
        assertEquals(1L, listing.getCertificationResults().get(0).getTestProcedures().get(0).getTestProcedure().getId());
        assertEquals(TestProcedureDTO.DEFAULT_TEST_PROCEDURE, listing.getCertificationResults().get(0).getTestProcedures().get(0).getTestProcedure().getName());
    }

    @Test
    public void normalize_testProcedureVersionExistsAndTestProcedureNameNull_fillsInIdAndNameForDefault() {
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
        testProcedures.add(CertificationResultTestProcedure.builder()
                .testProcedure(TestProcedure.builder()
                        .id(null)
                        .name(null)
                        .build())
                .testProcedureVersion("1.1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (b)(1)")
                                .build())
                        .testProcedures(testProcedures)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestProcedures().size());
        assertNotNull(listing.getCertificationResults().get(0).getTestProcedures().get(0).getTestProcedure());
        assertEquals(1L, listing.getCertificationResults().get(0).getTestProcedures().get(0).getTestProcedure().getId());
        assertEquals(TestProcedureDTO.DEFAULT_TEST_PROCEDURE, listing.getCertificationResults().get(0).getTestProcedures().get(0).getTestProcedure().getName());
    }

    @Test
    public void normalize_testProcedureVersionExistsAndTestProcedureNameEmpty_fillsInIdAndNameForDefault() {
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
        testProcedures.add(CertificationResultTestProcedure.builder()
                .testProcedure(TestProcedure.builder()
                        .id(null)
                        .name("")
                        .build())
                .testProcedureVersion("1.1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (b)(1)")
                                .build())
                        .testProcedures(testProcedures)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestProcedures().size());
        assertNotNull(listing.getCertificationResults().get(0).getTestProcedures().get(0).getTestProcedure());
        assertEquals(1L, listing.getCertificationResults().get(0).getTestProcedures().get(0).getTestProcedure().getId());
        assertEquals(TestProcedureDTO.DEFAULT_TEST_PROCEDURE, listing.getCertificationResults().get(0).getTestProcedures().get(0).getTestProcedure().getName());
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void normalize_testProcedureNameNotFound_idIsNull() {
        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
        testProcedures.add(CertificationResultTestProcedure.builder()
                .testProcedure(TestProcedure.builder()
                        .name("not found")
                        .build())
                .testProcedureVersion("1.1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (b)(1)")
                                .build())
                        .testProcedures(testProcedures)
                        .build())
                .build();

        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestProcedures().size());
        assertNull(listing.getCertificationResults().get(0).getTestProcedures().get(0).getTestProcedure().getId());
        assertEquals("not found", listing.getCertificationResults().get(0).getTestProcedures().get(0).getTestProcedure().getName());
    }
}
