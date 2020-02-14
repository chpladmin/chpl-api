package gov.healthit.chpl.scheduler.job.urlStatus;

import java.util.Date;

import org.springframework.beans.BeanUtils;

/**
 * Class to hold the url result along with extra information
 * about the object (atl, acb, developer, or listing) that had
 * a URL in error.
 * @author kekey
 *
 */
public class FailedUrlResult {
    private String url;
    private UrlType urlType;
    private Date lastChecked;
    private Integer responseCode;
    private String responseMessage;

    //not all of the below fields are relevant to every failure
    //it depends on the type of url (developer website, listing report, etc)
    private String atlName;
    private String acbName;
    private String developerName;
    private String productName;
    private String version;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String chplProductNumber;
    private String edition;
    private String certificationStatus;
    private Date certificationDate;
    private String criteria;

    public FailedUrlResult() {
    }

    public FailedUrlResult(final UrlResult urlResult) {
        BeanUtils.copyProperties(urlResult, this);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public UrlType getUrlType() {
        return urlType;
    }

    public void setUrlType(UrlType urlType) {
        this.urlType = urlType;
    }

    public Date getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(Date lastChecked) {
        this.lastChecked = lastChecked;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getAtlName() {
        return atlName;
    }

    public void setAtlName(String atlName) {
        this.atlName = atlName;
    }

    public String getAcbName() {
        return acbName;
    }

    public void setAcbName(String acbName) {
        this.acbName = acbName;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(String certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public Date getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(Date certificationDate) {
        this.certificationDate = certificationDate;
    }
}
