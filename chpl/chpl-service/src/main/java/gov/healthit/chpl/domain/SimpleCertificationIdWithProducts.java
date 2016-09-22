package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertificationIdDTO;

public class SimpleCertificationIdWithProducts extends SimpleCertificationId {
	private String products;
	
	public SimpleCertificationIdWithProducts() {
		super();
	}
	
	public SimpleCertificationIdWithProducts(CertificationIdDTO dto) {
		super(dto);
	}

	public String getProducts() {
		return products;
	}

	public void setProducts(String products) {
		this.products = products;
	}
}
