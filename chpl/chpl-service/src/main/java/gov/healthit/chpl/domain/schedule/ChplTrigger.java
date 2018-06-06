package gov.healthit.chpl.domain.schedule;

import java.io.Serializable;

import org.quartz.CronTrigger;

import gov.healthit.chpl.domain.concept.ScheduleTypeConcept;

/**
 * Basic representation of Quartz Triggers.
 * @author alarned
 *
 */
public class ChplTrigger implements Serializable {
    private static final long serialVersionUID = -2569776712615051892L;

    private String name;
    private String group;
    private String jobName;
    private String jobGroup;
    private ScheduleTypeConcept scheduleType;
    private String cronSchedule;
    private String email;

    ChplTrigger() { }

    /**
     * Constructor based on CHPL trigger.
     * @param chplTrigger the quartz trigger
     */
    ChplTrigger(final ChplTrigger chplTrigger) {
        this.name = chplTrigger.getName();
        this.group = chplTrigger.getGroup();
        this.jobName = chplTrigger.getJobName();
        this.jobGroup = chplTrigger.getJobGroup();
        this.cronSchedule = chplTrigger.getCronSchedule();
        this.email = chplTrigger.getEmail();
    }

    /**
     * Constructor based on Quartz trigger.
     * @param quartzTrigger the quartz trigger
     */
    public ChplTrigger(final CronTrigger quartzTrigger) {
        this.name = quartzTrigger.getKey().getName();
        this.group = quartzTrigger.getKey().getGroup();
        this.jobName = quartzTrigger.getJobKey().getName();
        this.jobGroup = quartzTrigger.getJobKey().getName();
        this.cronSchedule = quartzTrigger.getCronExpression();
        this.email = quartzTrigger.getJobDataMap().getString("email");
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(final String jobName) {
        this.jobName = jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(final String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public ScheduleTypeConcept getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(final ScheduleTypeConcept scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getCronSchedule() {
        return cronSchedule;
    }

    public void setCronSchedule(final String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
