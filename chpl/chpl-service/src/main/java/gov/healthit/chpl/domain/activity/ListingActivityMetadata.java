package gov.healthit.chpl.domain.activity;

public class ListingActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 5473773376581297578L;

    private String chplProductNumber;
    private String abcName;
    private String developerName;
    private String productName;
    private String edition;
    private Long certificationDate;

    public ListingActivityMetadata() {
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public String getAbcName() {
        return abcName;
    }

    public void setAbcName(String abcName) {
        this.abcName = abcName;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public Long getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(Long certificationDate) {
        this.certificationDate = certificationDate;
    }

}
