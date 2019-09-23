package gov.healthit.chpl.changerequest.manager;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class ChangeRequestStatusHelper {

    @Value("${changerequest.status.cancelledbyrequester}")
    private Long cancelledByRequesterStatus;

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbActionStatus;

    private ChangeRequestStatusDAO crStatusDAO;
    private ChangeRequestStatusTypeDAO crStatusTypeDAO;

    @Autowired
    public ChangeRequestStatusHelper(final ChangeRequestStatusDAO crStatusDAO,
            final ChangeRequestStatusTypeDAO crStatusTypeDAO) {
        this.crStatusDAO = crStatusDAO;
        this.crStatusTypeDAO = crStatusTypeDAO;
    }

    public ChangeRequestStatus saveInitialStatus(ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequestStatusType crStatusType = new ChangeRequestStatusType();
        crStatusType.setId(this.pendingAcbActionStatus);

        ChangeRequestStatus crStatus = new ChangeRequestStatus();
        crStatus.setStatusChangeDate(new Date());
        crStatus.setChangeRequestStatusType(crStatusType);

        return crStatusDAO.create(cr, crStatus);
    }

    public ChangeRequestStatus updateChangeRequestStatus(ChangeRequest crFromDb, ChangeRequest crFromCaller)
            throws EntityRetrievalException {
        // Check for nulls - a Java 8 way to check a chain of objects for null
        Long statusTypeIdFromDB = Optional.ofNullable(crFromDb)
                .map(ChangeRequest::getCurrentStatus)
                .map(ChangeRequestStatus::getChangeRequestStatusType)
                .map(ChangeRequestStatusType::getId)
                .orElse(null);
        Long statusTypeIdFromCaller = Optional.ofNullable(crFromCaller)
                .map(ChangeRequest::getCurrentStatus)
                .map(ChangeRequestStatus::getChangeRequestStatusType)
                .map(ChangeRequestStatusType::getId)
                .orElse(null);

        if (statusTypeIdFromDB != null && statusTypeIdFromCaller != null
                && statusTypeIdFromDB != statusTypeIdFromCaller
                && isStatusChangeValid(statusTypeIdFromDB, statusTypeIdFromCaller)) {
            return saveNewStatusForChangeRequest(crFromDb, statusTypeIdFromCaller,
                    crFromCaller.getCurrentStatus().getComment());
        } else {
            return null;
        }
    }

    private boolean isStatusChangeValid(final Long previousStatusTypeId, final Long newStatusTypeId) {
        // Does this status type id exist?
        try {
            crStatusTypeDAO.getChangeRequestStatusTypeById(newStatusTypeId);
        } catch (EntityRetrievalException e) {
            return false;
        }

        // Cannot change status is Cancelled
        if (previousStatusTypeId == this.cancelledByRequesterStatus) {
            return false;
        }
        return true;
    }

    private ChangeRequestStatus saveNewStatusForChangeRequest(final ChangeRequest cr, final Long crStatusTypeId,
            final String comment) throws EntityRetrievalException {
        ChangeRequestStatus crStatus = new ChangeRequestStatus();
        ChangeRequestStatusType crStatusType = new ChangeRequestStatusType();
        crStatusType.setId(crStatusTypeId);
        crStatus.setChangeRequestStatusType(crStatusType);
        crStatus.setComment(comment);
        crStatus.setStatusChangeDate(new Date());

        return crStatusDAO.create(cr, crStatus);
    }

}
