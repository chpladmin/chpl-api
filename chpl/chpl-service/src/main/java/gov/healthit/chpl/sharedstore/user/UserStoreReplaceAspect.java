package gov.healthit.chpl.sharedstore.user;

import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.scheduler.job.CognitoUserCacheRefreshJob;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import lombok.extern.log4j.Log4j2;

@Component
@Aspect
@Log4j2
public class UserStoreReplaceAspect {
    private SharedUserStoreProvider sharedUserStoreProvider;
    private SchedulerManager schedulerManager;
    private CognitoApiWrapper cognitoApiWrapper;
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Autowired
    public UserStoreReplaceAspect(SharedUserStoreProvider sharedUserStoreProvider,
            SchedulerManager schedulerManager,
            CognitoApiWrapper cognitoApiWrapper,
            ResourcePermissionsFactory resourcePermissionsFactory) {
        this.sharedUserStoreProvider = sharedUserStoreProvider;
        this.schedulerManager = schedulerManager;
        this.cognitoApiWrapper = cognitoApiWrapper;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    @AfterReturning(pointcut = "execution(* *.*(..)) && @annotation(UserStoreReplace)", returning = "returnObject")
    @Transactional
    public void userStoreReplace(JoinPoint joinPoint, Object returnObject) {
        if (returnObject == null) {
           LOGGER.error("Return object from UserStoreReplace was null.");
        } else if (returnObject instanceof UUID) {
            replaceUserInStore((UUID) returnObject);
        } else if (returnObject instanceof User) {
            User user = (User) returnObject;
            replaceUserInStore(user.getCognitoId());
        } else {
            LOGGER.error("Return object was of class " + returnObject.getClass() + ". This is not handled. " + returnObject);
        }

        if (doesUserHavePermissionToTriggerReload()) {
            triggerUserStoreReload();
        }
    }

    private void replaceUserInStore(UUID cognitoUuid) {
        if (cognitoUuid == null) {
            LOGGER.error("Attempting to replace user in the shared store but the Cognito UUID passed in was null. "
                + "Nothing will be replaced in the store.");
        }

        sharedUserStoreProvider.remove(cognitoUuid.toString());
        try {
            cognitoApiWrapper.getUserInfo(cognitoUuid);
        } catch (UserRetrievalException ex) {
            LOGGER.error("Unable to get replace user in store with ID " + cognitoUuid, ex);
        }
    }

    private boolean doesUserHavePermissionToTriggerReload() {
        return resourcePermissionsFactory.get().isUserRoleAdmin()
                || resourcePermissionsFactory.get().isUserRoleOnc()
                || resourcePermissionsFactory.get().isUserRoleAcbAdmin()
                || resourcePermissionsFactory.get().isUserRoleDeveloperAdmin();
    }

    private void triggerUserStoreReload() {
        LOGGER.info("Scheduling the job to refresh users...");
        ChplOneTimeTrigger trigger = new ChplOneTimeTrigger();
        trigger.setJob(ChplJob.builder()
                .name(CognitoUserCacheRefreshJob.JOB_NAME)
                .group(CognitoUserCacheRefreshJob.JOB_GROUP)
                .jobDataMap(new JobDataMap())
                .build());
        trigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.FIVE_SECONDS_IN_MILLIS);

        try {
            schedulerManager.createBackgroundJobTrigger(trigger);
            LOGGER.info("Scheduled background job " + trigger.getJob().getName() + " to run at " + trigger.getRunDateMillis());
        } catch (Exception ex) {
            LOGGER.error("Unable to schedule trigger " + CognitoUserCacheRefreshJob.JOB_NAME, ex);
        }
    }
}
