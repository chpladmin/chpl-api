package gov.healthit.chpl.domain.schedule;

import java.io.Serializable;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChplJob implements Serializable {
    private static final long serialVersionUID = -7634197357525761051L;
    private String description;
    private String group;
    private String name;
    private JobDataMap jobDataMap;

    public ChplJob(JobDetail jobDetails) {
        this.description = jobDetails.getDescription();
        this.group = jobDetails.getKey().getGroup();
        this.name = jobDetails.getKey().getName();
        if (jobDetails.getJobDataMap() != null) {
            this.jobDataMap = jobDetails.getJobDataMap();
        }
    }
}
