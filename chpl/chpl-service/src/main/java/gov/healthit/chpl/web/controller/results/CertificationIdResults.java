package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

public class CertificationIdResults implements Serializable {
	private static final long serialVersionUID = 4350936762994127624L;

	static public class Product {
		private String name;
		private Long productId;
		private String version;

		public Product(CertifiedProductDetailsDTO dto) {
			this.name = dto.getProduct().getName();
			this.productId = dto.getId();
			this.version = dto.getVersion().getVersion();
		}

		public String getVersion() {
			return this.version;
		}
		
		public void setVersion(String version) {
			this.version = version;
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
	private String year;
	private boolean isValid;

	public String getYear() {
		return this.year;
	}
	
	public void setYear(String year) {
		this.year = year;
	}
	
	public boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(boolean valid) {
		this.isValid = valid;
	}
	
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
