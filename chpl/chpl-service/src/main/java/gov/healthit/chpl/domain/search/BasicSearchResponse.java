package gov.healthit.chpl.domain.search;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BasicSearchResponse implements Serializable {
    private static final long serialVersionUID = 2569559170265522799L;

    private List<CertifiedProductFlatSearchResult> results;
    private boolean directReviewsAvailable;

    @JsonView({
            SearchViews.Default.class
    })
    public List<CertifiedProductFlatSearchResult> getResults() {
        return results;
    }

    public void setResults(final List<CertifiedProductFlatSearchResult> results) {
        this.results = results;
    }

    @JsonView({
        SearchViews.Default.class
    })
    public boolean isDirectReviewsAvailable() {
        return directReviewsAvailable;
    }

    public void setDirectReviewsAvailable(boolean directReviewsAvailable) {
        this.directReviewsAvailable = directReviewsAvailable;
    }
}
