package gov.healthit.chpl.domain.schedule;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledSystemJob {
    private String name;
    private String description;
    private Date nextRunDate;
    private TriggerSchedule triggerScheduleType;
    private String triggerName;
    private String triggerGroup;

//    public ScheduledSystemJob(String name, String description, Date nextRunDate, TriggerSchedule triggerScheduleType) {
//        this.name = name;
//        this.description = description;
//        this.nextRunDate = nextRunDate;
//        this.triggerScheduleType = triggerScheduleType;
//
//    }
}
