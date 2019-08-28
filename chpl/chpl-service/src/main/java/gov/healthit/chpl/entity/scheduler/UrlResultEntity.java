package gov.healthit.chpl.entity.scheduler;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Entity containing a url, its type, and http response code the last time it was called.
 * @author kekey
 *
 */
@Entity
@Table(name = "url_check_result")
public class UrlResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "url")
    private String url;

    @Basic(optional = false)
    @Column(name = "url_type_id")
    private Long urlTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "url_type_id", insertable = false, updatable = false)
    private UrlTypeEntity urlType;

    @Basic(optional = false)
    @Column(name = "http_status")
    private Integer httpStatus;;

    @Basic(optional = false)
    @Column(name = "response_time")
    private Long responseTimeMillis;

    @Basic(optional = false)
    @Column(name = "checked_date")
    private Date urlCheckedDate;

    @Column(name = "deleted", insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

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

    public Long getUrlTypeId() {
        return urlTypeId;
    }

    public void setUrlTypeId(final Long urlTypeId) {
        this.urlTypeId = urlTypeId;
    }

    public UrlTypeEntity getUrlType() {
        return urlType;
    }

    public void setUrlType(final UrlTypeEntity urlType) {
        this.urlType = urlType;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(final Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public Long getResponseTimeMillis() {
        return responseTimeMillis;
    }

    public void setResponseTimeMillis(final Long responseTimeMillis) {
        this.responseTimeMillis = responseTimeMillis;
    }

    public Date getUrlCheckedDate() {
        return urlCheckedDate;
    }

    public void setUrlCheckedDate(final Date urlCheckedDate) {
        this.urlCheckedDate = urlCheckedDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }
}
