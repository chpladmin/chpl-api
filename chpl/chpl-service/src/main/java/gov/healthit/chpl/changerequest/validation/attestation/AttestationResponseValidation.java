package gov.healthit.chpl.changerequest.validation.attestation;

import java.util.List;

import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestStatusService;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.search.domain.ListingSearchResult;

public class AttestationResponseValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        Form form =  ((ChangeRequestAttestationSubmission) context.getNewChangeRequest().getDetails()).getForm();
        Long attestationPeriodId = ((ChangeRequestAttestationSubmission) context.getNewChangeRequest().getDetails()).getAttestationPeriod().getId();
        Long developerId = context.getNewChangeRequest().getDeveloper().getId();
        List<ListingSearchResult> activeListingsForDeveloper = context.getListingSearchService().findActiveListingsForDeveloper(developerId);

        if (shouldUserReceiveAcbResponseWarnings(context)
                && isChangeRequestBeingAccepted(context)) {
            if (context.getAttestationResponseValidationService().isApiApplicableAndResponseIsNotApplicable(activeListingsForDeveloper, form)) {
                getMessages().add(getErrorMessage("attestation.acb.apiApplicableNotConsistent"));
            } else if (context.getAttestationResponseValidationService().isApiNotApplicableAndResponseIsCompliant(activeListingsForDeveloper, form)) {
                getMessages().add(getErrorMessage("attestation.acb.apiNotApplicableNotConsistent"));
            }

            if (context.getAttestationResponseValidationService().isAssurancesApplicableAndResponseIsNotApplicable(
                    activeListingsForDeveloper, form, attestationPeriodId)) {
                getMessages().add(getErrorMessage("attestation.acb.assurancesApplicableNotConsistent"));
            } else if (context.getAttestationResponseValidationService().isAssurancesNotApplicableAndResponseIsCompliant(
                    activeListingsForDeveloper, form, attestationPeriodId)) {
                getMessages().add(getErrorMessage("attestation.acb.assurancesNotApplicableNotConsistent"));
            }

            if (context.getAttestationResponseValidationService().isRwtApplicableAndResponseIsNotApplicable(activeListingsForDeveloper, form)) {
                getMessages().add(getErrorMessage("attestation.acb.rwtApplicableNotConsistent"));
            } else if (context.getAttestationResponseValidationService().isRwtNotApplicableAndResponseIsCompliant(activeListingsForDeveloper, form)) {
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
