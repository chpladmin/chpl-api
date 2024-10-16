package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class MeasureDuplicateReviewerTest {
    private static final String DUPLICATE_MSG_ERROR =
            "Duplicate %s Measure: %s for %s was found. The measure must be associated with all of the relevant criteria as a single element.";
    private static final String DUPLICATE_MSG_WARNING =
            "Duplicate %s Measure: %s for %s was found with the same relevant criteria. The duplicates have been removed.";
    private static final String MEASURE_NAME = "Patient-Specific Education: Eligible Professional";
    private static final String MEASURE_NAME_2 = "Secure Electronic Messaging: Eligible Clinician";
    private static final String RT3 = "RT3";
    private static final String RT5 = "RT5";

    private ErrorMessageUtil msgUtil;
    private MeasureDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateMeasure.differentCriteria"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DUPLICATE_MSG_ERROR, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateMeasure.sameCriteria"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DUPLICATE_MSG_WARNING, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        reviewer = new MeasureDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        ListingMeasure measure1 = getMeasure(1L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        ListingMeasure measure2 = getMeasure(2L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        listing.getMeasures().add(measure1);
        listing.getMeasures().add(measure2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(msg -> msg.equals(String.format(DUPLICATE_MSG_WARNING, "G1", MEASURE_NAME, RT3)))
                .count());
        assertEquals(1, listing.getMeasures().size());
    }

    @Test
    public void review_duplicateExistsButDifferentCriteria_errorFound() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationCriterion a1Criterion = CertificationCriterion.builder()
                .id(1L)
                .number("170.315 (a)(1)")
                .build();
        CertificationCriterion a2Criterion = CertificationCriterion.builder()
                .id(2L)
                .number("170.315 (a)(2)")
                .build();
        ListingMeasure measure1 = getMeasure(1L, 1L, MEASURE_NAME, RT3, 1L, "G1", a1Criterion);
        ListingMeasure measure2 = getMeasure(2L, 1L, MEASURE_NAME, RT3, 1L, "G1", a2Criterion);
        listing.getMeasures().add(measure1);
        listing.getMeasures().add(measure2);

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertEquals(1, listing.getErrorMessages().stream()
                .filter(msg -> msg.equals(String.format(DUPLICATE_MSG_ERROR, "G1", MEASURE_NAME, RT3)))
                .count());
        assertEquals(2, listing.getMeasures().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_duplicateExistsWithSameCriteria_warningFound() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationCriterion a1Criterion = CertificationCriterion.builder()
                .id(1L)
                .number("170.315 (a)(1)")
                .build();
        ListingMeasure measure1 = getMeasure(1L, 1L, MEASURE_NAME, RT3, 1L, "G1", a1Criterion);
        ListingMeasure measure2 = getMeasure(2L, 1L, MEASURE_NAME, RT3, 1L, "G1", a1Criterion);
        listing.getMeasures().add(measure1);
        listing.getMeasures().add(measure2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(msg -> msg.equals(String.format(DUPLICATE_MSG_WARNING, "G1", MEASURE_NAME, RT3)))
                .count());
        assertEquals(1, listing.getMeasures().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_differentTypes_noError() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        ListingMeasure measure1 = getMeasure(1L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        ListingMeasure measure2 = getMeasure(2L, 1L, MEASURE_NAME, RT3, 2L, "G2");
        listing.getMeasures().add(measure1);
        listing.getMeasures().add(measure2);

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(2, listing.getMeasures().size());
    }

    @Test
    public void review_noDuplicates_noError() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        ListingMeasure measure1 = getMeasure(1L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        ListingMeasure measure2 = getMeasure(2L, 2L, MEASURE_NAME_2, RT5, 2L, "G2");
        listing.getMeasures().add(measure1);
        listing.getMeasures().add(measure2);

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(2, listing.getMeasures().size());
    }

    @Test
    public void review_emptyMeasures_noError() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMeasures().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getMeasures().size());
    }

    @Test
    public void testDuplicateExistInLargerSet() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        ListingMeasure measure1 = getMeasure(1L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        ListingMeasure measure2 = getMeasure(2L, 2L, MEASURE_NAME_2, RT5, 2L, "G2");
        ListingMeasure measure3 = getMeasure(3L, 1L, MEASURE_NAME, RT3, 1L, "G1");
        ListingMeasure measure4 = getMeasure(4L, 2L, MEASURE_NAME_2, RT5, 1L, "G1");

        listing.getMeasures().add(measure1);
        listing.getMeasures().add(measure2);
        listing.getMeasures().add(measure3);
        listing.getMeasures().add(measure4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(msg -> msg.equals(String.format(DUPLICATE_MSG_WARNING, "G1", MEASURE_NAME, RT3)))
                .count());
        assertEquals(3, listing.getMeasures().size());
    }

    private ListingMeasure getMeasure(Long id, Long measureId, String measureName,
            String rtAbbrev, Long typeId, String typeName) {
        return ListingMeasure.builder()
            .id(id)
            .measure(Measure.builder()
                    .id(measureId)
                    .name(measureName)
                    .abbreviation(rtAbbrev)
                    .build())
            .measureType(MeasureType.builder()
                    .id(typeId)
                    .name(typeName)
                    .build())
            .build();
    }

    private ListingMeasure getMeasure(Long id, Long measureId, String measureName,
            String rtAbbrev, Long typeId, String typeName, CertificationCriterion criterion) {
        LinkedHashSet<CertificationCriterion> criteria = new LinkedHashSet<CertificationCriterion>();
        criteria.add(criterion);

        return ListingMeasure.builder()
            .id(id)
            .measure(Measure.builder()
                    .id(measureId)
                    .name(measureName)
                    .abbreviation(rtAbbrev)
                    .build())
            .measureType(MeasureType.builder()
                    .id(typeId)
                    .name(typeName)
                    .build())
            .associatedCriteria(criteria)
            .build();
    }
}
