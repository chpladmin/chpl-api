package gov.healthit.chpl.surveillance.report.dto;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.surveillance.report.AnnualReportEntity;

public class AnnualReportDTO {

    private Long id;
    private Integer year;
    private CertificationBodyDTO acb;
    private String obstacleSummary;
    private String findingsSummary;

    public AnnualReportDTO() {}

    public AnnualReportDTO(final AnnualReportEntity entity) {
        this.id = entity.getId();
        this.year = entity.getYear();
        this.obstacleSummary = entity.getObstacleSummary();
        this.findingsSummary = entity.getFindingsSummary();
        if (entity.getAcb() != null) {
            this.acb = new CertificationBodyDTO(entity.getAcb());
        } else {
            this.acb = new CertificationBodyDTO();
            this.acb.setId(entity.getCertificationBodyId());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(final Integer year) {
        this.year = year;
    }

    public CertificationBodyDTO getAcb() {
        return acb;
    }

    public void setAcb(final CertificationBodyDTO acb) {
        this.acb = acb;
    }

    public String getObstacleSummary() {
        return obstacleSummary;
    }

    public void setObstacleSummary(final String obstacleSummary) {
        this.obstacleSummary = obstacleSummary;
    }

    public String getFindingsSummary() {
        return findingsSummary;
    }

    public void setFindingsSummary(final String findingsSummary) {
        this.findingsSummary = findingsSummary;
    }
}
