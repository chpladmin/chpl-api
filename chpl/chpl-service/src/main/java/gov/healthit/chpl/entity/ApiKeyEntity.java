package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "api_key")
public class ApiKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "api_key_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "api_key")
    private String apiKey;

    @Basic(optional = false)
    @Column(name = "email")
    private String email;

    @Basic(optional = false)
    @Column(name = "name_organization")
    private String nameOrganization;

    @Basic(optional = false)
    @Column(name = "whitelisted", nullable = false, insertable = false)
    private Boolean whitelisted;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "last_used_date", nullable = false)
    private Date lastUsedDate;

    @Column(name = "delete_warning_sent_date", nullable = true)
    private Date deleteWarningSentDate;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public String getNameOrganization() {
        return nameOrganization;
    }

    public void setNameOrganization(final String nameOrganization) {
        this.nameOrganization = nameOrganization;
    }

    public Boolean getWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(final Boolean whitelisted) {
        this.whitelisted = whitelisted;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastUsedDate() {
        return Util.getNewDate(lastUsedDate);
    }

    public void setLastUsedDate(final Date lastUsedDate) {
        this.lastUsedDate = Util.getNewDate(lastUsedDate);
    }

    public Date getDeleteWarningSentDate() {
        return Util.getNewDate(deleteWarningSentDate);
    }

    public void setDeleteWarningSentDate(final Date deleteWarningSentDate) {
        this.deleteWarningSentDate = Util.getNewDate(deleteWarningSentDate);
    }

}
