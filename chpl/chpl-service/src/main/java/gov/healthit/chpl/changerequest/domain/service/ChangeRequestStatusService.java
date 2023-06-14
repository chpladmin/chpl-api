package gov.healthit.chpl.changerequest.domain.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.AuthUtil;

@Component
public class ChangeRequestStatusService {
    private static final Logger LOGGER = LogManager.getLogger(ChangeRequestStatusService.class);

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
    private ResourcePermissions resourcePermissions;

    @Autowired
    public ChangeRequestStatusService(final ChangeRequestStatusDAO crStatusDAO,
            final ChangeRequestStatusTypeDAO crStatusTypeDAO, final ChangeRequestDAO crDAO,
            final ChangeRequestDetailsFactory crDetailsFactory, final ActivityManager activityManager,
            final ResourcePermissions resourcePermissions) {
        this.crStatusDAO = crStatusDAO;
        this.crStatusTypeDAO = crStatusTypeDAO;
        this.crDAO = crDAO;
        this.crDetailsFactory = crDetailsFactory;
        this.activityManager = activityManager;
        this.resourcePermissions = resourcePermissions;
    }

    public ChangeRequestStatus saveInitialStatus(ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequestStatusType crStatusType = new ChangeRequestStatusType();
        crStatusType.setId(this.pendingAcbActionStatus);

        ChangeRequestStatus crStatus = new ChangeRequestStatus();
        crStatus.setStatusChangeDateTime(LocalDateTime.now());
        crStatus.setChangeRequestStatusType(crStatusType);
        crStatus.setUserPermission(resourcePermissions.getRoleByUserId(AuthUtil.getCurrentUser().getId()));

        return crStatusDAO.create(cr, crStatus);
    }

    public ChangeRequest updateChangeRequestStatus(ChangeRequest crFromCaller)
            throws EntityRetrievalException, EmailNotSentException {
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
                && statusTypeIdFromDB.longValue() != statusTypeIdFromCaller.longValue()
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

    public static boolean doesCurrentStatusExist(ChangeRequest cr) {
        return cr.getCurrentStatus() != null
                && cr.getCurrentStatus().getChangeRequestStatusType() != null
                && cr.getCurrentStatus().getChangeRequestStatusType().getId() != null;
    }

    private boolean isStatusChangeValid(final Long previousStatusTypeId, final Long newStatusTypeId) {
        // Does this status type id exist?
        try {
            crStatusTypeDAO.getChangeRequestStatusTypeById(newStatusTypeId);
        } catch (EntityRetrievalException e) {
            return false;
        }

        // Cannot change status is Cancelled
        if (previousStatusTypeId.equals(this.cancelledByRequesterStatus)) {
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
        crStatus.setStatusChangeDateTime(LocalDateTime.now());
        crStatus.setUserPermission(resourcePermissions.getRoleByUserId(AuthUtil.getCurrentUser().getId()));
        if (resourcePermissions.isUserRoleAcbAdmin()) {
            crStatus.setCertificationBody(getCertificationBodyForCurrentUser());
        }

        crStatus = crStatusDAO.create(cr, crStatus);
        cr.setCurrentStatus(crStatus);
        cr.getStatuses().add(crStatus);

        return cr;
    }

    private CertificationBody getCertificationBodyForCurrentUser() {
        if (resourcePermissions.isUserRoleAcbAdmin()) {
            if (resourcePermissions.getAllAcbsForCurrentUser().size() == 1) {
                return new CertificationBody(resourcePermissions.getAllAcbsForCurrentUser().get(0));
            } else {
                String msg = "Cannot determine ACB for current user.  There are multiple ACBs for this user: "
                        + AuthUtil.getUsername();
                LOGGER.error(msg);
                throw new RuntimeException(msg);
            }
        } else {
            return null;
        }
    }
}
