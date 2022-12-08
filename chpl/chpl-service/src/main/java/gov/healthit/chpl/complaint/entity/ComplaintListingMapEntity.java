package gov.healthit.chpl.complaint.entity;

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

import org.hibernate.annotations.Where;

import gov.healthit.chpl.complaint.domain.ComplaintListingMap;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntitySimple;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "complaint_listing_map")
public class ComplaintListingMapEntity {
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

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

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
