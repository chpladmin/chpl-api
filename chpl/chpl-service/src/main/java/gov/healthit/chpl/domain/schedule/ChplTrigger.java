package gov.healthit.chpl.domain.schedule;

import org.quartz.Trigger;

import gov.healthit.chpl.domain.concept.ScheduleTypeConcept;

/**
 * Basic representation of Quartz Triggers.
 * @author alarned
 *
 */
public abstract class ChplTrigger {
    private String name;
    private String group;
    private String jobName;
    private String jobGroup;
    private ScheduleTypeConcept scheduleType;

    /**
     * Constructor based on Quartz trigger.
     * @param quartzTrigger the quartz trigger
     */
    ChplTrigger(final Trigger quartzTrigger) {
        this.name = quartzTrigger.getKey().getName();
        this.group = quartzTrigger.getKey().getGroup();
        this.jobName = quartzTrigger.getJobKey().getName();
        this.jobGroup = quartzTrigger.getJobKey().getName();
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
}
