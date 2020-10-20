package gov.healthit.chpl.listing.mipsMeasure;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
@Table(name = "pending_certified_product_mips_measure")
public class PendingListingMipsMeasureEntity {

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

    @Basic(optional = false)
    @Column(name = "entered_value")
    private String value;

    @Column(name = "mips_type_id")
    private Long typeId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "mips_type_id", unique = true, nullable = true)
    private ListingMipsMeasureTypeEntity type;

    @Basic(optional = false)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingListingMipsMeasureMapId")
    @Column(name = "pending_listing_mips_measure_map_id", nullable = false)
    private List<PendingListingMipsMeasureCriterionMapEntity> associatedCriteria
        = new ArrayList<PendingListingMipsMeasureCriterionMapEntity>();

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
