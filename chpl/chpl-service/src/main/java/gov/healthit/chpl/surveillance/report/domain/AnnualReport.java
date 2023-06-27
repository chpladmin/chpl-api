package gov.healthit.chpl.surveillance.report.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.surveillance.report.entity.AnnualReportEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnualReport implements Serializable {
    private static final long serialVersionUID = 8743838678379539305L;

    private Long id;
    private CertificationBody acb;
    private Integer year;
    private String obstacleSummary;
    private String priorityChangesFromFindingsSummary;

    public AnnualReport(AnnualReportEntity entity) {
        this.id = entity.getId();
        this.year = entity.getYear();
        if (entity.getAcb() != null) {
            this.acb = entity.getAcb().toDomain();
        }
        this.obstacleSummary = entity.getObstacleSummary();
        this.priorityChangesFromFindingsSummary = entity.getFindingsSummary();
    }
}
