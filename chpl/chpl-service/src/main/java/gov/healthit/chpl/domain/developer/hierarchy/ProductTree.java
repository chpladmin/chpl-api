package gov.healthit.chpl.domain.developer.hierarchy;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import gov.healthit.chpl.domain.Product;
import lombok.Data;

@Data
public class ProductTree extends Product {
    private static final long serialVersionUID = 1317931840652660303L;

    private Set<VersionTree> versions = new LinkedHashSet<VersionTree>();

    public ProductTree() {
        super();
    }

    public ProductTree(Product product) {
        super();
        this.setId(product.getId());
        this.setProductId(product.getProductId());
        this.setContact(product.getContact());
        this.setLastModifiedDate(product.getLastModifiedDate());
        this.setName(product.getName());
        this.setOwner(product.getOwner());
        this.setOwnerHistory(product.getOwnerHistory());
        this.setReportFileLocation(product.getReportFileLocation());
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ProductTree)) {
            return false;
        }
        ProductTree otherProduct = (ProductTree) obj;
        return ObjectUtils.equals(this.getProductId(), otherProduct.getProductId());
    }

    public int hashCode() {
        if (this.getProductId() == null) {
            return -1;
        }

        return this.getProductId().hashCode();
    }
}
