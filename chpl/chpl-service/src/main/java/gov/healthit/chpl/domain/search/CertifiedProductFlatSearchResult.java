package gov.healthit.chpl.domain.search;

public class CertifiedProductFlatSearchResult extends CertifiedProductSearchResult {
	private static final long serialVersionUID = -2547390525592841044L;
	
	private String previousDevelopers; //unicode-char delimited string of developer names that owned the product
	private String criteriaMet; //unicode-char delimited string of criteria numbers
	private String cqmsMet; //unicode-char delimited string of cmqs that were met (any version)
	
	public CertifiedProductFlatSearchResult() {
	}

	public String getPreviousDevelopers() {
		return previousDevelopers;
	}

	public void setPreviousDevelopers(String previousDevelopers) {
		this.previousDevelopers = previousDevelopers;
	}

	public String getCriteriaMet() {
		return criteriaMet;
	}

	public void setCriteriaMet(String criteriaMet) {
		this.criteriaMet = criteriaMet;
	}

	public String getCqmsMet() {
		return cqmsMet;
	}

	public void setCqmsMet(String cqmsMet) {
		this.cqmsMet = cqmsMet;
	}

}
