package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.ProductOwnerDTO;

public class ProductOwner {
	private Long id;
	private Developer developer;
	private Long transferDate;
	
	public ProductOwner() {}
	
	public ProductOwner(ProductOwnerDTO dto) {
		this.id = dto.getId();
		if(dto.getDeveloper() != null) {
			this.developer = new Developer(dto.getDeveloper());
		}
		this.transferDate = dto.getTransferDate();
	}

	public Developer getDeveloper() {
		return developer;
	}

	public void setDeveloper(Developer developer) {
		this.developer = developer;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTransferDate() {
		return transferDate;
	}

	public void setTransferDate(Long transferDate) {
		this.transferDate = transferDate;
	}
}
