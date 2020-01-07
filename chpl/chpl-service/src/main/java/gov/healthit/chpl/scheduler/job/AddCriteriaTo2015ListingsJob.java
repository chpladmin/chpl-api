package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Matcher;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.KeyMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.scheduler.ChplSchedulerReference;
import gov.healthit.chpl.scheduler.job.extra.JobResponseTriggerListener;
import gov.healthit.chpl.scheduler.job.extra.JobResponseTriggerWrapper;

public class AddCriteriaTo2015ListingsJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("addCriteriaToListingsJobLogger");
    private static final String JOB_NAME_FOR_EXISTING_LISTINGS = "addCriteriaToSingleListingJob";
    private static final String JOB_NAME_FOR_PENDING_LISTINGS = "addCriteriaToSinglePendingListingJob";
        private static final String JOB_GROUP = "subordinateJobs";

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private PendingCertifiedProductDAO pendingCertifiedProductDAO;

    @Autowired
    private ChplSchedulerReference chplScheduler;

    @Autowired
    private Environment env;

    private static final String CRITERIA_TO_ADD = "170.315 (b)(10);170.315 (d)(12);170.315 (d)(13);170.315 (g)(10)";

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        LOGGER.info("********* Starting the Add Criteria to 2015 Listings job. *********");

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setSecurityContext();
        LOGGER.info("statusInterval = " + jobContext.getMergedJobDataMap().getInt("statusInterval"));
        List<JobResponseTriggerWrapper> wrappers = new ArrayList<JobResponseTriggerWrapper>();
        wrappers.addAll(getExistingListingWrappers(jobContext));
        wrappers.addAll(getPendingListingWrappers(jobContext));

        LOGGER.info("Total number of listings to update: " + wrappers.size());

        try {
            JobResponseTriggerListener listener = new JobResponseTriggerListener(
                    wrappers,
                    jobContext.getMergedJobDataMap().getString("email"),
                    jobContext.getMergedJobDataMap().getString("emailCsvFileName"),
                    jobContext.getMergedJobDataMap().getString("emailSubject"),
                    jobContext.getMergedJobDataMap().getInt("statusInterval"),
                    env,
                    LOGGER);

            // Add the triggers and listener to the scheduler
            chplScheduler.getScheduler().getListenerManager()
                    .addTriggerListener(listener, getTriggerKeyMatchers(wrappers));

            // Fire the triggers
            wrappers.stream()
                    .forEach(wrapper -> fireTrigger(wrapper.getTrigger()));

        } catch (SchedulerException e) {
            LOGGER.error("Scheduler Error: " + e.getMessage(), e);
        }
        LOGGER.info("********* Completed the Update 2014 Listings Status job. *********");
    }

    private List<JobResponseTriggerWrapper> getExistingListingWrappers(JobExecutionContext jobContext) {
        List<Long> listings = getListingIds();
        List<JobResponseTriggerWrapper> wrappers = new ArrayList<JobResponseTriggerWrapper>();

        for (Long cpId : listings) {
            wrappers.add(buildTriggerWrapper(cpId, jobContext, JOB_NAME_FOR_EXISTING_LISTINGS));
        }
        LOGGER.info("Total number of existing listings to update: " + listings.size());
        return wrappers;
    }

    private List<JobResponseTriggerWrapper> getPendingListingWrappers(JobExecutionContext jobContext) {
        List<Long> listings = getPendingListingIds();
        List<JobResponseTriggerWrapper> wrappers = new ArrayList<JobResponseTriggerWrapper>();

        for (Long cpId : listings) {
            wrappers.add(buildTriggerWrapper(cpId, jobContext, JOB_NAME_FOR_PENDING_LISTINGS));
        }
        LOGGER.info("Total number of pending listings to update: " + listings.size());
        return wrappers;
    }

    private JobResponseTriggerWrapper buildTriggerWrapper(final Long cpId, final JobExecutionContext jobContext, String jobName) {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("listing", cpId);
        dataMap.put("criteria", CRITERIA_TO_ADD);
        dataMap.put("logger", LOGGER);

        return new JobResponseTriggerWrapper(
                TriggerBuilder.newTrigger()
                        .forJob(jobName, JOB_GROUP)
                        .usingJobData(dataMap)
                        .build());
    }

    private List<Matcher<TriggerKey>> getTriggerKeyMatchers(final List<JobResponseTriggerWrapper> wrappers) {
        return wrappers.stream()
                .map(wrapper -> KeyMatcher.keyEquals(wrapper.getTrigger().getKey()))
                .collect(Collectors.toList());
    }

    private void fireTrigger(final Trigger trigger) {
        try {
            chplScheduler.getScheduler().scheduleJob(trigger);
        } catch (SchedulerException e) {
            LOGGER.error("Scheduler Error: " + e.getMessage(), e);
        }
    }

    private List<Long> getListingIds() {
        List<CertifiedProductDetailsDTO> cps = certifiedProductDAO.findByEdition("2015");

        return cps.stream()
                .map(cp -> cp.getId())
                .filter(cp -> cp >= 10000L) //for testing purposes
                .collect(Collectors.toList());
    }

    private List<Long> getPendingListingIds() {
        List<PendingCertifiedProductDTO> cps = pendingCertifiedProductDAO.findAll();

        return cps.stream()
                .filter(cp -> cp.getCertificationEdition().equalsIgnoreCase("2015"))
                .map(cp -> cp.getId())
                .collect(Collectors.toList());
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}
