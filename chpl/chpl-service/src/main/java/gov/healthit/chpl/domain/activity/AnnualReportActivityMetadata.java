package gov.healthit.chpl.domain.activity;

import gov.healthit.chpl.domain.CertificationBody;

public class AnnualReportActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = -3877462517237563058L;

    private CertificationBody acb;
    private Integer year;

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

}
