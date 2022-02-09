package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.ProductVersionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    public ProductVersionDTO(ProductVersionEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.creationDate = entity.getCreationDate();
            this.deleted = entity.getDeleted();
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
}
