package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.ProductVersionEntity;
import gov.healthit.chpl.util.Util;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductVersionDTO implements Serializable {
    private static final long serialVersionUID = -1371133241003414009L;
    private Long id;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Long productId;
    private String productName;
    private Long developerId;
    private String developerName;
    private String version;

    public ProductVersionDTO() {
    }

    public ProductVersionDTO(final ProductVersionEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.creationDate = entity.getCreationDate();
            this.deleted = entity.isDeleted();
            this.lastModifiedDate = entity.getLastModifiedDate();
            this.lastModifiedUser = entity.getLastModifiedUser();
            this.version = entity.getVersion();
            if (entity.getProduct() != null) {
                this.productId = entity.getProduct().getId();
                this.productName = entity.getProduct().getName();
                if (entity.getProduct().getDeveloper() != null) {
                    this.developerId = entity.getProduct().getDeveloper().getId();
                    this.developerName = entity.getProduct().getDeveloper().getName();
                }
            } else if (entity.getProductId() != null) {
                this.productId = entity.getProductId();
            }
        }
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }
    
    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(final Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(final String productName) {
        this.productName = productName;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(Long developerId) {
        this.developerId = developerId;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }
}
