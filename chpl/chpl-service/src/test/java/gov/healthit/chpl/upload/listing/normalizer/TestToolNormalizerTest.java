package gov.healthit.chpl.upload.listing.normalizer;

//TODO: OCD-4242
public class TestToolNormalizerTest {

//    private TestToolDAO testToolDao;
//    private TestToolNormalizer normalizer;
//
//    @Before
//    public void setup() {
//        testToolDao = Mockito.mock(TestToolDAO.class);
//        List<TestToolCriteriaMap> allowedTestTools = new ArrayList<TestToolCriteriaMap>();
//        allowedTestTools.add(TestToolCriteriaMap.builder()
//                .criterion(CertificationCriterion.builder()
//                        .id(1L)
//                        .number("170.315 (a)(1)")
//                        .build())
//                .testTool(new TestTool(1L, "TT1"))
//                .build());
//        allowedTestTools.add(TestToolCriteriaMap.builder()
//                .criterion(CertificationCriterion.builder()
//                        .id(1L)
//                        .number("170.315 (a)(1)")
//                        .build())
//                .testTool(new TestTool(2L, "TT2"))
//                .build());
//
//        try {
//            Mockito.when(testToolDao.getAllTestToolCriteriaMap()).thenReturn(allowedTestTools);
//        } catch (EntityRetrievalException e) {
//        }
//
//        normalizer = new TestToolNormalizer(testToolDao);
//    }
//
//    @Test
//    public void normalize_nullTestTools_noChanges() {
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .certificationResult(CertificationResult.builder()
//                        .build())
//                .build();
//        listing.getCertificationResults().get(0).setTestToolsUsed(null);
//        normalizer.normalize(listing);
//        assertNull(listing.getCertificationResults().get(0).getTestToolsUsed());
//    }
//
//    @Test
//    public void normalize_emptyTestTools_noChanges() {
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .certificationResult(CertificationResult.builder()
//                        .testToolsUsed(new ArrayList<CertificationResultTestTool>())
//                        .build())
//                .build();
//        normalizer.normalize(listing);
//        assertNotNull(listing.getCertificationResults().get(0).getTestToolsUsed());
//        assertEquals(0, listing.getCertificationResults().get(0).getTestToolsUsed().size());
//    }
//
//    @Test
//    public void normalize_testToolNameFound_fillsInId() {
//        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
//        testTools.add(CertificationResultTestTool.builder()
//                .testToolName("a name")
//                .build());
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .certificationResult(CertificationResult.builder()
//                        .testToolsUsed(testTools)
//                        .build())
//                .build();
//        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("a name")))
//            .thenReturn(TestToolDTO.builder()
//                    .id(1L)
//                    .name("a name")
//                    .build());
//
//        normalizer.normalize(listing);
//        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
//        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().get(0).getTestToolId());
//    }
//
//    @Test
//    public void normalize_testToolNameFound_fillsInRetired() {
//        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
//        testTools.add(CertificationResultTestTool.builder()
//                .testToolName("a name")
//                .build());
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .certificationResult(CertificationResult.builder()
//                        .testToolsUsed(testTools)
//                        .build())
//                .build();
//        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("a name")))
//            .thenReturn(TestToolDTO.builder()
//                    .id(1L)
//                    .name("a name")
//                    .retired(true)
//                    .build());
//
//        normalizer.normalize(listing);
//        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
//        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().get(0).getTestToolId());
//        assertTrue(listing.getCertificationResults().get(0).getTestToolsUsed().get(0).isRetired());
//    }
//
//    @Test
//    public void normalize_testToolNameNotFound_noChanges() {
//        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
//        testTools.add(CertificationResultTestTool.builder()
//                .testToolName("a name")
//                .build());
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .certificationResult(CertificationResult.builder()
//                        .testToolsUsed(testTools)
//                        .build())
//                .build();
//        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("a name"))).thenReturn(null);
//
//        normalizer.normalize(listing);
//        assertEquals(1, listing.getCertificationResults().get(0).getTestToolsUsed().size());
//        assertNull(listing.getCertificationResults().get(0).getTestToolsUsed().get(0).getTestToolId());
//    }
//
//    @Test
//    public void normalize_criterionHasAllowedTestTools_addsAllowedTestToolsToCertificationResult() {
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .certificationResult(CertificationResult.builder()
//                        .criterion(CertificationCriterion.builder()
//                                    .id(1L)
//                                    .number("170.315 (a)(1)")
//                                    .build())
//                        .testToolsUsed(new ArrayList<CertificationResultTestTool>())
//                        .build())
//                .build();
//        normalizer.normalize(listing);
//        assertEquals(2, listing.getCertificationResults().get(0).getAllowedTestTools().size());
//    }
//
//    @Test
//    public void normalize_criterionHasNoAllowedTestTools_noAllowedTestToolsAdded() {
//        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
//                .certificationResult(CertificationResult.builder()
//                        .criterion(CertificationCriterion.builder()
//                                    .id(2L)
//                                    .number("170.315 (a)(2)")
//                                    .build())
//                        .testToolsUsed(new ArrayList<CertificationResultTestTool>())
//                        .build())
//                .build();
//        normalizer.normalize(listing);
//        assertEquals(0, listing.getCertificationResults().get(0).getAllowedTestTools().size());
//    }
}
