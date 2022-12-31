package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureDomain;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class MeasureReviewerTest {
    private static final String MEASURE_NOT_FOUND = "%s Measure '%s' was not found %s and has been removed from the listing.";
    private static final String INVALID_MEASURE_TYPE = "Invalid G1/G2 Measure Type: '%s' was not found.";
    private static final String MISSING_MEASURE_TYPE = "G1/G2 Measure type is missing.";
    private static final String MISSING_G1_MEASURES = "Listing has attested to (g)(1), but no measures have been successfully tested for (g)(1).";
    private static final String MISSING_G2_MEASURES = "Listing has attested to (g)(2), but no measures have been successfully tested for (g)(2).";
    private static final String MISSING_ASSOCIATED_CRITERIA = "The %s measure %s for %s must have at least one associated criterion.";
    private static final String MISSING_REQUIRED_CRITERIA = "The %s measure %s for %s is missing required criterion %s.";
    private static final String NOT_ALLOWED_ASSOCIATED_CRITERIA = "The %s measure %s for %s cannot have associated criterion %s.";
    private static final String INVALID_ASSOCIATED_CRITERIA = "The %s measure %s for %s has an invalid associated criterion %s.";
    private static final String REMOVED_MEASURE_WITHOUT_ICS = "The %s Measure: %s for %s may not be referenced since this listing does not have ICS. The measure has been removed.";

    private CertificationCriterionService criterionService;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private MeasureReviewer reviewer;

    @Before
    public void setup() {
        criterionService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criterionService.getByNumber(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(buildCriterion(1L, "170.315 (a)(1)")).collect(Collectors.toList()));
        Mockito.when(criterionService.getByNumber(ArgumentMatchers.eq("170.315 (g)(1)")))
            .thenReturn(Stream.of(buildCriterion(1L, "170.315 (g)(1)"),
                    buildCuresCriterion(2L, "170.315 (g)(1)")).collect(Collectors.toList()));

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.measureNotFoundAndRemoved"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MEASURE_NOT_FOUND, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.invalidMeasureType"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_MEASURE_TYPE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.missingMeasureType")))
            .thenAnswer(i -> MISSING_MEASURE_TYPE);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.missingG1Measures")))
            .thenAnswer(i -> String.format(MISSING_G1_MEASURES));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.missingG2Measures")))
            .thenAnswer(i -> String.format(MISSING_G2_MEASURES));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.measure.missingAssociatedCriteria"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_ASSOCIATED_CRITERIA, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.measure.missingRequiredCriterion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_REQUIRED_CRITERIA, i.getArgument(1), i.getArgument(2), i.getArgument(3), i.getArgument(4)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.measure.associatedCriterionNotAllowed"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(NOT_ALLOWED_ASSOCIATED_CRITERIA, i.getArgument(1), i.getArgument(2), i.getArgument(3), i.getArgument(4)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.measure.invalidAssociatedCriterion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ASSOCIATED_CRITERIA, i.getArgument(1), i.getArgument(2), i.getArgument(3), i.getArgument(4)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.removedMeasureNoIcs"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(REMOVED_MEASURE_WITHOUT_ICS, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        validationUtils = Mockito.mock(ValidationUtils.class);
        Mockito.when(validationUtils.hasCert(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
            .thenCallRealMethod();

        reviewer = new MeasureReviewer(validationUtils, msgUtil);
    }

    @Test
    public void review_listinguNullMeasures_noError() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setMeasures(null);

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_listingEmptyMeasures_noError() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMeasures().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_listingAttestsG1NoMeasures_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult g1Result = CertificationResult.builder()
                .id(1L)
                .criterion(CertificationCriterion.builder()
                        .id(10L)
                        .number("170.315 (g)(1)")
                        .build())
                .success(true)
                .build();
        listing.getCertificationResults().add(g1Result);
        listing.getMeasures().clear();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_G1_MEASURES));
    }

    @Test
    public void review_listingAttestsG1HasG2Measures_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult g1Result = CertificationResult.builder()
                .id(1L)
                .criterion(CertificationCriterion.builder()
                        .id(10L)
                        .number("170.315 (g)(1)")
                        .build())
                .success(true)
                .build();
        listing.getCertificationResults().add(g1Result);
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .allowedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                        .build())
                .measureType(MeasureType.builder()
                        .id(2L)
                        .name("G2")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_G1_MEASURES));
    }

    @Test
    public void review_listingAttestsG2NoMeasures_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult g1Result = CertificationResult.builder()
                .id(1L)
                .criterion(CertificationCriterion.builder()
                        .id(11L)
                        .number("170.315 (g)(2)")
                        .build())
                .success(true)
                .build();
        listing.getCertificationResults().add(g1Result);
        listing.getMeasures().clear();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_G2_MEASURES));
    }

    @Test
    public void review_listingAttestsG2HasG1Measures_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult g1Result = CertificationResult.builder()
                .id(1L)
                .criterion(CertificationCriterion.builder()
                        .id(11L)
                        .number("170.315 (g)(2)")
                        .build())
                .success(true)
                .build();
        listing.getCertificationResults().add(g1Result);
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .allowedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_G2_MEASURES));
    }

    @Test
    public void review_mipsMeasureWithNameNoId_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .name("m1")
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());
        assertEquals(1, listing.getMeasures().size());

        reviewer.review(listing);
        assertEquals(0, listing.getMeasures().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(MEASURE_NOT_FOUND, "G1", "m1", "associated with 170.315 (a)(1)")));
    }

    @Test
    public void review_mipsMeasureWithDomainNoId_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .domain(MeasureDomain.builder()
                                .name("m1")
                                .build())
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());
        assertEquals(1, listing.getMeasures().size());

        reviewer.review(listing);
        assertEquals(0, listing.getMeasures().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(MEASURE_NOT_FOUND, "G1", "m1", "associated with 170.315 (a)(1)")));
    }

    @Test
    public void review_mipsMeasureWithDomainAndRtNoId_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .domain(MeasureDomain.builder()
                                .name("EC")
                                .build())
                        .abbreviation("RT1")
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());
        assertEquals(1, listing.getMeasures().size());

        reviewer.review(listing);
        assertEquals(0, listing.getMeasures().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(MEASURE_NOT_FOUND, "G1", "EC + RT1", "associated with 170.315 (a)(1)")));
    }

    @Test
    public void review_mipsMeasureWithRtNoId_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .domain(null)
                        .abbreviation("RT1")
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());
        assertEquals(1, listing.getMeasures().size());

        reviewer.review(listing);
        assertEquals(0, listing.getMeasures().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(MEASURE_NOT_FOUND, "G1", "RT1", "associated with 170.315 (a)(1)")));
    }

    @Test
    public void review_mipsMeasureWithDomainNoIdNoCriteria_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .domain(MeasureDomain.builder()
                                .name("m1")
                                .build())
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .build());
        assertEquals(1, listing.getMeasures().size());

        reviewer.review(listing);
        assertEquals(0, listing.getMeasures().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(MEASURE_NOT_FOUND, "G1", "m1", "")));
    }

    @Test
    public void review_noMeasureTypeId_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .build())
                .measureType(MeasureType.builder()
                        .name("BOGUS")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_MEASURE_TYPE, "BOGUS")));
    }

    @Test
    public void review_nullMeasureType_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .build())
                .measureType(null)
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_MEASURE_TYPE)));
    }

    @Test
    public void review_nullMeasureTypeName_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .build())
                .measureType(MeasureType.builder()
                        .name(null)
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_MEASURE_TYPE)));
    }

    @Test
    public void review_emptyMeasureTypeName_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .build())
                .measureType(MeasureType.builder()
                        .name("")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_MEASURE_TYPE)));
    }

    @Test
    public void review_noAssociatedCriteria_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .abbreviation("T")
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_ASSOCIATED_CRITERIA, "G1", "Test", "T")));
    }

    @Test
    public void review_missingRequiredAssociatedCriteria_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        Set<CertificationCriterion> allowedCriterion = new LinkedHashSet<CertificationCriterion>();
        allowedCriterion.add(CertificationCriterion.builder()
                .id(1L)
                .number("170.315 (a)(1)")
                .build());
        allowedCriterion.add(CertificationCriterion.builder()
                .id(2L)
                .number("170.315 (a)(2)")
                .build());
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .abbreviation("T")
                        .requiresCriteriaSelection(false)
                        .allowedCriteria(allowedCriterion)
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(2L, "170.315 (a)(2)"))
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_REQUIRED_CRITERIA, "G1", "Test", "T", "170.315 (a)(1)")));
    }

    @Test
    public void review_associatesNotAllowedCriteria_errorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        Set<CertificationCriterion> associatedCriterion = new LinkedHashSet<CertificationCriterion>();
        associatedCriterion.add(CertificationCriterion.builder()
                .id(1L)
                .number("170.315 (a)(1)")
                .build());
        associatedCriterion.add(CertificationCriterion.builder()
                .id(2L)
                .number("170.315 (a)(2)")
                .build());
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .abbreviation("T")
                        .requiresCriteriaSelection(false)
                        .allowedCriteria(buildCriterionSet(2L, "170.315 (a)(2)"))
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(associatedCriterion)
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(NOT_ALLOWED_ASSOCIATED_CRITERIA, "G1", "Test", "T", "170.315 (a)(1)")));
    }

    @Test
    public void review_associatesInvalidCriterion_hasWarningAndRemovesCriterion() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        Set<CertificationCriterion> associatedCriterion = new LinkedHashSet<CertificationCriterion>();
        associatedCriterion.add(CertificationCriterion.builder()
                .id(null)
                .number("a1")
                .build());
        associatedCriterion.add(CertificationCriterion.builder()
                .id(null)
                .number("junk")
                .build());
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .abbreviation("T")
                        .requiresCriteriaSelection(false)
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(associatedCriterion)
                .build());

        reviewer.review(listing);

        assertEquals(2, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(INVALID_ASSOCIATED_CRITERIA, "G1", "Test", "T", "a1")));
        assertTrue(listing.getWarningMessages().contains(
                String.format(INVALID_ASSOCIATED_CRITERIA, "G1", "Test", "T", "junk")));
        assertEquals(0, listing.getMeasures().get(0).getAssociatedCriteria().size());
    }

    @Test
    public void review_missingAssociatedCuresCriterion_hasErrorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult g1Result = CertificationResult.builder()
                .id(1L)
                .criterion(buildCriterion(1L, "170.315 (g)(1)"))
                .success(true)
                .build();
        listing.getCertificationResults().add(g1Result);
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .abbreviation("T")
                        .allowedCriteria(Stream.of(buildCriterion(1L, "170.315 (g)(1)"), buildCuresCriterion(2L, "170.315 (g)(1)")).collect(Collectors.toSet()))
                        .requiresCriteriaSelection(false)
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(Stream.of(buildCriterion(1L, "170.315 (g)(1)")).collect(Collectors.toSet()))
                .build());
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_REQUIRED_CRITERIA, "G1", "Test", "T", "170.315 (g)(1) (Cures Update)")));
    }

    @Test
    public void review_missingAssociatedLegacyCriterion_hasErrorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult g1Result = CertificationResult.builder()
                .id(1L)
                .criterion(buildCriterion(1L, "170.315 (g)(1)"))
                .success(true)
                .build();
        listing.getCertificationResults().add(g1Result);
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .abbreviation("T")
                        .allowedCriteria(Stream.of(buildCriterion(1L, "170.315 (g)(1)"), buildCuresCriterion(2L, "170.315 (g)(1)")).collect(Collectors.toSet()))
                        .requiresCriteriaSelection(false)
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(Stream.of(buildCuresCriterion(2L, "170.315 (g)(1)")).collect(Collectors.toSet()))
                .build());
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_REQUIRED_CRITERIA, "G1", "Test", "T", "170.315 (g)(1)")));
    }

    @Test
    public void review_listingHasRemovedMeasuresFalseIcs_hasError() throws ParseException {

        CertificationResult g1Result = CertificationResult.builder()
                .id(1L)
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (g)(1)")
                        .build())
                .success(true)
                .build();
        ListingMeasure measure = ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .abbreviation("T")
                        .allowedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                        .removed(true)
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .certificationResult(g1Result)
                .measures(Stream.of(measure).collect(Collectors.toList()))
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(REMOVED_MEASURE_WITHOUT_ICS, "G1", "Test", "T")));
    }

    @Test
    public void review_listingHasRemovedMeasuresNullIcs_hasError() throws ParseException {

        CertificationResult g1Result = CertificationResult.builder()
                .id(1L)
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (g)(1)")
                        .build())
                .success(true)
                .build();
        ListingMeasure measure = ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .abbreviation("T")
                        .allowedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                        .removed(true)
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(g1Result)
                .measures(Stream.of(measure).collect(Collectors.toList()))
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(REMOVED_MEASURE_WITHOUT_ICS, "G1", "Test", "T")));
    }

    @Test
    public void review_listingHasRemovedMeasuresWithIcs_noError() throws ParseException {

        CertificationResult g1Result = CertificationResult.builder()
                .id(1L)
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (g)(1)")
                        .build())
                .success(true)
                .build();
        ListingMeasure measure = ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .allowedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                        .removed(true)
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .certificationResult(g1Result)
                .measures(Stream.of(measure).collect(Collectors.toList()))
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_hasAssociatedCuresAndLegacyCriterion_noErrorMessage() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult g1Result = CertificationResult.builder()
                .id(1L)
                .criterion(buildCriterion(1L, "170.315 (g)(1)"))
                .success(true)
                .build();
        listing.getCertificationResults().add(g1Result);
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .abbreviation("T")
                        .allowedCriteria(Stream.of(buildCriterion(1L, "170.315 (g)(1)"), buildCuresCriterion(2L, "170.315 (g)(1)")).collect(Collectors.toSet()))
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(Stream.of(buildCriterion(1L, "170.315 (g)(1)"), buildCuresCriterion(2L, "170.315 (g)(1)")).collect(Collectors.toSet()))
                .build());
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_listingHasValidMeasures_noError() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult g1Result = CertificationResult.builder()
                .id(1L)
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (g)(1)")
                        .build())
                .success(true)
                .build();
        listing.getCertificationResults().add(g1Result);
        listing.getMeasures().add(ListingMeasure.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .allowedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    private Set<CertificationCriterion> buildCriterionSet(Long id, String number) {
        Set<CertificationCriterion> critSet = new LinkedHashSet<CertificationCriterion>();
        critSet.add(buildCriterion(id, number));
        return critSet;
    }

    private CertificationCriterion buildCriterion(Long id, String number) {
        return CertificationCriterion.builder()
        .id(id)
        .number(number)
        .build();
    }

    private CertificationCriterion buildCuresCriterion(Long id, String number) {
        return CertificationCriterion.builder()
        .id(id)
        .number(number)
        .title("(Cures Update)")
        .build();
    }
}
