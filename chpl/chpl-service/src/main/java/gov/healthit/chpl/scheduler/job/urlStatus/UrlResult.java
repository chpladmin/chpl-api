package gov.healthit.chpl.scheduler.job.urlStatus;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

public class UrlResult {
    private Long id;
    private String url;
    private UrlType urlType;
    private Date lastChecked;
    private Integer responseCode;
    private String responseMessage;

    public UrlResult() {
    }

    public UrlResult(final UrlResultEntity entity) {
        BeanUtils.copyProperties(entity, this);
        if (this.urlType == null) {
            if (entity.getUrlType() != null) {
                this.urlType = UrlType.findByName(entity.getUrlType().getName());
            }
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof UrlResult)) {
            return false;
        }
        UrlResult otherDto = (UrlResult) obj;
        //equal if ids are the same
        if (otherDto.getId() != null && this.getId() != null
                && otherDto.getId().equals(this.getId())) {
            return true;
        }
        //if ids aren't both there, check the url and the type to determine equality
        if (!StringUtils.isEmpty(otherDto.getUrl()) && !StringUtils.isEmpty(this.getUrl())
                && otherDto.getUrl().equals(this.getUrl())
                && otherDto.getUrlType() != null && this.getUrlType() != null
                && otherDto.getUrlType() == this.getUrlType()) {
            return true;
        }
        return false;
    }
    public Long getId() {
        return id;
    }
    public void setId(final Long id) {
        this.id = id;
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
}
