package gov.healthit.chpl.changerequest.domain.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public abstract class ChangeRequestDetailsService<T> {

    @Value("${changerequest.status.pendingdeveloperaction}")
    protected Long pendingDeveloperActionStatus;

    @Value("${changerequest.status.accepted}")
    protected Long acceptedStatus;

    @Value("${changerequest.status.rejected}")
    protected Long rejectedStatus;

    @Value("${user.permission.onc}")
    private Long oncPermission;

    @Value("${user.permission.admin}")
    private Long adminPermission;

    private UserDeveloperMapDAO userDeveloperMapDAO;

    @Autowired
    public ChangeRequestDetailsService(UserDeveloperMapDAO userDeveloperMapDAO) {
        this.userDeveloperMapDAO = userDeveloperMapDAO;
    }

    public ChangeRequest postStatusChangeProcessing(ChangeRequest cr) {
        try {
            if (cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(pendingDeveloperActionStatus)) {
                sendPendingDeveloperActionEmail(cr);
            } else if (cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(rejectedStatus)) {
                sendRejectedEmail(cr);
            } else if (cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(acceptedStatus)) {
                cr = execute(cr);
                sendApprovalEmail(cr);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cr;
    }

    public abstract T getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException;

    public abstract ChangeRequest create(ChangeRequest cr);

    public abstract ChangeRequest update(ChangeRequest cr);

    protected abstract ChangeRequest execute(ChangeRequest cr) throws EntityRetrievalException, EntityCreationException;
    protected abstract void sendApprovalEmail(ChangeRequest cr) throws MessagingException;
    protected abstract void sendPendingDeveloperActionEmail(ChangeRequest cr) throws MessagingException;
    protected abstract void sendRejectedEmail(ChangeRequest cr) throws MessagingException;


    protected String getApprovalBody(ChangeRequest cr) {
        if (cr.getCurrentStatus().getCertificationBody() != null) {
            return cr.getCurrentStatus().getCertificationBody().getName();
        } else if (cr.getCurrentStatus().getUserPermission().getId().equals(adminPermission)) {
            return "the CHPL Admin";
        } else if (cr.getCurrentStatus().getUserPermission().getId().equals(oncPermission)) {
            return "ONC";
        } else {
            return "";
        }
    }

    protected List<UserDTO> getUsersForDeveloper(Long developerId) {
        return userDeveloperMapDAO.getByDeveloperId(developerId).stream()
                .map(userDeveloperMap -> userDeveloperMap.getUser())
                .collect(Collectors.<UserDTO> toList());
    }
}
