package gov.healthit.chpl.entity.listing;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntity;

/**
 * Listings associated with a developer that is banned.
 * @author kekey
 *
 */
@Deprecated
@Entity
@Immutable
@Table(name = "listings_from_banned_developers")
public class ListingsFromBannedDevelopersEntity {

    @Id
    @Column(name = "certified_product_id")
    private Long id;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", unique = true, nullable = true, insertable = false, updatable = false)
    private DeveloperEntity developer;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "acb_id", unique = true, nullable = true, insertable = false, updatable = false)
    private CertificationBodyEntity acb;

    @Column(name = "last_dev_status_change")
    private Date developerStatusDate;

    @Column(name = "acb_id")
    private Long acbId;

    @Column(name = "acb_name")
    private String acbName;

    @Column(name = "developer_id")
    private Long developerId;

    @Column(name = "developer_name")
    private String developerName;

    @Column(name = "developer_status_id")
    private Long developerStatusId;

    @Column(name = "developer_status_name")
    private String developerStatusName;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public DeveloperEntity getDeveloper() {
        return developer;
    }

    public void setDeveloper(final DeveloperEntity developer) {
        this.developer = developer;
    }

    public CertificationBodyEntity getAcb() {
        return acb;
    }

    public void setAcb(final CertificationBodyEntity acb) {
        this.acb = acb;
    }

    public Date getDeveloperStatusDate() {
        return developerStatusDate;
    }

    public void setDeveloperStatusDate(final Date developerStatusDate) {
        this.developerStatusDate = developerStatusDate;
    }

    public Long getAcbId() {
        return acbId;
    }

    public void setAcbId(final Long acbId) {
        this.acbId = acbId;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(final Long developerId) {
        this.developerId = developerId;
    }

    public Long getDeveloperStatusId() {
        return developerStatusId;
    }

    public void setDeveloperStatusId(final Long developerStatusId) {
        this.developerStatusId = developerStatusId;
    }

    public String getDeveloperStatusName() {
        return developerStatusName;
    }

    public void setDeveloperStatusName(final String developerStatusName) {
        this.developerStatusName = developerStatusName;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(final String developerName) {
        this.developerName = developerName;
    }

    public String getAcbName() {
        return acbName;
    }

    public void setAcbName(final String acbName) {
        this.acbName = acbName;
    }
}
