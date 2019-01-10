package gov.healthit.chpl.domain;

import java.util.List;

public class SurveillanceUploadResult {
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String NOT_STARTED = "NOT_STARTED";
    public static final String ERROR = "ERROR";
    public static final String SUCCESS = "SUCCESS";

    private List<Surveillance> surveillances = null;
    private Job job;
    private String jobStatus;

    public List<Surveillance> getSurveillances() {
        return surveillances;
    }

    public void setSurveillances(List<Surveillance> surveillances) {
        this.surveillances = surveillances;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }
}
