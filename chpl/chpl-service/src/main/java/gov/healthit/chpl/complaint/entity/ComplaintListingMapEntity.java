package gov.healthit.chpl.complaint.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.complaint.domain.ComplaintListingMap;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntitySimple;
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
    @Where(clause = "deleted <> 'true'")
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
