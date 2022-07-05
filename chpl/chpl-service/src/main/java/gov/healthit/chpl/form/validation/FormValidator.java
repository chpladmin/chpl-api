package gov.healthit.chpl.form.validation;

import java.util.ArrayList;
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
        List<FormItem> rolledUpFormItems = formToValidate.getSectionHeadings().stream()
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
    }

    private List<String> validateFormItem(FormItem formItemToValidate) {
        try {
            List<String> errorMessages = new ArrayList<String>();
            FormItem cleanFormItem = getMatchingFormItem(formItemToValidate.getId());

            errorMessages.addAll(validateRequired(formItemToValidate, cleanFormItem));
            errorMessages.addAll(validateCardinality(formItemToValidate, cleanFormItem));
            errorMessages.addAll(validateResponses(formItemToValidate, cleanFormItem));

            formItemToValidate.getChildFormItems().stream()
                    .forEach(fi -> errorMessages.addAll(validateFormItem(fi)));

            return errorMessages;
        } catch (EntityRetrievalException e) {
            return List.of("There was an unexpected exception validation the Form.");
        }
    }

    private List<String> validateRequired(FormItem formItemToValidate, FormItem cleanFormItem) {
        List<String> errorMessages = new ArrayList<String>();

        if (cleanFormItem.getRequired()
                && CollectionUtils.isEmpty(formItemToValidate.getSubmittedResponses())) {
            errorMessages.add(errorMessageService.getMessage("form.formItem.required", cleanFormItem.getQuestion().getQuestion()));
        }
        return errorMessages;
    }

    private List<String> validateCardinality(FormItem formItemToValidate, FormItem cleanFormItem) {
        List<String> errorMessages = new ArrayList<String>();

        if (cleanFormItem.getQuestion().getResponseCardinalityType().getId().equals(1L)
                && !CollectionUtils.isEmpty(formItemToValidate.getSubmittedResponses())
                && formItemToValidate.getSubmittedResponses().size() > 1) {
            errorMessages.add(errorMessageService.getMessage("form.formItem.cardinalitySingle", cleanFormItem.getQuestion().getQuestion()));
        }
        return errorMessages;
    }

    private List<String> validateResponses(FormItem formItemToValidate, FormItem cleanFormItem) {
        List<String> errorMessages = new ArrayList<String>();

        if (!CollectionUtils.isEmpty(formItemToValidate.getSubmittedResponses())) {
            for (AllowedResponse resp : formItemToValidate.getSubmittedResponses()) {
                if (cleanFormItem.getQuestion().getAllowedResponses().stream()
                        .filter(allowedResponse -> allowedResponse.getId().equals(resp.getId()))
                        .findAny()
                        .isEmpty()) {

                    errorMessages.add(errorMessageService.getMessage("form.formItem.invalidResponse", resp.getResponse(), cleanFormItem.getQuestion().getQuestion()));
                }
            }
        }

        return errorMessages;
    }

    private FormItem getMatchingFormItem(Long formItemId) throws EntityRetrievalException {
        return formService.getFormItem(formItemId);
    }

    private List<FormItem> gatherAllFormItems(List<FormItem> formItems) {
        List<FormItem> accumulatedFormItems = new ArrayList<FormItem>();
        formItems.forEach(fi -> {
            accumulatedFormItems.add(fi);
            accumulatedFormItems.addAll(gatherAllFormItems(fi.getChildFormItems()));
        });

        return accumulatedFormItems;
    }

}
