package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.util.Util;

public class ApiKeyActivity implements Serializable {
    private static final long serialVersionUID = 7717599216397121980L;
    private Long id;
    private Long apiKeyId;
    private String apiKey;
    private String email;
    private String name;
    private String apiCallPath;
    private Date creationDate;

    public ApiKeyActivity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getApiKeyId() {
        return apiKeyId;
    }

    public void setApiKeyId(final Long apiKeyId) {
        this.apiKeyId = apiKeyId;
    }

    public String getApiCallPath() {
        return apiCallPath;
    }

    public void setApiCallPath(final String apiCallPath) {
        this.apiCallPath = apiCallPath;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
