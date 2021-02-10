package gov.healthit.chpl.surveillance.report.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.surveillance.report.dto.AnnualReportDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;

public class AnnualReport implements Serializable {
    private static final long serialVersionUID = 8743838678379539305L;

    private Long id;
    private CertificationBody acb;
    private Integer year;
    private String obstacleSummary;
    private String priorityChangesFromFindingsSummary;

    public AnnualReport() {
    }

    public AnnualReport(final AnnualReportDTO dto) {
        this.id = dto.getId();
        this.year = dto.getYear();
        if (dto.getAcb() != null) {
            this.acb = new CertificationBody(dto.getAcb());
        }
        this.obstacleSummary = dto.getObstacleSummary();
        this.priorityChangesFromFindingsSummary = dto.getFindingsSummary();
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

    public String getObstacleSummary() {
        return obstacleSummary;
    }

    public void setObstacleSummary(final String obstacleSummary) {
        this.obstacleSummary = obstacleSummary;
    }

    public String getPriorityChangesFromFindingsSummary() {
        return priorityChangesFromFindingsSummary;
    }

    public void setPriorityChangesFromFindingsSummary(final String priorityChangesFromFindingsSummary) {
        this.priorityChangesFromFindingsSummary = priorityChangesFromFindingsSummary;
    }

}
