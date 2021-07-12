package gov.healthit.chpl.domain.search;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonView;

import gov.healthit.chpl.search.domain.SearchViews;

@Deprecated
public class CertifiedProductBasicSearchResultLegacy extends CertifiedProductSearchResultLegacy {
    private static final long serialVersionUID = -2547390525592841038L;

    @JsonView({
            SearchViews.Default.class
    })
    private Set<String> previousDevelopers; // list of previous developer names
                                             // that owned the product

    @JsonView({
            SearchViews.Default.class
    })
    private Set<String> criteriaMet; // list of criteria numbers

    @JsonView({
            SearchViews.Default.class
    })
    private Set<String> cqmsMet; // list of cmqs that were met (any version)

    //list of start and end dates for surveillance, each entry will be start&end or just start&
    private Set<String> surveillanceDates;

    public CertifiedProductBasicSearchResultLegacy() {
        previousDevelopers = new HashSet<String>();
        criteriaMet = new HashSet<String>();
        cqmsMet = new HashSet<String>();
        surveillanceDates = new HashSet<String>();
    }

    public Set<String> getCriteriaMet() {
        return criteriaMet;
    }

    public void setCriteriaMet(final Set<String> criteriaMet) {
        this.criteriaMet = criteriaMet;
    }

    public Set<String> getCqmsMet() {
        return cqmsMet;
    }

    public void setCqmsMet(final Set<String> cqmsMet) {
        this.cqmsMet = cqmsMet;
    }

    public Set<String> getPreviousDevelopers() {
        return previousDevelopers;
    }

    public void setPreviousDevelopers(final Set<String> previousDevelopers) {
        this.previousDevelopers = previousDevelopers;
    }

    public Set<String> getSurveillanceDates() {
        return surveillanceDates;
    }

    public void setSurveillanceDates(final Set<String> surveillanceDates) {
        this.surveillanceDates = surveillanceDates;
    }
}
