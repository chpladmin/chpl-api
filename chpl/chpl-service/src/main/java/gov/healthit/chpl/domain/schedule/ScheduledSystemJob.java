package gov.healthit.chpl.domain.schedule;

import java.text.DateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ScheduledSystemJob {
    private String name;
    private String description;
    private Date nextRunDate;
    private TriggerSchedule triggerScheduleType;

    public ScheduledSystemJob() {

    }

    public ScheduledSystemJob(String name, String description, Date nextRunDate, TriggerSchedule triggerScheduleType) {
        this.name = name;
        this.description = description;
        this.nextRunDate = nextRunDate;
        this.triggerScheduleType = triggerScheduleType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getNextRunDate() {
        return nextRunDate;
    }

    public TriggerSchedule getTriggerScheduleType() {
        return triggerScheduleType;
    }

    @JsonIgnore
    public String getNextRunDatePretty() {
        return DateFormat.getDateInstance().format(nextRunDate) ;
    }

    @JsonIgnore
    public String getTriggerScheduleTypePretty() {
        return triggerScheduleType.getPrettyName();
    }
}
