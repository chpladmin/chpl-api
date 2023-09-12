package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class OptionalStandardReviewerTest {
    private static final String OPTIONAL_STANDARDS_NOT_APPLICABLE = "Optional Standards are not applicable for the criterion %s.";
    private static final String OPTIONAL_STANDARD_NOT_FOUND = "Criteria %s contains an optional standard '%s' which does not exist.";
    private static final String OPTIONAL_STANDARD_NAME_MISSING = "There was no optional standard name found for certification criteria %s.";
    private static final String OPTIONAL_STANDARD_NOT_FOR_CRITERION = "Optional Standard %s is not valid for criteria %s.";

    private OptionalStandardDAO optionalStandardDao;
    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;
    private OptionalStandardReviewer reviewer;
    private CertificationEdition edition2015;

    @Before
    public void before() throws EntityRetrievalException {
        edition2015 = CertificationEdition.builder()
                .id(3L)
                .name("2015")
                .build();
        optionalStandardDao = Mockito.mock(OptionalStandardDAO.class);
        try {
            Mockito.when(optionalStandardDao.getAllOptionalStandardCriteriaMap())
                .thenReturn(buildOptionalStandardCriteriaMaps());
        } catch (EntityRetrievalException ex) {
            fail("Could not intiialize optional standard criteria maps");
        }

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        certResultRules = Mockito.mock(CertificationResultRules.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.optionalStandardsNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(OPTIONAL_STANDARDS_NOT_APPLICABLE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.optionalStandardNotFound"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(OPTIONAL_STANDARD_NOT_FOUND, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingOptionalStandardName"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(OPTIONAL_STANDARD_NAME_MISSING, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.optionalStandard.invalidCriteria"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(OPTIONAL_STANDARD_NOT_FOR_CRITERION, i.getArgument(1), i.getArgument(2)));
        reviewer = new OptionalStandardReviewer(optionalStandardDao, certResultRules,
                new ValidationUtils(Mockito.mock(CertificationCriterionService.class)),
                msgUtil);
    }

    private List<OptionalStandardCriteriaMap> buildOptionalStandardCriteriaMaps() {
        List<OptionalStandardCriteriaMap> maps = new ArrayList<OptionalStandardCriteriaMap>();
        maps.add(OptionalStandardCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                .id(1L)
                .optionalStandard(OptionalStandard.builder()
                        .id(1L)
                        .citation("optional std")
                        .build())
                .build());
        maps.add(OptionalStandardCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(2)")
                                .build())
                .id(2L)
                .optionalStandard(OptionalStandard.builder()
                        .id(2L)
                        .citation("optional std2")
                        .build())
                .build());
        return maps;
    }

    @Test
    public void review_nullOptionalStandards_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .build())
                        .success(true)
                        .build())
                .build();
        listing.getCertificationResults().get(0).setOptionalStandards(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyOptionalStandards_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .build())
                        .success(true)
                        .optionalStandards(new ArrayList<CertificationResultOptionalStandard>())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void optionalStandardsNotApplicableToCriteria_hasWarningAndStandardsSetNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
            .thenReturn(false);
        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .citation("optional std")
                .optionalStandardId(1L)
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .build())
                        .success(true)
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(OPTIONAL_STANDARDS_NOT_APPLICABLE, "170.315 (a)(1)")));
        assertNull(listing.getCertificationResults().get(0).getOptionalStandards());
    }

    @Test
    public void optionalStandardsNotApplicableToRemovedCriteria_noWarnings() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
            .thenReturn(false);
        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .citation("optional std")
                .optionalStandardId(1L)
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .build())
                        .success(true)
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_optionalStandardsWithoutId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
            .thenReturn(true);

        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(1L)
                .citation("optional std")
                .build());
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .citation("bad name")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .build())
                        .success(true)
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(OPTIONAL_STANDARD_NOT_FOUND, "170.315 (a)(1)", "bad name")));
    }

    @Test
    public void review_optionalStandardsWithoutIdForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
            .thenReturn(true);

        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(1L)
                .citation("optional std")
                .build());
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .citation("bad name")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .build())
                        .success(true)
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_optionalStandardWithIdOnInvalidCriterion_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
            .thenReturn(true);

        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(1L)
                .citation("optional std")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(2)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .build())
                        .success(true)
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(OPTIONAL_STANDARD_NOT_FOR_CRITERION, "optional std", "170.315 (a)(2)")));
    }

    @Test
    public void review_optionalStandardWithIdOnInvalidRemovedCriterion_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
            .thenReturn(true);

        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(1L)
                .citation("optional std")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(2)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .build())
                        .success(true)
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_optionalStandardWithoutCitationAndWithoutId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
            .thenReturn(true);

        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(1L)
                .citation("optional std")
                .build());
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(null)
                .citation("")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .build())
                        .success(true)
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(OPTIONAL_STANDARD_NAME_MISSING, "170.315 (a)(1)", "")));
    }

    @Test
    public void review_optionalStandardWithoutCitationAndWithoutIdForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
            .thenReturn(true);

        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(1L)
                .citation("optional std")
                .build());
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(null)
                .citation("")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .build())
                        .success(true)
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_optionalStandardsWithoutCitationWithId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
        .thenReturn(true);

        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(1L)
                .citation("optional std")
                .build());
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(2L)
                .citation("")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .build())
                        .success(true)
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(OPTIONAL_STANDARD_NAME_MISSING, "170.315 (a)(1)", "")));
        assertTrue(listing.getErrorMessages().contains(
                String.format(OPTIONAL_STANDARD_NOT_FOR_CRITERION, "", "170.315 (a)(1)")));
    }

    @Test
    public void review_optionalStandardsWithoutCitationWithIdForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
        .thenReturn(true);

        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(1L)
                .citation("optional std")
                .build());
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(2L)
                .citation("")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .build())
                        .success(true)
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_validOptionalStandard_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
            .thenReturn(true);

        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        optionalStandards.add(CertificationResultOptionalStandard.builder()
                .optionalStandardId(1L)
                .citation("optional std")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .build())
                        .success(true)
                        .optionalStandards(optionalStandards)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }
}
