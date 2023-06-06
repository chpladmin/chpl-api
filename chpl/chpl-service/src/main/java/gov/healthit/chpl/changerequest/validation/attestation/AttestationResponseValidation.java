package gov.healthit.chpl.changerequest.validation.attestation;

import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestStatusService;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class AttestationResponseValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        Form form =  ((ChangeRequestAttestationSubmission) context.getNewChangeRequest().getDetails()).getForm();
        Long attestationPeriodId = ((ChangeRequestAttestationSubmission) context.getNewChangeRequest().getDetails()).getAttestationPeriod().getId();
        Long developerId = context.getNewChangeRequest().getDeveloper().getId();

        if (shouldUserReceiveAcbResponseWarnings(context)
                && isChangeRequestBeingAccepted(context)) {
            if (context.getAttestationResponseValidationService().isApiApplicableAndResponseIsNotApplicable(developerId, form)) {
                getMessages().add(getErrorMessage("attestation.acb.apiApplicableNotConsistent"));
            } else if (context.getAttestationResponseValidationService().isApiNotApplicableAndResponseIsCompliant(developerId, form)) {
                getMessages().add(getErrorMessage("attestation.acb.apiNotApplicableNotConsistent"));
            }

            if (context.getAttestationResponseValidationService().isAssurancesApplicableAndResponseIsNotApplicable(
                    developerId, form, attestationPeriodId)) {
                getMessages().add(getErrorMessage("attestation.acb.assurancesApplicableNotConsistent"));
            } else if (context.getAttestationResponseValidationService().isAssurancesNotApplicableAndResponseIsCompliant(
                    developerId, form, attestationPeriodId)) {
                getMessages().add(getErrorMessage("attestation.acb.assurancesNotApplicableNotConsistent"));
            }

            if (context.getAttestationResponseValidationService().isRwtApplicableAndResponseIsNotApplicable(developerId, form)) {
                getMessages().add(getErrorMessage("attestation.acb.rwtApplicableNotConsistent"));
            } else if (context.getAttestationResponseValidationService().isRwtNotApplicableAndResponseIsCompliant(developerId, form)) {
                getMessages().add(getErrorMessage("attestation.acb.rwtNotApplicableNotConsistent"));
            }
        }
        return getMessages().size() == 0;
    }

    private boolean shouldUserReceiveAcbResponseWarnings(ChangeRequestValidationContext context) {
        return context.getResourcePermissions().isUserRoleAcbAdmin()
                || context.getResourcePermissions().isUserRoleOnc()
                || context.getResourcePermissions().isUserRoleAdmin();
    }

    private boolean isChangeRequestBeingAccepted(ChangeRequestValidationContext context) {
        return ChangeRequestStatusService.doesCurrentStatusExist(context.getNewChangeRequest())
                && context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId()
                    .equals(context.getChangeRequestStatusIds().getAcceptedStatus());
    }
}
