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

import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.dto.TestDataDTO;

public class TestDataNormalizerTest {

    private TestDataDAO testDataDao;
    private TestDataNormalizer normalizer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        testDataDao = Mockito.mock(TestDataDAO.class);
        Mockito.when(testDataDao.getByCriterionAndValue(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(TestDataDTO.DEFAULT_TEST_DATA)))
            .thenReturn(TestDataDTO.builder()
                    .id(5L)
                    .name(TestDataDTO.DEFAULT_TEST_DATA)
                    .build());
        normalizer = new TestDataNormalizer(testDataDao);
    }

    @Test
    public void normalize_nullTestData_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestDataUsed(null);
        normalizer.normalize(listing);
        assertNull(listing.getCertificationResults().get(0).getTestDataUsed());
    }

    @Test
    public void normalize_emptyTestData_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testDataUsed(new ArrayList<CertificationResultTestData>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults().get(0).getTestDataUsed());
        assertEquals(0, listing.getCertificationResults().get(0).getTestDataUsed().size());
    }

    @Test
    public void normalize_testToolDataFound_fillsInId() {
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .name("a name")
                        .build())
                .version("1.1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (b)(1)")
                                .build())
                        .testDataUsed(testData)
                        .build())
                .build();
        List<TestDataDTO> foundTestData = new ArrayList<TestDataDTO>();
        foundTestData.add(TestDataDTO.builder()
                .id(1L)
                .name("a name")
                .build());
        foundTestData.add(TestDataDTO.builder()
                .id(2L)
                .name("b name")
                .build());
        Mockito.when(testDataDao.getByCriterionId(ArgumentMatchers.eq(1L))).thenReturn(foundTestData);

        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestDataUsed().size());
        assertEquals(1, listing.getCertificationResults().get(0).getTestDataUsed().get(0).getTestData().getId());
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void normalize_testDataNameNotFound_doesNotFillInData() {
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .name("c name")
                        .build())
                .version("1.1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (b)(1)")
                                .build())
                        .testDataUsed(testData)
                        .build())
                .build();
        List<TestDataDTO> foundTestData = new ArrayList<TestDataDTO>();
        foundTestData.add(TestDataDTO.builder()
                .id(1L)
                .name("a name")
                .build());
        foundTestData.add(TestDataDTO.builder()
                .id(2L)
                .name("b name")
                .build());
        Mockito.when(testDataDao.getByCriterionId(ArgumentMatchers.eq(1L))).thenReturn(foundTestData);

        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestDataUsed().size());
        CertificationResultTestData normalizedTestData = listing.getCertificationResults().get(0).getTestDataUsed().get(0);
        assertNull(normalizedTestData.getTestData().getId());
        assertEquals("c name", normalizedTestData.getTestData().getName());
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void normalize_testDataNameEmpty_fillsInData() {
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .name("")
                        .build())
                .version("1.1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (b)(1)")
                                .build())
                        .testDataUsed(testData)
                        .build())
                .build();
        List<TestDataDTO> foundTestData = new ArrayList<TestDataDTO>();
        foundTestData.add(TestDataDTO.builder()
                .id(5L)
                .name(TestDataDTO.DEFAULT_TEST_DATA)
                .build());
        Mockito.when(testDataDao.getByCriterionId(ArgumentMatchers.eq(1L))).thenReturn(foundTestData);

        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestDataUsed().size());
        CertificationResultTestData normalizedTestData = listing.getCertificationResults().get(0).getTestDataUsed().get(0);
        assertNotNull(normalizedTestData.getTestData().getId());
        assertEquals(TestDataDTO.DEFAULT_TEST_DATA, normalizedTestData.getTestData().getName());
        assertEquals("1.1", normalizedTestData.getVersion());
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void normalize_testDataNull_fillsInData() {
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(null)
                .version("1.1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (b)(1)")
                                .build())
                        .testDataUsed(testData)
                        .build())
                .build();
        List<TestDataDTO> foundTestData = new ArrayList<TestDataDTO>();
        foundTestData.add(TestDataDTO.builder()
                .id(5L)
                .name(TestDataDTO.DEFAULT_TEST_DATA)
                .build());
        Mockito.when(testDataDao.getByCriterionId(ArgumentMatchers.eq(1L))).thenReturn(foundTestData);

        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestDataUsed().size());
        CertificationResultTestData normalizedTestData = listing.getCertificationResults().get(0).getTestDataUsed().get(0);
        assertNotNull(normalizedTestData.getTestData().getId());
        assertEquals(TestDataDTO.DEFAULT_TEST_DATA, normalizedTestData.getTestData().getName());
        assertEquals("1.1", normalizedTestData.getVersion());
    }
}
