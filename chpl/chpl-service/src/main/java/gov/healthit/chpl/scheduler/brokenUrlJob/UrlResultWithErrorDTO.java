package gov.healthit.chpl.scheduler.brokenUrlJob;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;

/**
 * Class to hold the url result along with extra information
 * about the object (atl, acb, developer, or listing) that had
 * a URL in error.
 * @author kekey
 *
 */
public class UrlResultWithErrorDTO {
    private String url;
    private UrlType urlType;
    private Date lastChecked;
    private Integer responseCode;
    private String responseMessage;
    private String atlName;
    private String acbName;
    private DeveloperDTO developer;
    private CertifiedProductDetailsDTO listing;

    public UrlResultWithErrorDTO() {
    }

    public UrlResultWithErrorDTO(final UrlResultDTO urlResult) {
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

    public DeveloperDTO getDeveloper() {
        return developer;
    }

    public void setDeveloper(final DeveloperDTO developer) {
        this.developer = developer;
    }

    public CertifiedProductDetailsDTO getListing() {
        return listing;
    }

    public void setListing(final CertifiedProductDetailsDTO listing) {
        this.listing = listing;
    }
}
