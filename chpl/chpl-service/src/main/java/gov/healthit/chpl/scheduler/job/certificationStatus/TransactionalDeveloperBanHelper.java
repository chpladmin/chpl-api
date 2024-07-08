package gov.healthit.chpl.scheduler.job.certificationStatus;

import java.time.LocalDate;

import jakarta.transaction.Transactional;

import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.scheduler.job.TriggerDeveloperBanJob;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "updateCurrentCertificationStatusJobLogger")
public class TransactionalDeveloperBanHelper {

    private DeveloperManager developerManager;
    private SchedulerManager schedulerManager;
    private DeveloperDAO developerDao;
    private DeveloperStatusDAO devStatusDao;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Autowired
    public TransactionalDeveloperBanHelper(DeveloperManager developerManager, SchedulerManager schedulerManager,
            DeveloperDAO developerDao,
            DeveloperStatusDAO devStatusDao, ErrorMessageUtil msgUtil, ResourcePermissionsFactory resourcePermissionsFactory) {
        this.developerManager = developerManager;
        this.schedulerManager = schedulerManager;
        this.developerDao = developerDao;
        this.devStatusDao = devStatusDao;
        this.msgUtil = msgUtil;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    @Transactional
    public void handleCertificationStatusChange(CertifiedProductSearchDetails listing, JWTAuthenticatedUser user, String reason)
            throws EntityRetrievalException, ValidationException, EntityCreationException, ActivityException {

        CertificationStatus currentStatus = listing.getCurrentStatus().getStatus();
        switch (CertificationStatusType.getValue(currentStatus.getName())) {
        case SuspendedByOnc:
        case TerminatedByOnc:
            // Only roles ONC or ADMIN can do this and it always triggers developer ban
            if (resourcePermissionsFactory.get().isUserRoleAdmin() || resourcePermissionsFactory.get().isUserRoleOnc()) {
                LOGGER.info("Banning developer with ID " + listing.getDeveloper().getId());
                banDeveloper(listing);
            } else {
                LOGGER.error("User " + user.getSubjectName() + " does not have ROLE_ADMIN or ROLE_ONC and cannot "
                        + "change the status of developer for listing with id " + listing.getId());
                return;
            }
            break;
        case WithdrawnByAcb:
        case WithdrawnByDeveloperUnderReview:
            // initiate TriggerDeveloperBan job, telling ONC that they might need to ban a Developer
            LOGGER.info("Sending developer ban email.");
            sendDeveloperBanEmail(listing, user, reason);
            break;
        default:
            LOGGER.info("New listing status is " + currentStatus.getName() + " which does not trigger a developer ban.");
            break;
        }
    }

    private void banDeveloper(CertifiedProductSearchDetails listing)
            throws EntityRetrievalException, ValidationException, EntityCreationException, ActivityException {

        CertificationStatus currentListingStatus = listing.getCurrentStatus().getStatus();
        Developer developer = developerDao.getById(listing.getDeveloper().getId());
        DeveloperStatus newDeveloperStatus = null;

        if (currentListingStatus.getName().equals(CertificationStatusType.SuspendedByOnc.toString())) {
            newDeveloperStatus = devStatusDao.getByName(DeveloperStatusType.SuspendedByOnc.toString());
        } else if (currentListingStatus.getName().equals(CertificationStatusType.TerminatedByOnc.toString())) {
            newDeveloperStatus = devStatusDao.getByName(DeveloperStatusType.UnderCertificationBanByOnc.toString());
        }

        DeveloperStatusEvent currentDeveloperStatusEvent = developer.getCurrentStatusEvent();
        if (newDeveloperStatus != null
                && (currentDeveloperStatusEvent == null
                    || !currentDeveloperStatusEvent.getStatus().getName().equals(newDeveloperStatus.getName()))) {
            LOGGER.info("Setting the developer to " + newDeveloperStatus.getName());
            DeveloperStatusEvent statusEventToAdd = DeveloperStatusEvent.builder()
                    .status(newDeveloperStatus)
                    .startDate(LocalDate.now())
                    .reason(msgUtil.getMessage("developer.statusAutomaticallyChanged"))
                    .build();
            developer.getStatuses().add(statusEventToAdd);
            developerManager.update(developer, false);
        } else {
            LOGGER.info("Not changing the developer's status to " + newDeveloperStatus.getName()
                + ". Developer's current status is " + currentDeveloperStatusEvent.getStatus().getName());
        }
    }

    private void sendDeveloperBanEmail(CertifiedProductSearchDetails listing, JWTAuthenticatedUser user, String reason) {
        ChplOneTimeTrigger possibleDeveloperBanTrigger = new ChplOneTimeTrigger();
        ChplJob triggerDeveloperBanJob = new ChplJob();
        triggerDeveloperBanJob.setName(TriggerDeveloperBanJob.JOB_NAME);
        triggerDeveloperBanJob.setGroup(SchedulerManager.CHPL_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(TriggerDeveloperBanJob.LISTING_ID, listing.getId());
        jobDataMap.put(TriggerDeveloperBanJob.USER, user);
        jobDataMap.put(TriggerDeveloperBanJob.CHANGE_DATE, System.currentTimeMillis());
        jobDataMap.put(TriggerDeveloperBanJob.USER_PROVIDED_REASON, reason);
        triggerDeveloperBanJob.setJobDataMap(jobDataMap);
        possibleDeveloperBanTrigger.setJob(triggerDeveloperBanJob);
        possibleDeveloperBanTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.FIVE_SECONDS_IN_MILLIS);
        try {
            possibleDeveloperBanTrigger = schedulerManager.createBackgroundJobTrigger(possibleDeveloperBanTrigger);
        } catch (Exception ex) {
            LOGGER.error("Unable to schedule Trigger Developer Ban Job.", ex);
        }
    }
}