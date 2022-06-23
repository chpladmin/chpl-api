package gov.healthit.chpl.changerequest.search;

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

import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ChangeRequestSearchRequestValidatorTest {
    private static final String DEVELOPER_ID_INVALID_FORMAT = "The developerId parameter '%s' is not a valid number.";
    private static final String DEVELOPER_DOES_NOT_EXIST = "There is no developer in the system with the ID '%s'.";
    private static final String STATUS_NAME_INVALID = "The change request status name '%s' is not valid.";
    private static final String TYPE_NAME_INVALID = "The change request type name '%s' is not valid.";
    private static final String CURRENT_STATUS_DATE_INVALID_FORMAT = "The \"current status\" date '%s' is not valid. It must be in the format %s.";
    private static final String CURRENT_STATUS_DATE_INVALID_ORDER = "The \"current status\" date range end '%s' is before the start '%s'.";
    private static final String SUBMITTED_DATE_INVALID_FORMAT = "The submitted date '%s' is not valid. It must be in the format %s.";
    private static final String SUBMITTED_DATE_INVALID_ORDER = "The submitted date range end '%s' is before the start '%s'.";
    private static final String INVALID_PAGE_NUMBER = "Page number '%s' is not a valid number.";
    private static final String PAGE_NUMBER_OUT_OF_RANGE = "Page number must be 0 or greater. '%s' is not valid";
    private static final String INVALID_PAGE_SIZE = "Page size '%s' is not a valid number.";
    private static final String PAGE_SIZE_OUT_OF_RANGE = "Page size must be between %s and %s. '%s' is not valid.";
    private static final String INVALID_ORDER_BY = "Order by parameter '%s' is invalid. Value must be one of %s.";

    private ChangeRequestManager changeRequestManager;
    private DeveloperDAO developerDao;
    private ErrorMessageUtil msgUtil;
    private ChangeRequestSearchRequestValidator validator;

    @Before
    public void setup() {
        changeRequestManager = Mockito.mock(ChangeRequestManager.class);
        developerDao = Mockito.mock(DeveloperDAO.class);

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.changeRequest.developerId.invalidFormat"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(DEVELOPER_ID_INVALID_FORMAT, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.changeRequest.developerId.doesNotExist"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(DEVELOPER_DOES_NOT_EXIST, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.changeRequest.statusName.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(STATUS_NAME_INVALID, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.changeRequest.typeName.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TYPE_NAME_INVALID, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.changeRequest.currentStatusDateTime.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CURRENT_STATUS_DATE_INVALID_FORMAT, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.changeRequest.currentStatusDateTimes.invalidOrder"), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(CURRENT_STATUS_DATE_INVALID_ORDER, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.changeRequest.submittedDateTime.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(SUBMITTED_DATE_INVALID_FORMAT, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.changeRequest.submittedDateTimes.invalidOrder"), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(SUBMITTED_DATE_INVALID_ORDER, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.changeRequest.pageNumber.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_PAGE_NUMBER, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.changeRequest.pageNumber.outOfRange"), ArgumentMatchers.any()))
            .thenAnswer(i -> String.format(PAGE_NUMBER_OUT_OF_RANGE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.changeRequest.pageSize.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_PAGE_SIZE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.changeRequest.pageSize.outOfRange"), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PAGE_SIZE_OUT_OF_RANGE, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.changeRequest.orderBy.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ORDER_BY, i.getArgument(1), i.getArgument(2)));
        validator = new ChangeRequestSearchRequestValidator(changeRequestManager, developerDao, msgUtil);
    }

    @Test
    public void validate_invalidDeveloperIdFormat_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .developerIdString("test")
            .developerId(null)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(DEVELOPER_ID_INVALID_FORMAT, "test", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_developerIdValidFormatDoesNotExist_addsError() throws EntityRetrievalException {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .developerIdString("1")
            .developerId(1L)
            .build();

        Mockito.when(developerDao.getById(ArgumentMatchers.eq(1L)))
            .thenThrow(EntityRetrievalException.class);
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(DEVELOPER_DOES_NOT_EXIST, "1", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_developerIdValidFormantAndExists_noError() throws EntityRetrievalException {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .developerIdString("1")
            .developerId(1L)
            .build();

        Mockito.when(developerDao.getById(ArgumentMatchers.eq(1L)))
            .thenReturn(Developer.builder().id(1L).name("Test Dev").build());
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_invalidStatusName_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .currentStatusNames(Stream.of("Sandwich").collect(Collectors.toSet()))
            .build();
        Mockito.when(changeRequestManager.getChangeRequestStatusTypes())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Active"), new KeyValueModel(2L, "Rejected")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(STATUS_NAME_INVALID, "Sandwich", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidStatusNames_addsErrors() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .currentStatusNames(Stream.of("Sandwich", "Horse").collect(Collectors.toSet()))
            .build();
        Mockito.when(changeRequestManager.getChangeRequestStatusTypes())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Active"), new KeyValueModel(2L, "Rejected")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(2, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(STATUS_NAME_INVALID, "Sandwich", "")));
            assertTrue(ex.getErrorMessages().contains(String.format(STATUS_NAME_INVALID, "Horse", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_emptyStatusNames_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .currentStatusNames(Collections.EMPTY_SET)
            .build();
        Mockito.when(changeRequestManager.getChangeRequestStatusTypes())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Active"), new KeyValueModel(2L, "Rejected")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_nullStatusNames_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder().build();
        request.setCurrentStatusNames(null);
        Mockito.when(changeRequestManager.getChangeRequestStatusTypes())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Active"), new KeyValueModel(2L, "Rejected")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_validStatusName_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
                .currentStatusNames(Stream.of("active").collect(Collectors.toSet()))
                .build();
        Mockito.when(changeRequestManager.getChangeRequestStatusTypes())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Active"), new KeyValueModel(2L, "Rejected")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_validStatusNames_noErrors() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
                .currentStatusNames(Stream.of("ActiVE", "Rejected").collect(Collectors.toSet()))
                .build();
        Mockito.when(changeRequestManager.getChangeRequestStatusTypes())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Active"), new KeyValueModel(2L, "Rejected")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_invalidTypeName_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .typeNames(Stream.of("Sandwich").collect(Collectors.toSet()))
            .build();
        Mockito.when(changeRequestManager.getChangeRequestTypes())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Demographic"), new KeyValueModel(2L, "Attestation")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(TYPE_NAME_INVALID, "Sandwich", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidTypeNames_addsErrors() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .typeNames(Stream.of("Sandwich", "Horse").collect(Collectors.toSet()))
            .build();
        Mockito.when(changeRequestManager.getChangeRequestTypes())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Demographic"), new KeyValueModel(2L, "Attestation")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(2, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(TYPE_NAME_INVALID, "Sandwich", "")));
            assertTrue(ex.getErrorMessages().contains(String.format(TYPE_NAME_INVALID, "Horse", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_emptyTypeNames_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .typeNames(Collections.EMPTY_SET)
            .build();
        Mockito.when(changeRequestManager.getChangeRequestTypes())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Demographic"), new KeyValueModel(2L, "Attestation")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_nullTypeNames_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder().build();
        request.setTypeNames(null);
        Mockito.when(changeRequestManager.getChangeRequestTypes())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Demographic"), new KeyValueModel(2L, "Attestation")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_validTypeName_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
                .typeNames(Stream.of("demographic").collect(Collectors.toSet()))
                .build();
        Mockito.when(changeRequestManager.getChangeRequestTypes())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Demographic"), new KeyValueModel(2L, "Attestation")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_validTypeNames_noErrors() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
                .typeNames(Stream.of("DemoGRAPHic", "attestaTion").collect(Collectors.toSet()))
                .build();
        Mockito.when(changeRequestManager.getChangeRequestTypes())
            .thenReturn(Stream.of(new KeyValueModel(1L, "Demographic"), new KeyValueModel(2L, "Attestation")).collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_invalidCurrentStatusChangeDateTimeStartFormat_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeStart("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(CURRENT_STATUS_DATE_INVALID_FORMAT, "12345", ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT)));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidCurrentStatusChangeDateTimeEndFormat_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeEnd("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(CURRENT_STATUS_DATE_INVALID_FORMAT, "12345", ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT)));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_backwardsCurrentStatusChangeDateTimeOrder_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeStart("2015-12-31T00:00:00")
            .currentStatusChangeDateTimeEnd("2015-01-01T01:00:00")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(CURRENT_STATUS_DATE_INVALID_ORDER, "2015-01-01T01:00", "2015-12-31T00:00")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_emptyCurrentStatusDateTimeStartAndEnd_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeStart("")
            .currentStatusChangeDateTimeEnd("")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validCurrentStatusDateTimeStartEmptyEnd_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeStart("2015-01-01T00:00:00")
            .currentStatusChangeDateTimeEnd("")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validCurrentStatusDateTimeEndEmptyStart_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeStart("")
            .currentStatusChangeDateTimeEnd("2015-12-31T00:00:00")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validCurrentStatusDateTimeStartAndEnd_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeStart("2015-01-01T00:00:00")
            .currentStatusChangeDateTimeEnd("2015-12-31T00:00:00")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidSubmittedDateTimeStartFormat_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .submittedDateTimeStart("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(SUBMITTED_DATE_INVALID_FORMAT, "12345", ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT)));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidSubmittedDateTimeEndFormat_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .submittedDateTimeEnd("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(SUBMITTED_DATE_INVALID_FORMAT, "12345", ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT)));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_backwardsSubmittedDateTimeOrder_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .submittedDateTimeStart("2015-12-31T00:00:00")
            .submittedDateTimeEnd("2015-01-01T01:00:00")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(SUBMITTED_DATE_INVALID_ORDER, "2015-01-01T01:00", "2015-12-31T00:00")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_emptySubmittedDateTimeStartAndEnd_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .submittedDateTimeStart("")
            .submittedDateTimeEnd("")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validSubmittedDateTimeStartEmptyEnd_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .submittedDateTimeStart("2015-01-01T00:00:00")
            .submittedDateTimeEnd("")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validSubmittedDateTimeEndEmptyStart_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .submittedDateTimeStart("")
            .submittedDateTimeEnd("2015-12-31T00:00:00")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validSubmittedDateTimeStartAndEnd_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .submittedDateTimeStart("2015-01-01T00:00:00")
            .submittedDateTimeEnd("2015-12-31T00:00:00")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_pageSizeInvalidNumber_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .pageSize(20)
            .pageSizeString("sandwich")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_PAGE_SIZE, "sandwich", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_pageNumberInvalidNumber_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .pageNumber(0)
            .pageNumberString("sandwich")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_PAGE_NUMBER, "sandwich", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_pageNumberVeryLarge_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .pageNumber(800000)
            .pageNumberString("800000")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_pageNumberSmallerThanMin_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .pageNumber(-1)
            .pageNumberString("-1")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(PAGE_NUMBER_OUT_OF_RANGE, "-1", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_pageNumberAverageValidValue_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .pageNumber(2)
            .pageNumberString("2")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_pageSizeLargerThanMax_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .pageSize(800000)
            .pageSizeString("800000")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(PAGE_SIZE_OUT_OF_RANGE, ChangeRequestSearchRequest.MIN_PAGE_SIZE,
                    ChangeRequestSearchRequest.MAX_PAGE_SIZE, "800000")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_pageSizeSmallerThanMin_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .pageSize(-1)
            .pageSizeString("-1")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(PAGE_SIZE_OUT_OF_RANGE, ChangeRequestSearchRequest.MIN_PAGE_SIZE,
                    ChangeRequestSearchRequest.MAX_PAGE_SIZE, "-1")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_pageSizeValid_noError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .pageSize(100)
            .pageSizeString("100")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_invalidOrderBy_addsError() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
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
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .orderBy(OrderByOption.CHANGE_REQUEST_STATUS)
            .orderByString("CHANGE_REQUEST_STATUS")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validOrderByWithoutString_noErrors() {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
            .orderBy(OrderByOption.CHANGE_REQUEST_TYPE)
            .orderByString(null)
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }
}
