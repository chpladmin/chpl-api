package gov.healthit.chpl.dto.questionableActivity;

import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityProductEntity;

public class QuestionableActivityProductDTO extends QuestionableActivityDTO {
    private Long productId;
    private ProductDTO product;

    public QuestionableActivityProductDTO() {
        super();
    }

    public QuestionableActivityProductDTO(QuestionableActivityProductEntity entity) {
        super(entity);
        this.productId = entity.getProductId();
        if (entity.getProduct() != null) {
            this.product = new ProductDTO(entity.getProduct());
        }
    }

    public Class<?> getActivityObjectClass() {
        return ProductDTO.class;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public ProductDTO getProduct() {
        return product;
    }

    public void setProduct(ProductDTO product) {
        this.product = product;
    }
}
