package gov.healthit.chpl.questionableactivity.search;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.questionableactivity.QuestionableActivityDAO;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SearchRequestValidatorTest {
    private static final String INVALID_TRIGGER_ID = "The trigger ID %s is not valid.";
    private static final String INVALID_ACTIVITY_DATE = "Could not parse '%s' as an activity date in the format %s.";
    private static final String INVALID_PAGE_SIZE = "Page size cannot exceed %s.";
    private static final String INVALID_ORDER_BY = "Order by parameter '%s' is invalid. Value must be one of %s.";

    private QuestionableActivityDAO questionableActivityDao;
    private ErrorMessageUtil msgUtil;
    private QuestionableActivityTrigger devTrigger, prodTrigger, listingTrigger;
    private SearchRequestValidator validator;

    @Before
    public void setup() {
        devTrigger = QuestionableActivityTrigger.builder()
                .id(1L)
                .name("Developer Name Changed")
                .level("Developer")
                .build();
        prodTrigger = QuestionableActivityTrigger.builder()
                .id(2L)
                .name("Product Name Changed")
                .level("Product")
                .build();
        listingTrigger = QuestionableActivityTrigger.builder()
                .id(3L)
                .name("Certification Status Changed")
                .level("Listing")
                .build();
        questionableActivityDao = Mockito.mock(QuestionableActivityDAO.class);
        Mockito.when(questionableActivityDao.getAllTriggers())
            .thenReturn(Stream.of(devTrigger, prodTrigger, listingTrigger).toList());

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.questionableActivity.activityDate.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ACTIVITY_DATE, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.questionableActivity.invalidTrigger"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_TRIGGER_ID, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.pageSize.invalid"), ArgumentMatchers.anyInt()))
            .thenAnswer(i -> String.format(INVALID_PAGE_SIZE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.orderBy.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ORDER_BY, i.getArgument(1), i.getArgument(2)));

        validator = new SearchRequestValidator(questionableActivityDao, msgUtil);
    }

    @Test
    public void validate_validTriggerIdFormat_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .triggerIdStrings(Stream.of("1", "2").collect(Collectors.toSet()))
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidTriggerIdFormat_addsError() {
        SearchRequest request = SearchRequest.builder()
            .triggerIdStrings(Stream.of("3 ", " 4 ", " 01", " ", "", null, "BAD").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_TRIGGER_ID, "BAD", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidTriggerIdDaoResponseExists_addsError() {
        SearchRequest request = SearchRequest.builder()
            .triggerIds(Stream.of(100L).collect(Collectors.toSet()))
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_TRIGGER_ID, "100", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validTriggerIds_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .triggerIds(Stream.of(1L, 2L, 3L).collect(Collectors.toSet()))
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }


    @Test
    public void validate_invalidCertificationDateStartFormat_addsError() {
        SearchRequest request = SearchRequest.builder()
            .activityDateStart("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_ACTIVITY_DATE, "12345", SearchRequest.DATE_SEARCH_FORMAT)));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidActivityDateEndFormat_addsError() {
        SearchRequest request = SearchRequest.builder()
            .activityDateEnd("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_ACTIVITY_DATE, "12345", SearchRequest.DATE_SEARCH_FORMAT)));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_backwardsActivityDateOrder_noError() {
        SearchRequest request = SearchRequest.builder()
            .activityDateStart("2015-12-31")
            .activityDateEnd("2015-01-01")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_emptyActivityDateStartAndEnd_noError() {
        SearchRequest request = SearchRequest.builder()
            .activityDateStart("")
            .activityDateEnd("")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validActivityDateStartEmptyEnd_noError() {
        SearchRequest request = SearchRequest.builder()
            .activityDateStart("2015-01-01")
            .activityDateEnd("")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validActivityDateEndEmptyStart_noError() {
        SearchRequest request = SearchRequest.builder()
            .activityDateEnd("")
            .activityDateEnd("2015-12-31")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validActivityDateStartAndEnd_noError() {
        SearchRequest request = SearchRequest.builder()
            .activityDateStart("2015-01-01")
            .activityDateEnd("2015-12-31")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_exceedsMaxPageSize_addsError() {
        SearchRequest request = SearchRequest.builder()
            .pageSize(5000)
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_PAGE_SIZE, SearchRequest.MAX_PAGE_SIZE, "")));
            return;
        }
        fail("Should not execute.");
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
            .orderBy(OrderByOption.ACTIVITY_DATE)
            .orderByString("ACTIVITY_DATE")
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
            .orderBy(OrderByOption.DEVELOPER)
            .orderByString(null)
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }
}
