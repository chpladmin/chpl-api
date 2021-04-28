package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class QmsStandardReviewerTest {
    private static final String MISSING_QMS = "QMS Standards are required.";
    private static final String MISSING_NAME = "A name is required for each QMS Standard listed.";
    private static final String MISSING_APPLICABLE_CRITERIA = "Applicable criteria is required for each QMS Standard listed.";

    private ErrorMessageUtil errorMessageUtil;
    private QmsStandardReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.qmsStandardsNotFound")))
            .thenReturn(MISSING_QMS);
        Mockito.when(errorMessageUtil.getMessage("listing.qmsStandardMissingName"))
            .thenReturn(MISSING_NAME);
        Mockito.when(errorMessageUtil.getMessage("listing.qmsStandardMissingApplicableCriteria"))
            .thenReturn(MISSING_APPLICABLE_CRITERIA);
        reviewer = new QmsStandardReviewer(errorMessageUtil);
    }

    @Test
    public void review_nullQmsStandards_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setQmsStandards(null);
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_QMS));
    }

    @Test
    public void review_emptyQmsStandards_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(new ArrayList<CertifiedProductQmsStandard>())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_QMS));
    }

    @Test
    public void review_hasNullQmsStandardName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .id(1L)
                        .qmsStandardName(null)
                        .qmsStandardId(null)
                        .qmsModification(null)
                        .applicableCriteria("test")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_NAME));
    }

    @Test
    public void review_hasEmptyQmsStandardName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .id(1L)
                        .qmsStandardName("")
                        .qmsStandardId(null)
                        .qmsModification(null)
                        .applicableCriteria("test")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_NAME));
    }

    @Test
    public void review_hasNullQmsStandardApplicableCriteria_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .id(1L)
                        .qmsStandardName("test")
                        .qmsStandardId(null)
                        .qmsModification(null)
                        .applicableCriteria(null)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_APPLICABLE_CRITERIA));
    }

    @Test
    public void review_hasEmptyQmsStandardApplicableCriteria_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .id(1L)
                        .qmsStandardName("test")
                        .qmsStandardId(null)
                        .qmsModification(null)
                        .applicableCriteria("")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_APPLICABLE_CRITERIA));
    }

    @Test
    public void review_hasQmsStandardNameAndApplicableCriteria_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .id(1L)
                        .qmsStandardName("test")
                        .qmsStandardId(null)
                        .qmsModification(null)
                        .applicableCriteria("ac")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_hasQmsStandardNameAndApplicableCriteriaAndId_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .id(1L)
                        .qmsStandardName("test")
                        .qmsStandardId(2L)
                        .qmsModification(null)
                        .applicableCriteria("ac")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_hasQmsStandardNameAndApplicableCriteriaAndIdAndModification_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .id(1L)
                        .qmsStandardName("test")
                        .qmsStandardId(2L)
                        .qmsModification("mod")
                        .applicableCriteria("ac")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    //TODO: add tests to check for fuzzy match warnings

}
