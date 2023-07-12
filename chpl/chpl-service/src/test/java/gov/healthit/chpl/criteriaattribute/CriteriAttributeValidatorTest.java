package gov.healthit.chpl.criteriaattribute;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.criteriaattribute.rule.RuleDAO;
import gov.healthit.chpl.criteriaattribute.rule.RuleEntity;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CriteriAttributeValidatorTest {

    private CriteriaAttributeValidator criteriaAttributeValidator;
    private ErrorMessageUtil errorMessageUtil;
    private RuleDAO ruleDAO;

    private CriteriaAttributeDAO criteriaAttributeDAO;

    @Before
    public void setup() throws EntityRetrievalException {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        ruleDAO = Mockito.mock(RuleDAO.class);
        criteriaAttributeDAO = Mockito.mock(CriteriaAttributeDAO.class);

        Mockito.when(ruleDAO.getRuleEntityById(ArgumentMatchers.anyLong()))
                .thenReturn(RuleEntity.builder()
                        .id(1L)
                        .name("Rule Name")
                        .build());

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
                .thenAnswer(i -> i.getArguments()[0]);


        Mockito.when(criteriaAttributeDAO.getCriteriaAttributeById(ArgumentMatchers.anyLong()))
                .thenReturn(getStandardCriteriaAttrbute());
        Mockito.when(criteriaAttributeDAO.getCertifiedProductsByCriteriaAttributeAndCriteria(ArgumentMatchers.any(CriteriaAttribute.class), ArgumentMatchers.any(CertificationCriterion.class)))
                .thenReturn(List.of(CertifiedProductDetailsDTO.builder().build()));
        Mockito.when(criteriaAttributeDAO.getCertifiedProductsByCriteriaAttribute(ArgumentMatchers.any(CriteriaAttribute.class)))
                .thenReturn(List.of(CertifiedProductDetailsDTO.builder().build()));
        Mockito.when(criteriaAttributeDAO.getAllAssociatedCriteriaMaps())
                .thenReturn(List.of(CriteriaAttributeCriteriaMap.builder()
                        .criteriaAttribute(getStandardCriteriaAttrbute())
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("999.999(a)(1)")
                                .build())
                        .build()));

        criteriaAttributeValidator = new CriteriaAttributeValidator(errorMessageUtil, ruleDAO);
    }

    @Test
    public void validateForEdit_ValueIsNotNull_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_ValueIsNull_ThrowsExcptionWithRelatedMessage() throws ValidationException, EntityRetrievalException {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();

        context.getCriteriaAttribe().setValue(null);

        ValidationException e = assertThrows(ValidationException.class, () -> criteriaAttributeValidator.validateForEdit(context));
        assertNotNull(e);
        assertTrue(e.getErrorMessages().contains("criteriaAttribute.edit.emptyValue"));
    }

    @Test
    public void validateForEdit_RegulatoryTextCitationIsNotNullAndNotRequired_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_RegulatoryTextCitationNullAndNotRequired_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();
        context.getCriteriaAttribe().setRegulatoryTextCitation(null);

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_RegulatoryTextCitationIsNotNullAndIsRequired_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();
        context.setIsRegulatoryTextCitationRequired(true);

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_RegulatoryTextCitationIsNullAndIsRequired_ThrowsExcptionWithRelevantMessage() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();
        context.getCriteriaAttribe().setRegulatoryTextCitation(null);
        context.setIsRegulatoryTextCitationRequired(true);

        ValidationException e = assertThrows(ValidationException.class, () -> criteriaAttributeValidator.validateForEdit(context));
        assertNotNull(e);
        assertTrue(e.getErrorMessages().contains("criteriaAttribute.edit.emptyRegulatoryTextCitation"));
    }

    @Test
    public void validateForEdit_StartDayIsNotNullAndNotRequired_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_StartDayNullAndNotRequired_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();
        context.getCriteriaAttribe().setStartDay(null);

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_StartDayIsNotNullAndIsRequired_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();
        context.setStartDayRequired(true);

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_StartDayIsNullAndIsRequired_ThrowsExcptionWithRelevantMessage() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();
        context.getCriteriaAttribe().setStartDay(null);
        context.setStartDayRequired(true);

        ValidationException e = assertThrows(ValidationException.class, () -> criteriaAttributeValidator.validateForEdit(context));
        assertNotNull(e);
        assertTrue(e.getErrorMessages().contains("criteriaAttribute.edit.emptyStartDay"));
    }

    @Test
    public void validateForEdit_EndDayIsNotNullAndNotRequired_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_EndDayNullAndNotRequired_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();
        context.getCriteriaAttribe().setEndDay(null);

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_EndDayIsNotNullAndIsRequired_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();
        context.setEndDayRequired(true);

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_EndDayIsNullAndIsRequired_ThrowsExcptionWithRelevantMessage() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();
        context.getCriteriaAttribe().setEndDay(null);
        context.setEndDayRequired(true);

        ValidationException e = assertThrows(ValidationException.class, () -> criteriaAttributeValidator.validateForEdit(context));
        assertNotNull(e);
        assertTrue(e.getErrorMessages().contains("criteriaAttribute.edit.emptyEndDay"));
    }

    @Test
    public void validateForEdit_RequiredDayIsNotNullAndNotRequired_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_RequiredDayNullAndNotRequired_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();
        context.getCriteriaAttribe().setRequiredDay(null);

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_RequiredDayIsNotNullAndIsRequired_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();
        context.setRequiredDayRequired(true);

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_RequiredDayIsNullAndIsRequire_ThrowsExcptionWithRelevantMessage() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();
        context.getCriteriaAttribe().setRequiredDay(null);
        context.setRequiredDayRequired(true);

        ValidationException e = assertThrows(ValidationException.class, () -> criteriaAttributeValidator.validateForEdit(context));
        assertNotNull(e);
        assertTrue(e.getErrorMessages().contains("criteriaAttribute.edit.emptyRequiredDay"));
    }

    @Test
    public void validateForEdit_CriteriaListIsNotNull_DoesNotThrowExcption() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForEdit_CriteriaListIsNull_ThrowsExcptionWithRelevantMessage() {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();
        context.getCriteriaAttribe().setCriteria(null);

        ValidationException e = assertThrows(ValidationException.class, () -> criteriaAttributeValidator.validateForEdit(context));
        assertNotNull(e);
        assertTrue(e.getErrorMessages().contains("criteriaAttribute.edit.noCriteria"));
    }

    @Test
    public void validateForEdit_DuplicateCriteriaAttributeOnEdit_ThrowsExcptionWithRelevantMessage() throws EntityRetrievalException {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();

        Mockito.when(criteriaAttributeDAO.getAllAssociatedCriteriaMaps())
                .thenReturn(List.of(CriteriaAttributeCriteriaMap.builder()
                        .criteriaAttribute(CriteriaAttribute.builder()
                                .id(1L)
                                .value("A Value")
                                .regulatoryTextCitation("text citation")
                                .criteria(List.of(CertificationCriterion.builder()
                                        .id(1L)
                                        .number("999.999(a)(1)")
                                        .build()))
                                .startDay(LocalDate.now())
                                .endDay(LocalDate.now())
                                .requiredDay(LocalDate.now())
                                .build())
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("999.999(a)(1)")
                                .build())
                        .build(), CriteriaAttributeCriteriaMap.builder()
                        .criteriaAttribute(CriteriaAttribute.builder()
                                .id(2L)
                                .value("A Value")
                                .regulatoryTextCitation("text citation")
                                .criteria(List.of(CertificationCriterion.builder()
                                        .id(1L)
                                        .number("999.999(a)(1)")
                                        .build()))
                                .startDay(LocalDate.now())
                                .endDay(LocalDate.now())
                                .requiredDay(LocalDate.now())
                                .build())
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("999.999(a)(1)")
                                .build())
                        .build()));

        ValidationException e = assertThrows(ValidationException.class, () -> criteriaAttributeValidator.validateForEdit(context));
        assertNotNull(e);
        assertTrue(e.getErrorMessages().contains("criteriaAttribute.edit.duplicate"));
    }

    @Test
    public void validateForEdit_RemoveAttributeBeingUsedOnExistingListing_ThrowsExcptionWithRelevantMessage() throws EntityRetrievalException {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();

        Mockito.when(criteriaAttributeDAO.getCriteriaAttributeById(ArgumentMatchers.anyLong()))
                .thenReturn(CriteriaAttribute.builder()
                        .id(1L)
                        .value("A Value")
                        .regulatoryTextCitation("text citation")
                        .criteria(List.of(CertificationCriterion.builder()
                                .id(1L)
                                .number("999.999(a)(1)")
                                .build(), CertificationCriterion.builder()
                                .id(2L)
                                .number("999.999(a)(2)")
                                .build()))
                        .startDay(LocalDate.now())
                        .endDay(LocalDate.now())
                        .requiredDay(LocalDate.now())
                        .build());

        Mockito.when(criteriaAttributeDAO.getCertifiedProductsByCriteriaAttributeAndCriteria(ArgumentMatchers.any(CriteriaAttribute.class), ArgumentMatchers.any(CertificationCriterion.class)))
                .thenReturn(List.of(CertifiedProductDetailsDTO.builder()
                        .id(1L)
                        .chplProductNumber("chpl_prod_nbr")
                        .build()));

        ValidationException e = assertThrows(ValidationException.class, () -> criteriaAttributeValidator.validateForEdit(context));
        assertNotNull(e);
        assertTrue(e.getErrorMessages().contains("criteriaAttribute.edit.deletedCriteria.listingsExist chpl_prod_nbr"));
    }

    @Test
    public void validateForEdit_RemoveAttributeNotBeingUsedOnExistingListing_DoesNotThrowExcption() throws EntityRetrievalException {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();

        Mockito.when(criteriaAttributeDAO.getCriteriaAttributeById(ArgumentMatchers.anyLong()))
                .thenReturn(CriteriaAttribute.builder()
                        .id(1L)
                        .value("A Value")
                        .regulatoryTextCitation("text citation")
                        .criteria(List.of(CertificationCriterion.builder()
                                .id(1L)
                                .number("999.999(a)(1)")
                                .build(), CertificationCriterion.builder()
                                .id(2L)
                                .number("999.999(a)(2)")
                                .build()))
                        .startDay(LocalDate.now())
                        .endDay(LocalDate.now())
                        .requiredDay(LocalDate.now())
                        .build());
        Mockito.when(criteriaAttributeDAO.getCertifiedProductsByCriteriaAttributeAndCriteria(ArgumentMatchers.any(CriteriaAttribute.class), ArgumentMatchers.any(CertificationCriterion.class)))
            .thenReturn(List.of());

        assertDoesNotThrow(() -> criteriaAttributeValidator.validateForEdit(context));
    }

    @Test
    public void validateForAdd_DuplicateCriteriaAttributeOnEdit_ThrowsExcptionWithRelevantMessage() throws EntityRetrievalException {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();

        context.getCriteriaAttribe().setId(null);

        Mockito.when(criteriaAttributeDAO.getAllAssociatedCriteriaMaps())
                .thenReturn(List.of(CriteriaAttributeCriteriaMap.builder()
                        .criteriaAttribute(CriteriaAttribute.builder()
                                .id(1L)
                                .value("A Value")
                                .regulatoryTextCitation("text citation")
                                .criteria(List.of(CertificationCriterion.builder()
                                        .id(1L)
                                        .number("999.999(a)(1)")
                                        .build()))
                                .startDay(LocalDate.now())
                                .endDay(LocalDate.now())
                                .requiredDay(LocalDate.now())
                                .build())
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("999.999(a)(1)")
                                .build())
                        .build(), CriteriaAttributeCriteriaMap.builder()
                        .criteriaAttribute(CriteriaAttribute.builder()
                                .id(2L)
                                .value("A Value")
                                .regulatoryTextCitation("text citation")
                                .criteria(List.of(CertificationCriterion.builder()
                                        .id(1L)
                                        .number("999.999(a)(1)")
                                        .build()))
                                .startDay(LocalDate.now())
                                .endDay(LocalDate.now())
                                .requiredDay(LocalDate.now())
                                .build())
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("999.999(a)(1)")
                                .build())
                        .build()));

        ValidationException e = assertThrows(ValidationException.class, () -> criteriaAttributeValidator.validateForAdd(context));
        assertNotNull(e);
        assertTrue(e.getErrorMessages().contains("criteriaAttribute.edit.duplicate"));
    }

    @Test
    public void validateForDelete_CriteriaAttributeBeingUsedOnExistingListing_ThrowsExcptionWithRelevantMessage() throws EntityRetrievalException {
        CriteriaAttributeValidationContext context = CriteriaAttributeValidationContext.builder()
                .criteriaAttributeDAO(criteriaAttributeDAO)
                .criteriaAttribe(getStandardCriteriaAttrbute())
                .name("Criteria Attribute")
                .build();

        Mockito.when(criteriaAttributeDAO.getCertifiedProductsByCriteriaAttribute(ArgumentMatchers.any(CriteriaAttribute.class)))
                .thenReturn(List.of(CertifiedProductDetailsDTO.builder()
                        .id(1L)
                        .chplProductNumber("chpl_prod_nbr")
                        .build()));

        ValidationException e = assertThrows(ValidationException.class, () -> criteriaAttributeValidator.validateForDelete(context));
        assertNotNull(e);
        assertTrue(e.getErrorMessages().contains("criteriaAttribute.delete.listingsExist chpl_prod_nbr"));
    }

    private CriteriaAttribute getStandardCriteriaAttrbute() {
        return CriteriaAttribute.builder()
                .id(1L)
                .value("A Value")
                .regulatoryTextCitation("text citation")
                .criteria(List.of(CertificationCriterion.builder()
                        .id(1L)
                        .number("999.999(a)(1)")
                        .build()))
                .startDay(LocalDate.now())
                .endDay(LocalDate.now())
                .requiredDay(LocalDate.now())
                .build();
    }

}
