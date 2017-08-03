package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.CertificationIdDTO;

public class SimpleCertificationIdWithProducts extends SimpleCertificationId implements Serializable {
	private static final long serialVersionUID = -2818214498196264669L;
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
