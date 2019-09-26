package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;

@Component
public class CurrentStatusValidation extends ValidationRule<ChangeRequestValidationContext> {

    private ChangeRequestStatusTypeDAO crStatusTypeDAO;
    private ResourcePermissions resourcePermissions;

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbActionStatus;

    @Value("${changerequest.status.pendingdeveloperaction}")
    private Long pendingDeveloperActionStatus;

    @Value("${changerequest.status.cancelledbyrequester}")
    private Long cancelledStatus;

    @Value("${changerequest.status.accepted}")
    private Long acceptedStatus;

    @Value("${changerequest.status.rejected}")
    private Long rejectedStatus;

    @Autowired
    public CurrentStatusValidation(final ChangeRequestStatusTypeDAO crStatusTypeDAO,
            final ResourcePermissions resourcePermissions) {
        this.crStatusTypeDAO = crStatusTypeDAO;
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // It's fine if it's not set... We aren't going to do anything with it.
        if (context.getChangeRequest().getCurrentStatus() == null
                || context.getChangeRequest().getCurrentStatus().getChangeRequestStatusType() == null
                || context.getChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId() == null) {
            return true;
        }

        // Make sure the current status type is is valid
        // Does it exist in the DB?
        try {
            crStatusTypeDAO.getChangeRequestStatusTypeById(
                    context.getChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId());
        } catch (EntityRetrievalException e) {
            getMessages().add(getErrorMessage("changeRequest.statusType.notExists"));
            return false;
        }

        Long statusTypeId = context.getChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId();
        // Is this a valid status change based on the user's role?
        if (resourcePermissions.isUserRoleDeveloperAdmin()
                && !getValidStatusesForDeveloper().contains(statusTypeId)) {

            getMessages().add(getErrorMessage("changeRequest.statusType.invalid"));
            return false;
        } else if ((resourcePermissions.isUserRoleAcbAdmin()
                || resourcePermissions.isUserRoleOnc()
                || resourcePermissions.isUserRoleAdmin())
                && !getValidStatusesForChangeRequestAdmin().contains(statusTypeId)) {

            getMessages().add(getErrorMessage("changeRequest.statusType.invalid"));
            return false;
        }
        return true;
    }

    private List<Long> getValidStatusesForDeveloper() {
        return new ArrayList<Long>(Arrays.asList(cancelledStatus, pendingAcbActionStatus));
    }

    private List<Long> getValidStatusesForChangeRequestAdmin() {
        return new ArrayList<Long>(Arrays.asList(acceptedStatus, rejectedStatus, pendingDeveloperActionStatus));
    }
}
