package gov.healthit.chpl.surveillance.report.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import lombok.Data;

@Data
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
        this.transparencyDisclosureSummary = dto.getDisclosureSummary();
        if (dto.getQuarter() != null) {
            this.quarter = dto.getQuarter().getName();
        }
        if (dto.getAcb() != null) {
            this.acb = new CertificationBody(dto.getAcb());
        }
        this.startDate = dto.getStartDate().getTime();
        this.endDate = dto.getEndDate().getTime();
    }
}
