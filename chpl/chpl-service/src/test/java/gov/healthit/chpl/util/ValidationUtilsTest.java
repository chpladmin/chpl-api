package gov.healthit.chpl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.service.CertificationCriterionService;

public class ValidationUtilsTest {

    private ErrorMessageUtil errorMessageUtil;
    private ValidationUtils validationUtil;

    @Before
    public void before() {
        validationUtil = new ValidationUtils(Mockito.mock(CertificationCriterionService.class));
    }

    @Test
    public void checkSubordinateCriteriaAllRequired_AllRequiredCriteriaAttestedTo_NoMessages() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Attesting to Criteria %s requires that Criteria %s must also be attested to.",
                        i.getArgument(1), i.getArgument(2)));

        List<CertificationCriterion> attestedToCriteria = Arrays.asList(
                CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .build(),
                CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (d)(12)")
                        .build(),
                CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (d)(13)")
                        .build());

        List<CertificationCriterion> subordinateCriteria = Arrays.asList(
                CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .build());

        List<CertificationCriterion> requiredCriteria = Arrays.asList(
                CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (d)(12)")
                        .build(),
                CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (d)(13)")
                        .build());

        List<String> errors = validationUtil.checkSubordinateCriteriaAllRequired(subordinateCriteria, requiredCriteria,
                attestedToCriteria, errorMessageUtil);

        assertEquals(0, errors.size());
    }

    @Test
    public void checkSubordinateCriteriaAllRequired_NoCriteriaAttestedTo_NoMessages() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Attesting to Criteria %s requires that Criteria %s must also be attested to.",
                        i.getArgument(1), i.getArgument(2)));

        List<CertificationCriterion> attestedToCriteria = Arrays.asList(
                CertificationCriterion.builder()
                        .id(2L)
                        .number("170.315 (a)(2)")
                        .build(),
                CertificationCriterion.builder()
                        .id(3L)
                        .number("170.315 (a)(3)")
                        .build(),
                CertificationCriterion.builder()
                        .id(166L)
                        .number("170.315 (d)(12)")
                        .build(),
                CertificationCriterion.builder()
                        .id(167L)
                        .number("170.315 (d)(13)")
                        .build());

        List<CertificationCriterion> subordinateCriteria = Arrays.asList(
                CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .build());

        List<CertificationCriterion> requiredCriteria = Arrays.asList(
                CertificationCriterion.builder()
                        .id(166L)
                        .number("170.315 (d)(12)")
                        .build(),
                CertificationCriterion.builder()
                        .id(167L)
                        .number("170.315 (d)(13)")
                        .build());

        List<String> errors = validationUtil.checkSubordinateCriteriaAllRequired(subordinateCriteria, requiredCriteria,
                attestedToCriteria, errorMessageUtil);

        assertEquals(0, errors.size());
    }

    @Test
    public void checkSubordinateCriteriaAllRequired_MissingRequiredCriteriaAttestedTo_ErrorMessagesExist() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Attesting to Criteria %s requires that Criteria %s must also be attested to.",
                        i.getArgument(1), i.getArgument(2)));

        List<CertificationCriterion> attestedToCriteria = Arrays.asList(
                CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .build(),
                CertificationCriterion.builder()
                        .id(3L)
                        .number("170.315 (a)(3)")
                        .build(),
                CertificationCriterion.builder()
                        .id(166L)
                        .number("170.315 (d)(12)")
                        .build());

        List<CertificationCriterion> subordinateCriteria = Arrays.asList(
                CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .build());

        List<CertificationCriterion> requiredCriteria = Arrays.asList(
                CertificationCriterion.builder()
                        .id(166L)
                        .number("170.315 (d)(12)")
                        .build(),
                CertificationCriterion.builder()
                        .id(167L)
                        .number("170.315 (d)(13)")
                        .build());

        List<String> errors = validationUtil.checkSubordinateCriteriaAllRequired(subordinateCriteria, requiredCriteria,
                attestedToCriteria, errorMessageUtil);

        assertEquals(1, errors.size());
    }
}
