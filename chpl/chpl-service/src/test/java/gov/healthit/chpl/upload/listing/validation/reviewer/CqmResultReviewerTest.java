package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.AttestedCriteriaCqmReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.CqmAttestedCriteriaReviewer;

public class CqmResultReviewerTest {
    private static final String MISSING_CMS_ID = "A CQM was found with versions or criteria specified but is missing the required CMS ID.";
    private static final String INVALID_CMS_ID = "CMS ID '%s' is not a valid.";
    private static final String MISSING_VERSION = "The CQM with CMS ID '%s' does not specify a version. A version is required.";
    private static final String INVALID_VERSION = "The CQM with CMS ID '%s' has an invalid version '%s'.";

    private CertificationCriterion c1;
    private ErrorMessageUtil msgUtil;
    private CqmResultReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        c1 = buildCriterion(1L, "170.315 (c)(1)", "C1");
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.cqm.missingCmsId")))
            .thenReturn(MISSING_CMS_ID);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.cqm.invalidCmsId"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CMS_ID, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.cqm.missingVersion"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_VERSION, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.cqm.invalidCqmVersion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_VERSION, i.getArgument(1), i.getArgument(2)));

        reviewer = new CqmResultReviewer(Mockito.mock(CqmAttestedCriteriaReviewer.class),
                Mockito.mock(AttestedCriteriaCqmReviewer.class),
                msgUtil);
    }

    @Test
    public void review_nullCqms_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        listing.setCqmResults(null);
        reviewer.review(listing);

        assertNull(listing.getCqmResults());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_EmptyCqms_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listing);

        assertNotNull(listing.getCqmResults());
        assertEquals(0, listing.getCqmResults().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_UnattestedCqmNullCmsIdWithCriterion_noError() {
        CQMResultDetails cqmResult = CQMResultDetails.builder()
                .success(false)
                .cmsId(null)
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_NullCmsIdWithCriterion_hasError() {
        CQMResultDetails cqmResult = CQMResultDetails.builder()
                .success(true)
                .cmsId(null)
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CMS_ID));
    }

    @Test
    public void review_EmptyCmsIdWithCriterion_hasError() {
        CQMResultDetails cqmResult = CQMResultDetails.builder()
                .success(true)
                .cmsId("")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CMS_ID));
    }

    @Test
    public void review_NullCmsIdWithSuccessVersion_hasError() {
        CQMResultDetails cqmResult = CQMResultDetails.builder()
                .success(true)
                .cmsId(null)
                .successVersions(Stream.of("v1").collect(Collectors.toSet()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CMS_ID));
    }

    @Test
    public void review_EmptyCmsIdWithSuccessVersion_hasError() {
        CQMResultDetails cqmResult = CQMResultDetails.builder()
                .success(true)
                .cmsId("")
                .successVersions(Stream.of("v1", "v6").collect(Collectors.toSet()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CMS_ID));
    }

    @Test
    public void review_UnattestedCqmInvalidCmsIdWithSuccessVersion_hasError() {
        CQMResultDetails cqmResult = CQMResultDetails.builder()
                .success(false)
                .cmsId("CMS1")
                .cqmCriterionId(null)
                .successVersions(Stream.of("v1", "v6").collect(Collectors.toSet()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_CMS_ID, "CMS1")));
    }

    @Test
    public void review_InvalidCmsIdWithSuccessVersion_hasError() {
        CQMResultDetails cqmResult = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS1")
                .cqmCriterionId(null)
                .successVersions(Stream.of("v1", "v6").collect(Collectors.toSet()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_CMS_ID, "CMS1")));
    }

    @Test
    public void review_ValidCmsIdMissingSuccessVersion_hasError() {
        CQMResultDetails cqmResult = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS1")
                .cqmCriterionId(1L)
                .allVersions(Stream.of("v1", "v6").collect(Collectors.toSet()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_VERSION, "CMS1")));
    }

    @Test
    public void review_ValidCmsIdInvalidSuccessVersion_hasError() {
        CQMResultDetails cqmResult = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS1")
                .cqmCriterionId(1L)
                .successVersions(Stream.of("v1", "v6").collect(Collectors.toSet()))
                .allVersions(Stream.of("v1", "v2").collect(Collectors.toSet()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_VERSION, "CMS1", "v6")));
    }

    @Test
    public void review_UnattestedCqmValidCmsIdInvalidSuccessVersions_hasErrors() {
        CQMResultDetails cqmResult = CQMResultDetails.builder()
                .success(false)
                .cmsId("CMS1")
                .cqmCriterionId(1L)
                .successVersions(Stream.of("v1", "v6", "v12").collect(Collectors.toSet()))
                .allVersions(Stream.of("v1", "v2").collect(Collectors.toSet()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_VERSION, "CMS1", "v6")));
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_VERSION, "CMS1", "v12")));    }

    @Test
    public void review_ValidCmsIdInvalidSuccessVersions_hasError() {
        CQMResultDetails cqmResult = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS1")
                .cqmCriterionId(1L)
                .successVersions(Stream.of("v1", "v6", "v12").collect(Collectors.toSet()))
                .allVersions(Stream.of("v1", "v2").collect(Collectors.toSet()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_VERSION, "CMS1", "v6")));
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_VERSION, "CMS1", "v12")));
    }

    @Test
    public void review_Valid_noErrors() {
        CQMResultDetails cqmResult = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS1")
                .cqmCriterionId(1L)
                .successVersions(Stream.of("v1", "v6").collect(Collectors.toSet()))
                .allVersions(Stream.of("v1", "V2", "v3", "v4", "v5", "v6").collect(Collectors.toSet()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    private CertificationCriterion buildCriterion(Long id, String number, String title) {
        return CertificationCriterion.builder()
                .id(id)
                .number(number)
                .title(title)
                .build();
    }
}
