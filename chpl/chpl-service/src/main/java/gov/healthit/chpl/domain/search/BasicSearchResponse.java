package gov.healthit.chpl.domain.search;

import java.io.Serializable;
import java.util.List;

public class BasicSearchResponse implements Serializable {
	private static final long serialVersionUID = 2569559170265522799L;

	private List<? extends CertifiedProductSearchResult> results;
	
	public BasicSearchResponse() {}

	public List<? extends CertifiedProductSearchResult> getResults() {
		return results;
	}

	public void setResults(List<? extends CertifiedProductSearchResult> results) {
		this.results = results;
	}
}
