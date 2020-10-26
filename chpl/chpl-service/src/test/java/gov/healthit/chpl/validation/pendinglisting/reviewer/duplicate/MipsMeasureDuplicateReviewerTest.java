package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.MipsMeasure;
import gov.healthit.chpl.domain.MipsMeasurementType;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductMipsMeasureDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.MipsMeasureDuplicateReviewer;

public class MipsMeasureDuplicateReviewerTest {
    private static final String DUPLICATE_MSG =
            "Duplicate %s Measure: %s for %s was found. The duplicates have been removed.";
    private static final String MEASURE_NAME = "Patient-Specific Education: Eligible Professional";
    private static final String MEASURE_NAME_2 = "Secure Electronic Messaging: Eligible Clinician";
    private static final String RT3 = "RT3";
    private static final String RT5 = "RT5";

    private ErrorMessageUtil msgUtil;
    private MipsMeasureDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateMipsMeasure"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DUPLICATE_MSG, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        reviewer = new MipsMeasureDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductMipsMeasureDTO measure1 = getMipsMeasure(1L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        PendingCertifiedProductMipsMeasureDTO measure2 = getMipsMeasure(2L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        listing.getMipsMeasures().add(measure1);
        listing.getMipsMeasures().add(measure2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_MSG, "G1", MEASURE_NAME, RT3)))
                .count());
        assertEquals(1, listing.getMipsMeasures().size());
    }

    @Test
    public void review_duplicateExistsButDifferentCriteria_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductMipsMeasureDTO measure1 = getMipsMeasure(1L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        measure1.getAssociatedCriteria().add(CertificationCriterion.builder()
                .id(1L)
                .number("170.315 (a)(1)")
                .build());
        PendingCertifiedProductMipsMeasureDTO measure2 = getMipsMeasure(2L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        measure2.getAssociatedCriteria().add(CertificationCriterion.builder()
                .id(2L)
                .number("170.315 (a)(2)")
                .build());
        listing.getMipsMeasures().add(measure1);
        listing.getMipsMeasures().add(measure2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_MSG, "G1", MEASURE_NAME, RT3)))
                .count());
        assertEquals(1, listing.getMipsMeasures().size());
    }

    @Test
    public void review_differentTypes_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductMipsMeasureDTO measure1 = getMipsMeasure(1L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        PendingCertifiedProductMipsMeasureDTO measure2 = getMipsMeasure(2L, 1L, MEASURE_NAME, RT3, 2L, "G2");
        listing.getMipsMeasures().add(measure1);
        listing.getMipsMeasures().add(measure2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getMipsMeasures().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductMipsMeasureDTO measure1 = getMipsMeasure(1L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        PendingCertifiedProductMipsMeasureDTO measure2 = getMipsMeasure(2L, 2L, MEASURE_NAME_2, RT5, 2L, "G2");
        listing.getMipsMeasures().add(measure1);
        listing.getMipsMeasures().add(measure2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getMipsMeasures().size());
    }

    @Test
    public void review_emptyMipsMeasures_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        listing.getMipsMeasures().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getMipsMeasures().size());
    }

    @Test
    public void testDuplicateExistInLargerSet() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductMipsMeasureDTO measure1 = getMipsMeasure(1L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        PendingCertifiedProductMipsMeasureDTO measure2 = getMipsMeasure(2L, 2L, MEASURE_NAME_2, RT5, 2L, "G2");
        PendingCertifiedProductMipsMeasureDTO measure3 = getMipsMeasure(3L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        PendingCertifiedProductMipsMeasureDTO measure4 = getMipsMeasure(4L, 2L, MEASURE_NAME_2, RT5, 1L, "G1");

        listing.getMipsMeasures().add(measure1);
        listing.getMipsMeasures().add(measure2);
        listing.getMipsMeasures().add(measure3);
        listing.getMipsMeasures().add(measure4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_MSG, "G1", MEASURE_NAME, RT3)))
                .count());
        assertEquals(3, listing.getMipsMeasures().size());
    }

    private PendingCertifiedProductMipsMeasureDTO getMipsMeasure(Long id, Long measureId, String measureName,
            String rtAbbrev, Long typeId, String typeName) {
        return PendingCertifiedProductMipsMeasureDTO.builder()
            .id(id)
            .measure(MipsMeasure.builder()
                    .id(measureId)
                    .name(measureName)
                    .abbreviation(rtAbbrev)
                    .build())
            .measurementType(MipsMeasurementType.builder()
                    .id(typeId)
                    .name(typeName)
                    .build())
            .associatedCriteria(new LinkedHashSet<CertificationCriterion>())
            .build();
    }
}
