package gov.healthit.chpl.scheduler.job.downloadfile;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.domain.schedule.ScheduledSystemJob;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Component
@Aspect
@Log4j2
public class GenerateListingDownloadFilesAspect {
    private static final String DOWNLOAD_FILE_JOB_2011 = "downloadFileJob2011";
    private static final String DOWNLOAD_FILE_JOB_2014 = "downloadFileJob2014";
    private static final String DOWNLOAD_FILE_JOB_INACTIVE = "downloadFileJobInactive";
    private static final String DOWNLOAD_FILE_JOB_ACTIVE = "downloadFileJobActive";

    private SchedulerManager schedulerManager;

    private Map<ListingSet, DownloadJobRunInformation> listingSetToJobNameMap = new HashMap<ListingSet, DownloadJobRunInformation>();

    @Autowired
    public GenerateListingDownloadFilesAspect(SchedulerManager schedulerManager, Environment env) {
        this.schedulerManager = schedulerManager;

        listingSetToJobNameMap.put(ListingSet.EDITION_2011, new DownloadJobRunInformation(DOWNLOAD_FILE_JOB_2011, env));
        listingSetToJobNameMap.put(ListingSet.EDITION_2014, new DownloadJobRunInformation(DOWNLOAD_FILE_JOB_2014, env));
        listingSetToJobNameMap.put(ListingSet.INACTIVE, new DownloadJobRunInformation(DOWNLOAD_FILE_JOB_INACTIVE, env));
        listingSetToJobNameMap.put(ListingSet.ACTIVE, new DownloadJobRunInformation(DOWNLOAD_FILE_JOB_ACTIVE, env));
    }

    @AfterReturning("execution(* *.*(..)) && @annotation(generateListingDownloadFile)")
    @Transactional
    public void generateListingDownloadFile(JoinPoint joinPoint, GenerateListingDownloadFile generateListingDownloadFile) {
        Arrays.asList(generateListingDownloadFile.listingSet()).stream()
                .forEach(listingSet -> scheduleDownloadFileJob(listingSet));
    }

    private void scheduleDownloadFileJob(ListingSet listingSet) {
        DownloadJobRunInformation info = listingSetToJobNameMap.get(listingSet);

        if (!isJobAlreadyScheduled(info)) {
            ChplOneTimeTrigger downloadFileTrigger = new ChplOneTimeTrigger();
            ChplJob downloadFileJob = getDownloadFileJob(listingSetToJobNameMap.get(listingSet).jobName);
            downloadFileTrigger.setJob(downloadFileJob);
            downloadFileTrigger.setRunDateMillis(listingSetToJobNameMap.get(listingSet).getRunDateTime().toInstant().toEpochMilli());
            downloadFileTrigger = addTriggerToScheduler(downloadFileTrigger);

            LOGGER.info("System job {}/{} has been scheduled for {}",
                    downloadFileTrigger.getJob().getGroup(),
                    downloadFileTrigger.getJob().getName(),
                    DateUtil.toLocalDateTime(downloadFileTrigger.getRunDateMillis()).toString());
        }
    }

    private ChplOneTimeTrigger addTriggerToScheduler(ChplOneTimeTrigger trigger) {
        try {
            return schedulerManager.createBackgroundJobTrigger(trigger);
        } catch (ValidationException | SchedulerException e) {
            LOGGER.error("Could not schedule trigger - {}", trigger.toString(), e);
            return null;
        }
    }

    private ChplJob getDownloadFileJob(String jobName) {
        return getAllJobs().stream()
                .filter(job -> job.getName().equals(jobName))
                .findAny()
                .get();
    }

    private List<ChplJob> getAllJobs() {
        try {
            return schedulerManager.getAllJobs();
        } catch (SchedulerException e) {
            LOGGER.error("Could not retrieve list of Quartz jobs", e);
            return List.of();
        }
    }

    private boolean isJobAlreadyScheduled(DownloadJobRunInformation info) {
        return getAllScheduledSystemJobs().stream()
                .filter(systemJob -> systemJob.getName().equalsIgnoreCase(info.getJobName())
                        && systemJob.getNextRunDate().getTime() == info.getRunDateTime().toInstant().toEpochMilli())
                .findAny()
                .isPresent();
    }

    private List<ScheduledSystemJob> getAllScheduledSystemJobs() {
        try {
            return schedulerManager.getScheduledSystemJobsForUser();
        } catch (SchedulerException e) {
            LOGGER.error("Could not retrieve list of scheduled system Quartz jobs", e);
            return List.of();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private class DownloadJobRunInformation {
        private String jobName;
        private Environment env;

        private ZonedDateTime getRunDateTime() {
            Integer hour = 0;
            Integer minute = 0;

            switch (jobName) {
            case DOWNLOAD_FILE_JOB_2011 :
                hour = Integer.valueOf(env.getProperty("download2011Hour"));
                minute = Integer.valueOf(env.getProperty("download2011Minute"));
                break;
            case DOWNLOAD_FILE_JOB_2014 :
                hour = Integer.valueOf(env.getProperty("download2014Hour"));
                minute = Integer.valueOf(env.getProperty("download2014Minute"));
                break;
            case DOWNLOAD_FILE_JOB_INACTIVE :
                hour = Integer.valueOf(env.getProperty("downloadInactiveHour"));
                minute = Integer.valueOf(env.getProperty("downloadInactiveMinute"));
                break;
            case DOWNLOAD_FILE_JOB_ACTIVE :
                hour = Integer.valueOf(env.getProperty("downloadActiveHour"));
                minute = Integer.valueOf(env.getProperty("downloadActiveMinute"));
                break;
            default:
            }

            return ZonedDateTime.now(ZoneId.of("UTC"))
                    .with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
                    .withHour(hour)
                    .withMinute(minute)
                    .withSecond(0)
                    .withNano(0);
        }
    }

}
