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
    public void setUrlType(final UrlType urlType) {
        this.urlType = urlType;
    }
    public Date getLastChecked() {
        return lastChecked;
    }
    public void setLastChecked(final Date lastChecked) {
        this.lastChecked = lastChecked;
    }
    public Integer getResponseCode() {
        return responseCode;
    }
    public void setResponseCode(final Integer responseCode) {
        this.responseCode = responseCode;
    }
    public String getResponseMessage() {
        return responseMessage;
    }
    public void setResponseMessage(final String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getAtlName() {
        return atlName;
    }

    public void setAtlName(final String atlName) {
        this.atlName = atlName;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(final String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(final String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(final String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(final String edition) {
        this.edition = edition;
    }

    public String getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(final String certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(final String criteria) {
        this.criteria = criteria;
    }
}
