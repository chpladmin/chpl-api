package gov.healthit.chpl.changerequest.validation.attestation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.form.FormItem;
import gov.healthit.chpl.form.validation.FormValidationResult;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class AttestationValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        ChangeRequestAttestationSubmission attestationSubmission =
                (ChangeRequestAttestationSubmission) context.getNewChangeRequest().getDetails();

        getMessages().addAll(isAllowedToSubmitAttestationPeriod(context, attestationSubmission));
        if (isChangeRequestNew(context)) {
            getMessages().addAll(canDeveloperSubmitChangeRequest(context));
            getMessages().addAll(validateSignature(context, attestationSubmission));
        } else if (hasAttestationInformationChanged(context)) {
            getMessages().addAll(validateSignature(context, attestationSubmission));
        }

        Form formToValidate =  ((ChangeRequestAttestationSubmission) context.getNewChangeRequest().getDetails()).getForm();
        FormValidationResult formValidationResult = context.getFormValidator().validate(formToValidate);

        getMessages().addAll(formValidationResult.getErrorMessages());
        return getMessages().size() == 0;
    }

    private List<String> isAllowedToSubmitAttestationPeriod(ChangeRequestValidationContext context,
            ChangeRequestAttestationSubmission attestationSubmission) {
        List<String> errors = new ArrayList<String>();
        try {
            AttestationPeriod submittableAttestationPeriod
                = context.getAttestationPeriodService().getSubmittableAttestationPeriod(context.getNewChangeRequest().getDeveloper().getId());
            if (submittableAttestationPeriod == null
                    || attestationSubmission.getAttestationPeriod() == null
                    || attestationSubmission.getAttestationPeriod().getId() == null
                    || !attestationSubmission.getAttestationPeriod().getId().equals(submittableAttestationPeriod.getId())) {
                errors.add(getErrorMessage("changeRequest.attestation.attestationPeriodMissing"));
            }
        } catch (Exception e) {
            errors.add(getErrorMessage("changeRequest.attestation.attestationPeriodMissing"));
        }
        return errors;
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

        return !CollectionUtils.isEqualCollection(origFormItems, newFormItems, new FormItem.FormItemByIdEquator());
    }

}
