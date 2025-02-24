package gov.healthit.chpl.scheduler.job.onetime.jobedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.scheduler.SchedulerSecurityContextService;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "fixJobDataJobLogger")
public class FixJobDataJob implements Job {

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

                            if (authoritiesList.contains("ROLE_ADMIN") && !authoritiesList.contains("chpl-admin")) {
                                authoritiesList.add("chpl-admin");
                                LOGGER.info("Added chpl-admin");
                            }
                            if (authoritiesList.contains("ROLE_ONC") && !authoritiesList.contains("chpl-onc")) {
                                authoritiesList.add("chpl-onc");
                                LOGGER.info("Added chpl-onc");
                            }
                            if (authoritiesList.contains("ROLE_ACB") && !authoritiesList.contains("chpl-onc-acb")) {
                                authoritiesList.add("chpl-onc-acb");
                                LOGGER.info("Added chpl-onc-acb");
                            }
                            if (authoritiesList.contains("ROLE_CMS_STAFF") && !authoritiesList.contains("chpl-cms-staff")) {
                                authoritiesList.add("chpl-cms-staff");
                                LOGGER.info("Added chpl-cms-staff");
                            }
                            if (authoritiesList.contains("ROLE_DEVELOPER") && !authoritiesList.contains("chpl-developer")) {
                                authoritiesList.add("chpl-developer");
                                LOGGER.info("Added chpl-developer");
                            }
                            job.getJobDataMap().put("authorities", String.join(";", authoritiesList));
                            schedulerManager.updateJob(job);
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

}
