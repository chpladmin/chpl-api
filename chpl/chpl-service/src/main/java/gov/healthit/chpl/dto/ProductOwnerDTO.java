package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.ProductOwnerEntity;

public class ProductOwnerDTO {
	
	private Long id;
	private Long productId;
	private DeveloperDTO developer;
	private Long transferDate;
	
	public ProductOwnerDTO(){}
	public ProductOwnerDTO(ProductOwnerEntity entity){
		
		this.id = entity.getId();
		this.productId = entity.getProductId();
		if(entity.getDeveloper() != null) {
			this.developer = new DeveloperDTO(entity.getDeveloper());
		} else {
			this.developer = new DeveloperDTO();
			this.developer.setId(entity.getDeveloperId());
		}
		if(entity.getTransferDate() != null) {
			this.transferDate = entity.getTransferDate().getTime();
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
	public Long getTransferDate() {
		return transferDate;
	}
	public void setTransferDate(Long transferDate) {
		this.transferDate = transferDate;
	}
	public DeveloperDTO getDeveloper() {
		return developer;
	}
	public void setDeveloper(DeveloperDTO developer) {
		this.developer = developer;
	}
}
