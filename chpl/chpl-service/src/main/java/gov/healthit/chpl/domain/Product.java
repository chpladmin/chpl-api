package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;

public class Product implements Serializable {
	private static final long serialVersionUID = 2177195816284265811L;
	private Long productId;
	private String name;
	private String reportFileLocation;
	private Contact contact;
	private String lastModifiedDate;
	private Developer owner;
	private List<ProductOwner> ownerHistory;
	
	public Product() {
		ownerHistory = new ArrayList<ProductOwner>();
	}
	
	public Product(ProductDTO dto) {
		this();
		this.productId = dto.getId();
		this.name = dto.getName();
		this.reportFileLocation = dto.getReportFileLocation();
		if(dto.getLastModifiedDate() != null) {
			this.lastModifiedDate = dto.getLastModifiedDate().getTime()+"";
		}
		if(dto.getContact() != null) {
			this.contact = new Contact(dto.getContact());
		}
		if(dto.getDeveloperId() != null) {
			this.owner = new Developer();
			this.owner.setDeveloperId(dto.getDeveloperId());
			this.owner.setName(dto.getDeveloperName());
			this.owner.setDeveloperCode(dto.getDeveloperCode());
		}
		if(dto.getOwnerHistory() != null && dto.getOwnerHistory().size() > 0) {
			for(ProductOwnerDTO prevOwnerDto : dto.getOwnerHistory()) {
				ProductOwner prevOwner = new ProductOwner(prevOwnerDto);
				this.ownerHistory.add(prevOwner);
			}
		}
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

	public Developer getOwner() {
		return owner;
	}

	public void setOwner(Developer owner) {
		this.owner = owner;
	}

	public List<ProductOwner> getOwnerHistory() {
		return ownerHistory;
	}

	public void setOwnerHistory(List<ProductOwner> ownerHistory) {
		this.ownerHistory = ownerHistory;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}
	

}
