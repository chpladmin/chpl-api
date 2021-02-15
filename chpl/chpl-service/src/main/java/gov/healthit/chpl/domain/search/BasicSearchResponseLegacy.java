package gov.healthit.chpl.domain.search;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Deprecated
public class BasicSearchResponseLegacy implements Serializable {
    private static final long serialVersionUID = 2569559170265522799L;

    private List<CertifiedProductFlatSearchResultLegacy> results;

    @JsonView({
            SearchViews.Default.class
    })
    public List<CertifiedProductFlatSearchResultLegacy> getResults() {
        return results;
    }

    public void setResults(final List<CertifiedProductFlatSearchResultLegacy> results) {
        this.results = results;
    }
}
