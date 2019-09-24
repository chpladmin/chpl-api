package gov.healthit.chpl.changerequest.manager;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;

@Component
public class ChangeRequestStatusHelper {

    @Value("${changerequest.status.cancelledbyrequester}")
    private Long cancelledByRequesterStatus;

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbActionStatus;

    @Value("${changerequest.status.pendingdeveloperaction}")
    private Long pendingDeveloperActionStatus;

    private ChangeRequestStatusDAO crStatusDAO;
    private ChangeRequestStatusTypeDAO crStatusTypeDAO;
    private ChangeRequestDAO crDAO;
    private ChangeRequestDetailsFactory crDetailsFactory;
    private ActivityManager activityManager;

    @Autowired
    public ChangeRequestStatusHelper(final ChangeRequestStatusDAO crStatusDAO,
            final ChangeRequestStatusTypeDAO crStatusTypeDAO, final ChangeRequestDAO crDAO,
            final ChangeRequestDetailsFactory crDetailsFactory, final ActivityManager activityManager) {
        this.crStatusDAO = crStatusDAO;
        this.crStatusTypeDAO = crStatusTypeDAO;
        this.crDAO = crDAO;
        this.crDetailsFactory = crDetailsFactory;
        this.activityManager = activityManager;
    }

    public ChangeRequestStatus saveInitialStatus(ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequestStatusType crStatusType = new ChangeRequestStatusType();
        crStatusType.setId(this.pendingAcbActionStatus);

        ChangeRequestStatus crStatus = new ChangeRequestStatus();
        crStatus.setStatusChangeDate(new Date());
        crStatus.setChangeRequestStatusType(crStatusType);

        return crStatusDAO.create(cr, crStatus);
    }

    public ChangeRequest updateChangeRequestStatus(ChangeRequest crFromCaller)
            throws EntityRetrievalException {
        ChangeRequest crFromDb = crDAO.get(crFromCaller.getId());

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

            createNewStatusForChangeRequest(
                    crFromDb,
                    statusTypeIdFromCaller,
                    crFromCaller.getCurrentStatus().getComment());

            // Need the updated CR with the new status
            ChangeRequest updatedCrFromDB = crDAO.get(crFromDb.getId());

            // Run any post processing based on the change request type
            crDetailsFactory.get(updatedCrFromDB.getChangeRequestType().getId())
                    .postStatusChangeProcessing(updatedCrFromDB);

            try {
                activityManager.addActivity(
                        ActivityConcept.CHANGE_REQUEST,
                        updatedCrFromDB.getId(),
                        "Change request status updated",
                        crFromDb,
                        updatedCrFromDB);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return updatedCrFromDB;
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

    private ChangeRequest createNewStatusForChangeRequest(final ChangeRequest cr, final Long crStatusTypeId,
            final String comment) throws EntityRetrievalException {
        ChangeRequestStatus crStatus = new ChangeRequestStatus();
        ChangeRequestStatusType crStatusType = new ChangeRequestStatusType();
        crStatusType.setId(crStatusTypeId);
        crStatus.setChangeRequestStatusType(crStatusType);
        crStatus.setComment(comment);
        crStatus.setStatusChangeDate(new Date());
        crStatus = crStatusDAO.create(cr, crStatus);

        cr.setCurrentStatus(crStatus);
        cr.getStatuses().add(crStatus);

        return cr;
    }

}
