package gov.healthit.chpl.surveillance.report.domain;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
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
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate startDay;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate endDay;
    private String surveillanceActivitiesAndOutcomes;
    private String reactiveSurveillanceSummary;
    private String prioritizedElementSummary;
    private String disclosureRequirementsSummary;
    private boolean acknowledgeWarnings;

    public QuarterlyReport(QuarterlyReportDTO dto) {
        this();
        this.id = dto.getId();
        this.year = dto.getYear();
        this.surveillanceActivitiesAndOutcomes = dto.getActivitiesOutcomesSummary();
        this.reactiveSurveillanceSummary = dto.getReactiveSurveillanceSummary();
        this.prioritizedElementSummary = dto.getPrioritizedElementSummary();
        this.disclosureRequirementsSummary = dto.getDisclosureRequirementsSummary();
        if (dto.getQuarter() != null) {
            this.quarter = dto.getQuarter().getName();
        }
        if (dto.getAcb() != null) {
            this.acb = dto.getAcb();
        }
        this.startDay = dto.getStartDate();
        this.endDay = dto.getEndDate();
    }
}
