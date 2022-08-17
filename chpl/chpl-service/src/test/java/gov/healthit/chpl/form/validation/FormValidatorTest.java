package gov.healthit.chpl.form.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.AllowedResponse;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.form.FormItem;
import gov.healthit.chpl.form.FormService;
import gov.healthit.chpl.form.Question;
import gov.healthit.chpl.form.ResponseCardinalityType;
import gov.healthit.chpl.form.SectionHeading;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class FormValidatorTest {
    private FormService formService;
    private ErrorMessageUtil errorMessageUtil;

    private FormValidator formValidator;

    @Before
    public void setup() {
        formService = Mockito.mock(FormService.class);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        formValidator = new FormValidator(formService, errorMessageUtil);
    }

    @Test
    public void validate_RequiredQuestionNotAnswered_ReturnsFalseAndErrorMessage() throws EntityRetrievalException {
        Mockito.when(formService.getForm(ArgumentMatchers.anyLong())).thenReturn(getFormWithChildItems());
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));

        Form formToValidate = getFormWithChildItems();
        formToValidate.getSectionHeadings().get(0).getFormItems().get(0).getSubmittedResponses().add(
                AllowedResponse.builder()
                    .id(1L)
                    .response("Yes")
                    .build());

        FormValidationResult result = formValidator.validate(formToValidate);

        assertNotNull(result);
        assertFalse(result.getValid());
        assertNotNull(result.getErrorMessages());
        assertTrue(result.getErrorMessages().contains("form.formItem.required"));
    }

    @Test
    public void validate_ResponsesDoMatchCardinality_ReturnsFalseAndErrorMessage() throws EntityRetrievalException {
        Mockito.when(formService.getForm(ArgumentMatchers.anyLong())).thenReturn(getFormWithChildItems());
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));

        Form formToValidate = getFormWithChildItems();
        formToValidate.getSectionHeadings().get(0).getFormItems().get(0).getSubmittedResponses().add(
                AllowedResponse.builder()
                    .id(1L)
                    .response("Yes")
                    .build());
        formToValidate.getSectionHeadings().get(0).getFormItems().get(0).getSubmittedResponses().add(
                AllowedResponse.builder()
                    .id(2L)
                    .response("No")
                    .build());
        formToValidate.getSectionHeadings().get(1).getFormItems().get(0).getSubmittedResponses().add(
                AllowedResponse.builder()
                    .id(1L)
                    .response("Yes")
                    .build());

        FormValidationResult result = formValidator.validate(formToValidate);

        assertNotNull(result);
        assertFalse(result.getValid());
        assertNotNull(result.getErrorMessages());
        assertTrue(result.getErrorMessages().contains("form.formItem.cardinalitySingle"));
    }

    @Test
    public void validate_ResponseNotInAllowedList_ReturnsFalseAndErrorMessage() throws EntityRetrievalException {
        Mockito.when(formService.getForm(ArgumentMatchers.anyLong())).thenReturn(getFormWithChildItems());
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));

        Form formToValidate = getFormWithChildItems();
        formToValidate.getSectionHeadings().get(0).getFormItems().get(0).getSubmittedResponses().add(
                AllowedResponse.builder()
                    .id(3L)
                    .response("More Than 10")
                    .build());
        formToValidate.getSectionHeadings().get(1).getFormItems().get(0).getSubmittedResponses().add(
                AllowedResponse.builder()
                    .id(1L)
                    .response("Yes")
                    .build());

        FormValidationResult result = formValidator.validate(formToValidate);

        assertNotNull(result);
        assertFalse(result.getValid());
        assertNotNull(result.getErrorMessages());
        assertTrue(result.getErrorMessages().contains("form.formItem.invalidResponse"));
    }

    private Form getFormWithChildItems() {
        return Form.builder()
                .id(1L)
                .sectionHeading(SectionHeading.builder()
                        .id(1L)
                        .name("Section 1")
                        .sortOrder(1)
                        .formItem(FormItem.builder()
                                .id(1L)
                                .question(Question.builder()
                                        .id(1L)
                                        .responseCardinalityType(ResponseCardinalityType.builder()
                                                .id(1L)
                                                .description("Single")
                                                .build())
                                        .question("Question #1")
                                        .allowedResponse(AllowedResponse.builder()
                                                .id(1L)
                                                .response("Yes")
                                                .build())
                                        .allowedResponse(AllowedResponse.builder()
                                                .id(2L)
                                                .response("No")
                                                .build())
                                        .build())
                                .required(true)
                                .sortOrder(1)
                                .childFormItem(FormItem.builder()
                                        .id(2L)
                                        .question(Question.builder()
                                                .id(2L)
                                                .responseCardinalityType(ResponseCardinalityType.builder()
                                                        .id(2L)
                                                        .description("Multiple")
                                                        .build())
                                                .question("Question #2")
                                                .allowedResponse(AllowedResponse.builder()
                                                        .id(3L)
                                                        .response("More than 10")
                                                        .build())
                                                .allowedResponse(AllowedResponse.builder()
                                                        .id(4L)
                                                        .response("Less Than 20")
                                                        .build())
                                                .build())
                                                .parentResponse(AllowedResponse.builder()
                                                        .id(1L)
                                                        .response("Yes")
                                                        .build())
                                        .required(false)
                                        .sortOrder(1)
                                        .build())
                                .build()
                                )
                        .build())
                .sectionHeading(SectionHeading.builder()
                        .id(1L)
                        .name("Section 1")
                        .sortOrder(1)
                        .formItem(FormItem.builder()
                                .id(3L)
                                .question(Question.builder()
                                        .id(1L)
                                        .responseCardinalityType(ResponseCardinalityType.builder()
                                                .id(1L)
                                                .description("Single")
                                                .build())
                                        .question("Question #3")
                                        .allowedResponse(AllowedResponse.builder()
                                                .id(1L)
                                                .response("Yes")
                                                .build())
                                        .allowedResponse(AllowedResponse.builder()
                                                .id(2L)
                                                .response("No")
                                                .build())
                                        .build())
                                .required(true)
                                .sortOrder(1)
                                .build())
                        .build())
                .build();

    }
}
