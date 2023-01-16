package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.fuzzyMatching.FuzzyType;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class QmsStandardReviewerTest {
    private static final String MISSING_QMS = "QMS Standards are required.";
    private static final String MISSING_NAME = "A name is required for each QMS Standard listed.";
    private static final String MISSING_APPLICABLE_CRITERIA = "Applicable criteria is required for each QMS Standard listed.";
    private static final String FUZZY_MATCH_REPLACEMENT = "The %s value was changed from %s to %s.";
    private static final String NOT_FOUND_AND_REMOVED = "The QMS Standard '%s' was not found in the system and has been removed from the listing.";

    private ErrorMessageUtil errorMessageUtil;
    private QmsStandardReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.qmsStandardsNotFound")))
            .thenReturn(MISSING_QMS);
        Mockito.when(errorMessageUtil.getMessage("listing.qmsStandardMissingName"))
            .thenReturn(MISSING_NAME);
        Mockito.when(errorMessageUtil.getMessage("listing.qmsStandardMissingApplicableCriteria"))
            .thenReturn(MISSING_APPLICABLE_CRITERIA);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.fuzzyMatch"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(FUZZY_MATCH_REPLACEMENT, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.qmsStandardNotFoundAndRemoved"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(NOT_FOUND_AND_REMOVED, i.getArgument(1), ""));
        reviewer = new QmsStandardReviewer(errorMessageUtil);
    }

    @Test
    public void review_nullQmsStandards_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setQmsStandards(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_QMS));
    }

    @Test
    public void review_emptyQmsStandards_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(new ArrayList<CertifiedProductQmsStandard>())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_QMS));
    }

    @Test
    public void review_hasNullQmsStandardName_hasError() {
        List<CertifiedProductQmsStandard> qmsStandards = Stream.of(CertifiedProductQmsStandard.builder()
                .id(1L)
                .qmsStandardName(null)
                .qmsStandardId(1L)
                .qmsModification(null)
                .applicableCriteria("test")
                .build()).collect(Collectors.toList());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(qmsStandards)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_NAME));
    }

    @Test
    public void review_hasEmptyQmsStandardName_hasError() {
        List<CertifiedProductQmsStandard> qmsStandards = Stream.of(CertifiedProductQmsStandard.builder()
                .id(1L)
                .qmsStandardName("")
                .qmsStandardId(1L)
                .qmsModification(null)
                .applicableCriteria("test")
                .build()).collect(Collectors.toList());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(qmsStandards)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_NAME));
    }

    @Test
    public void review_hasNullQmsStandardApplicableCriteria_hasError() {
        List<CertifiedProductQmsStandard> qmsStandards = Stream.of(CertifiedProductQmsStandard.builder()
                .id(1L)
                .qmsStandardName("test")
                .qmsStandardId(1L)
                .qmsModification(null)
                .applicableCriteria(null)
                .build()).collect(Collectors.toList());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(qmsStandards)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_APPLICABLE_CRITERIA));
    }

    @Test
    public void review_hasEmptyQmsStandardApplicableCriteria_hasError() {
        List<CertifiedProductQmsStandard> qmsStandards = Stream.of(CertifiedProductQmsStandard.builder()
                .id(1L)
                .qmsStandardName("test")
                .qmsStandardId(1L)
                .qmsModification(null)
                .applicableCriteria("")
                .build()).collect(Collectors.toList());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(qmsStandards)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_APPLICABLE_CRITERIA));
    }

    @Test
    public void review_hasQmsStandardNameAndApplicableCriteriaNoId_hasWarningAndIsRemoved() {
        List<CertifiedProductQmsStandard> qmsStandards = Stream.of(CertifiedProductQmsStandard.builder()
                .id(1L)
                .qmsStandardName("test")
                .qmsStandardId(null)
                .qmsModification(null)
                .applicableCriteria("ac")
                .build()).collect(Collectors.toList());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(qmsStandards)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(NOT_FOUND_AND_REMOVED, "test")));
        assertTrue(listing.getErrorMessages().contains(MISSING_QMS));
    }

    @Test
    public void review_hasEmptyQmsStandardNameAndApplicableCriteriaNoId_hasWarningAndIsRemoved() {
        List<CertifiedProductQmsStandard> qmsStandards = Stream.of(CertifiedProductQmsStandard.builder()
                        .id(1L)
                        .qmsStandardName("")
                        .qmsStandardId(null)
                        .qmsModification(null)
                        .applicableCriteria("ac")
                        .build()).collect(Collectors.toList());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(qmsStandards)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(NOT_FOUND_AND_REMOVED, "")));
        assertTrue(listing.getErrorMessages().contains(MISSING_QMS));
    }

    @Test
    public void review_hasQmsStandardNameAndApplicableCriteriaAndId_noError() {
        List<CertifiedProductQmsStandard> qmsStandards = Stream.of(CertifiedProductQmsStandard.builder()
                .id(1L)
                .qmsStandardName("test")
                .qmsStandardId(2L)
                .qmsModification(null)
                .applicableCriteria("ac")
                .build()).collect(Collectors.toList());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(qmsStandards)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_hasQmsStandardNameAndApplicableCriteriaAndIdAndModification_noError() {
        List<CertifiedProductQmsStandard> qmsStandards = Stream.of(CertifiedProductQmsStandard.builder()
                .id(1L)
                .qmsStandardName("test")
                .qmsStandardId(2L)
                .qmsModification("mod")
                .applicableCriteria("ac")
                .build()).collect(Collectors.toList());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(qmsStandards)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_hasQmsStandardNameAndIdFindsFuzzyMatch_hasWarning() {
        List<CertifiedProductQmsStandard> qmsStandards = Stream.of(CertifiedProductQmsStandard.builder()
                .id(1L)
                .qmsStandardName("test")
                .userEnteredQmsStandardName("tst")
                .qmsStandardId(1L)
                .qmsModification("mod")
                .applicableCriteria("ac")
                .build()).collect(Collectors.toList());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .qmsStandards(qmsStandards)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(FUZZY_MATCH_REPLACEMENT, FuzzyType.QMS_STANDARD.fuzzyType(), "tst", "test")));
    }
}
