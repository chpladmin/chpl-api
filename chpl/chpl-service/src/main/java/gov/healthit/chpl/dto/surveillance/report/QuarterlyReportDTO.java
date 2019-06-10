package gov.healthit.chpl.dto.surveillance.report;

import java.util.Calendar;
import java.util.Date;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.surveillance.report.QuarterlyReportEntity;

public class QuarterlyReportDTO {

    private Long id;
    private CertificationBodyDTO acb;
    private Integer year;
    private QuarterDTO quarter;
    private String activitiesOutcomesSummary;
    private String reactiveSummary;
    private String prioritizedElementSummary;
    private String transparencyDisclosureSummary;

    public QuarterlyReportDTO() {}

    public QuarterlyReportDTO(final QuarterlyReportEntity entity) {
        this.id = entity.getId();
        this.year = entity.getYear();
        this.activitiesOutcomesSummary = entity.getActivitiesOutcomesSummary();
        this.reactiveSummary = entity.getReactiveSummary();
        this.prioritizedElementSummary = entity.getPrioritizedElementSummary();
        this.transparencyDisclosureSummary = entity.getTransparencyDisclosureSummary();

        if (entity.getAcb() != null) {
            this.acb = new CertificationBodyDTO();
            this.acb.setId(entity.getAcb().getId());
            this.acb.setName(entity.getAcb().getName());
            this.acb.setAcbCode(entity.getAcb().getAcbCode());
            this.acb.setRetired(entity.getAcb().getRetired());
        } else {
            this.acb = new CertificationBodyDTO();
            this.acb.setId(entity.getCertificationBodyId());
        }

        if (entity.getQuarter() != null) {
            this.quarter = new QuarterDTO(entity.getQuarter());
        } else {
            this.quarter = new QuarterDTO();
            this.quarter.setId(entity.getQuarterId());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public CertificationBodyDTO getAcb() {
        return acb;
    }

    public void setAcb(final CertificationBodyDTO acb) {
        this.acb = acb;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(final Integer year) {
        this.year = year;
    }

    public QuarterDTO getQuarter() {
        return quarter;
    }

    public void setQuarter(final QuarterDTO quarter) {
        this.quarter = quarter;
    }

    public Date getStartDate() {
        if (getYear() == null || getQuarter() == null) {
            return null;
        }
        Calendar quarterStartCal = Calendar.getInstance();
        quarterStartCal.set(Calendar.YEAR, getYear());
        quarterStartCal.set(Calendar.MONTH, getQuarter().getStartMonth()-1);
        quarterStartCal.set(Calendar.DAY_OF_MONTH, getQuarter().getStartDay());
        return quarterStartCal.getTime();
    }

    public Date getEndDate() {
        if (getYear() == null || getQuarter() == null) {
            return null;
        }
        Calendar quarterEndCal = Calendar.getInstance();
        quarterEndCal.set(Calendar.YEAR, getYear());
        quarterEndCal.set(Calendar.MONTH, getQuarter().getEndMonth()-1);
        quarterEndCal.set(Calendar.DAY_OF_MONTH, getQuarter().getEndDay());
        return quarterEndCal.getTime();
    }

    public String getActivitiesOutcomesSummary() {
        return activitiesOutcomesSummary;
    }

    public void setActivitiesOutcomesSummary(final String activitiesOutcomesSummary) {
        this.activitiesOutcomesSummary = activitiesOutcomesSummary;
    }

    public String getReactiveSummary() {
        return reactiveSummary;
    }

    public void setReactiveSummary(final String reactiveSummary) {
        this.reactiveSummary = reactiveSummary;
    }

    public String getPrioritizedElementSummary() {
        return prioritizedElementSummary;
    }

    public void setPrioritizedElementSummary(final String prioritizedElementSummary) {
        this.prioritizedElementSummary = prioritizedElementSummary;
    }

    public String getTransparencyDisclosureSummary() {
        return transparencyDisclosureSummary;
    }

    public void setTransparencyDisclosureSummary(final String transparencyDisclosureSummary) {
        this.transparencyDisclosureSummary = transparencyDisclosureSummary;
    }

}
