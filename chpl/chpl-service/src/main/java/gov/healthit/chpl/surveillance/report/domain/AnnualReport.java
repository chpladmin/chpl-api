package gov.healthit.chpl.surveillance.report.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.surveillance.report.dto.AnnualReportDTO;
import lombok.Data;

@Data
public class AnnualReport implements Serializable {
    private static final long serialVersionUID = 8743838678379539305L;

    private Long id;
    private CertificationBody acb;
    private Integer year;
    private String obstacleSummary;
    private String priorityChangesFromFindingsSummary;

    public AnnualReport() {
    }

    public AnnualReport(AnnualReportDTO dto) {
        this.id = dto.getId();
        this.year = dto.getYear();
        if (dto.getAcb() != null) {
            this.acb = new CertificationBody(dto.getAcb());
        }
        this.obstacleSummary = dto.getObstacleSummary();
        this.priorityChangesFromFindingsSummary = dto.getFindingsSummary();
    }
}
