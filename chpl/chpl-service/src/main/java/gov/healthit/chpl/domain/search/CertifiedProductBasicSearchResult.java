package gov.healthit.chpl.domain.search;

import java.util.ArrayList;
import java.util.List;

public class CertifiedProductBasicSearchResult extends CertifiedProductSearchResult {
	private static final long serialVersionUID = -2547390525592841038L;
	
	private List<String> previousDevelopers; //list of previous developer names that owned the product
	private List<String> criteriaMet; //list of criteria numbers
	private List<String> cqmsMet; //list of cmqs that were met (any version)
	
	public CertifiedProductBasicSearchResult() {
		previousDevelopers = new ArrayList<String>();
		criteriaMet = new ArrayList<String>();
		cqmsMet = new ArrayList<String>();
	}

	public List<String> getCriteriaMet() {
		return criteriaMet;
	}
	public void setCriteriaMet(List<String> criteriaMet) {
		this.criteriaMet = criteriaMet;
	}
	public List<String> getCqmsMet() {
		return cqmsMet;
	}
	public void setCqmsMet(List<String> cqmsMet) {
		this.cqmsMet = cqmsMet;
	}

	public List<String> getPreviousDevelopers() {
		return previousDevelopers;
	}

	public void setPreviousDevelopers(List<String> previousDevelopers) {
		this.previousDevelopers = previousDevelopers;
	}
}
