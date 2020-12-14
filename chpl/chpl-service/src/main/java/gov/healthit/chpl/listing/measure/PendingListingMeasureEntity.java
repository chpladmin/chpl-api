package gov.healthit.chpl.listing.measure;

import java.util.Date;
import java.util.LinkedHashSet;
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

import lombok.Data;

@Entity
@Data
@Table(name = "pending_certified_product_measure")
public class PendingListingMeasureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "pending_certified_product_id")
    private Long pendingCertifiedProductId;

    @Column(name = "measure_id")
    private Long measureId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "measure_id", unique = true, nullable = true, insertable = false, updatable = false)
    private MeasureEntity measure;

    @Basic(optional = false)
    @Column(name = "uploaded_value")
    private String uploadedValue;

    @Column(name = "measure_type_id")
    private Long measureTypeId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "measure_type_id", unique = true, nullable = true, insertable = false, updatable = false)
    private ListingMeasureTypeEntity type;

    @Basic(optional = false)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingListingMeasureId")
    @Column(name = "pending_certified_product_measure_id", nullable = false)
    private Set<PendingListingMeasureCriterionMapEntity> associatedCriteria
        = new LinkedHashSet<PendingListingMeasureCriterionMapEntity>();

    @Column(name = "last_modified_date", updatable = false, insertable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    private Date creationDate;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
}
