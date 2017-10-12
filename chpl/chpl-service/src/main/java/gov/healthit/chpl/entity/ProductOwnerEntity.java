package gov.healthit.chpl.entity;

import java.util.Date;

import gov.healthit.chpl.entity.developer.DeveloperEntity;

public interface ProductOwnerEntity {

    public Long getId();

    public void setId(final Long id);

    public Long getDeveloperId();

    public void setDeveloperId(final Long developerId);

    public Long getProductId();

    public void setProductId(final Long productId);

    public Date getTransferDate();

    public void setTransferDate(final Date transferDate);

    public Date getCreationDate();

    public void setCreationDate(final Date creationDate);

    public Boolean getDeleted();

    public void setDeleted(final Boolean deleted);

    public Date getLastModifiedDate();

    public void setLastModifiedDate(final Date lastModifiedDate);

    public Long getLastModifiedUser();

    public void setLastModifiedUser(final Long lastModifiedUser);

    public DeveloperEntity getDeveloper();

    public void setDeveloper(final DeveloperEntity developer);
}
