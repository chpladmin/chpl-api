package gov.healthit.chpl.web.controller.results;

import gov.healthit.chpl.domain.Product;

public class SplitProductResponse {
    private Product oldProduct;
    private Product newProduct;

    public Product getOldProduct() {
        return oldProduct;
    }

    public void setOldProduct(final Product oldProduct) {
        this.oldProduct = oldProduct;
    }

    public Product getNewProduct() {
        return newProduct;
    }

    public void setNewProduct(final Product newProduct) {
        this.newProduct = newProduct;
    }
}
