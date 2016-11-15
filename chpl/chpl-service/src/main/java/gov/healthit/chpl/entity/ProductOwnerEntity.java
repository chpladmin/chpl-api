package gov.healthit.chpl.entity;

import java.util.Date;

public interface ProductOwnerEntity {

	public Long getId();
	public void setId(Long id);
	public Long getDeveloperId();
	public void setDeveloperId(Long developerId);
	public Long getProductId(); 
	public void setProductId(Long productId);
	public Date getTransferDate();
	public void setTransferDate(Date transferDate);
	public Date getCreationDate();
	public void setCreationDate(Date creationDate);
	public Boolean getDeleted();
	public void setDeleted(Boolean deleted);
	public Date getLastModifiedDate();
	public void setLastModifiedDate(Date lastModifiedDate);
	public Long getLastModifiedUser();
	public void setLastModifiedUser(Long lastModifiedUser);
	public DeveloperEntity getDeveloper();
	public void setDeveloper(DeveloperEntity developer);
}
