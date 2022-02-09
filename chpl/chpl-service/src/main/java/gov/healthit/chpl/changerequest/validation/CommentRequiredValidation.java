package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.changerequest.domain.service.ChangeRequestStatusService;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class CommentRequiredValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (ChangeRequestStatusService.doesCurrentStatusExist(context.getNewChangeRequest())) {
            if (isStatusChange(context) && doesNewStatusRequireComment(context)) {
                if (StringUtils.isEmpty(context.getNewChangeRequest().getCurrentStatus().getComment())) {
                    getMessages().add(getErrorMessage("changeRequest.status.changeRequiresComment"));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isStatusChange(ChangeRequestValidationContext context) {
        return !context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId().equals(
                context.getOrigChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId());

    }

    private boolean doesNewStatusRequireComment(ChangeRequestValidationContext context) {
        List<Long> statusesRequiringComment = new ArrayList<Long>(Arrays.asList(
                context.getChangeRequestStatusIds().getRejectedStatus(),
                context.getChangeRequestStatusIds().getPendingDeveloperActionStatus()));

        return statusesRequiringComment.stream()
                .anyMatch(status -> status
                        .equals(context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId()));
    }

}
