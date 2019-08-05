package gov.healthit.chpl.domain.activity;

import gov.healthit.chpl.domain.CertificationBody;

public class QuarterlyReportActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = -3877401017237563058L;

    private CertificationBody acb;
    private String quarterName;
    private Integer year;

    public CertificationBody getAcb() {
        return acb;
    }
    public void setAcb(final CertificationBody acb) {
        this.acb = acb;
    }
    public String getQuarterName() {
        return quarterName;
    }
    public void setQuarterName(final String quarterName) {
        this.quarterName = quarterName;
    }
    public Integer getYear() {
        return year;
    }
    public void setYear(final Integer year) {
        this.year = year;
    }
}
