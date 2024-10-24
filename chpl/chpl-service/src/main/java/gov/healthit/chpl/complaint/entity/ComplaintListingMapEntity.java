package gov.healthit.chpl.complaint.entity;

import org.hibernate.annotations.SQLRestriction;

import gov.healthit.chpl.complaint.domain.ComplaintListingMap;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntitySimple;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "complaint_listing_map")
public class ComplaintListingMapEntity extends EntityAudit {
    private static final long serialVersionUID = 1403924322469395073L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_listing_map_id", nullable = false)
    private Long id;

    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    @SQLRestriction("deleted <> true")
    private CertifiedProductDetailsEntitySimple listing;

    public ComplaintListingMap buildComplaintListingMap() {
        return ComplaintListingMap.builder()
            .chplProductNumber(this.getListing().getChplProductNumber())
            .complaintId(this.complaintId)
            .id(this.getId())
            .listingId(this.getListingId())
            .developerName(this.getListing().getDeveloperName())
            .productName(this.getListing().getProductName())
            .versionName(this.getListing().getProductVersion())
            .build();
    }
}
