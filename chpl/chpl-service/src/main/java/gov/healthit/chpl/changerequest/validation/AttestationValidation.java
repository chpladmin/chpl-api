package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Equator;

import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.AllowedResponse;
import gov.healthit.chpl.form.FormItem;
import gov.healthit.chpl.form.validation.FormValidationResult;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class AttestationValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        ChangeRequestAttestationSubmission attestationSubmission =
                (ChangeRequestAttestationSubmission) context.getNewChangeRequest().getDetails();

        if (isChangeRequestNew(context)) {
            getMessages().addAll(canDeveloperSubmitChangeRequest(context));
            getMessages().addAll(validateSignature(context, attestationSubmission));
        } else if (hasAttestationInformationChanged(context)) {
            getMessages().addAll(validateSignature(context, attestationSubmission));
        }

        FormValidationResult formValidationResult = context.getFormValidator().validate(
                ((ChangeRequestAttestationSubmission) context.getNewChangeRequest().getDetails()).getForm());

        getMessages().addAll(formValidationResult.getErrorMessages());
        return formValidationResult.getValid();
    }

    private List<String> canDeveloperSubmitChangeRequest(ChangeRequestValidationContext context) {
        List<String> errors = new ArrayList<String>();
        try {
            if (!context.getDomainManagers().getAttestationManager().canDeveloperSubmitChangeRequest(context.getNewChangeRequest().getDeveloper().getId())) {
                errors.add(getErrorMessage("changeRequest.attestation.submissionWindow"));
            }
        } catch (EntityRetrievalException e) {
            errors.add(getErrorMessage("changeRequest.attestation.submissionWindow"));
        }
        return errors;
    }

    private Boolean isChangeRequestNew(ChangeRequestValidationContext context) {
        return context.getOrigChangeRequest() == null;
    }

    private List<String> validateSignature(ChangeRequestValidationContext context, ChangeRequestAttestationSubmission attestation) {
        List<String> errors = new ArrayList<String>();
        if (attestation.getSignature() == null || !context.getCurrentUser().getFullName().equals(attestation.getSignature())) {
            errors.add(getErrorMessage("changeRequest.attestation.invalidSignature"));
        }
        return errors;
    }

    private Boolean hasAttestationInformationChanged(ChangeRequestValidationContext context) {
        List<FormItem> origFormItems = ((ChangeRequestAttestationSubmission) context.getOrigChangeRequest().getDetails()).getForm().getSectionHeadings().stream()
                .map(sh -> sh.getFormItems().stream())
                .flatMap(fi -> fi)
                .toList();
        List<FormItem> newFormItems = ((ChangeRequestAttestationSubmission) context.getOrigChangeRequest().getDetails()).getForm().getSectionHeadings().stream()
                .map(sh -> sh.getFormItems().stream())
                .flatMap(fi -> fi)
                .toList();

        return !CollectionUtils.isEqualCollection(origFormItems, newFormItems, new FormItemEquator());
    }

    private static class FormItemEquator implements Equator<FormItem> {
        @Override
        public boolean equate(FormItem o1, FormItem o2) {
           return o1.getId().equals(o2.getId())
                   && CollectionUtils.isEqualCollection(o1.getSubmittedResponses(), o2.getSubmittedResponses(), new AllowedResponseEquator());
        }

        @Override
        public int hash(FormItem o) {
            AllowedResponseEquator equator = new AllowedResponseEquator();
            return o.getId().intValue()
                    + o.getSubmittedResponses().stream()
                        .collect(Collectors.summingInt(resp -> equator.hash(resp)));
        }
    }

    private static class AllowedResponseEquator implements Equator<AllowedResponse> {

        @Override
        public boolean equate(AllowedResponse o1, AllowedResponse o2) {
            return o1.getId().equals(o2.getId());
        }

        @Override
        public int hash(AllowedResponse o) {
            return o.getId().intValue();
        }
    }
}
