package gov.healthit.chpl.domain.activity;

/**
 * Product specific activity summary fields.
 * @author kekey
 *
 */
public class ProductActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 9069117187924463180L;

    private String developerName;
    private String proudctName;

    public ProductActivityMetadata() {
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(final String developerName) {
        this.developerName = developerName;
    }

    public String getProudctName() {
        return proudctName;
    }

    public void setProudctName(String proudctName) {
        this.proudctName = proudctName;
    }
}
