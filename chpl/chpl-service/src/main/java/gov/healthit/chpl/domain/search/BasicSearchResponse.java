package gov.healthit.chpl.domain.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BasicSearchResponse implements Serializable {
	private static final long serialVersionUID = 2569559170265522799L;

	private List<CertifiedProductBasicSearchResult> results = new ArrayList<CertifiedProductBasicSearchResult>();
	
	public BasicSearchResponse() {}

	public List<CertifiedProductBasicSearchResult> getResults() {
		return results;
	}

	public void setResults(List<CertifiedProductBasicSearchResult> results) {
		this.results = results;
	}
}
