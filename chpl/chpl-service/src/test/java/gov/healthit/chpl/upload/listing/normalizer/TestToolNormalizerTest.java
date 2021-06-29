package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestToolDTO;

public class TestToolNormalizerTest {

    private TestToolDAO testToolDao;
    private TestToolNormalizer normalizer;

    @Before
    public void setup() {
        testToolDao = Mockito.mock(TestToolDAO.class);
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
                .testToolName("a name")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testToolsUsed(testTools)
                        .build())
                .build();
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("a name")))
            .thenReturn(TestToolDTO.builder()
                    .id(1L)
                    .name("a name")
                    .build());

        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().get(0).getTestToolId());
    }

    @Test
    public void normalize_testToolNameFound_fillsInRetired() {
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testToolName("a name")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testToolsUsed(testTools)
                        .build())
                .build();
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("a name")))
            .thenReturn(TestToolDTO.builder()
                    .id(1L)
                    .name("a name")
                    .retired(true)
                    .build());

        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().get(0).getTestToolId());
        assertTrue(listing.getCertificationResults().get(0).getTestToolsUsed().get(0).isRetired());
    }

    @Test
    public void normalize_testToolNameNotFound_noChanges() {
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        testTools.add(CertificationResultTestTool.builder()
                .testToolName("a name")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .testToolsUsed(testTools)
                        .build())
                .build();
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("a name"))).thenReturn(null);

        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
        assertNull(listing.getCertificationResults().get(0).getTestToolsUsed().get(0).getTestToolId());
    }
}
