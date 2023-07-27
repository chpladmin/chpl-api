package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

@Deprecated
public enum CertificationEditionConcept implements Serializable {
    CERTIFICATION_EDITION_2011(1L, "2011"), CERTIFICATION_EDITION_2014(2L, "2014"), CERTIFICATION_EDITION_2015(3L,
            "2015");

    @Deprecated
    private final Long id;
    @Deprecated
    private final String year;

    CertificationEditionConcept(Long id, String year) {
        this.id = id;
        this.year = year;
    }

    @Deprecated
    public Long getId() {
        return id;
    }

    @Deprecated
    public String getYear() {
        return year;
    }
}
