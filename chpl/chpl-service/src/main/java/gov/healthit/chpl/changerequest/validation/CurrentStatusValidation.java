package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
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
    public List<String> getErrorMessages(ChangeRequestValidationContext context) {
        List<String> errorMessages = new ArrayList<String>();

        // It's fine if it's not set... We aren't going to do anything with it.
        if (!doesCurrentStatusExist(context.getNewChangeRequest())) {
            return errorMessages;
        }

        // Make sure the current status type is is valid
        // Does it exist in the DB?
        try {
            crStatusTypeDAO.getChangeRequestStatusTypeById(
                    context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId());
        } catch (EntityRetrievalException e) {
            errorMessages.add(getErrorMessageFromResource("changeRequest.statusType.notExists"));
            return errorMessages;
        }


        Long statusTypeId = context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId();
        // Is this a valid status change based on the user's role?
        if (resourcePermissions.isUserRoleDeveloperAdmin()
                && !getValidStatusesForDeveloper().contains(statusTypeId)) {

            errorMessages.add(getErrorMessageFromResource("changeRequest.statusType.invalid"));
        } else if ((resourcePermissions.isUserRoleAcbAdmin()
                || resourcePermissions.isUserRoleOnc()
                || resourcePermissions.isUserRoleAdmin())
                && !getValidStatusesForChangeRequestAdmin().contains(statusTypeId)) {

            errorMessages.add(getErrorMessageFromResource("changeRequest.statusType.invalid"));
        }
        return errorMessages;
    }

    private List<Long> getValidStatusesForDeveloper() {
        return new ArrayList<Long>(Arrays.asList(cancelledStatus, pendingAcbActionStatus));
    }

    private List<Long> getValidStatusesForChangeRequestAdmin() {
        return new ArrayList<Long>(Arrays.asList(acceptedStatus, rejectedStatus, pendingDeveloperActionStatus));
    }

    private Boolean doesCurrentStatusExist(ChangeRequest cr) {
        return cr.getCurrentStatus() == null
                || cr.getCurrentStatus().getChangeRequestStatusType() == null
                || cr.getCurrentStatus().getChangeRequestStatusType().getId() == null;
    }
}
