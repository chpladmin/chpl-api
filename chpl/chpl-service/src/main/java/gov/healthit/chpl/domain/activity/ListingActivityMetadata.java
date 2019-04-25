package gov.healthit.chpl.domain.activity;

/**
 * Listing specific activity summary fields.
 * @author kekey
 *
 */
public class ListingActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 5473773376581297578L;

    private String chplProductNumber;
    private String acbName;
    private String developerName;
    private String productName;
    private String edition;
    private Long certificationDate;

    public ListingActivityMetadata() {
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public String getAcbName() {
        return acbName;
    }

    public void setAcbName(final String acbName) {
        this.acbName = acbName;
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

    public String getEdition() {
        return edition;
    }

    public void setEdition(final String edition) {
        this.edition = edition;
    }

    public Long getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(final Long certificationDate) {
        this.certificationDate = certificationDate;
    }

}
