package gov.healthit.chpl.search.domain;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@SuperBuilder
public class CertifiedProductBasicSearchResult extends CertifiedProductSearchResult {
    private static final long serialVersionUID = -2547390525592841038L;

    @JsonView({
            SearchViews.Default.class
    })
    private Set<String> previousDevelopers; // list of previous developer names
                                             // that owned the product

    @JsonView({
            SearchViews.Default.class
    })
    private Set<Long> criteriaMet; // list of criteria IDs

    @JsonView({
            SearchViews.Default.class
    })
    private Set<String> cqmsMet; // list of cmqs that were met (any version)

    //list of start and end dates for surveillance, each entry will be start&end or just start&
    private Set<String> surveillanceDates;

    //list of status changes for the listing, each entry will be of the format statusDate?statusName
    private Set<String> statusEvents;

    //list of criteria with API documentation and the corresponding value
    private Set<String> apiDocumentation;

    //list of criteria with Service Base URL List and the corresponding value
    private Set<String> serviceBaseUrlList;

    private String rwtPlansUrl;
    private String rwtResultsUrl;

    public CertifiedProductBasicSearchResult() {
        this.setDirectReviewCount(0);
        this.setSurveillanceCount(0L);
        this.setOpenDirectReviewNonConformityCount(0);
        this.setClosedDirectReviewNonConformityCount(0);
        this.setOpenSurveillanceCount(0L);
        this.setClosedSurveillanceCount(0L);
        this.setOpenSurveillanceNonConformityCount(0L);
        this.setClosedSurveillanceNonConformityCount(0L);
        previousDevelopers = new HashSet<String>();
        criteriaMet = new HashSet<Long>();
        cqmsMet = new HashSet<String>();
        surveillanceDates = new HashSet<String>();
        statusEvents = new HashSet<String>();
        apiDocumentation = new HashSet<String>();
        serviceBaseUrlList = new HashSet<String>();
    }

    public CertifiedProductBasicSearchResult(CertifiedProductBasicSearchResult other) {
        super(other);
        this.rwtPlansUrl = other.getRwtPlansUrl();
        this.rwtResultsUrl = other.getRwtResultsUrl();
    }

    @Override
    public boolean equals(Object another) {
        return super.equals(another);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
