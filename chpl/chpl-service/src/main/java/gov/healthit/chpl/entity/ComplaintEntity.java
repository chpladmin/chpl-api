package gov.healthit.chpl.entity;

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

@Entity
@Table(name = "complaint")
public class ComplaintEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "complaint_id", nullable = false)
    private Long id;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id", unique = true, nullable = true)
    private CertificationBodyEntity certificationBody;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_type_id", unique = true, nullable = true)
    private ComplaintTypeEntity complaintType;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_status_type_id", unique = true, nullable = true)
    private ComplaintStatusTypeEntity complaintStatusType;

    @Column(name = "onc_complaint_id", nullable = true)
    private String oncComplaintId;

    @Column(name = "received_date", nullable = true)
    private Date receivedDate;

    @Column(name = "summary", nullable = true)
    private String summary;

    @Column(name = "actions", nullable = true)
    private String actions;

    @Column(name = "complainant_contacted", nullable = true)
    private boolean complainantContacted;

    @Column(name = "developer_contacted", nullable = true)
    private boolean developerContacted;

    @Column(name = "onc_atl_contacted", nullable = true)
    private boolean oncAtlContacted;

    @Column(name = "closed_date", nullable = true)
    private Date closedDate;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public CertificationBodyEntity getCertificationBody() {
        return certificationBody;
    }

    public void setCertificationBody(final CertificationBodyEntity certificationBody) {
        this.certificationBody = certificationBody;
    }

    public ComplaintTypeEntity getComplaintType() {
        return complaintType;
    }

    public void setComplaintType(final ComplaintTypeEntity complaintType) {
        this.complaintType = complaintType;
    }

    public ComplaintStatusTypeEntity getComplaintStatusType() {
        return complaintStatusType;
    }

    public void setComplaintStatusType(final ComplaintStatusTypeEntity complaintStatusType) {
        this.complaintStatusType = complaintStatusType;
    }

    public String getOncComplaintId() {
        return oncComplaintId;
    }

    public void setOncComplaintId(final String oncComplaintId) {
        this.oncComplaintId = oncComplaintId;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(final Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(final String actions) {
        this.actions = actions;
    }

    public boolean isComplainantContacted() {
        return complainantContacted;
    }

    public void setComplainantContacted(final boolean complainantContacted) {
        this.complainantContacted = complainantContacted;
    }

    public boolean isDeveloperContacted() {
        return developerContacted;
    }

    public void setDeveloperContacted(final boolean developerContacted) {
        this.developerContacted = developerContacted;
    }

    public boolean isOncAtlContacted() {
        return oncAtlContacted;
    }

    public void setOncAtlContacted(final boolean oncAtlContacted) {
        this.oncAtlContacted = oncAtlContacted;
    }

    public Date getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(final Date closedDate) {
        this.closedDate = closedDate;
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
}
