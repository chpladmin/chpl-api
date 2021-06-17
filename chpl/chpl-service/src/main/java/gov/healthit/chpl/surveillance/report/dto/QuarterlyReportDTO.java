package gov.healthit.chpl.surveillance.report.dto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.surveillance.report.entity.QuarterlyReportEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuarterlyReportDTO {

    private Long id;
    private CertificationBodyDTO acb;
    private Integer year;
    private QuarterDTO quarter;
    private String activitiesOutcomesSummary;
    private String reactiveSummary;
    private String prioritizedElementSummary;
    private String disclosureSummary;

    public QuarterlyReportDTO(QuarterlyReportEntity entity) {
        this();
        this.id = entity.getId();
        this.year = entity.getYear();
        this.activitiesOutcomesSummary = entity.getActivitiesOutcomesSummary();
        this.reactiveSummary = entity.getReactiveSummary();
        this.prioritizedElementSummary = entity.getPrioritizedElementSummary();
        this.disclosureSummary = entity.getDisclosureSummary();

        if (entity.getAcb() != null) {
            this.acb = new CertificationBodyDTO(entity.getAcb());
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

    public LocalDate getStartDate() {
        if (getYear() == null || getQuarter() == null) {
            return null;
        }
        return LocalDate.of(getYear(), getQuarter().getStartMonth() - 1, getQuarter().getStartDay());
    }

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
        return LocalDate.of(getYear(), getQuarter().getEndMonth() - 1, getQuarter().getEndDay());
    }

    public Date getEndDateTime() {
        LocalDate localDate = getEndDate();
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
