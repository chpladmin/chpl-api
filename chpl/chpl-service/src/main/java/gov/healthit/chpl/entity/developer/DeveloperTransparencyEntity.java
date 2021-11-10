package gov.healthit.chpl.entity.developer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "developers_with_attestations")
public class DeveloperTransparencyEntity {
    private static final long serialVersionUID = -2928065796554477869L;

    @Id
    @Column(name = "vendor_id", nullable = false)
    private Long id;

    @Column(name = "vendor_name")
    private String name;

    @Column(name = "status_name")
    private String status;

    @Column(name = "countActiveListings")
    private Long countActiveListings;

    @Column(name = "countRetiredListings")
    private Long countRetiredListings;

    @Column(name = "countPendingListings")
    private Long countPendingListings;

    @Column(name = "countWithdrawnByDeveloperListings")
    private Long countWithdrawnByDeveloperListings;

    @Column(name = "countWithdrawnByOncAcbListings")
    private Long countWithdrawnByOncAcbListings;

    @Column(name = "countSuspendedByOncAcbListings")
    private Long countSuspendedByOncAcbListings;

    @Column(name = "countSuspendedByOncListings")
    private Long countSuspendedByOncListings;

    @Column(name = "countTerminatedByOncListings")
    private Long countTerminatedByOncListings;

    @Column(name = "countWithdrawnByDeveloperUnderSurveillanceListings")
    private Long countWithdrawnByDeveloperUnderSurveillanceListings;

    @Column(name = "mandatory_disclosures")
    private String mandatoryDisclosures;

    @Column(name = "attestations")
    private String acbAttestations;
}
