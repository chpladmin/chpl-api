package gov.healthit.chpl.developer.search;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SearchRequestValidatorTest {
    private static final String INVALID_STATUS = "Could not find developer status with value '%s'. Value must be one of %s.";
    private static final String INVALID_ACB = "Could not find certification body with value '%s'.";
    private static final String INVALID_DECERTIFICATION_DATE = "Could not parse '%s' as date in the format %s.";
    private static final String INVALID_ORDER_BY = "Order by parameter '%s' is invalid. Value must be one of %s.";
    private static final String INVALID_OPERATOR = "Invalid search operator value '%s'. Value must be one of %s.";
    private static final String MISSING_ACTIVE_LISTINGS_OPERATOR = "Multiple active listing filters were found without a search operator (AND/OR). A search operator is required.";
    private static final String INVALID_ACTIVE_LISTINGS_OPTIONS = "No active listings search option matches '%s'. Values must be one of %s.";
    private static final String MISSING_ATTESTATIONS_OPERATOR = "Multiple attestations filters were found without a search operator (AND/OR). A search operator is required.";
    private static final String INVALID_ATTESTATIONS_OPTIONS = "No attestations search option matches '%s'. Values must be one of %s.";

    private DimensionalDataManager dimensionalDataManager;
    private ErrorMessageUtil msgUtil;
    private SearchRequestValidator validator;

    @Before
    public void setup() {
        dimensionalDataManager = Mockito.mock(DimensionalDataManager.class);

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.developer.statuses.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_STATUS, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationBodies.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ACB, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.developer.decertificationDate.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.eq(DeveloperSearchRequest.DATE_SEARCH_FORMAT)))
            .thenAnswer(i -> String.format(INVALID_DECERTIFICATION_DATE, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.orderBy.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ORDER_BY, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.searchOperator.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_OPERATOR, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.searchOperator.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_OPERATOR, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.developer.missingActiveListingsOperator")))
            .thenReturn(MISSING_ACTIVE_LISTINGS_OPERATOR);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.developer.activeListingsSearchOption.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ACTIVE_LISTINGS_OPTIONS, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.developer.missingAttestationsOperator")))
            .thenReturn(MISSING_ATTESTATIONS_OPERATOR);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.developer.attestationsSearchOption.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ATTESTATIONS_OPTIONS, i.getArgument(1), i.getArgument(2)));

        validator = new SearchRequestValidator(dimensionalDataManager, msgUtil);
    }

    @Test
    public void validate_invalidStatusNullDimensionalData_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .statuses(Stream.of("Active").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getDeveloperStatuses())
            .thenReturn(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_STATUS, "Active", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidStatusDimensionalDataExists_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .statuses(Stream.of("Active").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getDeveloperStatuses())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Suspended"), new KeyValueModel(2L, "Withdrawn")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_STATUS, "Active", "Suspended, Withdrawn")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validStatus_noErrors() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .statuses(Stream.of("Active").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getDeveloperStatuses())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Active")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validStatusIgnoreCase_noErrors() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .statuses(Stream.of("Active").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getDeveloperStatuses())
            .thenReturn(Stream.of(new KeyValueModel(1L, "actIVE")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidAcbWithActiveListingsNullDimensionalData_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .acbsForActiveListings(Stream.of("ICSA").collect(Collectors.toSet()))
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
    public void validate_invalidAcbWithActiveListingsDimensionalDataExists_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .acbsForActiveListings(Stream.of("ICSA").collect(Collectors.toSet()))
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
    public void validate_validAcbWithActiveListings_noErrors() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .acbsForActiveListings(Stream.of("ICSA").collect(Collectors.toSet()))
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
    public void validate_invalidAcbWithAnyListingsNullDimensionalData_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .acbsForAllListings(Stream.of("ICSA").collect(Collectors.toSet()))
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
    public void validate_invalidAcbWithAnyListingsDimensionalDataExists_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .acbsForAllListings(Stream.of("ICSA").collect(Collectors.toSet()))
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
    public void validate_validAcbWithAnyListings_noErrors() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .acbsForAllListings(Stream.of("ICSA").collect(Collectors.toSet()))
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
    public void validate_invalidDecertificationDateStartFormat_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .decertificationDateStart("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_DECERTIFICATION_DATE, "12345", DeveloperSearchRequest.DATE_SEARCH_FORMAT)));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidDecertificationDateEndFormat_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .decertificationDateEnd("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_DECERTIFICATION_DATE, "12345", DeveloperSearchRequest.DATE_SEARCH_FORMAT)));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_backwardsDecertificationDateOrder_noError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .decertificationDateStart("2015-12-31")
            .decertificationDateEnd("2015-01-01")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_emptyCertificationDateStartAndEnd_noError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .decertificationDateStart("")
            .decertificationDateEnd("")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validDecertificationDateStartEmptyEnd_noError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .decertificationDateStart("2015-01-01")
            .decertificationDateEnd("")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validDecertificationDateEndEmptyStart_noError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .decertificationDateEnd("")
            .decertificationDateEnd("2015-12-31")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validDecertificationDateStartAndEnd_noError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .decertificationDateStart("2015-01-01")
            .decertificationDateEnd("2015-12-31")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidActveListingsSearchOperator_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .activeListingsOptions(Stream.of(
                    ActiveListingSearchOptions.HAS_ANY_ACTIVE, ActiveListingSearchOptions.HAS_NO_ACTIVE).collect(Collectors.toSet()))
            .activeListingsOptionsOperatorString("XOR")
            .activeListingsOptionsOperator(null)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_OPERATOR, "XOR", "AND,OR")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_missingActveListingsSearchOperator_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .activeListingsOptions(Stream.of(
                    ActiveListingSearchOptions.HAS_ANY_ACTIVE, ActiveListingSearchOptions.HAS_NO_ACTIVE).collect(Collectors.toSet()))
            .activeListingsOptionsOperator(null)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(MISSING_ACTIVE_LISTINGS_OPERATOR));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_singleActiveListingsOptionAndNoActveListingsSearchOperator_noError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .activeListingsOptions(Stream.of(
                    ActiveListingSearchOptions.HAS_ANY_ACTIVE).collect(Collectors.toSet()))
            .activeListingsOptionsOperator(null)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validActveListingsSearchOperator_noError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .activeListingsOptions(Stream.of(
                    ActiveListingSearchOptions.HAS_ANY_ACTIVE, ActiveListingSearchOptions.HAS_NO_ACTIVE).collect(Collectors.toSet()))
            .activeListingsOptionsOperator(SearchSetOperator.AND)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidActveListingsSearchOption_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .activeListingsOptions(Stream.of(ActiveListingSearchOptions.HAS_ANY_ACTIVE).collect(Collectors.toSet()))
            .activeListingsOptionsStrings(Stream.of(ActiveListingSearchOptions.HAS_ANY_ACTIVE.name(), "NONE_ACTIVE").collect(Collectors.toSet()))
            .activeListingsOptionsOperator(null)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_ACTIVE_LISTINGS_OPTIONS, "NONE_ACTIVE",
                    Stream.of(ActiveListingSearchOptions.values())
                    .map(value -> value.name())
                    .collect(Collectors.joining(",")))));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidAttestationsSearchOperator_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .attestationsOptions(Stream.of(
                    AttestationsSearchOptions.HAS_NOT_PUBLISHED,
                    AttestationsSearchOptions.HAS_PUBLISHED).collect(Collectors.toSet()))
            .attestationsOptionsOperatorString("XOR")
            .attestationsOptionsOperator(null)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_OPERATOR, "XOR", "AND,OR")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_missingAttestationsSearchOperator_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .attestationsOptions(Stream.of(
                    AttestationsSearchOptions.HAS_NOT_PUBLISHED,
                    AttestationsSearchOptions.HAS_PUBLISHED).collect(Collectors.toSet()))
            .activeListingsOptionsOperator(null)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(MISSING_ATTESTATIONS_OPERATOR));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_singleAttestationsOptionAndNoAttestationsSearchOperator_noError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .attestationsOptions(Stream.of(
                    AttestationsSearchOptions.HAS_SUBMITTED).collect(Collectors.toSet()))
            .attestationsOptionsOperator(null)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validAttestationsSearchOperator_noError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .attestationsOptions(Stream.of(
                    AttestationsSearchOptions.HAS_SUBMITTED,
                    AttestationsSearchOptions.HAS_PUBLISHED).collect(Collectors.toSet()))
            .attestationsOptionsOperator(SearchSetOperator.AND)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidAttestationsSearchOption_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .attestationsOptions(Stream.of(AttestationsSearchOptions.HAS_SUBMITTED).collect(Collectors.toSet()))
            .attestationsOptionsStrings(Stream.of(AttestationsSearchOptions.HAS_SUBMITTED.name(), "NONE_SUBMITTED").collect(Collectors.toSet()))
            .attestationsOptionsOperator(null)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_ATTESTATIONS_OPTIONS, "NONE_SUBMITTED",
                    Stream.of(AttestationsSearchOptions.values())
                    .map(value -> value.name())
                    .collect(Collectors.joining(",")))));
            return;
        }
        fail("Should not execute.");
    }


    @Test
    public void validate_invalidOrderBy_addsError() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
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
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .orderBy(OrderByOption.DECERTIFICATION_DATE)
            .orderByString("DECERTIFICATION_DATE")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validOrderByWithoutString_noErrors() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
            .orderBy(OrderByOption.DECERTIFICATION_DATE)
            .orderByString(null)
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }
}
