package gov.healthit.chpl.domain.activity;

/**
 * Product specific activity summary fields.
 * @author kekey
 *
 */
public class VersionActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 9069117087924463180L;

    private String productName;
    private String version;

    public VersionActivityMetadata() {
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(final String productName) {
        this.productName = productName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }
}
