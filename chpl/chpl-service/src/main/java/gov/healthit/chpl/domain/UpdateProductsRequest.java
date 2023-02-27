package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.List;

public class UpdateProductsRequest implements Serializable {
    private static final long serialVersionUID = -5814847900559692235L;
    private List<Long> productIds;
    private Product product;

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(final List<Long> productIds) {
        this.productIds = productIds;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(final Product product) {
        this.product = product;
    }
}
