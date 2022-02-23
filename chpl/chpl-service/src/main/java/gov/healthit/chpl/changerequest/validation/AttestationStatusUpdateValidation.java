package gov.healthit.chpl.changerequest.validation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bouncycastle.util.Objects;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class AttestationStatusUpdateValidation extends ValidationRule<ChangeRequestValidationContext> {


    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        try {
            if (!Objects.areEqual(context.getOrigChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId(), context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId())
                    && getNonAllowedStatusesForUpdate(context).contains(context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId())) {
                getMessages().add(getErrorMessage(context, context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType()));
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            // This will be caught by other validators
            return true;
        }
    }

    private List<Long> getNonAllowedStatusesForUpdate(ChangeRequestValidationContext context) {
        return Stream.of(
                context.getChangeRequestStatusIds().getPendingAcbActionStatus(),
                context.getChangeRequestStatusIds().getPendingDeveloperActionStatus())
                .collect(Collectors.toList());
    }

    private String getErrorMessage(ChangeRequestValidationContext context, ChangeRequestStatusType changeRequestStatusType) {
        return String.format(getErrorMessage("changeRequest.attestation.statusNotAllowed"),
                context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getName());
    }
}
