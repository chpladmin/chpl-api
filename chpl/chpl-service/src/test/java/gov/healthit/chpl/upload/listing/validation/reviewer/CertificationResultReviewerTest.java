package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.GapAllowedReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.OldCriteriaWithoutIcsReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.SedG32015Reviewer;

public class CertificationResultReviewerTest {
    private static final String MISSING_CERT_RESULTS = "At least one certification result is required for the listing.";

    private ErrorMessageUtil msgUtil;
    private CertificationCriterion a6;
    private RemovedCriteriaReviewer removedCriteriaReviewer;
    private CriteriaReviewer criteriaReviewer;
    private PrivacyAndSecurityFrameworkReviewer pAndSFrameworkReviewer;
    private AdditionalSoftwareReviewer additionalSoftwareReviewer;
    private GapAllowedReviewer gapAllowedReviewer;
    private TestToolReviewer testToolReviewer;
    private TestDataReviewer testDataReviewer;
    private TestProcedureReviewer testProcedureReviewer;
    private TestFunctionalityReviewer testFunctionalityReviewer;
    private TestStandardReviewer testStandardReviewer;
    private OptionalStandardReviewer optionalStandardReviewer;
    private SvapReviewer svapReviewer;
    private UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;
    private OldCriteriaWithoutIcsReviewer oldCriteriaWithoutIcsReviewer;
    private SedG32015Reviewer sedG3Reviewer;
    private CertificationResultReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(msgUtil.getMessage("listing.missingCertificationResults"))
            .thenReturn(MISSING_CERT_RESULTS);

        a6 = CertificationCriterion.builder()
                .id(6L)
                .certificationEditionId(3L)
                .number("170.315 (a)(6)")
                .title("a6")
                .removed(true)
                .build();

        removedCriteriaReviewer = Mockito.mock(RemovedCriteriaReviewer.class);
        criteriaReviewer = Mockito.mock(CriteriaReviewer.class);
        pAndSFrameworkReviewer = Mockito.mock(PrivacyAndSecurityFrameworkReviewer.class);
        additionalSoftwareReviewer = Mockito.mock(AdditionalSoftwareReviewer.class);
        gapAllowedReviewer = Mockito.mock(GapAllowedReviewer.class);
        testToolReviewer = Mockito.mock(TestToolReviewer.class);
        testDataReviewer = Mockito.mock(TestDataReviewer.class);
        testProcedureReviewer =  Mockito.mock(TestProcedureReviewer.class);
        testFunctionalityReviewer = Mockito.mock(TestFunctionalityReviewer.class);
        testStandardReviewer = Mockito.mock(TestStandardReviewer.class);
        optionalStandardReviewer = Mockito.mock(OptionalStandardReviewer.class);
        svapReviewer = Mockito.mock(SvapReviewer.class);
        unattestedCriteriaWithDataReviewer = Mockito.mock(UnattestedCriteriaWithDataReviewer.class);
        oldCriteriaWithoutIcsReviewer = Mockito.mock(OldCriteriaWithoutIcsReviewer.class);
        sedG3Reviewer = Mockito.mock(SedG32015Reviewer.class);
        reviewer = new CertificationResultReviewer(removedCriteriaReviewer, criteriaReviewer,
                pAndSFrameworkReviewer, additionalSoftwareReviewer, gapAllowedReviewer, testToolReviewer,
                testDataReviewer, testProcedureReviewer, testFunctionalityReviewer, testStandardReviewer,
                optionalStandardReviewer, svapReviewer, unattestedCriteriaWithDataReviewer,
                oldCriteriaWithoutIcsReviewer, sedG3Reviewer,
                Mockito.mock(CertificationResultRules.class),
                new ValidationUtils(Mockito.mock(CertificationCriterionService.class)),
                msgUtil);
    }

    @Test
    public void review_nullCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        listing.setCertificationResults(null);
        reviewer.review(listing);

        assertNull(listing.getCertificationResults());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CERT_RESULTS));
    }

    @Test
    public void review_EmptyCertificationResults_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        listing.setCertificationResults(Collections.emptyList());
        reviewer.review(listing);

        assertNotNull(listing.getCertificationResults());
        assertEquals(0, listing.getCertificationResults().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CERT_RESULTS));
    }

    @Test
    public void review_removedCertificationResult_reviewersCalledCorrectly() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a6)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);

        verify(removedCriteriaReviewer, times(1)).review(listing, listing.getCertificationResults().get(0));
        verify(unattestedCriteriaWithDataReviewer, times(1)).review(listing);

        verify(criteriaReviewer, never()).review(listing);
        verify(pAndSFrameworkReviewer, never()).review(listing);
        verify(additionalSoftwareReviewer, never()).review(listing);
        verify(gapAllowedReviewer, never()).review(listing);
        verify(testToolReviewer, never()).review(listing);
        verify(testDataReviewer, never()).review(listing);
        verify(testProcedureReviewer, never()).review(listing);
        verify(testFunctionalityReviewer, never()).review(listing);
        verify(testStandardReviewer, never()).review(listing);
        verify(optionalStandardReviewer, never()).review(listing);
        verify(svapReviewer, never()).review(listing);
        verify(oldCriteriaWithoutIcsReviewer, never()).review(listing);
        verify(sedG3Reviewer, never()).review(listing);
    }
}
