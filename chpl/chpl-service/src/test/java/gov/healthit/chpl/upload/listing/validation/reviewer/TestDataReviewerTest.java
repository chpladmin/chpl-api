package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.testdata.CertificationResultTestData;
import gov.healthit.chpl.testdata.TestData;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestDataReviewerTest {
    private static final String TEST_DATA_NOT_APPLICABLE = "Test data is not applicable for the criterion %s. It has been removed.";

    private ErrorMessageUtil msgUtil;
    private TestDataReviewer reviewer;

    @BeforeEach
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testDataNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_DATA_NOT_APPLICABLE, i.getArgument(1), ""));
        reviewer = new TestDataReviewer(msgUtil);
    }

    @Test
    public void review_nullTestDataA1_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .build())
                .build();
        listing.getCertificationResults().get(0).setTestDataUsed(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestDataA1_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testDataPresent_hasWarningAndTestDataSetNull() {
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        testData.add(CertificationResultTestData.builder()
                .testData(TestData.builder()
                        .id(1L)
                        .name("ONC Test Method")
                        .build())
                .version("1")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .testDataUsed(testData)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(TEST_DATA_NOT_APPLICABLE, "170.315 (a)(1)")));
        assertNull(listing.getCertificationResults().get(0).getTestDataUsed());
    }
}
