package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestStandardAndOptionalStandardReviewerTest {
    private static final String BOTH_TEST_AND_OPTIONAL_STANDARDS = "Criteria %s contains both Test Standards and Optional Standards. It may have only one type of Standard.";

    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;
    private TestStandardAndOptionalStandardReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        certResultRules = Mockito.mock(CertificationResultRules.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.hasBothTestAndOptionalStandards"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(BOTH_TEST_AND_OPTIONAL_STANDARDS, i.getArgument(1), ""));
        reviewer = new TestStandardAndOptionalStandardReviewer(msgUtil);
    }

    @Test
    public void review_nullTestStandardsAndOptionalStandards_noError() {
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
        listing.getCertificationResults().get(0).setOptionalStandards(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestStandardsAndOptionalStandards_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .testStandards(new ArrayList<CertificationResultTestStandard>())
                        .optionalStandards(new ArrayList<CertificationResultOptionalStandard>())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testStandardsAndOptionalStandards_hasError() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(CertificationResultTestStandard.builder()
                .testStandardId(1L)
                .testStandardName("test std")
                .build());
        testStandards.add(CertificationResultTestStandard.builder()
                .testStandardName("bad name")
                .build());
        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(1L)
                .citation("citation1")
                .build());
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .citation("citation2")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .testStandards(testStandards)
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(BOTH_TEST_AND_OPTIONAL_STANDARDS, "170.315 (a)(1)")));
    }


    @Test
    public void review_hasTestStandardsAndNoOptionalStandards_noError() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(CertificationResultTestStandard.builder()
                .testStandardId(1L)
                .testStandardName("test std")
                .build());
        testStandards.add(CertificationResultTestStandard.builder()
                .testStandardId(2L)
                .testStandardName("test std 2")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
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

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_hasOptionalStandardsAndNoTestStandards_noError() {
        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(1L)
                .citation("citation1")
                .build());
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .citation("citation2")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }
}
