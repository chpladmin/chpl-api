package gov.healthit.chpl.caching;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.scheduler.job.CognitoUserCacheRefreshJob;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@Aspect
public class CognitoUserCacheRefreshAspect {

    private SchedulerManager schedulerManager;

    @Autowired
    public CognitoUserCacheRefreshAspect(SchedulerManager schedulerManager) {
        this.schedulerManager = schedulerManager;
    }

    @AfterReturning("@annotation(CognitoUserCacheRefresh)")
    public void cognitoUserCacheRefresh(JoinPoint joinPoint) {
        LOGGER.info("Method invoked with CognitoUserCacheRefresh annotation. Scheduling the job to refresh users...");
        ChplOneTimeTrigger trigger = new ChplOneTimeTrigger();
        trigger.setJob(ChplJob.builder()
                .name(CognitoUserCacheRefreshJob.JOB_NAME)
                .group(CognitoUserCacheRefreshJob.JOB_GROUP)
                .build());
        trigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.FIVE_SECONDS_IN_MILLIS);

        try {
            schedulerManager.createOneTimeTrigger(trigger);
            LOGGER.info("Scheduling background job " + trigger.getJob().getName() + " to run at " + trigger.getRunDateMillis());
        } catch (Exception ex) {
            LOGGER.error("Unable to schedule trigger " + CognitoUserCacheRefreshJob.JOB_NAME, ex);
        }
    }
}
