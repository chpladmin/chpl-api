package gov.healthit.chpl.web.controller.results;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

public class CertificationIdResults {
	
	static public class Product {
		private String name;
		private Long productId;
		private String version;
		private String acbCertificationId;
		private String year;
	
		public Product(CertifiedProductDetailsDTO dto) {
			this.name = dto.getProductName();
			this.productId = dto.getId();
			this.version = dto.getProductVersion();
			this.acbCertificationId = dto.getAcbCertificationId();
			this.year = dto.getYear();
		}

		public String getYear() {
			return this.year;
		}
		
		public void setYear(String year) {
			this.year = year;
		}
		
		public String getVersion() {
			return this.version;
		}
		
		public void setVersion(String version) {
			this.version = version;
		}
		
		public String getAcbCertificationId() {
			return this.acbCertificationId;
		}
		
		public void setAcbCertificationId(String acbCertificationId) {
			this.acbCertificationId = acbCertificationId;
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
