package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.criteriaattribute.testtool.TestTool;
import gov.healthit.chpl.criteriaattribute.testtool.TestToolDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestToolCriteriaMap;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class TestToolNormalizerTest {

    private TestToolDAO testToolDao;
    private TestToolNormalizer normalizer;

    @Before
    public void setup() {
        testToolDao = Mockito.mock(TestToolDAO.class);
        List<TestToolCriteriaMap> allowedTestTools = new ArrayList<TestToolCriteriaMap>();
        allowedTestTools.add(TestToolCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .build())
                .testTool(TestTool.builder()
                        .id(1L)
                        .value("TT1")
                        .build())
                .build());
        allowedTestTools.add(TestToolCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .build())
                .testTool(TestTool.builder()
                        .id(2L)
                        .value("TT2")
                        .build())
                .build());

        try {
            Mockito.when(testToolDao.getAllTestToolCriteriaMap()).thenReturn(allowedTestTools);
        } catch (EntityRetrievalException e) {
        }

        normalizer = new TestToolNormalizer(testToolDao);
    }

    @Test
    public void normalize_nullTestTools_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestToolsUsed(null);
        normalizer.normalize(listing);
        assertNull(listing.getCertificationResults().get(0).getTestToolsUsed());
    }

    @Test
    public void normalize_emptyTestTools_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testToolsUsed(new ArrayList<CertificationResultTestTool>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults().get(0).getTestToolsUsed());
        assertEquals(0, listing.getCertificationResults().get(0).getTestToolsUsed().size());
    }

    @Test
    public void normalize_testToolNameFound_fillsInId() {
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .value("a name")
                        .build())
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("a name")))
            .thenReturn(TestTool.builder()
                    .id(1L)
                    .value("a name")
                    .build());

        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().get(0).getTestTool().getId());
    }

    @Test
    public void normalize_testToolNameFound_fillsInRetired() {
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .value("a name")
                        .build())
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("a name")))
            .thenReturn(TestTool.builder()
                    .id(1L)
                    .value("a name")
                    .startDay(LocalDate.MIN)
                    .endDay(LocalDate.MIN.plusDays(1L))
                    .build());

        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().get(0).getTestTool().getId());
        assertTrue(listing.getCertificationResults().get(0).getTestToolsUsed().get(0).getTestTool().isRetired());
    }

    @Test
    public void normalize_testToolNameNotFound_noChanges() {
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .value("a name")
                        .build())
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .testToolsUsed(testTools)
                        .build())
                .build();
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("a name"))).thenReturn(null);

        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertNull(listing.getCertificationResults().get(0).getTestToolsUsed().get(0).getTestTool().getId());
    }
}
