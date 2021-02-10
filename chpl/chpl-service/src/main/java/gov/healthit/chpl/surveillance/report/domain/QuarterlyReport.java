package gov.healthit.chpl.surveillance.report.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;

public class QuarterlyReport implements Serializable {
    private static final long serialVersionUID = 8743838678379539305L;

    private Long id;
    private CertificationBody acb;
    private Integer year;
    private String quarter;
    private Long startDate;
    private Long endDate;
    private String surveillanceActivitiesAndOutcomes;
    private String reactiveSummary;
    private String prioritizedElementSummary;
    private String transparencyDisclosureSummary;

    public QuarterlyReport() {
    }

    public QuarterlyReport(final QuarterlyReportDTO dto) {
        this();
        this.id = dto.getId();
        this.year = dto.getYear();
        this.surveillanceActivitiesAndOutcomes = dto.getActivitiesOutcomesSummary();
        this.reactiveSummary = dto.getReactiveSummary();
        this.prioritizedElementSummary = dto.getPrioritizedElementSummary();
        this.transparencyDisclosureSummary = dto.getTransparencyDisclosureSummary();
        if (dto.getQuarter() != null) {
            this.quarter = dto.getQuarter().getName();
        }
        if (dto.getAcb() != null) {
            this.acb = new CertificationBody(dto.getAcb());
        }
        this.startDate = dto.getStartDate().getTime();
        this.endDate = dto.getEndDate().getTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public CertificationBody getAcb() {
        return acb;
    }

    public void setAcb(final CertificationBody acb) {
        this.acb = acb;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(final Integer year) {
        this.year = year;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(final String quarter) {
        this.quarter = quarter;
    }

    public String getSurveillanceActivitiesAndOutcomes() {
        return surveillanceActivitiesAndOutcomes;
    }

    public void setSurveillanceActivitiesAndOutcomes(final String surveillanceActivitiesAndOutcomes) {
        this.surveillanceActivitiesAndOutcomes = surveillanceActivitiesAndOutcomes;
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

    public Long getStartDate() {
        return startDate;
    }

    public Long getEndDate() {
        return endDate;
    }
}
