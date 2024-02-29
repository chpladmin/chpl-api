package gov.healthit.chpl.domain.schedule;

import java.util.Date;

import org.quartz.JobDataMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    @JsonIgnore
    private JobDataMap jobDataMap;
    private Date nextRunDate;
    private TriggerSchedule triggerScheduleType;
    private String triggerName;
    private String triggerGroup;
}
