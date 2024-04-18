package gov.healthit.chpl.search;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriteriaManager;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.compliance.directreview.DirectReviewSearchService;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.search.domain.ComplianceSearchFilter;
import gov.healthit.chpl.search.domain.NonConformitySearchOptions;
import gov.healthit.chpl.search.domain.OrderByOption;
import gov.healthit.chpl.search.domain.RwtSearchOptions;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SearchRequestValidatorTest {
    private static final String INVALID_LISTING_ID_FORMAT = "Listing ID %s is invalid. It must be a positive whole number.";
    private static final String TOO_MANY_LISTING_IDS = "There are %s listing IDs included in the search request. The maximum allowed is %s.";
    private static final String INVALID_CERTIFICATION_STATUS = "Could not find certification status with value '%s'.";
    private static final String INVALID_CERTIFICATION_EDITION = "Could not find certification edition with value '%s'.";
    private static final String INVALID_DERIVED_CERTIFICATION_EDITION = "Could not find derived certification edition with value '%s'.";
    private static final String INVALID_CERTIFICATION_CRITERION = "Could not find certification criterion with value '%s'.";
    private static final String INVALID_CERTIFICATION_CRITERION_FORMAT = "Certification Criterion ID %s is invalid. It must be a positive whole number.";
    private static final String MISSING_CRITERIA_SEARCH_OPERATOR = "Multiple certification criteria were found without a search operator (AND/OR). A search operator is required.";
    private static final String INVALID_OPERATOR = "Invalid search operator value '%s'. Value must be one of %s.";
    private static final String INVALID_CQM = "Could not find CQM with value '%s'.";
    private static final String MISSING_CQM_SEARCH_OPERATOR = "Multiple CQMs were found without a search operator (AND/OR). A search operator is required.";
    private static final String INVALID_ACB = "Could not find certification body with value '%s'.";
    private static final String INVALID_PRACTICE_TYPE = "Could not find practice type with value '%s'.";
    private static final String INVALID_CERTIFICATION_DATE = "Could not parse '%s' as date in the format %s.";
    private static final String MISSING_NC_SEARCH_OPERATOR = "Multiple non-conformity search options were found without a search operator (AND/OR). A search operator is required.";
    private static final String INVALID_NONCONFORMITY_SEARCH_OPTION = "No non-conformity search option matches '%s'. Values must be one of %s.";
    private static final String DIRECT_REVIEWS_UNAVAILABLE = "Compliance and non-conformity filtering is unavailable at this time.";
    private static final String RWT_OPERATOR_MISSING = "Multiple RWT search options were found without a search operator (AND/OR). A search operator is required.";
    private static final String RWT_OPTION_INVALID = "No RWT search option matches '%s'. Values must be one of %s.";
    private static final String INVALID_SVAP = "Could not find SVAP with value '%s'.";
    private static final String INVALID_SVAP_ID_FORMAT = "SVAP ID %s is invalid. It must be a positive whole number.";
    private static final String MISSING_SVAP_OPERATOR = "Multiple SVAPs were found without a search operator (AND/OR). A search operator is required.";
    private static final String INVALID_ORDER_BY = "Order by parameter '%s' is invalid. Value must be one of %s.";

    private DimensionalDataManager dimensionalDataManager;
    private CertificationCriteriaManager certificationCriteriaManager;
    private SvapDAO svapDao;
    private DirectReviewSearchService drService;
    private ErrorMessageUtil msgUtil;
    private SearchRequestValidator validator;

    @Before
    public void setup() {
        dimensionalDataManager = Mockito.mock(DimensionalDataManager.class);
        certificationCriteriaManager = Mockito.mock(CertificationCriteriaManager.class);
        svapDao = Mockito.mock(SvapDAO.class);
        drService = Mockito.mock(DirectReviewSearchService.class);
        Mockito.when(drService.doesCacheHaveAnyOkData()).thenReturn(true);

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.listingId.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_LISTING_ID_FORMAT, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.listingIds.moreThanAllowed"), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt()))
            .thenAnswer(i -> String.format(TOO_MANY_LISTING_IDS, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationStatuses.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CERTIFICATION_STATUS, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationEdition.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CERTIFICATION_EDITION, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.derivedCertificationEdition.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_DERIVED_CERTIFICATION_EDITION, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationCriteria.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CERTIFICATION_CRITERION, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationCriteriaId.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CERTIFICATION_CRITERION_FORMAT, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationCriteria.missingSearchOperator")))
            .thenAnswer(i -> MISSING_CRITERIA_SEARCH_OPERATOR);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.searchOperator.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_OPERATOR, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.cqms.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CQM, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.cqms.missingSearchOperator")))
            .thenAnswer(i -> MISSING_CQM_SEARCH_OPERATOR);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationBodies.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ACB, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.practiceType.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_PRACTICE_TYPE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationDate.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.eq(SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT)))
            .thenAnswer(i -> String.format(INVALID_CERTIFICATION_DATE, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.compliance.missingSearchOperator")))
            .thenAnswer(i -> MISSING_NC_SEARCH_OPERATOR);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.nonconformitySearchOption.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_NONCONFORMITY_SEARCH_OPTION, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complianceFilter.unavailable")))
             .thenAnswer(i -> DIRECT_REVIEWS_UNAVAILABLE);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.rwt.missingSearchOperator")))
            .thenAnswer(i -> RWT_OPERATOR_MISSING);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.rwtOption.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(RWT_OPTION_INVALID, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.svap.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_SVAP, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.svapId.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_SVAP_ID_FORMAT, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.svap.missingSearchOperator")))
            .thenAnswer(i -> MISSING_SVAP_OPERATOR);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.orderBy.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ORDER_BY, i.getArgument(1), i.getArgument(2)));

        validator = new SearchRequestValidator(dimensionalDataManager, certificationCriteriaManager, svapDao, msgUtil);
    }

    @Test
    public void validate_validListingIdFormat_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .listingIdStrings(Stream.of("1", "2").collect(Collectors.toSet()))
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidListingIdFormat_addsError() {
        SearchRequest request = SearchRequest.builder()
            .listingIdStrings(Stream.of("3 ", " 4 ", " 01", " ", "", null, "BAD").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_LISTING_ID_FORMAT, "BAD", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_tooManyListingIdStrings_addsError() {
        SearchRequest request = SearchRequest.builder()
            .listingIdStrings(Stream.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                    "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                    "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
                    "31", "32", "33", "34", "35", "36", "37", "38", "39", "40",
                    "41", "42", "43", "44", "45", "46", "47", "48", "49", "50",
                    "51", "52", "53", "54", "55", "56", "57", "58", "59", "60",
                    "61", "62", "63", "64", "65", "66", "67", "68", "69", "70",
                    "71", "72", "73", "74", "75", "76", "77", "78", "79", "80",
                    "81", "82", "83", "84", "85", "86", "87", "88", "89", "90",
                    "91", "92", "93", "94", "95", "96", "97", "98", "99", "100",
                    "101").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(TOO_MANY_LISTING_IDS, "101", "100")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_tooManyListingIds_addsError() {
        SearchRequest request = SearchRequest.builder()
            .listingIds(Stream.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
                    11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L,
                    21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L,
                    31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L,
                    41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L,
                    51L, 52L, 53L, 54L, 55L, 56L, 57L, 58L, 59L, 60L,
                    61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L, 70L,
                    71L, 72L, 73L, 74L, 75L, 76L, 77L, 78L, 79L, 80L,
                    81L, 82L, 83L, 84L, 85L, 86L, 87L, 88L, 89L, 90L,
                    91L, 92L, 93L, 94L, 95L, 96L, 97L, 98L, 99L, 100L,
                    101L).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(TOO_MANY_LISTING_IDS, "101", "100")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidCertificationStatusNullDimensionalData_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationStatuses(Stream.of("Active").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getCertificationStatuses())
            .thenReturn(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CERTIFICATION_STATUS, "Active", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidCertificationStatusDimensionalDataExists_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationStatuses(Stream.of("Active").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getCertificationStatuses())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Suspended"), new KeyValueModel(2L, "Withdrawn")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CERTIFICATION_STATUS, "Active", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validCertificationStatus_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .certificationStatuses(Stream.of("Active").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getCertificationStatuses())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Active")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidCertificationEditionNullDimensionalData_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationEditions(Stream.of("2021").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getEditionNames(ArgumentMatchers.anyBoolean()))
            .thenReturn(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CERTIFICATION_EDITION, "2021", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidCertificationEditionsDimensionalDataExists_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationEditions(Stream.of("2021").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getEditionNames(ArgumentMatchers.anyBoolean()))
            .thenReturn(Stream.of(new KeyValueModel(1L, "2011"), new KeyValueModel(2L, "2014")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CERTIFICATION_EDITION, "2021", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validCertificationEdition_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .certificationEditions(Stream.of("2014").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getEditionNames(ArgumentMatchers.anyBoolean()))
            .thenReturn(Stream.of(new KeyValueModel(1L, "2014")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidDerivedCertificationEdition_addsError() {
        SearchRequest request = SearchRequest.builder()
            .derivedCertificationEditions(Stream.of("2021").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_DERIVED_CERTIFICATION_EDITION, "2021", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidDerivedCertificationEditions_addsError() {
        SearchRequest request = SearchRequest.builder()
            .derivedCertificationEditions(Stream.of("2016", "2021").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(2, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_DERIVED_CERTIFICATION_EDITION, "2021", "")));
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_DERIVED_CERTIFICATION_EDITION, "2016", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_2011DerivedCertificationEdition_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .derivedCertificationEditions(Stream.of("2011").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_2014DerivedCertificationEdition_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .derivedCertificationEditions(Stream.of("2014").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_2015DerivedCertificationEdition_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .derivedCertificationEditions(Stream.of("2015").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_2015CuresUpdateDerivedCertificationEdition_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .derivedCertificationEditions(Stream.of("2015 Cures Update").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidCertificationCriteriaNullDimensionalData_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationCriteriaIds(Stream.of(1L).collect(Collectors.toSet()))
            .build();
        Mockito.when(certificationCriteriaManager.getAll())
            .thenReturn(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CERTIFICATION_CRITERION, "1", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidCertificationCriteriaDimensionalDataExists_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationCriteriaIds(Stream.of(3L).collect(Collectors.toSet()))
            .build();
        Mockito.when(certificationCriteriaManager.getAll())
            .thenReturn(Stream.of(CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").build(),
                    CertificationCriterion.builder().id(2L).number("170.315 (a)(2)").build())
                    .collect(Collectors.toList()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CERTIFICATION_CRITERION, "3", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validCertificationCriteria_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .certificationCriteriaIds(Stream.of(1L).collect(Collectors.toSet()))
            .build();
        Mockito.when(certificationCriteriaManager.getAll())
        .thenReturn(Stream.of(CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").build(),
                CertificationCriterion.builder().id(2L).number("170.315 (a)(2)").build())
                .collect(Collectors.toList()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidCertificationCriteriaIdFormat_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationCriteriaIdStrings(Stream.of("3 ", " 4 ", " 01", " ", "", null, "BAD").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CERTIFICATION_CRITERION_FORMAT, "BAD", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidCriteriaOperator_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationCriteriaOperator(null)
            .certificationCriteriaOperatorString("XOR")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_OPERATOR, "XOR",
                    Stream.of(SearchSetOperator.values())
                    .map(value -> value.name())
                    .collect(Collectors.joining(",")))));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validCriteriaOperatorParsedFromString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .certificationCriteriaOperator(SearchSetOperator.OR)
            .certificationCriteriaOperatorString("OR")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validCriteriaOperatorWithoutString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .certificationCriteriaOperator(SearchSetOperator.OR)
            .certificationCriteriaOperatorString(null)
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_hasMultipleCriteriaIdsAndMissingCriteriaOperator_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationCriteriaIds(Stream.of(1L, 2L).collect(Collectors.toSet()))
            .build();
        Mockito.when(certificationCriteriaManager.getAll())
            .thenReturn(Stream.of(
                    CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").build(),
                    CertificationCriterion.builder().id(2L).number("170.315 (a)(2)").build())
                .collect(Collectors.toList()));
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(MISSING_CRITERIA_SEARCH_OPERATOR));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidCqmNullDimensionalData_addsError() {
        SearchRequest request = SearchRequest.builder()
            .cqms(Stream.of("CMS1").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getCQMCriterionNumbers(ArgumentMatchers.anyBoolean()))
            .thenReturn(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CQM, "CMS1", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidCqmDimensionalDataExists_addsError() {
        SearchRequest request = SearchRequest.builder()
            .cqms(Stream.of("CMS3").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getCQMCriterionNumbers(ArgumentMatchers.anyBoolean()))
            .thenReturn(Stream.of(new DescriptiveModel(1L, "CMS1", ""), new DescriptiveModel(2L, "CMS2", ""))
                    .collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CQM, "CMS3", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validCqm_noErrors() {
        SearchRequest request = SearchRequest.builder()
                .cqms(Stream.of("CMS1").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getCQMCriterionNumbers(ArgumentMatchers.anyBoolean()))
        .thenReturn(Stream.of(new DescriptiveModel(1L, "CMS1", ""), new DescriptiveModel(2L, "CMS2", ""))
                .collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidCqmsOperator_addsError() {
        SearchRequest request = SearchRequest.builder()
            .cqmsOperator(null)
            .cqmsOperatorString("XOR")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_OPERATOR, "XOR",
                    Stream.of(SearchSetOperator.values())
                    .map(value -> value.name())
                    .collect(Collectors.joining(",")))));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validCqmsOperatorParsedFromString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .cqmsOperator(SearchSetOperator.OR)
            .cqmsOperatorString("OR")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validCqmsOperatorWithoutString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .cqmsOperator(SearchSetOperator.OR)
            .cqmsOperatorString(null)
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_hasMultipleCqmsAndMissingCqmOperator_addsError() {
        SearchRequest request = SearchRequest.builder()
            .cqms(Stream.of("CMS1", "CMS2").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getCQMCriterionNumbers(ArgumentMatchers.anyBoolean()))
        .thenReturn(Stream.of(
                new DescriptiveModel(1L, "CMS1", ""),
                new DescriptiveModel(2L, "CMS2", ""))
                .collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(MISSING_CQM_SEARCH_OPERATOR));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidAcbNullDimensionalData_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationBodies(Stream.of("ICSA").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getAllAcbs())
            .thenReturn(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_ACB, "ICSA", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidAcbDimensionalDataExists_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationBodies(Stream.of("ICSA").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getAllAcbs())
            .thenReturn(Stream.of(CertificationBody.builder().id(1L).name("Drummond").build(),
                    CertificationBody.builder().id(2L).name("SLI").build())
                    .collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_ACB, "ICSA", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validAcb_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .certificationBodies(Stream.of("ICSA").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getAllAcbs())
        .thenReturn(Stream.of(CertificationBody.builder().id(1L).name("Drummond").build(),
                CertificationBody.builder().id(2L).name("ICSA").build())
                .collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidPracticeTypeNullDimensionalData_addsError() {
        SearchRequest request = SearchRequest.builder()
            .practiceType("Inpatient")
            .build();
        Mockito.when(dimensionalDataManager.getPracticeTypeNames())
            .thenReturn(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_PRACTICE_TYPE, "Inpatient", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidPracticeTypeDimensionalDataExists_addsError() {
        SearchRequest request = SearchRequest.builder()
            .practiceType("Bad")
            .build();
        Mockito.when(dimensionalDataManager.getPracticeTypeNames())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Inpatient"),
                    new KeyValueModel(2L, "Ambulatory"))
                    .collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_PRACTICE_TYPE, "Bad", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validPracticeType_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .practiceType("Inpatient")
            .build();
        Mockito.when(dimensionalDataManager.getPracticeTypeNames())
        .thenReturn(Stream.of(new KeyValueModel(1L, "Inpatient"),
                new KeyValueModel(2L, "Ambulatory"))
                .collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidCertificationDateStartFormat_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationDateStart("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CERTIFICATION_DATE, "12345", SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT)));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidCertificationDateEndFormat_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationDateEnd("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CERTIFICATION_DATE, "12345", SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT)));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_backwardsCertificationDateOrder_noError() {
        SearchRequest request = SearchRequest.builder()
            .certificationDateStart("2015-12-31")
            .certificationDateEnd("2015-01-01")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_emptyCertificationDateStartAndEnd_noError() {
        SearchRequest request = SearchRequest.builder()
            .certificationDateStart("")
            .certificationDateEnd("")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validCertificationDateStartEmptyEnd_noError() {
        SearchRequest request = SearchRequest.builder()
            .certificationDateStart("2015-01-01")
            .certificationDateEnd("")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validCertificationDateEndEmptyStart_noError() {
        SearchRequest request = SearchRequest.builder()
            .certificationDateEnd("")
            .certificationDateEnd("2015-12-31")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validCertificationDateStartAndEnd_noError() {
        SearchRequest request = SearchRequest.builder()
            .certificationDateStart("2015-01-01")
            .certificationDateEnd("2015-12-31")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidNonConformityOptionsOperator_addsError() {
        SearchRequest request = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .nonConformityOptionsOperator(null)
                    .nonConformityOptionsOperatorString("BADVALUE")
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_OPERATOR, "BADVALUE",
                    Stream.of(SearchSetOperator.values())
                    .map(value -> value.name())
                    .collect(Collectors.joining(",")))));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validNonConformityOptionsOperatorParsedFromString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .nonConformityOptionsOperator(SearchSetOperator.OR)
                    .nonConformityOptionsOperatorString("OR")
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validNonConformityOperatorWithoutString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .nonConformityOptionsOperator(SearchSetOperator.OR)
                    .nonConformityOptionsOperatorString(null)
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_hasNonConformityOptionObjectsAndMissingNonConformitySearchOperator_addsError() {
        SearchRequest request = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .nonConformityOptionsStrings(null)
                    .nonConformityOptions(Stream.of(NonConformitySearchOptions.NEVER_NONCONFORMITY, NonConformitySearchOptions.OPEN_NONCONFORMITY).collect(Collectors.toSet()))
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(MISSING_NC_SEARCH_OPERATOR));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidNonConformitySearchOption_addsError() {
        SearchRequest request = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .nonConformityOptionsStrings(Stream.of("NEVER_NONCONFORMITY", "BADVALUE").collect(Collectors.toSet()))
                    .nonConformityOptions(Stream.of(NonConformitySearchOptions.NEVER_NONCONFORMITY).collect(Collectors.toSet()))
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_NONCONFORMITY_SEARCH_OPTION,
                    "BADVALUE",
                    Stream.of(NonConformitySearchOptions.values())
                    .map(value -> value.name())
                    .collect(Collectors.joining(",")))));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_emptyNonConformitySearchOptions_noError() {
        SearchRequest request = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .nonConformityOptionsStrings(Stream.of("NEVER_NONCONFORMITY", " ", "", null).collect(Collectors.toSet()))
                    .nonConformityOptions(Stream.of(NonConformitySearchOptions.NEVER_NONCONFORMITY).collect(Collectors.toSet()))
                    .nonConformityOptionsOperator(SearchSetOperator.OR)
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
        assertEquals(1, request.getComplianceActivity().getNonConformityOptions().size());
    }

    @Test
    public void validate_validNonConformitySearchOptionsParsedFromString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .nonConformityOptionsStrings(Stream.of("NEVER_NONCONFORMITY", "NOT_NEVER_NONCONFORMITY",
                            "OPEN_NONCONFORMITY", "NOT_OPEN_NONCONFORMITY",
                            "CLOSED_NONCONFORMITY", "NOT_CLOSED_NONCONFORMITY").collect(Collectors.toSet()))
                    .nonConformityOptions(Stream.of(NonConformitySearchOptions.NEVER_NONCONFORMITY,
                            NonConformitySearchOptions.NOT_NEVER_NONCONFORMITY,
                            NonConformitySearchOptions.OPEN_NONCONFORMITY,
                            NonConformitySearchOptions.NOT_OPEN_NONCONFORMITY,
                            NonConformitySearchOptions.CLOSED_NONCONFORMITY,
                            NonConformitySearchOptions.NOT_CLOSED_NONCONFORMITY).collect(Collectors.toSet()))
                    .nonConformityOptionsOperator(SearchSetOperator.OR)
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
        assertEquals(6, request.getComplianceActivity().getNonConformityOptions().size());
    }

    @Test
    public void validate_nullComplianceFilterDirectReviewsNotAvailable_noErrors() {
        Mockito.when(drService.doesCacheHaveAnyOkData()).thenReturn(false);

        SearchRequest request = SearchRequest.builder()
            .complianceActivity(null)
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_emptyComplianceFilterDirectReviewsNotAvailable_noErrors() {
        Mockito.when(drService.doesCacheHaveAnyOkData()).thenReturn(false);

        SearchRequest request = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(null)
                    .nonConformityOptions(Collections.emptySet())
                    .nonConformityOptionsOperator(null)
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_hasHadComplianceFilterNotNullDirectReviewsNotAvailable_noError() {
        Mockito.when(drService.doesCacheHaveAnyOkData()).thenReturn(false);

        SearchRequest request = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .nonConformityOptions(Collections.emptySet())
                    .nonConformityOptionsOperator(null)
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
            return;
        }
    }

    @Test
    public void validate_hasNonConformityOptionsDirectReviewsNotAvailable_noError() {
        Mockito.when(drService.doesCacheHaveAnyOkData()).thenReturn(false);

        SearchRequest request = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(null)
                    .nonConformityOptions(Stream.of(NonConformitySearchOptions.NEVER_NONCONFORMITY).collect(Collectors.toSet()))
                    .nonConformityOptionsOperator(null)
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
            return;
        }
    }

    @Test
    public void validate_hasNonConformityOptionsOperatorDirectReviewsNotAvailable_noError() {
        Mockito.when(drService.doesCacheHaveAnyOkData()).thenReturn(false);

        SearchRequest request = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(null)
                    .nonConformityOptions(Collections.emptySet())
                    .nonConformityOptionsOperator(SearchSetOperator.OR)
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
            return;
        }
    }

    @Test
    public void validate_rwtOperatorInvalid_addsError() {
        SearchRequest request = SearchRequest.builder()
            .rwtOperator(null)
            .rwtOperatorString("BADVALUE")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_OPERATOR, "BADVALUE",
                    Stream.of(SearchSetOperator.values())
                    .map(value -> value.name())
                    .collect(Collectors.joining(",")))));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_rwtOperatorValidParsedFromString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .rwtOperator(SearchSetOperator.OR)
            .rwtOperatorString("OR")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_rwtOperatorValidWithoutString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .rwtOperator(SearchSetOperator.OR)
            .rwtOperatorString(null)
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_hasRwtOptionObjectsAndMissingRwtOperator_addsError() {
        SearchRequest request = SearchRequest.builder()
            .rwtOptionsStrings(null)
            .rwtOptions(Stream.of(RwtSearchOptions.HAS_PLANS_URL, RwtSearchOptions.HAS_RESULTS_URL).collect(Collectors.toSet()))
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(RWT_OPERATOR_MISSING));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_rwtSearchOptionInvalid_addsError() {
        SearchRequest request = SearchRequest.builder()
            .rwtOptionsStrings(Stream.of("HAS_PLANS_URL", "BADVALUE").collect(Collectors.toSet()))
            .rwtOptions(Stream.of(RwtSearchOptions.HAS_PLANS_URL).collect(Collectors.toSet()))
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(RWT_OPTION_INVALID,
                    "BADVALUE",
                    Stream.of(RwtSearchOptions.values())
                    .map(value -> value.name())
                    .collect(Collectors.joining(",")))));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidSvapNullAllSvaps_addsError() {
        SearchRequest request = SearchRequest.builder()
            .svapIds(Stream.of(1L).collect(Collectors.toSet()))
            .build();
        Mockito.when(svapDao.getAll())
            .thenReturn(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_SVAP, "1", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidSvapButSvapDataExists_addsError() {
        SearchRequest request = SearchRequest.builder()
            .svapIds(Stream.of(3L).collect(Collectors.toSet()))
            .build();
        Mockito.when(svapDao.getAll())
            .thenReturn(Stream.of(Svap.builder().svapId(1L).build(),
                    Svap.builder().svapId(2L).build())
                    .collect(Collectors.toList()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_SVAP, "3", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validSvapId_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .svapIds(Stream.of(1L).collect(Collectors.toSet()))
            .build();
        Mockito.when(svapDao.getAll())
            .thenReturn(Stream.of(Svap.builder().svapId(1L).build(),
                Svap.builder().svapId(2L).build())
                .collect(Collectors.toList()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidSvapIdFormat_addsError() {
        SearchRequest request = SearchRequest.builder()
            .svapIdStrings(Stream.of("3 ", " 4 ", " 01", " ", "", null, "BAD").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_SVAP_ID_FORMAT, "BAD", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidSvapOperator_addsError() {
        SearchRequest request = SearchRequest.builder()
            .svapOperator(null)
            .svapOperatorString("XOR")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_OPERATOR, "XOR",
                    Stream.of(SearchSetOperator.values())
                    .map(value -> value.name())
                    .collect(Collectors.joining(",")))));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validSvapOperatorParsedFromString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .svapOperator(SearchSetOperator.OR)
            .svapOperatorString("OR")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validSvapOperatorWithoutString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .svapOperator(SearchSetOperator.OR)
            .svapOperatorString(null)
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_hasMultipleSvapIdsAndMissingSvapOperator_addsError() {
        SearchRequest request = SearchRequest.builder()
            .svapIds(Stream.of(1L, 2L).collect(Collectors.toSet()))
            .build();
        Mockito.when(svapDao.getAll())
            .thenReturn(Stream.of(
                    Svap.builder().svapId(1L).build(),
                    Svap.builder().svapId(2L).build())
                .collect(Collectors.toList()));
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(MISSING_SVAP_OPERATOR));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_emptyOptionsStrings_noError() {
        SearchRequest request = SearchRequest.builder()
            .rwtOptionsStrings(Stream.of("HAS_PLANS_URL", " ", "", null).collect(Collectors.toSet()))
            .rwtOptions(Stream.of(RwtSearchOptions.HAS_PLANS_URL).collect(Collectors.toSet()))
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
        assertEquals(1, request.getRwtOptions().size());
    }

    @Test
    public void validate_validRwtOptionsParsedFromString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .rwtOptionsStrings(Stream.of("HAS_PLANS_URL", "HAS_RESULTS_URL",
                    "NO_PLANS_URL", "NO_RESULTS_URL").collect(Collectors.toSet()))
            .rwtOptions(Stream.of(RwtSearchOptions.HAS_PLANS_URL, RwtSearchOptions.HAS_RESULTS_URL,
                    RwtSearchOptions.NO_PLANS_URL, RwtSearchOptions.NO_RESULTS_URL).collect(Collectors.toSet()))
            .rwtOperator(SearchSetOperator.OR)
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
        assertEquals(4, request.getRwtOptions().size());
    }

    @Test
    public void validate_invalidOrderBy_addsError() {
        SearchRequest request = SearchRequest.builder()
            .orderBy(null)
            .orderByString("NOTVALID")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_ORDER_BY, "NOTVALID",
                    Stream.of(OrderByOption.values())
                    .map(value -> value.name())
                    .collect(Collectors.joining(",")))));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validOrderByParsedFromString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .orderBy(OrderByOption.CERTIFICATION_DATE)
            .orderByString("CERTIFICATION_DATE")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validOrderByWithoutString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .orderBy(OrderByOption.CERTIFICATION_DATE)
            .orderByString(null)
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }
}
