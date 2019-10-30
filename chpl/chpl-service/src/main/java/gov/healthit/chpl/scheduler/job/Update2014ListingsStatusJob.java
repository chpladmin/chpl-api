package gov.healthit.chpl.scheduler.job;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.scheduler.ChplSchedulerReference;
import gov.healthit.chpl.scheduler.job.extra.StatusCollectorTriggerListener;
import gov.healthit.chpl.scheduler.job.extra.StatusCollectorTriggerWrapper;

public class Update2014ListingsStatusJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("updateL2014istingStatusJobLogger");
    private static final String JOB_NAME = "updateSingleListingStatusJob";
    private static final String JOB_GROUP = "subordinateJobs";

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private ChplSchedulerReference chplScheduler;

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        LOGGER.info("********* Starting the Update Listing Status job. *********");

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setSecurityContext();

        List<Long> listings = getListingIds();
        listings = listings.subList(831, 930);

        CertificationStatus certificationStatus = getCertificationStatus(jobContext);

        Date statusDate = getStatusEffectiveDate(jobContext);

        String email = jobContext.getMergedJobDataMap().getString("email");

        StatusCollectorTriggerListener listener = new StatusCollectorTriggerListener(email, env, LOGGER);
        List<StatusCollectorTriggerWrapper> wrappers = new ArrayList<StatusCollectorTriggerWrapper>();

        for (Long cpId : listings) {
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("listing", cpId);
            dataMap.put("certificationStatus", certificationStatus);
            dataMap.put("statusDate", statusDate);

            StatusCollectorTriggerWrapper wrapper = new StatusCollectorTriggerWrapper(
                    TriggerBuilder.newTrigger()
                            .forJob(JOB_NAME, JOB_GROUP)
                            .usingJobData(dataMap)
                            .build());

            wrappers.add(wrapper);
            listener.getTriggerWrappers().add(wrapper);
        }

        try {
            // Add the triggers to the listener
            List<Matcher<TriggerKey>> matchers = wrappers.stream()
                    .map(wrapper -> KeyMatcher.keyEquals(wrapper.getTrigger().getKey()))
                    .collect(Collectors.toList());

            chplScheduler.getScheduler().getListenerManager()
                    .addTriggerListener(listener, matchers);

            // Fire the trigger
            wrappers.stream()
                    .forEach(wrapper -> fireTrigger(wrapper.getTrigger()));

        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        LOGGER.info("********* Completed the Update Listing Status job. *********");
    }

    private void fireTrigger(final Trigger trigger) {
        try {
            chplScheduler.getScheduler().scheduleJob(trigger);
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private CertificationStatus getCertificationStatus(JobExecutionContext context) {
        @SuppressWarnings("unchecked") Map<String, Object> csMap = (Map<String, Object>) context.getMergedJobDataMap()
                .get("certificationStatus");

        CertificationStatus cs = new CertificationStatus();
        cs.setId(Long.parseLong(csMap.get("id").toString()));
        cs.setName(csMap.get("name").toString());
        return cs;
    }

    private Date getStatusEffectiveDate(JobExecutionContext jobContext) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date statusDate;
        try {
            statusDate = sdf.parse(jobContext.getMergedJobDataMap().getString("effectiveDate").substring(0, 10));
        } catch (ParseException e) {
            LOGGER.error(
                    "Could not parse the effectiveDate" + jobContext.getMergedJobDataMap().getString("effectiveDate"),
                    e);
            throw new RuntimeException(e);
        }
        return statusDate;
    }

    private List<Long> getListingIds() {
        List<CertifiedProductDetailsDTO> cps = certifiedProductDAO.findByEdition("2014");

        return cps.stream()
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
