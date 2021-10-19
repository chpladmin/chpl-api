package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestStandardReviewerTest {
    private static final String TEST_STANDARDS_NOT_APPLICABLE = "Test standards are not applicable to criterion '%s'. They have been removed.";

    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;
    private TestStandardReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        certResultRules = Mockito.mock(CertificationResultRules.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testStandardsNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_STANDARDS_NOT_APPLICABLE, i.getArgument(1), ""));
        reviewer = new TestStandardReviewer(certResultRules, msgUtil);
    }

    @Test
    public void review_nullTestStandards_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestStandards(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestStandards_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .testStandards(new ArrayList<CertificationResultTestStandard>())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testStandardsPresentAndAllowed_hasWarningAndRemovedTestStandard() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.STANDARDS_TESTED)))
            .thenReturn(true);

        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(CertificationResultTestStandard.builder()
                .testStandardName("test std")
                .testStandardId(1L)
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .testStandards(testStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(TEST_STANDARDS_NOT_APPLICABLE, "170.315 (a)(1)")));
        assertEquals(0, listing.getCertificationResults().get(0).getTestStandards().size());
    }

    @Test
    public void review_testStandardsPresentAndNotAllowed_hasWarningAndNullTestStandards() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.STANDARDS_TESTED)))
            .thenReturn(false);

        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(CertificationResultTestStandard.builder()
                .testStandardName("test std")
                .testStandardId(null)
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .testStandards(testStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(TEST_STANDARDS_NOT_APPLICABLE, "170.315 (a)(1)")));
        assertNull(listing.getCertificationResults().get(0).getTestStandards());
    }

    private Map<String, Object> create2015EditionMap() {
        Map<String, Object> editionMap = new HashMap<String, Object>();
        editionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        editionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        return editionMap;
    }
}
