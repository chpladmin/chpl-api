package gov.healthit.chpl.entity;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ComplaintSurveillanceMap;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.domain.complaint.ComplaintCriterionMap;
import gov.healthit.chpl.domain.complaint.ComplaintListingMap;
import lombok.Data;

@Entity
@Data
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

    public Complaint buildComplaint() {
        return Complaint.builder()
                .acbComplaintId(this.getAcbComplaintId())
                .actions(this.getActions())
                .certificationBody(this.getCertificationBody() == null
                    ? CertificationBody.builder()
                            .id(this.getCertificationBodyId())
                            .build() : this.getCertificationBody().buildCertificationBody())
                .closedDate(this.getClosedDate())
                .complainantContacted(this.isComplainantContacted())
                .complainantType(this.getComplainantType() == null ? null
                        : this.getComplainantType().buildComplainantType())
                .complainantTypeOther(this.getComplainantTypeOther())
                .criteria(createCriteriaCollection())
                .developerContacted(this.isDeveloperContacted())
                .flagForOncReview(this.isFlagForOncReview())
                .id(this.getId())
                .listings(createListingCollection())
                .oncAtlContacted(this.isOncAtlContacted())
                .oncComplaintId(this.getOncComplaintId())
                .receivedDate(this.getReceivedDate())
                .summary(this.getSummary())
                .surveillances(createSurveillanceCollection())
                .build();
    }


    private Set<ComplaintCriterionMap> createCriteriaCollection() {
        if (this.getCriteria() == null || this.getCriteria().size() == 0) {
            return new LinkedHashSet<ComplaintCriterionMap>();
        }
        return this.getCriteria().stream()
            .map(entity -> entity.buildComplaintCriterionMap())
            .collect(Collectors.toSet());
    }

    private Set<ComplaintListingMap> createListingCollection() {
        if (this.getListings() == null || this.getListings().size() == 0) {
            return new LinkedHashSet<ComplaintListingMap>();
        }
        return this.getListings().stream()
            .map(entity -> entity.buildComplaintListingMap())
            .collect(Collectors.toSet());
    }

    private Set<ComplaintSurveillanceMap> createSurveillanceCollection() {
        if (this.getSurveillances() == null || this.getSurveillances().size() == 0) {
            return new LinkedHashSet<ComplaintSurveillanceMap>();
        }
        return this.getSurveillances().stream()
            .map(entity -> entity.buildComplaintSurveillanceMap())
            .collect(Collectors.toSet());
    }
}
