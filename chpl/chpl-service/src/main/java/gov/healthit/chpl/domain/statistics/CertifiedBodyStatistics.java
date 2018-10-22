package gov.healthit.chpl.domain.statistics;

import java.io.Serializable;

public class CertifiedBodyStatistics extends Statistic implements Serializable  {
    private static final long serialVersionUID = -2104318867819249394L;

    private String certificationStatusName;
    private Long totalDevelopersWithListings;
    private Long totalListings;

    public CertifiedBodyStatistics() {
    }

    public String getCertificationStatusName() {
        return certificationStatusName;
    }

    public void setCertificationStatusName(final String certificationStatusName) {
        this.certificationStatusName = certificationStatusName;
    }

    public Long getTotalDevelopersWithListings() {
        return totalDevelopersWithListings;
    }

    public void setTotalDevelopersWithListings(final Long totalDevelopersWithListings) {
        this.totalDevelopersWithListings = totalDevelopersWithListings;
    }

    public Long getTotalListings() {
        return totalListings;
    }

    public void setTotalListings(final Long totalListings) {
        this.totalListings = totalListings;
    }
}
