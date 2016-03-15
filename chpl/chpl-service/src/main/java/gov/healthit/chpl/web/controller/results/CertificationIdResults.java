package gov.healthit.chpl.web.controller.results;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import gov.healthit.chpl.domain.Product;

public class CertificationIdResults {
	
	private List<Product> products;
	private String ehrCertificationId;
	private Map<String, Integer> metCounts;
	private Map<String, Integer> metPercentages;

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public String getEhrCertificationId() {
		return this.ehrCertificationId;
	}
	
	public void setEhrCertificationId(String ehrCertificationId) {
		this.ehrCertificationId = ehrCertificationId;
	}
	
	public Map<String, Integer> getMetPercentages() {
		return this.metPercentages;
	}

	public void setMetPercentages(Map<String, Integer> metPercentages) {
		this.metPercentages = metPercentages;
	}

	public Map<String, Integer> getMetCounts() {
		return this.metCounts;
	}

	public void setMetCounts(Map<String, Integer> metCounts) {
		this.metCounts = metCounts;
	}
	

}
