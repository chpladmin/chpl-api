package gov.healthit.chpl.surveillance.report.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuarterlyReport implements Serializable {
    private static final long serialVersionUID = 8743838678379539305L;

    private Long id;
    private CertificationBody acb;
    private Integer year;
    private String quarter;
    private Long startDate;
    private Long endDate;
    private String surveillanceActivitiesAndOutcomes;
    @Deprecated
    private String reactiveSummary;
    private String reactiveSurveillanceSummary;
    private String prioritizedElementSummary;
    @Deprecated
    private String transparencyDisclosureSummary;
    private String disclosureRequirementsSummary;
    private boolean acknowledgeWarnings;

    public QuarterlyReport(QuarterlyReportDTO dto) {
        this();
        this.id = dto.getId();
        this.year = dto.getYear();
        this.surveillanceActivitiesAndOutcomes = dto.getActivitiesOutcomesSummary();
        this.reactiveSummary = dto.getReactiveSurveillanceSummary();
        this.reactiveSurveillanceSummary = dto.getReactiveSurveillanceSummary();
        this.prioritizedElementSummary = dto.getPrioritizedElementSummary();
        this.transparencyDisclosureSummary = dto.getDisclosureRequirementsSummary();
        this.disclosureRequirementsSummary = dto.getDisclosureRequirementsSummary();
        if (dto.getQuarter() != null) {
            this.quarter = dto.getQuarter().getName();
        }
        if (dto.getAcb() != null) {
            this.acb = new CertificationBody(dto.getAcb());
        }
        this.startDate = dto.getStartDateTime().getTime();
        this.endDate = dto.getEndDateTime().getTime();
    }
}
