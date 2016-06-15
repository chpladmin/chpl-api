package gov.healthit.chpl.web.controller.results;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashMap;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

public class CertificationIdResults {
	
	static public class Product {
		private String name;
		private Long productId;
		private String version;
		private String chplProductNumber;
		private String year;
		private String practiceType;
		private String acb;
		private String vendor;
		private String classification;
		private String additionalSoftware;

		public Product(CertifiedProductDetailsDTO dto) {
			this.name = dto.getProductName();
			this.productId = dto.getId();
			this.version = dto.getProductVersion();
			if(!StringUtils.isEmpty(dto.getChplProductNumber())) {
				this.setChplProductNumber(dto.getChplProductNumber());
			} else {
				this.setChplProductNumber(dto.getYearCode() + "." + dto.getTestingLabCode() + "." + dto.getCertificationBodyCode() + "." + 
						dto.getDeveloperCode() + "." + dto.getProductCode() + "." + dto.getVersionCode() + 
						"." + dto.getIcsCode() + "." + dto.getAdditionalSoftwareCode() + 
						"." + dto.getCertifiedDateCode());
			}
			this.year = dto.getYear();
			this.practiceType = dto.getPracticeTypeName();
			this.acb = dto.getCertificationBodyName();
			this.vendor = dto.getDeveloperName();
			this.classification = dto.getProductClassificationName();
			this.additionalSoftware = "";
			try {
				if (null != dto.getProductAdditionalSoftware()) {
					this.additionalSoftware = URLEncoder.encode(dto.getProductAdditionalSoftware(), "UTF-8");
				}
			} catch (UnsupportedEncodingException ex) {
				// Do nothing
			}
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
		
		public String getChplProductNumber() {
			return this.chplProductNumber;
		}
		
		public void setChplProductNumber(String chplProductNumber) {
			this.chplProductNumber = chplProductNumber;
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
		
		public String getPracticeType() {
			return this.practiceType;
		}
		
		public void setPracticeType(String practiceType) {
			this.practiceType = practiceType;
		}
		
		public String getAcb() {
			return this.acb;
		}
		
		public void setAcb(String acb) {
			this.acb = acb;
		}
		
		public String getVendor() {
			return this.vendor;
		}

		public void setVendor(String vendor) {
			this.vendor = vendor;
		}
		
		public String getClassification() {
			return this.classification;
		}
		
		public void setClassification(String classification) {
			this.classification = classification;
		}
		
		public String getAdditionalSoftware() {
			return this.additionalSoftware;
		}
		
		public void setAdditionalSoftware(String software) {
			this.additionalSoftware = software;
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
