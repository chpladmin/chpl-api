package gov.healthit.chpl.domain.schedule;

import java.io.Serializable;

import org.quartz.CronTrigger;

/**
 * Basic representation of Quartz Triggers.
 * @author alarned
 *
 */
public class ChplTrigger implements Serializable {
    private static final long serialVersionUID = -2569776712615051892L;

    private String name;
    private String group;
    private ChplJob job;
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
        this.job = chplTrigger.getJob();
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
        this.job = new ChplJob();
        this.job.setDescription("");
        this.job.setGroup(quartzTrigger.getJobKey().getGroup());
        this.job.setName(quartzTrigger.getJobKey().getName());
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

    public ChplJob getJob() {
        return job;
    }

    public void setJob(final ChplJob job) {
        this.job = job;
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
