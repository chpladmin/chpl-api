package gov.healthit.chpl.changerequest.domain.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;

public abstract class ChangeRequestDetailsService<T> {

    @Value("${changerequest.status.pendingdeveloperaction}")
    protected Long pendingDeveloperActionStatus;

    @Value("${changerequest.status.accepted}")
    protected Long acceptedStatus;

    @Value("${changerequest.status.rejected}")
    protected Long rejectedStatus;

    @Value("${changerequest.status.cancelledbyrequester}")
    private Long cancelledByRequesterStatus;

    private UserDeveloperMapDAO userDeveloperMapDAO;

    @Autowired
    public ChangeRequestDetailsService(UserDeveloperMapDAO userDeveloperMapDAO) {
        this.userDeveloperMapDAO = userDeveloperMapDAO;
    }

    public ChangeRequest postStatusChangeProcessing(ChangeRequest cr) throws EmailNotSentException {
        try {
            if (cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(pendingDeveloperActionStatus)) {
                sendPendingDeveloperActionEmail(cr);
            } else if (cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(rejectedStatus)) {
                sendRejectedEmail(cr);
            } else if (cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(acceptedStatus)) {
                cr = execute(cr);
                sendApprovalEmail(cr);
            } else if (cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(cancelledByRequesterStatus)) {
                sendCancelledEmail(cr);
            }
        } catch (EmailNotSentException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cr;
    }

    public abstract T getByChangeRequestId(Long changeRequestId, Long developerId) throws EntityRetrievalException;

    public abstract ChangeRequest create(ChangeRequest cr);

    public abstract ChangeRequest update(ChangeRequest cr) throws InvalidArgumentsException;

    public abstract List<CertificationBody> getAssociatedCertificationBodies(ChangeRequest cr);

    protected abstract ChangeRequest execute(ChangeRequest cr) throws EntityRetrievalException, EntityCreationException;
    protected abstract void sendApprovalEmail(ChangeRequest cr) throws EmailNotSentException;
    protected abstract void sendPendingDeveloperActionEmail(ChangeRequest cr) throws EmailNotSentException;
    protected abstract void sendRejectedEmail(ChangeRequest cr) throws EmailNotSentException;
    protected abstract void sendCancelledEmail(ChangeRequest cr) throws EmailNotSentException;

    protected String getApprovalBody(ChangeRequest cr) {
        if (cr.getCurrentStatus().getCertificationBody() != null) {
            return cr.getCurrentStatus().getCertificationBody().getName();
        } else if (isUserGroupAdmin(cr.getCurrentStatus().getUserGroupName())) {
            return "CHPL Admin";
        } else if (isUserGroupOnc(cr.getCurrentStatus().getUserGroupName())) {
            return "ONC";
        } else {
            return "";
        }
    }

    protected List<UserDTO> getUsersForDeveloper(Long developerId) {
        return userDeveloperMapDAO.getByDeveloperId(developerId).stream()
                .map(userDeveloperMap -> userDeveloperMap.getUser())
                .collect(Collectors.<UserDTO>toList());
    }

    private Boolean isUserGroupAdmin(String userGroupName) {
        return userGroupName.equals(Authority.ROLE_ADMIN)
                || userGroupName.equals(CognitoGroups.CHPL_ADMIN);
    }

    private Boolean isUserGroupOnc(String userGroupName) {
        return userGroupName.equals(Authority.ROLE_ONC)
                || userGroupName.equals(CognitoGroups.CHPL_ONC);
    }
}
