package gov.healthit.chpl.search.annotation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.manager.SchedulerManager;
import lombok.extern.log4j.Log4j2;

@Component
@Aspect
@Log4j2
public class ReplaceListingSearchCacheAspect {

    private SchedulerManager schedulerManager;

    @Autowired
    public ReplaceListingSearchCacheAspect(SchedulerManager schedulerManager) {
        this.schedulerManager = schedulerManager;
    }

    @AfterReturning("execution(* *.*(..)) && @annotation(gov.healthit.chpl.search.annotation.ReplaceListingSearchCache)")
    public void executeListingSearchCacheReplaceJob(JoinPoint joinPoint) throws Throwable {
        LOGGER.info("ReplaceListingSearchCacheJob should be scheduled.");

        ChplOneTimeTrigger replaceListingSearchCacheTrigger = new ChplOneTimeTrigger();
        ChplJob replaceListingSearchCacheJob = new ChplJob();
        replaceListingSearchCacheJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        replaceListingSearchCacheTrigger.setJob(replaceListingSearchCacheJob);
        replaceListingSearchCacheTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.FIVE_SECONDS_IN_MILLIS);
        replaceListingSearchCacheTrigger = schedulerManager.createBackgroundJobTrigger(replaceListingSearchCacheTrigger);
    }
}
