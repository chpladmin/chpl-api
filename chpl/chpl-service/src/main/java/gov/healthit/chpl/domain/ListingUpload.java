package gov.healthit.chpl.domain;

import java.io.Serializable;

public class ListingUpload implements Serializable {
    private static final long serialVersionUID = 7978604053959535573L;

    private Long id;
    private String chplProductNumber;
    private CertificationBody acb;
    private Integer errorCount;
    private Integer warningCount;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public CertificationBody getAcb() {
        return acb;
    }
    public void setAcb(CertificationBody acb) {
        this.acb = acb;
    }
    public String getChplProductNumber() {
        return chplProductNumber;
    }
    public void setChplProductNumber(String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }
    public Integer getErrorCount() {
        return errorCount;
    }
    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }
    public Integer getWarningCount() {
        return warningCount;
    }
    public void setWarningCount(Integer warningCount) {
        this.warningCount = warningCount;
    }

}
