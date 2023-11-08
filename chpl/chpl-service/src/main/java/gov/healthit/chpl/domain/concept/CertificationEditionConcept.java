package gov.healthit.chpl.domain.concept;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

public enum CertificationEditionConcept implements Serializable {
    CERTIFICATION_EDITION_2011(1L, "2011"), CERTIFICATION_EDITION_2014(2L, "2014"), CERTIFICATION_EDITION_2015(3L,
            "2015");

    private final Long id;
    private final String year;

    CertificationEditionConcept(Long id, String year) {
        this.id = id;
        this.year = year;
    }

    public Long getId() {
        return id;
    }

    public String getYear() {
        return year;
    }

    public static CertificationEditionConcept getByYear(String year) {
        Optional<CertificationEditionConcept> matchedYear = Stream.of(values())
                .filter(val -> val.getYear().equals(year))
                .findAny();
        return matchedYear.isPresent() ? matchedYear.get() : null;
    }
}
