package gov.healthit.chpl.surveillance.report.dto;

import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.surveillance.report.QuarterlyReportEntity;
import lombok.Data;

@Data
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

    public QuarterlyReportDTO() {
    }

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

    public Date getStartDate() {
        if (getYear() == null || getQuarter() == null) {
            return null;
        }
        Calendar quarterStartCal = Calendar.getInstance();
        quarterStartCal.set(Calendar.YEAR, getYear());
        quarterStartCal.set(Calendar.MONTH, getQuarter().getStartMonth() - 1);
        quarterStartCal.set(Calendar.DAY_OF_MONTH, getQuarter().getStartDay());
        quarterStartCal.set(Calendar.HOUR_OF_DAY, 0);
        quarterStartCal.set(Calendar.MINUTE, 0);
        quarterStartCal.set(Calendar.SECOND, 0);
        quarterStartCal.set(Calendar.MILLISECOND, 0);
        return quarterStartCal.getTime();
    }

    public Date getEndDate() {
        if (getYear() == null || getQuarter() == null) {
            return null;
        }
        Calendar quarterEndCal = Calendar.getInstance();
        quarterEndCal.set(Calendar.YEAR, getYear());
        quarterEndCal.set(Calendar.MONTH, getQuarter().getEndMonth() - 1);
        quarterEndCal.set(Calendar.DAY_OF_MONTH, getQuarter().getEndDay());
        quarterEndCal.set(Calendar.HOUR_OF_DAY, 23);
        quarterEndCal.set(Calendar.MINUTE, 59);
        quarterEndCal.set(Calendar.SECOND, 59);
        quarterEndCal.set(Calendar.MILLISECOND, 999);
        return quarterEndCal.getTime();
    }
}
