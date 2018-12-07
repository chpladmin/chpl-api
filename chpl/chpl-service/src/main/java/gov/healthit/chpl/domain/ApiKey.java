package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

public class ApiKey implements Serializable {
    private static final long serialVersionUID = -3412202704187626073L;
    private String name;
    private String email;
    private String key;
    private Date lastUsedDate;
    private Date deleteWarningSentDate;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public Date getLastUsedDate() {
        return lastUsedDate;
    }

    public void setLastUsedDate(final Date lastUsedDate) {
        this.lastUsedDate = lastUsedDate;
    }

    public Date getDeleteWarningSentDate() {
        return deleteWarningSentDate;
    }

    public void setDeleteWarningSentDate(Date deleteWarningSentDate) {
        this.deleteWarningSentDate = deleteWarningSentDate;
    }
}
