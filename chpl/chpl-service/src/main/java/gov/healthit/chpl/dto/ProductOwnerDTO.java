package gov.healthit.chpl.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.ProductOwnerEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ProductOwnerDTO implements Serializable {
    private static final long serialVersionUID = 6840423832565720086L;
    private Long id;
    private Long productId;
    private DeveloperDTO developer;
    private Long transferDate;

    public ProductOwnerDTO(ProductOwnerEntity entity) {

        this.id = entity.getId();
        this.productId = entity.getProductId();
        if (entity.getDeveloper() != null) {
            this.developer = new DeveloperDTO(entity.getDeveloper());
        } else {
            this.developer = new DeveloperDTO();
            this.developer.setId(entity.getDeveloperId());
        }
        if (entity.getTransferDate() != null) {
            this.transferDate = entity.getTransferDate().getTime();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(final Long productId) {
        this.productId = productId;
    }

    public Long getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(final Long transferDate) {
        this.transferDate = transferDate;
    }

    public DeveloperDTO getDeveloper() {
        return developer;
    }

    public void setDeveloper(final DeveloperDTO developer) {
        this.developer = developer;
    }
}
