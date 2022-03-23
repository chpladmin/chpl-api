package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ConformanceMethodDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String DUPLICATE_NAME_AND_VERSION =
            "Certification %s contains duplicate Conformance Method: Name '%s', Version '%s'. The duplicates have been removed.";
    private static final String DUPLICATE_NAME =
            "Certification %s contains duplicate Conformance Method: '%s'.";

    private ErrorMessageUtil msgUtil;
    private ConformanceMethodDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateConformanceMethodNameAndVersion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DUPLICATE_NAME_AND_VERSION, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateConformanceMethodName"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DUPLICATE_NAME, i.getArgument(1), i.getArgument(2)));
        reviewer = new ConformanceMethodDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultConformanceMethod testCm1 = getConformanceMethod(1L, "TestCM1", "v1");
        CertificationResultConformanceMethod testCm2 = getConformanceMethod(1L, "TestCM1", "v1");
        cert.getConformanceMethods().add(testCm1);
        cert.getConformanceMethods().add(testCm2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestCM1", "v1")))
                .count());
        assertEquals(1, cert.getConformanceMethods().size());
    }

    @Test
    public void review_duplicateNameExists_errorFound() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultConformanceMethod testCm1 = getConformanceMethod(1L, "TestCM1", "v1");
        CertificationResultConformanceMethod testCm2 = getConformanceMethod(1L, "TestCM1", "v2");
        cert.getConformanceMethods().add(testCm1);
        cert.getConformanceMethods().add(testCm2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertEquals(1, listing.getErrorMessages().stream()
                .filter(error -> error.equals(String.format(DUPLICATE_NAME, CRITERION_NUMBER, "TestCM1")))
                .count());
        assertEquals(2, cert.getConformanceMethods().size());
    }

    @Test
    public void review_duplicateNameNullVersion_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        //2014 listings allow null cm version
        CertificationResultConformanceMethod testCm1 = getConformanceMethod(1L, "TestCM1", null);
        CertificationResultConformanceMethod testCm2 = getConformanceMethod(1L, "TestCM1", null);
        cert.getConformanceMethods().add(testCm1);
        cert.getConformanceMethods().add(testCm2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestCM1", "")))
                .count());
        assertEquals(1, cert.getConformanceMethods().size());
    }

    @Test
    public void review_duplicateNameEmptyVersion_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        //2014 listings allow empty cm version
        CertificationResultConformanceMethod testCm1 = getConformanceMethod(1L, "TestCM1", "");
        CertificationResultConformanceMethod testCm2 = getConformanceMethod(1L, "TestCM1", "");
        cert.getConformanceMethods().add(testCm1);
        cert.getConformanceMethods().add(testCm2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestCM1", "")))
                .count());
        assertEquals(1, cert.getConformanceMethods().size());
    }

    @Test
    public void review_noDuplicateIds_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultConformanceMethod testCm1 = getConformanceMethod(1L, "TestCM1", "v1");
        CertificationResultConformanceMethod testCm2 = getConformanceMethod(2L, "TestCM2", "v1");
        cert.getConformanceMethods().add(testCm1);
        cert.getConformanceMethods().add(testCm2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(2, cert.getConformanceMethods().size());
    }

    @Test
    public void review_emptyTestProcedures_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getConformanceMethods().clear();
        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, cert.getConformanceMethods().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultConformanceMethod testCm1 = getConformanceMethod(1L, "TestCM1", "v1");
        CertificationResultConformanceMethod testCm2 = getConformanceMethod(2L, "TestCM2", "v1");
        CertificationResultConformanceMethod testCm3 = getConformanceMethod(1L, "TestCM1", "v1");
        CertificationResultConformanceMethod testCm4 = getConformanceMethod(4L, "TestCM4", "v2");
        cert.getConformanceMethods().add(testCm1);
        cert.getConformanceMethods().add(testCm2);
        cert.getConformanceMethods().add(testCm3);
        cert.getConformanceMethods().add(testCm4);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestCM1", "v1")))
                .count());
        assertEquals(3, cert.getConformanceMethods().size());
    }

    private CertificationResultConformanceMethod getConformanceMethod(Long id, String name, String version) {
        return CertificationResultConformanceMethod.builder()
                .conformanceMethod(ConformanceMethod.builder()
                    .id(id)
                    .name(name)
                    .build())
                .conformanceMethodVersion(version)
                .build();
    }

    private CertificationResult getCertResult() {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(CRITERION_NUMBER);
        CertificationResult cert = new CertificationResult();
        cert.setCriterion(criterion);
        return cert;
    }
}