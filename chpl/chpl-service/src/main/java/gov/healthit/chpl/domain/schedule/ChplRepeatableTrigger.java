package gov.healthit.chpl.domain.schedule;

import java.io.Serializable;

import org.quartz.CronTrigger;

/**
 * Basic representation of Quartz Triggers.
 * 
 * @author alarned
 *
 */
public class ChplRepeatableTrigger extends ChplTrigger implements Serializable {
    private static final long serialVersionUID = -2569776712615051892L;

    private String name;
    private String group;
    private String cronSchedule;
    private String email;
    private String acb;

    ChplRepeatableTrigger() {
    }

    /**
     * Constructor based on CHPL trigger.
     * 
     * @param chplTrigger
     *            the quartz trigger
     */
    ChplRepeatableTrigger(final ChplRepeatableTrigger chplTrigger) {
        this.name = chplTrigger.getName();
        this.group = chplTrigger.getGroup();
        setJob(chplTrigger.getJob());
        this.cronSchedule = chplTrigger.getCronSchedule();
        this.email = chplTrigger.getEmail();
        this.acb = chplTrigger.getAcb();
    }

    /**
     * Constructor based on Quartz trigger.
     * 
     * @param quartzTrigger
     *            the quartz trigger
     */
    public ChplRepeatableTrigger(final CronTrigger quartzTrigger) {
        this.name = quartzTrigger.getKey().getName();
        this.group = quartzTrigger.getKey().getGroup();
        setJob(new ChplJob());
        getJob().setDescription("");
        getJob().setGroup(quartzTrigger.getJobKey().getGroup());
        getJob().setName(quartzTrigger.getJobKey().getName());
        this.cronSchedule = quartzTrigger.getCronExpression();
        this.email = quartzTrigger.getJobDataMap().getString("email");
        this.acb = quartzTrigger.getJobDataMap().getString("acb");
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

    public String getAcb() {
        return acb;
    }

    public void setAcb(final String acb) {
        this.acb = acb;
    }
}
