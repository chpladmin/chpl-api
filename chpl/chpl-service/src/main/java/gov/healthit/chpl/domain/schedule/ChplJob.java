package gov.healthit.chpl.domain.schedule;

import java.io.Serializable;

import org.quartz.JobDetail;

import gov.healthit.chpl.domain.concept.ScheduleFrequencyConcept;

public class ChplJob implements Serializable{
    private static final long serialVersionUID = -7634197357525761051L;
    private String description;
    private String group;
    private String name;
    private ScheduleFrequencyConcept frequency;
    
    public ChplJob(JobDetail jobDetails) {
        this.description = jobDetails.getDescription();
        this.group = jobDetails.getKey().getGroup();
        this.name = jobDetails.getKey().getName();
        if (jobDetails.getJobDataMap().containsKey("frequency")) {
            this.frequency = ScheduleFrequencyConcept.valueOf(jobDetails.getJobDataMap().get("frequency").toString());
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScheduleFrequencyConcept getFrequency() {
        return frequency;
    }

    public void setFrequency(ScheduleFrequencyConcept frequency) {
        this.frequency = frequency;
    }

}
