package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class CommentRequiredValidation extends ValidationRule<ChangeRequestValidationContext> {
    @Value("${changerequest.status.rejected}")
    private Long rejectedStatus;

    @Value("${changerequest.status.pendingdeveloperaction}")
    private Long pendingDeveloperAction;

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (isStatusChange(context) && doesNewStatusRequireComment(context)) {
            if (StringUtils.isEmpty(context.getChangeRequest().getCurrentStatus().getComment())) {
                getMessages().add(getErrorMessage("changeRequest.status.changeRequiresComment"));
                return false;
            }
        }
        return true;
    }

    private boolean isStatusChange(ChangeRequestValidationContext context) {
        return !context.getChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId().equals(
                context.getCrFromDb().getCurrentStatus().getChangeRequestStatusType().getId());

    }

    private boolean doesNewStatusRequireComment(ChangeRequestValidationContext context) {
        List<Long> statusesRequiringComment = new ArrayList<Long>(
                Arrays.asList(rejectedStatus, pendingDeveloperAction));

        return statusesRequiringComment.stream()
                .anyMatch(status -> status
                        .equals(context.getChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId()));
    }

}
