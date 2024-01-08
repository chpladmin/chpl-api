package gov.healthit.chpl.entity.listing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
