package gov.healthit.chpl.scheduler.job.onetime.jobedit;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.scheduler.ChplSchedulerReference;
import gov.healthit.chpl.scheduler.SchedulerSecurityContextService;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "fixJobDataJobLogger")
public class FixJobDataJob implements Job {

    @Autowired
    private ChplSchedulerReference chplScheduler;

    @Autowired
    private SchedulerSecurityContextService securityContextService;

    @Autowired
    private SchedulerManager schedulerManager;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Fix Job Data job. *********");
        securityContextService.setAdminSecurityContext();

        try {
            List<ChplJob> jobs = schedulerManager.getAllJobs();

            jobs.stream()
                    .filter(job -> job.getGroup().equals(SchedulerManager.CHPL_JOBS_KEY)
                            && job.getJobDataMap().containsKey("authorities"))
                    .peek(job -> LOGGER.info("Fixing job data for job {}", job.getName()))
                    .forEach(job -> {
                        try {
                            LOGGER.info("Authorities: {}", job.getJobDataMap().get("authorities"));

                            List<String> authoritiesList = new ArrayList<String>(Arrays.asList(job.getJobDataMap().get("authorities").toString().split(";")));

                            if (authoritiesList.contains("ROLE_ADMIN") && authoritiesList.contains("chpl-admin")) {
                                authoritiesList.remove("ROLE_ADMIN");
                                LOGGER.info("Removing ROLE_ADMIN from " + job.getName());
                            }
                            if (authoritiesList.contains("ROLE_ONC") && authoritiesList.contains("chpl-onc")) {
                                authoritiesList.remove("ROLE_ONC");
                                LOGGER.info("Removed ROLE_ONC from " + job.getName());
                            }
                            if (authoritiesList.contains("ROLE_ACB") && authoritiesList.contains("chpl-onc-acb")) {
                                authoritiesList.remove("ROLE_ACB");
                                LOGGER.info("Removed ROLE_ACB from " + job.getName());
                            }
                            if (authoritiesList.contains("ROLE_CMS_STAFF") && authoritiesList.contains("chpl-cms-staff")) {
                                authoritiesList.remove("ROLE_CMS_STAFF");
                                LOGGER.info("Removed ROLE_CMS_STAFF from " + job.getName());
                            }
                            if (authoritiesList.contains("ROLE_DEVELOPER") && authoritiesList.contains("chpl-developer")) {
                                authoritiesList.remove("ROLE_DEVELOPER");
                                LOGGER.info("Removed ROLE_DEVELOPER from " + job.getName());
                            }
                            job.getJobDataMap().put("authorities", String.join(";", authoritiesList));
                            updateJob(job);
                        } catch (Exception e) {
                            LOGGER.error("Error occurred while fixing job data for job {}", job.getName(), e);
                        }
                    });
        } catch (Exception e) {
            LOGGER.error("Error occurred while fixing job data.", e);
            throw new JobExecutionException("Error occurred while fixing job data.", e);
        } finally {
            LOGGER.info("********* Completed the Fix Job Data job. *********");
        }

    }

    private ChplJob updateJob(ChplJob job) throws SchedulerException {
        Scheduler scheduler = getScheduler();
        JobKey jobId = jobKey(job.getName(), job.getGroup());
        JobDetail oldJob = scheduler.getJobDetail(jobId);
        JobDetail newJob = newJob(oldJob.getJobClass()).withIdentity(jobId).withDescription(oldJob.getDescription())
                .usingJobData(job.getJobDataMap()).storeDurably(oldJob.isDurable())
                .requestRecovery(oldJob.requestsRecovery()).build();

        scheduler.addJob(newJob, true);
        ChplJob newChplJob = new ChplJob(newJob);
        return newChplJob;
    }

    private Scheduler getScheduler() throws SchedulerException {
        return chplScheduler.getScheduler();
    }

}