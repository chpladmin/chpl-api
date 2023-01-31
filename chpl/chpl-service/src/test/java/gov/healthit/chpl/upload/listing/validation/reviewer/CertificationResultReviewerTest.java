package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.ConformanceMethodReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.GapAllowedReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.OldCriteriaWithoutIcsReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.SedG32015Reviewer;

public class CertificationResultReviewerTest {
    private static final String MISSING_CERT_RESULTS = "At least one certification result is required for the listing.";

    private ErrorMessageUtil msgUtil;
    private CertificationResultReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        FF4j ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.ERD_PHASE_2)))
            .thenReturn(false);

        Mockito.when(msgUtil.getMessage("listing.missingCertificationResults"))
            .thenReturn(MISSING_CERT_RESULTS);

        reviewer = new CertificationResultReviewer(Mockito.mock(RemovedCriteriaReviewer.class),
                Mockito.mock(CriteriaReviewer.class),
                Mockito.mock(PrivacyAndSecurityFrameworkReviewer.class),
                Mockito.mock(AdditionalSoftwareReviewer.class),
                Mockito.mock(GapAllowedReviewer.class),
                Mockito.mock(TestToolReviewer.class),
                Mockito.mock(TestDataReviewer.class),
                Mockito.mock(ConformanceMethodReviewer.class),
                Mockito.mock(FunctionalityTestedReviewer.class),
                Mockito.mock(TestStandardReviewer.class),
                Mockito.mock(OptionalStandardReviewer.class),
                Mockito.mock(SvapReviewer.class),
                Mockito.mock(UnattestedCriteriaWithDataReviewer.class),
                Mockito.mock(OldCriteriaWithoutIcsReviewer.class),
                Mockito.mock(SedG32015Reviewer.class),
                Mockito.mock(CertificationResultRules.class),
                new ValidationUtils(Mockito.mock(CertificationCriterionService.class)),
                msgUtil, ff4j);
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
}
