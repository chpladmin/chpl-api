package gov.healthit.chpl.domain.search;

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
    private Set<String> criteriaMet; // list of criteria IDs

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

    public CertifiedProductBasicSearchResult() {
        previousDevelopers = new HashSet<String>();
        criteriaMet = new HashSet<String>();
        cqmsMet = new HashSet<String>();
        surveillanceDates = new HashSet<String>();
        statusEvents = new HashSet<String>();
        apiDocumentation = new HashSet<String>();
        serviceBaseUrlList = new HashSet<String>();
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
