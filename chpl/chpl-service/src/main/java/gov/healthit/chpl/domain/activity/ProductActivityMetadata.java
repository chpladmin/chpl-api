package gov.healthit.chpl.domain.activity;

/**
 * Product specific activity summary fields.
 * @author kekey
 *
 */
public class ProductActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 9069117187924463180L;

    private String developerName;
    private String productName;

    public ProductActivityMetadata() {
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(final String developerName) {
        this.developerName = developerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(final String productName) {
        this.productName = productName;
    }
}
