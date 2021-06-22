package gov.healthit.chpl.search;

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
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.search.domain.ComplianceSearchFilter;
import gov.healthit.chpl.search.domain.NonConformitySearchOptions;
import gov.healthit.chpl.search.domain.OrderByOption;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SearchRequestValidatorTest {
    private static final String INVALID_CERTIFICATION_STATUS = "Could not find certification status with value '%s'.";
    private static final String INVALID_CERTIFICATION_EDITION = "Could not find certification edition with value '%s'.";
    private static final String INVALID_CERTIFICATION_CRITERION = "Could not find certification criterion with value '%s'.";
    private static final String INVALID_OPERATOR = "Invalid search operator value '%s'. Value must be one of %s.";
    private static final String INVALID_CQM = "Could not find CQM with value '%s'.";
    private static final String INVALID_ACB = "Could not find certification body with value '%s'.";
    private static final String INVALID_PRACTICE_TYPE = "Could not find practice type with value '%s'.";
    private static final String INVALID_CERTIFICATION_DATE = "Could not parse '%s' as date in the format %s.";
    private static final String INVALID_DATE_ORDER = "The certification date range end '%s' is before the start '%s'.";
    private static final String INVALID_NONCONFORMITY_SEARCH_OPTION = "No non-conformity search option matches '%s'. Values must be one of %s.";
    private static final String INVALID_ORDER_BY = "Order by parameter '%s' is invalid. Value must be one of %s.";

    private ErrorMessageUtil msgUtil;
    private DimensionalDataManager dimensionalDataManager;
    private SearchRequestValidator validator;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        dimensionalDataManager = Mockito.mock(DimensionalDataManager.class);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationStatuses.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CERTIFICATION_STATUS, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationEdition.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CERTIFICATION_EDITION, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationCriteria.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CERTIFICATION_CRITERION, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.searchOperator.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_OPERATOR, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.cqms.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CQM, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationBodies.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ACB, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.practiceType.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_PRACTICE_TYPE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationDate.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.eq(SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT)))
            .thenAnswer(i -> String.format(INVALID_CERTIFICATION_DATE, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.certificationDateOrder.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_DATE_ORDER, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.nonconformitySearchOption.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_NONCONFORMITY_SEARCH_OPTION, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.orderBy.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ORDER_BY, i.getArgument(1), i.getArgument(2)));

        validator = new SearchRequestValidator(dimensionalDataManager, msgUtil);
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
    public void validate_invalidCertificationCriteriaNullDimensionalData_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationCriteriaIds(Stream.of(1L).collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getCertificationCriterion())
            .thenReturn(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
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
        Mockito.when(dimensionalDataManager.getCertificationCriterion())
            .thenReturn(Stream.of(CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").build(),
                    CertificationCriterion.builder().id(2L).number("170.315 (a)(2)").build())
                    .collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
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
        Mockito.when(dimensionalDataManager.getCertificationCriterion())
        .thenReturn(Stream.of(CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").build(),
                CertificationCriterion.builder().id(2L).number("170.315 (a)(2)").build())
                .collect(Collectors.toSet()));

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
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
    public void validate_invalidCqmNullDimensionalData_addsError() {
        SearchRequest request = SearchRequest.builder()
            .cqms(Stream.of("CMS1").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getCQMCriterionNumbers(ArgumentMatchers.anyBoolean()))
            .thenReturn(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
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
    public void validate_invalidAcbNullDimensionalData_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationBodies(Stream.of("ICSA").collect(Collectors.toSet()))
            .build();
        Mockito.when(dimensionalDataManager.getCertBodyNames())
            .thenReturn(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
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
    public void validate_invalidPracticeTypeNullDimensionalData_addsError() {
        SearchRequest request = SearchRequest.builder()
            .practiceType("Inpatient")
            .build();
        Mockito.when(dimensionalDataManager.getPracticeTypeNames())
            .thenReturn(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
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
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CERTIFICATION_DATE, "12345", SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT)));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_backwardsCertificationDateOrder_addsError() {
        SearchRequest request = SearchRequest.builder()
            .certificationDateStart("2015-12-31")
            .certificationDateEnd("2015-01-01")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_DATE_ORDER, "2015-01-01", "2015-12-31")));
            return;
        }
        fail("Should not execute.");
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
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validNonConformitySearchOptionsParsedFromString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .nonConformityOptionsStrings(Stream.of("NEVER_NONCONFORMITY", "OPEN_NONCONFORMITY", "CLOSED_NONCONFORMITY").collect(Collectors.toSet()))
                    .nonConformityOptions(Stream.of(NonConformitySearchOptions.NEVER_NONCONFORMITY, NonConformitySearchOptions.OPEN_NONCONFORMITY, NonConformitySearchOptions.CLOSED_NONCONFORMITY).collect(Collectors.toSet()))
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
        assertEquals(3, request.getComplianceActivity().getNonConformityOptions().size());
    }

    @Test
    public void validate_validNonConformitySearchOptionsWithoutString_noErrors() {
        SearchRequest request = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .nonConformityOptionsStrings(null)
                    .nonConformityOptions(Stream.of(NonConformitySearchOptions.NEVER_NONCONFORMITY, NonConformitySearchOptions.OPEN_NONCONFORMITY, NonConformitySearchOptions.CLOSED_NONCONFORMITY).collect(Collectors.toSet()))
                    .build())
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
        assertEquals(3, request.getComplianceActivity().getNonConformityOptions().size());
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
