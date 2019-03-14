package gov.healthit.chpl.domain.activity;

/**
 * Developer specific activity summary fields.
 * @author kekey
 *
 */
public class DeveloperActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 9069117187928313180L;

    private String developerName;
    private String developerCode;

    public DeveloperActivityMetadata() {
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public String getDeveloperCode() {
        return developerCode;
    }

    public void setDeveloperCode(String developerCode) {
        this.developerCode = developerCode;
    }
}
