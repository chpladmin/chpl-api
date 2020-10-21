package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMipsMeasure;
import gov.healthit.chpl.domain.MipsMeasure;
import gov.healthit.chpl.domain.MipsMeasurementType;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.MipsMeasureValidityReviewer;

public class MipsMeasureValidityReviewerTest {
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private MipsMeasureValidityReviewer reviewer;

    @Before
    public void setup() {
        validationUtils = Mockito.mock(ValidationUtils.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        reviewer = new MipsMeasureValidityReviewer(validationUtils, msgUtil);
    }

    @Test
    public void review_listingAttestsG1NoMeasures_errorMessage() throws ParseException {
        Mockito.when(validationUtils.hasCert(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
            .thenCallRealMethod();
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.missingG1Measures")))
                .thenAnswer(i -> String.format(
                        "Listing has attested to (g)(1), but no measures have been successfully tested for (g)(1)."));

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
        listing.getMipsMeasures().clear();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                "Listing has attested to (g)(1), but no measures have been successfully tested for (g)(1)."));
    }

    @Test
    public void review_listingAttestsG1HasG2Measures_errorMessage() throws ParseException {
        Mockito.when(validationUtils.hasCert(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
            .thenCallRealMethod();
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.missingG1Measures")))
                .thenAnswer(i -> String.format(
                        "Listing has attested to (g)(1), but no measures have been successfully tested for (g)(1)."));

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
        listing.getMipsMeasures().add(ListingMipsMeasure.builder()
                .measure(MipsMeasure.builder()
                        .id(1L)
                        .name("Test")
                        .allowedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                        .build())
                .measurementType(MipsMeasurementType.builder()
                        .id(2L)
                        .name("G2")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                "Listing has attested to (g)(1), but no measures have been successfully tested for (g)(1)."));
    }

    @Test
    public void review_listingAttestsG2NoMeasures_errorMessage() throws ParseException {
        Mockito.when(validationUtils.hasCert(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
            .thenCallRealMethod();
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.missingG2Measures")))
                .thenAnswer(i -> String.format(
                        "Listing has attested to (g)(2), but no measures have been successfully tested for (g)(2)."));

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
        listing.getMipsMeasures().clear();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                "Listing has attested to (g)(2), but no measures have been successfully tested for (g)(2)."));
    }

    @Test
    public void review_listingAttestsG2HasG1Measures_errorMessage() throws ParseException {
        Mockito.when(validationUtils.hasCert(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
            .thenCallRealMethod();
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.missingG2Measures")))
                .thenAnswer(i -> String.format(
                        "Listing has attested to (g)(2), but no measures have been successfully tested for (g)(2)."));

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
        listing.getMipsMeasures().add(ListingMipsMeasure.builder()
                .measure(MipsMeasure.builder()
                        .id(1L)
                        .name("Test")
                        .allowedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                        .build())
                .measurementType(MipsMeasurementType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                "Listing has attested to (g)(2), but no measures have been successfully tested for (g)(2)."));
    }

    @Test
    public void review_noMeasureId_errorMessage() throws ParseException {
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.invalidMipsMeasure"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Invalid G1/G2 Measure: '%s' was not found.", i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMipsMeasures().add(ListingMipsMeasure.builder()
                .measure(MipsMeasure.builder()
                        .name("Test")
                        .build())
                .measurementType(MipsMeasurementType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains("Invalid G1/G2 Measure: 'Test' was not found."));
    }

    @Test
    public void review_noMeasurementTypeId_errorMessage() throws ParseException {
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.invalidMipsMeasureType"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Invalid G1/G2 Measure Type: '%s' was not found.", i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMipsMeasures().add(ListingMipsMeasure.builder()
                .measure(MipsMeasure.builder()
                        .id(1L)
                        .name("Test")
                        .build())
                .measurementType(MipsMeasurementType.builder()
                        .name("BOGUS")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains("Invalid G1/G2 Measure Type: 'BOGUS' was not found."));
    }

    @Test
    public void review_noAssociatedCriteria_errorMessage() throws ParseException {
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.mipsMeasure.missingAssociatedCriteria"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("The %s measure %s must have at least one associated criterion.",
                        i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getMipsMeasures().add(ListingMipsMeasure.builder()
                .measure(MipsMeasure.builder()
                        .id(1L)
                        .name("Test")
                        .build())
                .measurementType(MipsMeasurementType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains("The G1 measure Test must have at least one associated criterion."));
    }

    @Test
    public void review_missingRequiredAssociatedCriteria_errorMessage() throws ParseException {
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.mipsMeasure.missingRequiredCriterion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("The criterion %s is missing for %s measure %s.",
                        i.getArgument(1), i.getArgument(2), i.getArgument(3)));

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
        listing.getMipsMeasures().add(ListingMipsMeasure.builder()
                .measure(MipsMeasure.builder()
                        .id(1L)
                        .name("Test")
                        .requiresCriteriaSelection(false)
                        .allowedCriteria(allowedCriterion)
                        .build())
                .measurementType(MipsMeasurementType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(2L, "170.315 (a)(2)"))
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains("The criterion 170.315 (a)(1) is missing for G1 measure Test."));
    }

    @Test
    public void review_associatesNotAllowedCriteria_errorMessage() throws ParseException {
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.mipsMeasure.associatedCriterionNotAllowed"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("The criterion %s cannot be associated with %s measure %s.",
                        i.getArgument(1), i.getArgument(2), i.getArgument(3)));

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
        listing.getMipsMeasures().add(ListingMipsMeasure.builder()
                .measure(MipsMeasure.builder()
                        .id(1L)
                        .name("Test")
                        .requiresCriteriaSelection(false)
                        .allowedCriteria(buildCriterionSet(2L, "170.315 (a)(2)"))
                        .build())
                .measurementType(MipsMeasurementType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(associatedCriterion)
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                "The criterion 170.315 (a)(1) cannot be associated with G1 measure Test."));
    }

    @Test
    public void review_listingHasValidMeasures_noErrorMessage() throws ParseException {
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
        listing.getMipsMeasures().add(ListingMipsMeasure.builder()
                .measure(MipsMeasure.builder()
                        .id(1L)
                        .name("Test")
                        .allowedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                        .build())
                .measurementType(MipsMeasurementType.builder()
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
        critSet.add(CertificationCriterion.builder()
                .id(id)
                .number(number)
                .build());
        return critSet;
    }
}
