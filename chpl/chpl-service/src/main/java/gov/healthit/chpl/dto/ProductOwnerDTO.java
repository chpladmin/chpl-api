package gov.healthit.chpl.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.entity.ProductOwnerEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProductOwnerDTO implements Serializable {
    private static final long serialVersionUID = 6840423832565720086L;
    private Long id;
    private Long productId;
    private Developer developer;
    private Long transferDate;

    public ProductOwnerDTO(ProductOwnerEntity entity) {

        this.id = entity.getId();
        this.productId = entity.getProductId();
        if (entity.getDeveloper() != null) {
            this.developer = entity.getDeveloper().toDomain();
        } else {
            this.developer = new Developer();
            this.developer.setId(entity.getDeveloperId());
        }
        if (entity.getTransferDate() != null) {
            this.transferDate = entity.getTransferDate().getTime();
        }
    }
}
