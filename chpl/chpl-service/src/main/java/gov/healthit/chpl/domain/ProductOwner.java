package gov.healthit.chpl.domain;

import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.XmlTransient;

import gov.healthit.chpl.dto.ProductOwnerDTO;

public class ProductOwner {
	private Long id;
	private Developer developer;
	private String transferDate;
	
	@XmlTransient
	private static final DateTimeFormatter xferDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public ProductOwner() {}
	
	public ProductOwner(ProductOwnerDTO dto) {
		this.id = dto.getId();
		if(dto.getDeveloper() != null) {
			this.developer = new Developer();
			this.developer.setDeveloperId(dto.getDeveloper().getId());
			this.developer.setName(dto.getDeveloper().getName());
		}
		this.transferDate = xferDateFormatter.format(dto.getTransferDate());
	}

	public Developer getDeveloper() {
		return developer;
	}

	public void setDeveloper(Developer developer) {
		this.developer = developer;
	}

	public String getTransferDate() {
		return transferDate;
	}

	public void setTransferDate(String transferDate) {
		this.transferDate = transferDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
