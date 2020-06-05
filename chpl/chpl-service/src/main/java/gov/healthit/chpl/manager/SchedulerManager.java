package gov.healthit.chpl.manager;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.domain.schedule.ChplRepeatableTrigger;
import gov.healthit.chpl.domain.schedule.ScheduledSystemJob;
import gov.healthit.chpl.domain.schedule.TriggerSchedule;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.scheduler.ChplSchedulerReference;
import gov.healthit.chpl.util.AuthUtil;

/**
 * Implementation of Scheduler Manager.
 *
 * @author alarned
 *
 */
@Service
public class SchedulerManager extends SecuredManager {

    private static String AUTHORITY_DELIMITER = ";";
    private static String DATA_DELIMITER = "\u263A";
    public static String CHPL_BACKGROUND_JOBS_KEY = "chplBackgroundJobs";
    public static String CHPL_JOBS_KEY = "chplJobs";
    public static String SYSTEM_JOBS_KEY = "systemJobs";

    private ChplSchedulerReference chplScheduler;
    private ResourcePermissions resourcePermissions;

    @Autowired
    public SchedulerManager(ChplSchedulerReference chplScheduler,
            ResourcePermissions resourcePermissions) {
        this.chplScheduler = chplScheduler;
        this.resourcePermissions = resourcePermissions;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SCHEDULER, "
            + "T(gov.healthit.chpl.permissions.domains.SchedulerDomainPermissions).CREATE_TRIGGER)")
    public ChplRepeatableTrigger createTrigger(ChplRepeatableTrigger trigger)
            throws SchedulerException, ValidationException {
        Scheduler scheduler = getScheduler();

        TriggerKey triggerId = triggerKey(createTriggerName(trigger), createTriggerGroup(trigger.getJob()));
        JobKey jobId = jobKey(trigger.getJob().getName(), trigger.getJob().getGroup());
        if (doesUserHavePermissionToJob(scheduler.getJobDetail(jobId))) {
            Trigger qzTrigger = null;
            if (trigger.getJob().getJobDataMap().getBooleanValue("acbSpecific")) {
                qzTrigger = newTrigger()
                        .withIdentity(triggerId)
                        .startNow()
                        .forJob(jobId)
                        .usingJobData("email", trigger.getEmail())
                        .usingJobData("acb", trigger.getAcb())
                        .usingJobData(trigger.getJob().getJobDataMap())
                        .withSchedule(cronSchedule(trigger.getCronSchedule()))
                        .build();
            } else {
                qzTrigger = newTrigger()
                        .withIdentity(triggerId)
                        .startNow()
                        .forJob(jobId)
                        .usingJobData("email", trigger.getEmail())
                        .usingJobData(trigger.getJob().getJobDataMap())
                        .withSchedule(cronSchedule(trigger.getCronSchedule()))
                        .build();
            }

            if (doesUserHavePermissionToTrigger(qzTrigger)) {
                scheduler.scheduleJob(qzTrigger);
            } else {
                throw new AccessDeniedException("Can not create this trigger");
            }

            ChplRepeatableTrigger newTrigger = new ChplRepeatableTrigger((CronTrigger) scheduler.getTrigger(triggerId));
            return newTrigger;
        } else {
            throw new AccessDeniedException("Can not create this trigger");
        }
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SCHEDULER, "
            + "T(gov.healthit.chpl.permissions.domains.SchedulerDomainPermissions).CREATE_ONE_TIME_TRIGGER)")
    public ChplOneTimeTrigger createOneTimeTrigger(ChplOneTimeTrigger chplTrigger)
            throws SchedulerException, ValidationException {
        Scheduler scheduler = getScheduler();

        SimpleTrigger trigger = (SimpleTrigger) newTrigger()
                .withIdentity(createTriggerName(chplTrigger), createTriggerGroup(chplTrigger.getJob()))
                .startAt(new Date(chplTrigger.getRunDateMillis()))
                .forJob(chplTrigger.getJob().getName(), chplTrigger.getJob().getGroup())
                .usingJobData(chplTrigger.getJob().getJobDataMap()).build();

        scheduler.scheduleJob(trigger);

        return chplTrigger;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SCHEDULER, "
            + "T(gov.healthit.chpl.permissions.domains.SchedulerDomainPermissions).CREATE_BACKGROUND_JOB_TRIGGER, #chplTrigger)")
    public ChplOneTimeTrigger createBackgroundJobTrigger(ChplOneTimeTrigger chplTrigger)
            throws SchedulerException, ValidationException {
        return createOneTimeTrigger(chplTrigger);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SCHEDULER, "
            + "T(gov.healthit.chpl.permissions.domains.SchedulerDomainPermissions).DELETE_TRIGGER)")
    public void deleteTrigger(String triggerGroup, String triggerName)
            throws SchedulerException, ValidationException {
        Scheduler scheduler = getScheduler();
        TriggerKey triggerKey = triggerKey(triggerName, triggerGroup);

        if (doesUserHavePermissionToTrigger(scheduler.getTrigger(triggerKey))) {
            scheduler.unscheduleJob(triggerKey);
        } else {
            throw new AccessDeniedException("Can not update this trigger");
        }
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SCHEDULER, "
            + "T(gov.healthit.chpl.permissions.domains.SchedulerDomainPermissions).GET_ALL_TRIGGERS)")
    public List<ChplRepeatableTrigger> getAllTriggersForUser() throws SchedulerException {
        List<ChplRepeatableTrigger> triggers = new ArrayList<ChplRepeatableTrigger>();
        Scheduler scheduler = getScheduler();
        for (String group : scheduler.getTriggerGroupNames()) {
            // enumerate each trigger in group
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(groupEquals(group))) {
                if (scheduler.getTrigger(triggerKey).getJobKey().getGroup().equalsIgnoreCase(CHPL_JOBS_KEY)) {
                    if (doesUserHavePermissionToTrigger(scheduler.getTrigger(triggerKey))
                            && getScheduler().getTrigger(triggerKey) instanceof CronTrigger) {
                        ChplRepeatableTrigger newTrigger = getChplTrigger(triggerKey);
                        triggers.add(newTrigger);
                    }
                }
            }
        }
        return triggers;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SCHEDULER, "
            + "T(gov.healthit.chpl.permissions.domains.SchedulerDomainPermissions).GET_ALL_SYSTEM_TRIGGERS)")
    public List<ScheduledSystemJob> getScheduledSystemJobsForUser() throws SchedulerException {
        List<ScheduledSystemJob> ssJobs = new ArrayList<ScheduledSystemJob>();
        Scheduler scheduler = getScheduler();
        for (String group : scheduler.getTriggerGroupNames()) {
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(groupEquals(group))) {
                String jobGroup = scheduler.getTrigger(triggerKey).getJobKey().getGroup();
                if (jobGroup.equalsIgnoreCase(SYSTEM_JOBS_KEY) || jobGroup.equalsIgnoreCase(CHPL_BACKGROUND_JOBS_KEY)) {
                    Trigger curTrigger = getScheduler().getTrigger(triggerKey);
                    String jobName = curTrigger.getKey().getName();
                    JobDetail jobDetail = getScheduler().getJobDetail(getScheduler().getTrigger(triggerKey).getJobKey());
                    String jobDescription = jobDetail.getDescription();
                    Date nextRunDate = curTrigger.getNextFireTime();
                    if (curTrigger instanceof CronTrigger) {
                        ssJobs.add(new ScheduledSystemJob(jobName, jobDescription, nextRunDate,
                                TriggerSchedule.REPEATABLE));
                    } else if (curTrigger instanceof SimpleTrigger) {
                        jobName = curTrigger.getJobKey().getName();
                        ssJobs.add(new ScheduledSystemJob(jobName, jobDescription, nextRunDate,
                                TriggerSchedule.ONE_TIME));
                    }
                }
            }
        }
        return ssJobs;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SCHEDULER, "
            + "T(gov.healthit.chpl.permissions.domains.SchedulerDomainPermissions).UPDATE_TRIGGER)")
    public ChplRepeatableTrigger updateTrigger(ChplRepeatableTrigger trigger)
            throws SchedulerException, ValidationException {
        Scheduler scheduler = getScheduler();
        Trigger oldTrigger = scheduler.getTrigger(triggerKey(trigger.getName(), trigger.getGroup()));
        Trigger qzTrigger = null;
        if (doesUserHavePermissionToTrigger(oldTrigger)) {
            if (trigger.getJob().getJobDataMap().getBooleanValue("acbSpecific")) {
                qzTrigger = newTrigger()
                        .withIdentity(oldTrigger.getKey())
                        .startNow()
                        .forJob(oldTrigger.getJobKey())
                        .usingJobData(trigger.getJob().getJobDataMap())
                        .usingJobData("acb", trigger.getAcb())
                        .withSchedule(cronSchedule(trigger.getCronSchedule()))
                        .build();
            } else {
                JobDataMap mergedMap = new JobDataMap(oldTrigger.getJobDataMap());
                mergedMap.putAll(trigger.getJob().getJobDataMap());
                qzTrigger = newTrigger()
                        .withIdentity(oldTrigger.getKey())
                        .startNow()
                        .forJob(oldTrigger.getJobKey())
                        .usingJobData(mergedMap)
                        .withSchedule(cronSchedule(trigger.getCronSchedule()))
                        .build();
            }
            scheduler.rescheduleJob(oldTrigger.getKey(), qzTrigger);

            ChplRepeatableTrigger newTrigger = getChplTrigger(qzTrigger.getKey());
            return newTrigger;
        } else {
            throw new AccessDeniedException("Can not update this trigger");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.healthit.chpl.manager.SchedulerManager#getAllJobs() As new jobs are added that have authorities other
     * than ROLE_ADMIN, those authorities will need to be added to the list.
     */
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SCHEDULER, "
            + "T(gov.healthit.chpl.permissions.domains.SchedulerDomainPermissions).GET_ALL)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SCHEDULER, "
            + "T(gov.healthit.chpl.permissions.domains.SchedulerDomainPermissions).GET_ALL, filterObject)")
    public List<ChplJob> getAllJobs() throws SchedulerException {
        List<ChplJob> jobs = new ArrayList<ChplJob>();
        Scheduler scheduler = getScheduler();

        // Get all the jobs (no security - it is handled with @PostFilter)
        for (String group : scheduler.getJobGroupNames()) {
            if (!CHPL_BACKGROUND_JOBS_KEY.equals(group)) {
                for (JobKey jobKey : scheduler.getJobKeys(groupEquals(group))) {
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    ChplJob chplJob = new ChplJob(jobDetail);
                    chplJob.setJobDataMap(jobDetail.getJobDataMap());
                    jobs.add(chplJob);
                }
            }
        }

        return jobs;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SCHEDULER, "
            + "T(gov.healthit.chpl.permissions.domains.SchedulerDomainPermissions).UPDATE_JOB)")
    public ChplJob updateJob(ChplJob job) throws SchedulerException {
        Scheduler scheduler = getScheduler();
        JobKey jobId = jobKey(job.getName(), job.getGroup());
        JobDetail oldJob = scheduler.getJobDetail(jobId);
        if (doesUserHavePermissionToJob(oldJob)) {
            JobDetail newJob = newJob(oldJob.getJobClass()).withIdentity(jobId).withDescription(oldJob.getDescription())
                    .usingJobData(job.getJobDataMap()).storeDurably(oldJob.isDurable())
                    .requestRecovery(oldJob.requestsRecovery()).build();

            scheduler.addJob(newJob, true);
            ChplJob newChplJob = new ChplJob(newJob);
            return newChplJob;
        } else {
            throw new AccessDeniedException("Can not update this job");
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public void retireAcb(String acb) throws SchedulerException, ValidationException {
        List<ChplRepeatableTrigger> allTriggers = getAllTriggersForUser();
        for (ChplRepeatableTrigger trigger : allTriggers) {
            if (!StringUtils.isEmpty(trigger.getAcb()) && trigger.getAcb().indexOf(acb) > -1) {
                ArrayList<String> acbs = new ArrayList<String>(Arrays.asList(trigger.getAcb().split(DATA_DELIMITER)));
                acbs.remove(acb);
                if (acbs.size() > 0) {
                    trigger.setAcb(String.join(DATA_DELIMITER, acbs));
                    createTrigger(trigger);
                }
                deleteTrigger(trigger.getGroup(), trigger.getName());
            }
        }
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SCHEDULER, "
            + "T(gov.healthit.chpl.permissions.domains.SchedulerDomainPermissions).UPDATE_ACB_NAME)")
    public void changeAcbName(String oldAcb, String newAcb) throws SchedulerException, ValidationException {
        Scheduler scheduler = getScheduler();

        // have to get all triggers in the system here without a permission check because
        // the acb name has been changed and the permission check will never pass
        // since it compares the acb names the user has access to (where name has changed) with
        // acb names in the trigger (where name has not changed).
        List<ChplRepeatableTrigger> allTriggers = getAllTriggers();

        for (ChplRepeatableTrigger trigger : allTriggers) {
            if (!StringUtils.isEmpty(trigger.getAcb()) && trigger.getAcb().indexOf(oldAcb) > -1) {
                ArrayList<String> acbs = new ArrayList<String>(Arrays.asList(trigger.getAcb().split(DATA_DELIMITER)));
                acbs.remove(oldAcb);
                acbs.add(newAcb);
                trigger.setAcb(String.join(DATA_DELIMITER, acbs));
                // create the trigger - can't use the method in this class (createTrigger)
                // because it will check user permissions and a user that has permission to change an ACB name
                // may not have permissions on all the jobs that include that ACB (like if the job includes other ACBs
                // that the current user doesn't have access to)
                TriggerKey triggerId = triggerKey(createTriggerName(trigger), createTriggerGroup(trigger.getJob()));
                JobKey jobId = jobKey(trigger.getJob().getName(), trigger.getJob().getGroup());
                Trigger qzTrigger = newTrigger().withIdentity(triggerId).startNow().forJob(jobId)
                        .usingJobData("email", trigger.getEmail()).usingJobData("acb", trigger.getAcb())
                        .withSchedule(cronSchedule(trigger.getCronSchedule())).build();
                scheduler.scheduleJob(qzTrigger);
                // delete the trigger - can't use the method in this class
                // because it will check user permissions but that will not give the user access
                // to the trigger to allow them to delete it; the permissions check is done
                // by acb name but the acb will have a different name now and the check will never pass.
                TriggerKey triggerKey = triggerKey(trigger.getName(), trigger.getGroup());
                scheduler.unscheduleJob(triggerKey);
            }
        }
    }

    private Scheduler getScheduler() throws SchedulerException {
        return chplScheduler.getScheduler();
    }

    private List<ChplRepeatableTrigger> getAllTriggers() throws SchedulerException {
        List<ChplRepeatableTrigger> allTriggers = new ArrayList<ChplRepeatableTrigger>();
        Scheduler scheduler = getScheduler();
        for (String group : scheduler.getTriggerGroupNames()) {
            // enumerate each trigger in group
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(groupEquals(group))) {
                if (scheduler.getTrigger(triggerKey).getJobKey().getGroup().equalsIgnoreCase(CHPL_JOBS_KEY)) {
                    allTriggers.add(getChplTrigger(triggerKey));
                }
            }
        }
        return allTriggers;
    }

    private ChplRepeatableTrigger getChplTrigger(TriggerKey triggerKey) throws SchedulerException {
        CronTrigger cronTrigger = (CronTrigger) getScheduler().getTrigger(triggerKey);
        ChplRepeatableTrigger chplTrigger = new ChplRepeatableTrigger(cronTrigger);

        JobDetail jobDetail = getScheduler().getJobDetail(getScheduler().getTrigger(triggerKey).getJobKey());
        ChplJob chplJob = new ChplJob(jobDetail);
        chplTrigger.setJob(chplJob);
        chplTrigger.getJob().setJobDataMap(mergeJobData(cronTrigger, chplJob));

        return chplTrigger;
    }

    private JobDataMap mergeJobData(Trigger trigger, ChplJob job) {
        JobDataMap merged = new JobDataMap();
        if (job.getJobDataMap() != null) {
            merged.putAll(job.getJobDataMap());
        }
        if (trigger.getJobDataMap() != null) {
            merged.putAll(trigger.getJobDataMap());
        }
        return merged;
    }

    private Boolean doesUserHavePermissionToJob(JobDetail jobDetail) {
        // Get the authorities from the job
        if (jobDetail.getJobDataMap().containsKey("authorities")) {
            List<String> authorities = new ArrayList<String>(
                    Arrays.asList(jobDetail.getJobDataMap().get("authorities").toString().split(AUTHORITY_DELIMITER)));
            Set<GrantedPermission> userRoles = AuthUtil.getCurrentUser().getPermissions();
            for (GrantedPermission permission : userRoles) {
                for (String authority : authorities) {
                    if (permission.getAuthority().equalsIgnoreCase(authority)) {
                        return true;
                    }
                }
            }
        } else {
            // If no authorities are present, we assume there are no permissions
            // on the job
            // and everyone has access
            return true;
        }
        // At this point we have fallen through all of the logic, and the user
        // does not have the appropriate rights
        return false;
    }

    private Boolean doesUserHavePermissionToTrigger(Trigger trigger) throws SchedulerException {
        // first check user has permission on job
        if (doesUserHavePermissionToJob(getScheduler().getJobDetail(trigger.getJobKey()))) {
            if (!StringUtils.isEmpty(trigger.getJobDataMap().getString("acb"))) {
                // get acbs user has access to
                List<CertificationBodyDTO> validAcbs = resourcePermissions.getAllAcbsForCurrentUser();
                for (String acb : trigger.getJobDataMap().getString("acb").split(DATA_DELIMITER)) {
                    boolean found = false;
                    for (CertificationBodyDTO validAcb : validAcbs) {
                        if (acb.equalsIgnoreCase(validAcb.getName())) {
                            found = true;
                        }
                    }
                    if (!found) {
                        return false;
                    }
                }
            }
            // if not acb specific no need to check acbs
            return true;
        }
        return false;
    }

    private String createTriggerGroup(ChplJob job) {
        return createTriggerGroup(job.getName());
    }

    private String createTriggerGroup(String triggerName) {
        String group = triggerName.replaceAll(" ", "");
        group += "Trigger";
        return group;
    }

    private String createTriggerName(ChplRepeatableTrigger trigger) {
        String name = trigger.getEmail().replaceAll("\\.", "_");
        if (!StringUtils.isEmpty(trigger.getAcb())) {
            name += trigger.getAcb();
        }
        return name;
    }

    private String createTriggerName(ChplOneTimeTrigger trigger) {
        Date toFormat = new Date(trigger.getRunDateMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        return sdf.format(toFormat);
    }
}
