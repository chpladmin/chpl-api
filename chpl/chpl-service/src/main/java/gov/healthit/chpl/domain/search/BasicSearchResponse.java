package gov.healthit.chpl.domain.search;

import java.io.Serializable;
import java.util.List;

public class BasicSearchResponse implements Serializable {
	private static final long serialVersionUID = 2569559170265522799L;

	private List<CertifiedProductFlatSearchResult> results;
	
	public BasicSearchResponse() {}

	public List<CertifiedProductFlatSearchResult> getResults() {
		return results;
	}

	public void setResults(List<CertifiedProductFlatSearchResult> results) {
		this.results = results;
	}
}
