package gov.healthit.chpl.dto;

import java.time.LocalDate;

import gov.healthit.chpl.entity.ProductOwnerEntity;

public class ProductOwnerDTO {
	
	private Long id;
	private Long productId;
	private Long developerId;
	private DeveloperDTO developer;
	private LocalDate transferDate;
	
	public ProductOwnerDTO(){}
	public ProductOwnerDTO(ProductOwnerEntity entity){
		
		this.id = entity.getId();
		this.productId = entity.getProductId();
		this.developerId = entity.getDeveloperId();
		if(entity.getDeveloper() != null) {
			this.developer = new DeveloperDTO(entity.getDeveloper());
		}
		if(entity.getTransferDate() != null) {
			this.transferDate = entity.getTransferDate().toLocalDate();
		}
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public Long getDeveloperId() {
		return developerId;
	}
	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}
	public DeveloperDTO getDeveloper() {
		return developer;
	}
	public void setDeveloper(DeveloperDTO developer) {
		this.developer = developer;
	}
	public LocalDate getTransferDate() {
		return transferDate;
	}
	public void setTransferDate(LocalDate transferDate) {
		this.transferDate = transferDate;
	}
}
