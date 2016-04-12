package gov.healthit.chpl.web.controller.results;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

public class CertificationIdResults {
	
	static public class Product {
		private String name;
		private Long productId;
	
		public Product(CertifiedProductDetailsDTO dto) {
			this.name = dto.getProductName();
			this.productId = dto.getId();
		}
		
		public String getName() {
			return this.name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public Long getProductId() {
			return this.productId;
		}
		
		public void setProductId(Long id) {
			this.productId = id;
		}
	}
	
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
