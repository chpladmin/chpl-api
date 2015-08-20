package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.ProductDTO;

public class Product {
	private Long productId;
	private String name;
	private String reportFileLocation;
	private String lastModifiedDate;
	
	public Product() {}
	
	public Product(ProductDTO dto) {
		this.productId = dto.getId();
		this.name = dto.getName();
		this.reportFileLocation = dto.getReportFileLocation();
		this.lastModifiedDate = dto.getLastModifiedDate().getTime()+"";
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReportFileLocation() {
		return reportFileLocation;
	}

	public void setReportFileLocation(String reportFileLocation) {
		this.reportFileLocation = reportFileLocation;
	}

	public String getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	

}
