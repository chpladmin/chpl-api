package gov.healthit.chpl.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

@Entity
@Table(name = "complaint")
public class ComplaintEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_id", nullable = false)
    private Long id;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id", insertable = false, updatable = false)
    private CertificationBodyEntity certificationBody;

    @Column(name = "certification_body_id", nullable = false)
    private Long certificationBodyId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "complainant_type_id", insertable = false, updatable = false)
    private ComplainantTypeEntity complainantType;

    @Column(name = "complainant_type_other", nullable = true)
    private String complainantTypeOther;

    @Column(name = "complainant_type_id", nullable = false)
    private Long complainantTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_status_type_id", insertable = false, updatable = false)
    private ComplaintStatusTypeEntity complaintStatusType;

    @Column(name = "complaint_status_type_id", nullable = false)
    private Long complaintStatusTypeId;

    @Column(name = "onc_complaint_id", nullable = true)
    private String oncComplaintId;

    @Column(name = "acb_complaint_id", nullable = false)
    private String acbComplaintId;

    @Column(name = "received_date", nullable = false)
    private Date receivedDate;

    @Column(name = "summary", nullable = false)
    private String summary;

    @Column(name = "actions", nullable = true)
    private String actions;

    @Column(name = "complainant_contacted", nullable = false)
    private boolean complainantContacted;

    @Column(name = "developer_contacted", nullable = false)
    private boolean developerContacted;

    @Column(name = "onc_atl_contacted", nullable = false)
    private boolean oncAtlContacted;

    @Column(name = "flag_for_onc_review", nullable = false)
    private boolean flagForOncReview;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "complaintId")
    @Basic(optional = true)
    @Column(name = "complaint_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<ComplaintListingMapEntity> listings;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "complaintId")
    @Basic(optional = true)
    @Column(name = "complaint_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<ComplaintCriterionMapEntity> criteria;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "complaintId")
    @Basic(optional = true)
    @Column(name = "complaint_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<ComplaintSurveillanceMapEntity> surveillances;

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

    public ComplainantTypeEntity getComplainantType() {
        return complainantType;
    }

    public void setComplainantType(final ComplainantTypeEntity complainantType) {
        this.complainantType = complainantType;
    }

    public String getComplainantTypeOther() {
        return complainantTypeOther;
    }

    public void setComplainantTypeOther(final String complainantTypeOther) {
        this.complainantTypeOther = complainantTypeOther;
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

    public String getAcbComplaintId() {
        return acbComplaintId;
    }

    public void setAcbComplaintId(final String acbComplaintId) {
        this.acbComplaintId = acbComplaintId;
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

    public boolean isFlagForOncReview() {
        return flagForOncReview;
    }

    public void setFlagForOncReview(final boolean flagForOncReview) {
        this.flagForOncReview = flagForOncReview;
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

    public Long getCertificationBodyId() {
        return certificationBodyId;
    }

    public void setCertificationBodyId(Long certificationBodyId) {
        this.certificationBodyId = certificationBodyId;
    }

    public Long getComplainantTypeId() {
        return complainantTypeId;
    }

    public void setComplainantTypeId(Long complainantTypeId) {
        this.complainantTypeId = complainantTypeId;
    }

    public Long getComplaintStatusTypeId() {
        return complaintStatusTypeId;
    }

    public void setComplaintStatusTypeId(Long complaintStatusTypeId) {
        this.complaintStatusTypeId = complaintStatusTypeId;
    }

    public Set<ComplaintListingMapEntity> getListings() {
        return listings;
    }

    public void setListings(final Set<ComplaintListingMapEntity> listings) {
        this.listings = listings;
    }

    public Set<ComplaintCriterionMapEntity> getCriteria() {
        return criteria;
    }

    public void setCriteria(final Set<ComplaintCriterionMapEntity> criteria) {
        this.criteria = criteria;
    }

    public Set<ComplaintSurveillanceMapEntity> getSurveillances() {
        return surveillances;
    }

    public void setSurveillances(Set<ComplaintSurveillanceMapEntity> surveillances) {
        this.surveillances = surveillances;
    }

    @Override
    public String toString() {
        return "ComplaintEntity [id=" + id + ", certificationBody=" + certificationBody + ", certificationBodyId="
                + certificationBodyId + ", complainantType=" + complainantType + ", complainantTypeOther="
                + complainantTypeOther + ", complainantTypeId=" + complainantTypeId + ", complaintStatusType="
                + complaintStatusType + ", complaintStatusTypeId=" + complaintStatusTypeId + ", oncComplaintId="
                + oncComplaintId + ", acbComplaintId=" + acbComplaintId + ", receivedDate=" + receivedDate
                + ", summary=" + summary + ", actions=" + actions + ", complainantContacted=" + complainantContacted
                + ", developerContacted=" + developerContacted + ", oncAtlContacted=" + oncAtlContacted
                + ", flagForOncReview=" + flagForOncReview + ", closedDate=" + closedDate + ", creationDate="
                + creationDate + ", lastModifiedDate=" + lastModifiedDate + ", lastModifiedUser=" + lastModifiedUser
                + ", deleted=" + deleted + ", listings=" + listings + ", criteria=" + criteria + ", surveillances="
                + surveillances + "]";
    }
}
