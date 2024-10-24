package gov.healthit.chpl.complaint.entity;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.annotations.SQLRestriction;

import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.complaint.domain.ComplaintCriterionMap;
import gov.healthit.chpl.complaint.domain.ComplaintListingMap;
import gov.healthit.chpl.complaint.domain.ComplaintType;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ComplaintSurveillanceMap;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.EntityAudit;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "complaint")
public class ComplaintEntity extends EntityAudit {
    private static final long serialVersionUID = 4097588663243344392L;

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
    private LocalDate receivedDate;

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
    private LocalDate closedDate;

    @Column(name = "complaint_type_other", nullable = true)
    private String complaintTypesOther;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "complaintId")
    @Basic(optional = true)
    @Column(name = "complaint_id", nullable = false)
    @SQLRestriction("deleted <> true")
    private Set<ComplaintToComplaintTypeMapEntity> complaintTypes;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "complaintId")
    @Basic(optional = true)
    @Column(name = "complaint_id", nullable = false)
    @SQLRestriction("deleted <> true")
    private Set<ComplaintListingMapEntity> listings;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "complaintId")
    @Basic(optional = true)
    @Column(name = "complaint_id", nullable = false)
    @SQLRestriction("deleted <> true")
    private Set<ComplaintCriterionMapEntity> criteria;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "complaintId")
    @Basic(optional = true)
    @Column(name = "complaint_id", nullable = false)
    @SQLRestriction("deleted <> true")
    private Set<ComplaintSurveillanceMapEntity> surveillances;

    public Complaint buildComplaint() {
        return Complaint.builder()
                .acbComplaintId(this.getAcbComplaintId())
                .actions(this.getActions())
                .certificationBody(this.getCertificationBody() == null
                    ? CertificationBody.builder()
                            .id(this.getCertificationBodyId())
                            .build() : this.getCertificationBody().toDomain())
                .closedDate(this.getClosedDate())
                .complainantContacted(this.isComplainantContacted())
                .complainantType(this.getComplainantType() == null ? null
                        : this.getComplainantType().buildComplainantType())
                .complainantTypeOther(this.getComplainantTypeOther())
                .complaintTypes(this.createComplaintTypeCollection())
                .complaintTypesOther(this.getComplaintTypesOther())
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

    private Set<ComplaintType> createComplaintTypeCollection() {
        if (this.getComplaintTypes() == null || this.getComplaintTypes().size() == 0) {
            return new LinkedHashSet<ComplaintType>();
        }
        return this.getComplaintTypes().stream()
            .map(entity -> entity.buildComplaintType())
            .collect(Collectors.toSet());
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
