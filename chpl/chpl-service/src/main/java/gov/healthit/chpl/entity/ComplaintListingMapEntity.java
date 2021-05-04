package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import gov.healthit.chpl.domain.complaint.ComplaintListingMap;
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

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Transient
    private String chplProductNumber;

    public ComplaintListingMap buildComplaintListingMap() {
        return ComplaintListingMap.builder()
            .chplProductNumber(this.getChplProductNumber())
            .complaintId(this.complaintId)
            .id(this.getId())
            .listingId(this.getListingId())
            .build();
    }
}
