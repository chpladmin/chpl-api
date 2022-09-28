package gov.healthit.chpl.entity.surveillance;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
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

import gov.healthit.chpl.entity.listing.CertifiedProductSummaryEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pending_surveillance")
@Getter
@Setter
public class PendingSurveillanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "surveillance_id_to_replace")
    private String survFriendlyIdToReplace;

    @Column(name = "certified_product_unique_id")
    private String certifiedProductUniqueId;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certified_product_id", insertable = false, updatable = false)
    private CertifiedProductSummaryEntity certifiedProduct;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "type_value")
    private String surveillanceType;

    @Column(name = "randomized_sites_used")
    private Integer numRandomizedSites;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingSurveillanceId")
    @Basic(optional = false)
    @Column(name = "pending_surveillance_id", nullable = false)
    private Set<PendingSurveillanceRequirementEntity> surveilledRequirements =
    new HashSet<PendingSurveillanceRequirementEntity>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingSurveillanceId")
    @Basic(optional = false)
    @Column(name = "pending_surveillance_id", nullable = false)
    private Set<PendingSurveillanceValidationEntity> validation = new HashSet<PendingSurveillanceValidationEntity>();

}
