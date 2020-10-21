package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMipsMeasure;
import gov.healthit.chpl.domain.MipsMeasure;
import gov.healthit.chpl.domain.MipsMeasurementType;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class MipsMeasureDuplicateReviewerTest {
    private static final String DUPLICATE_MSG =
            "Duplicate %s Measure: '%s' was found. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private MipsMeasureDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateMipsMeasure"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DUPLICATE_MSG, i.getArgument(1), i.getArgument(2)));
        reviewer = new MipsMeasureDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        ListingMipsMeasure measure1 = getMipsMeasure(1L, 1L, "EH-CAH", 1L, "G1");
        ListingMipsMeasure measure2 = getMipsMeasure(2L, 1L, "EH-CAH", 1L, "G1");
        listing.getMipsMeasures().add(measure1);
        listing.getMipsMeasures().add(measure2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_MSG, "G1", "EH-CAH")))
                .count());
        assertEquals(1, listing.getMipsMeasures().size());
    }

    @Test
    public void review_duplicateExistsButDifferentCriteria_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        ListingMipsMeasure measure1 = getMipsMeasure(1L, 1L, "EH-CAH", 1L, "G1");
        measure1.getAssociatedCriteria().add(CertificationCriterion.builder()
                .id(1L)
                .number("170.315 (a)(1)")
                .build());
        ListingMipsMeasure measure2 = getMipsMeasure(2L, 1L, "EH-CAH", 1L, "G1");
        measure2.getAssociatedCriteria().add(CertificationCriterion.builder()
                .id(2L)
                .number("170.315 (a)(2)")
                .build());
        listing.getMipsMeasures().add(measure1);
        listing.getMipsMeasures().add(measure2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_MSG, "G1", "EH-CAH")))
                .count());
        assertEquals(1, listing.getMipsMeasures().size());
    }

    @Test
    public void review_differentTypes_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        ListingMipsMeasure measure1 = getMipsMeasure(1L, 1L, "EH-CAH", 1L, "G1");
        ListingMipsMeasure measure2 = getMipsMeasure(2L, 1L, "EH-CAH", 2L, "G2");
        listing.getMipsMeasures().add(measure1);
        listing.getMipsMeasures().add(measure2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getMipsMeasures().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        ListingMipsMeasure measure1 = getMipsMeasure(1L, 1L, "EH-CAH", 1L, "G1");
        ListingMipsMeasure measure2 = getMipsMeasure(2L, 2L, "EP", 2L, "G2");
        listing.getMipsMeasures().add(measure1);
        listing.getMipsMeasures().add(measure2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getMipsMeasures().size());
    }

    @Test
    public void review_emptyMipsMeasures_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMipsMeasures().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getMipsMeasures().size());
    }

    @Test
    public void testDuplicateExistInLargerSet() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        ListingMipsMeasure measure1 = getMipsMeasure(1L, 1L, "EH-CAH", 1L, "G1");
        ListingMipsMeasure measure2 = getMipsMeasure(2L, 2L, "EP", 2L, "G2");
        ListingMipsMeasure measure3 = getMipsMeasure(3L, 1L, "EH-CAH", 1L, "G1");
        ListingMipsMeasure measure4 = getMipsMeasure(4L, 2L, "EP", 1L, "G1");

        listing.getMipsMeasures().add(measure1);
        listing.getMipsMeasures().add(measure2);
        listing.getMipsMeasures().add(measure3);
        listing.getMipsMeasures().add(measure4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_MSG, "G1", "EH-CAH")))
                .count());
        assertEquals(3, listing.getMipsMeasures().size());
    }

    private ListingMipsMeasure getMipsMeasure(Long id, Long measureId, String measureName,
            Long typeId, String typeName) {
        return ListingMipsMeasure.builder()
            .id(id)
            .measure(MipsMeasure.builder()
                    .id(measureId)
                    .name(measureName)
                    .abbreviation(measureName)
                    .build())
            .measurementType(MipsMeasurementType.builder()
                    .id(typeId)
                    .name(typeName)
                    .build())
            .associatedCriteria(new LinkedHashSet<CertificationCriterion>())
            .build();
    }
}