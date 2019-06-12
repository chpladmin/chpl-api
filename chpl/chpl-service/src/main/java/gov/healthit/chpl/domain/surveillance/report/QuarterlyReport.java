package gov.healthit.chpl.domain.surveillance.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;

public class QuarterlyReport implements Serializable {
    private static final long serialVersionUID = 8743838678379539305L;

    private Long id;
    private CertificationBody acb;
    private Integer year;
    private String quarter;
    private String surveillanceActivitiesAndOutcomes;
    private String reactiveSummary;
    private String prioritizedElementSummary;
    private String transparencyDisclosureSummary;
    private List<CertifiedProduct> relevantListings;

    public QuarterlyReport() {
        relevantListings = new ArrayList<CertifiedProduct>();
    }

    public QuarterlyReport(final QuarterlyReportDTO dto) {
        this();
        this.id = dto.getId();
        this.year = dto.getYear();
        this.surveillanceActivitiesAndOutcomes = dto.getActivitiesOutcomesSummary();
        this.reactiveSummary = dto.getReactiveSummary();
        this.prioritizedElementSummary = dto.getPrioritizedElementSummary();
        this.transparencyDisclosureSummary = dto.getTransparencyDisclosureSummary();
        if (dto.getQuarter() != null) {
            this.quarter = dto.getQuarter().getName();
        }
        if (dto.getAcb() != null) {
            this.acb = new CertificationBody(dto.getAcb());
        }
        if (dto.getRelevantListings() != null && dto.getRelevantListings().size() > 0) {
            for (CertifiedProductDetailsDTO relevantListingDto : dto.getRelevantListings()) {
                this.relevantListings.add(new CertifiedProduct(relevantListingDto));
            }
        }
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

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(final String quarter) {
        this.quarter = quarter;
    }

    public String getSurveillanceActivitiesAndOutcomes() {
        return surveillanceActivitiesAndOutcomes;
    }

    public void setSurveillanceActivitiesAndOutcomes(final String surveillanceActivitiesAndOutcomes) {
        this.surveillanceActivitiesAndOutcomes = surveillanceActivitiesAndOutcomes;
    }

    public String getReactiveSummary() {
        return reactiveSummary;
    }

    public void setReactiveSummary(final String reactiveSummary) {
        this.reactiveSummary = reactiveSummary;
    }

    public String getPrioritizedElementSummary() {
        return prioritizedElementSummary;
    }

    public void setPrioritizedElementSummary(final String prioritizedElementSummary) {
        this.prioritizedElementSummary = prioritizedElementSummary;
    }

    public String getTransparencyDisclosureSummary() {
        return transparencyDisclosureSummary;
    }

    public void setTransparencyDisclosureSummary(final String transparencyDisclosureSummary) {
        this.transparencyDisclosureSummary = transparencyDisclosureSummary;
    }

    public List<CertifiedProduct> getRelevantListings() {
        return relevantListings;
    }

    public void setRelevantListings(List<CertifiedProduct> relevantListings) {
        this.relevantListings = relevantListings;
    }
}
