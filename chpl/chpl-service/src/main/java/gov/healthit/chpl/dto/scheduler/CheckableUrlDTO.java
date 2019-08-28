package gov.healthit.chpl.dto.scheduler;

import java.util.Date;

public class CheckableUrlDTO {
    private String url;
    private UrlType urlType;
    private Date lastChecked;
    private int responseCode;
    private long responseTimeMillis;

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
    public int getResponseCode() {
        return responseCode;
    }
    public void setResponseCode(final int responseCode) {
        this.responseCode = responseCode;
    }
    public long getResponseTimeMillis() {
        return responseTimeMillis;
    }
    public void setResponseTimeMillis(final long responseTimeMillis) {
        this.responseTimeMillis = responseTimeMillis;
    }

}
