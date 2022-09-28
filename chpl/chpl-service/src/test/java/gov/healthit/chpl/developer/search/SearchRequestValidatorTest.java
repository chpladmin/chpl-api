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
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SearchRequestValidatorTest {
    private static final String INVALID_STATUS = "Could not find developer status with value '%s'. Value must be one of %s.";
    private static final String INVALID_ACB = "Could not find certification body with value '%s'.";
    private static final String INVALID_DECERTIFICATION_DATE = "Could not parse '%s' as date in the format %s.";
    private static final String INVALID_DATE_ORDER = "The decertification date range end '%s' is before the start '%s'.";
    private static final String INVALID_ORDER_BY = "Order by parameter '%s' is invalid. Value must be one of %s.";

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
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.developer.decertificationDate.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.eq(SearchRequest.DATE_SEARCH_FORMAT)))
            .thenAnswer(i -> String.format(INVALID_DECERTIFICATION_DATE, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.developer.decertificationDateOrder.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_DATE_ORDER, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.orderBy.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ORDER_BY, i.getArgument(1), i.getArgument(2)));

        validator = new SearchRequestValidator(dimensionalDataManager, msgUtil);
    }

    @Test
    public void validate_invalidStatusNullDimensionalData_addsError() {
        SearchRequest request = SearchRequest.builder()
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
        SearchRequest request = SearchRequest.builder()
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
        SearchRequest request = SearchRequest.builder()
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
        SearchRequest request = SearchRequest.builder()
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
    public void validate_invalidAcbNullDimensionalData_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationBodies(Stream.of("ICSA").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getCertBodyNames())
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
        Mockito.when(dimensionalDataManager.getCertBodyNames())
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
        Mockito.when(dimensionalDataManager.getCertBodyNames())
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
        SearchRequest request = SearchRequest.builder()
            .decertificationDateStart("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_DECERTIFICATION_DATE, "12345", SearchRequest.DATE_SEARCH_FORMAT)));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidDecertificationDateEndFormat_addsError() {
        SearchRequest request = SearchRequest.builder()
            .decertificationDateEnd("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_DECERTIFICATION_DATE, "12345", SearchRequest.DATE_SEARCH_FORMAT)));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_backwardsDecertificationDateOrder_addsError() {
        SearchRequest request = SearchRequest.builder()
            .decertificationDateStart("2015-12-31")
            .decertificationDateEnd("2015-01-01")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_DATE_ORDER, "2015-01-01", "2015-12-31")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_emptyCertificationDateStartAndEnd_noError() {
        SearchRequest request = SearchRequest.builder()
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
        SearchRequest request = SearchRequest.builder()
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
        SearchRequest request = SearchRequest.builder()
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
        SearchRequest request = SearchRequest.builder()
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
        SearchRequest request = SearchRequest.builder()
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
