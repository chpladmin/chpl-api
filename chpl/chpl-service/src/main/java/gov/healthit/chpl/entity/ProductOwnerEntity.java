package gov.healthit.chpl.entity;

import java.util.Date;

import gov.healthit.chpl.entity.developer.DeveloperEntity;

public interface ProductOwnerEntity {

    Long getId();

    void setId(Long id);

    Long getDeveloperId();

    void setDeveloperId(Long developerId);

    Long getProductId();

    void setProductId(Long productId);

    Date getTransferDate();

    void setTransferDate(Date transferDate);

    Date getCreationDate();

    void setCreationDate(Date creationDate);

    Boolean getDeleted();

    void setDeleted(Boolean deleted);

    Date getLastModifiedDate();

    void setLastModifiedDate(Date lastModifiedDate);

    Long getLastModifiedUser();

    void setLastModifiedUser(Long lastModifiedUser);

    DeveloperEntity getDeveloper();

    void setDeveloper(DeveloperEntity developer);
}
