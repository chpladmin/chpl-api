package gov.healthit.chpl.entity.listing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
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
@Table(name = "listing_to_listing_map")
public class ListingToListingMapEntity extends EntityAudit {
    private static final long serialVersionUID = -2928065796550375579L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_to_listing_map_id", nullable = false)
    private Long id;

    @Column(name = "parent_listing_id", nullable = false)
    private Long parentId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_listing_id", insertable = false, updatable = false)
    private CertifiedProductDetailsEntity parent;

    @Column(name = "child_listing_id", nullable = false)
    private Long childId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "child_listing_id", insertable = false, updatable = false)
    private CertifiedProductDetailsEntity child;

}
