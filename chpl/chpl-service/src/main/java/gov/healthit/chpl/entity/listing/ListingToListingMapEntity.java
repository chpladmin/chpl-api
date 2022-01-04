package gov.healthit.chpl.entity.listing;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "listing_to_listing_map")
public class ListingToListingMapEntity {
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

    @Column(name = "deleted", insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;
}
