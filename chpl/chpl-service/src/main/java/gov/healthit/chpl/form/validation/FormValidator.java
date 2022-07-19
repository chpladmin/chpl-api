package gov.healthit.chpl.form.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.AllowedResponse;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.form.FormItem;
import gov.healthit.chpl.form.FormService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class FormValidator {
    // 1. Are all required questions answered
    // 2. Is the cardinality of the responses correct
    // 3. Are the responses correct based on the allowed responses
    // 4. Subordinate questions...

    private FormService formService;
    private ErrorMessageUtil errorMessageService;

    @Autowired
    public FormValidator(FormService formService, ErrorMessageUtil errorMessageService) {
        this.formService = formService;
        this.errorMessageService = errorMessageService;
    }

    public FormValidationResult validate(Form formToValidate) {
        try {
            Form cleanFormWithResponses = populateCleanFormWithSubmittedResponses(formToValidate);

            List<FormItem> rolledUpFormItems = cleanFormWithResponses.getSectionHeadings().stream()
                    .map(sh -> gatherAllFormItems(sh.getFormItems()).stream())
                    .flatMap(fi -> fi)
                    .toList();
            List<String> errors = rolledUpFormItems.stream()
                    .map(fi -> validateFormItem(fi).stream())
                    .flatMap(fi -> fi)
                    .toList();
            return FormValidationResult.builder()
                    .errorMessages(errors)
                    .valid(errors.size() == 0)
                    .build();

        } catch (EntityRetrievalException e) {
            return FormValidationResult.builder()
                    .errorMessages(List.of("There was an unexpected exception validation the Form."))
                    .valid(false)
                    .build();
        }
    }

    private List<String> validateFormItem(FormItem formItemToValidate) {
        List<String> errorMessages = new ArrayList<String>();

        removeDuplicateResponses(formItemToValidate);

        errorMessages.addAll(validateRequired(formItemToValidate));
        errorMessages.addAll(validateCardinality(formItemToValidate));
        errorMessages.addAll(validateResponses(formItemToValidate));
        errorMessages.addAll(validateChildQuestionResponseAllowed(formItemToValidate));

        formItemToValidate.getChildFormItems().stream()
                .forEach(fi -> errorMessages.addAll(validateFormItem(fi)));

        return errorMessages;
    }

    private List<String> validateRequired(FormItem formItemToValidate) {
        List<String> errorMessages = new ArrayList<String>();

        if (formItemToValidate.getRequired()
                && CollectionUtils.isEmpty(formItemToValidate.getSubmittedResponses())) {
            errorMessages.add(errorMessageService.getMessage("form.formItem.required", formItemToValidate.getQuestion().getQuestion()));
        }
        return errorMessages;
    }

    private List<String> validateCardinality(FormItem formItemToValidate) {
        List<String> errorMessages = new ArrayList<String>();

        if (formItemToValidate.getQuestion().getResponseCardinalityType().getId().equals(1L)
                && !CollectionUtils.isEmpty(formItemToValidate.getSubmittedResponses())
                && formItemToValidate.getSubmittedResponses().size() > 1) {
            errorMessages.add(errorMessageService.getMessage("form.formItem.cardinalitySingle", formItemToValidate.getQuestion().getQuestion()));
        }
        return errorMessages;
    }

    private List<String> validateResponses(FormItem formItemToValidate) {
        List<String> errorMessages = new ArrayList<String>();

        if (!CollectionUtils.isEmpty(formItemToValidate.getSubmittedResponses())) {
            for (AllowedResponse resp : formItemToValidate.getSubmittedResponses()) {
                if (formItemToValidate.getQuestion().getAllowedResponses().stream()
                        .filter(allowedResponse -> allowedResponse.getId().equals(resp.getId()))
                        .findAny()
                        .isEmpty()) {

                    errorMessages.add(errorMessageService.getMessage("form.formItem.invalidResponse", resp.getResponse(), formItemToValidate.getQuestion().getQuestion()));
                }
            }
        }

        return errorMessages;
    }

    private List<String> validateChildQuestionResponseAllowed(FormItem formItemToValidate) {
        List<String> errorMessages = new ArrayList<String>();

        if (formItemToValidate.getChildFormItems() != null && formItemToValidate.getChildFormItems().size() > 0) {
            for (FormItem childFormItem : formItemToValidate.getChildFormItems()) {
                if (childFormItem.getSubmittedResponses() != null && childFormItem.getSubmittedResponses().size() > 0) {
                    List<Long> submittedResponseIds = formItemToValidate.getSubmittedResponses().stream()
                            .map(response -> response.getId())
                            .toList();

                    Boolean isResponseFound = submittedResponseIds.contains(childFormItem.getParentResponse().getId());
                    if (!isResponseFound) {
                        errorMessages.add(errorMessageService.getMessage("form.formItem.childQuestionNotApplicable",
                                childFormItem.getQuestion().getQuestion(),
                                formItemToValidate.getQuestion().getQuestion()));
                    }
                }
            }
        }

        return errorMessages;
    }

    private void removeDuplicateResponses(FormItem formItem) {
        HashSet<Long> seen = new HashSet<Long>();
        formItem.getSubmittedResponses().removeIf(ar -> !seen.add(ar.getId()));
    }

    private List<FormItem> gatherAllFormItems(List<FormItem> formItems) {
        List<FormItem> accumulatedFormItems = new ArrayList<FormItem>();
        formItems.forEach(fi -> {
            accumulatedFormItems.add(fi);
            accumulatedFormItems.addAll(gatherAllFormItems(fi.getChildFormItems()));
        });

        return accumulatedFormItems;
    }

    private Form populateCleanFormWithSubmittedResponses(Form submittedForm) throws EntityRetrievalException {
        Form cleanForm = formService.getForm(submittedForm.getId());

        List<FormItem> submittedFormItems = submittedForm.extractFlatFormItems();
        List<FormItem> cleanFormItems = cleanForm.extractFlatFormItems();

        submittedFormItems.stream()
                .forEach(sfi -> {
                    FormItem matchedCleanFormItem = cleanFormItems.stream()
                            .filter(cfi -> cfi.getId().equals(sfi.getId()))
                            .findAny()
                            .orElse(null);

                    if (matchedCleanFormItem != null) {
                        matchedCleanFormItem.setSubmittedResponses(sfi.getSubmittedResponses());
                    }
                });
        return cleanForm;
    }
}
