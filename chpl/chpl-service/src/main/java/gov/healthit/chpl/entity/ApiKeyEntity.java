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
import lombok.Data;

@Entity
@Data
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
    @Column(name = "unrestricted", nullable = false, insertable = false)
    private Boolean unrestricted;

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

    public Date getLastUsedDate() {
        return Util.getNewDate(lastUsedDate);
    }

    public void setLastUsedDate(final Date lastUsedDate) {
        this.lastUsedDate = Util.getNewDate(lastUsedDate);
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

    public Date getDeleteWarningSentDate() {
        return Util.getNewDate(deleteWarningSentDate);
    }

    public void setDeleteWarningSentDate(final Date deleteWarningSentDate) {
        this.deleteWarningSentDate = Util.getNewDate(deleteWarningSentDate);
    }
}
