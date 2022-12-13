package gov.healthit.chpl.surveillance.report.dto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.surveillance.report.domain.Quarter;
import gov.healthit.chpl.surveillance.report.entity.QuarterlyReportEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuarterlyReportDTO {

    private Long id;
    private CertificationBodyDTO acb;
    private Integer year;
    private Quarter quarter;
    private String activitiesOutcomesSummary;
    private String reactiveSurveillanceSummary;
    private String prioritizedElementSummary;
    private String disclosureRequirementsSummary;

    public QuarterlyReportDTO(QuarterlyReportEntity entity) {
        this();
        this.id = entity.getId();
        this.year = entity.getYear();
        this.activitiesOutcomesSummary = entity.getActivitiesOutcomesSummary();
        this.reactiveSurveillanceSummary = entity.getReactiveSurveillanceSummary();
        this.prioritizedElementSummary = entity.getPrioritizedElementSummary();
        this.disclosureRequirementsSummary = entity.getDisclosureRequirementsSummary();

        if (entity.getAcb() != null) {
            this.acb = new CertificationBodyDTO(entity.getAcb());
        } else {
            this.acb = new CertificationBodyDTO();
            this.acb.setId(entity.getCertificationBodyId());
        }

        if (entity.getQuarter() != null) {
            this.quarter = new Quarter(entity.getQuarter());
        } else {
            this.quarter = new Quarter();
            this.quarter.setId(entity.getQuarterId());
        }
    }

    public LocalDate getStartDate() {
        if (getYear() == null || getQuarter() == null) {
            return null;
        }
        return LocalDate.of(getYear(), getQuarter().getStartMonth(), getQuarter().getStartDay());
    }

    @Deprecated
    public Date getStartDateTime() {
        LocalDate localDate = getStartDate();
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public LocalDate getEndDate() {
        if (getYear() == null || getQuarter() == null) {
            return null;
        }
        return LocalDate.of(getYear(), getQuarter().getEndMonth(), getQuarter().getEndDay());
    }

    @Deprecated
    public Date getEndDateTime() {
        LocalDate localDate = getEndDate();
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
