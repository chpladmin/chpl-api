package gov.healthit.chpl.domain.schedule;

import org.quartz.CronTrigger;
import org.quartz.Trigger;

import gov.healthit.chpl.domain.concept.ScheduleTypeConcept;

/**
 * Trigger used for Cache Status Age job.
 * @author alarned
 *
 */
public class CacheStatusAgeTrigger extends ChplTrigger {
    private String cronSchedule;
    private String email;

    /**
     * Constructor based on Quartz trigger.
     * @param quartzTrigger the quartz trigger
     */
    public CacheStatusAgeTrigger(final CronTrigger quartzTrigger) {
        super(quartzTrigger);
        this.setScheduleType(ScheduleTypeConcept.CACHE_STATUS_AGE_NOTIFICATION);
        this.cronSchedule = quartzTrigger.getCronExpression();
        this.email = quartzTrigger.getJobDataMap().getString("email");
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
