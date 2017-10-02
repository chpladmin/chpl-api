package gov.healthit.chpl.domain.search;

import com.fasterxml.jackson.annotation.JsonView;

public class CertifiedProductFlatSearchResult extends CertifiedProductSearchResult {
	private static final long serialVersionUID = -2547390525592841044L;

	@JsonView({SearchViews.Default.class})
	private String previousDevelopers; //unicode-char delimited string of developer names that owned the product

	@JsonView({SearchViews.Default.class})
	private String criteriaMet; //unicode-char delimited string of criteria numbers

	@JsonView({SearchViews.Default.class})
	private String cqmsMet; //unicode-char delimited string of cmqs that were met (any version)

	private String apiDocumentation;

	public CertifiedProductFlatSearchResult() {

	}
	public CertifiedProductFlatSearchResult(CertifiedProductFlatSearchResult other) {
		super(other);
		this.previousDevelopers = other.getPreviousDevelopers();
		this.criteriaMet = other.getCriteriaMet();
		this.cqmsMet = other.getCqmsMet();
		this.apiDocumentation = other.getApiDocumentation();
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
	public String getApiDocumentation() {
		return apiDocumentation;
	}
	public void setApiDocumentation(String apiDocumentation) {
		this.apiDocumentation = apiDocumentation;
	}
}
