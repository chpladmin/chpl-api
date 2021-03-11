package gov.healthit.chpl.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.ProductOwnerEntity;
import gov.healthit.chpl.util.DeveloperMapper;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ProductOwnerDTO implements Serializable {
    private static final long serialVersionUID = 6840423832565720086L;
    private Long id;
    private Long productId;
    private DeveloperDTO developer;
    private Long transferDate;
    private DeveloperMapper developerMapper;

    public ProductOwnerDTO() {
        this.developerMapper = new DeveloperMapper();
    }

    public ProductOwnerDTO(ProductOwnerEntity entity) {
        this();
        this.id = entity.getId();
        this.productId = entity.getProductId();
        if (entity.getDeveloper() != null) {
            this.developer = developerMapper.from(entity.getDeveloper());
        } else {
            this.developer = new DeveloperDTO();
            this.developer.setId(entity.getDeveloperId());
        }
        if (entity.getTransferDate() != null) {
            this.transferDate = entity.getTransferDate().getTime();
        }
    }
}
