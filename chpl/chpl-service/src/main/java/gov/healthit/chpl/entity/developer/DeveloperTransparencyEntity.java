package gov.healthit.chpl.entity.developer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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

    @Column(name = "transparency_attestation_urls")
    private String transparencyAttestationUrls;

    @Column(name = "attestations")
    private String acbAttestations;

    public DeveloperTransparencyEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public Long getCountActiveListings() {
        return countActiveListings;
    }

    public void setCountActiveListings(final Long countActiveListings) {
        this.countActiveListings = countActiveListings;
    }

    public Long getCountRetiredListings() {
        return countRetiredListings;
    }

    public void setCountRetiredListings(final Long countRetiredListings) {
        this.countRetiredListings = countRetiredListings;
    }

    public Long getCountPendingListings() {
        return countPendingListings;
    }

    public void setCountPendingListings(final Long countPendingListings) {
        this.countPendingListings = countPendingListings;
    }

    public Long getCountWithdrawnByDeveloperListings() {
        return countWithdrawnByDeveloperListings;
    }

    public void setCountWithdrawnByDeveloperListings(final Long countWithdrawnByDeveloperListings) {
        this.countWithdrawnByDeveloperListings = countWithdrawnByDeveloperListings;
    }

    public Long getCountWithdrawnByOncAcbListings() {
        return countWithdrawnByOncAcbListings;
    }

    public void setCountWithdrawnByOncAcbListings(final Long countWithdrawnByOncAcbListings) {
        this.countWithdrawnByOncAcbListings = countWithdrawnByOncAcbListings;
    }

    public Long getCountSuspendedByOncAcbListings() {
        return countSuspendedByOncAcbListings;
    }

    public void setCountSuspendedByOncAcbListings(final Long countSuspendedByOncAcbListings) {
        this.countSuspendedByOncAcbListings = countSuspendedByOncAcbListings;
    }

    public Long getCountSuspendedByOncListings() {
        return countSuspendedByOncListings;
    }

    public void setCountSuspendedByOncListings(final Long countSuspendedByOncListings) {
        this.countSuspendedByOncListings = countSuspendedByOncListings;
    }

    public Long getCountTerminatedByOncListings() {
        return countTerminatedByOncListings;
    }

    public void setCountTerminatedByOncListings(final Long countTerminatedByOncListings) {
        this.countTerminatedByOncListings = countTerminatedByOncListings;
    }

    public Long getCountWithdrawnByDeveloperUnderSurveillanceListings() {
        return countWithdrawnByDeveloperUnderSurveillanceListings;
    }

    public void setCountWithdrawnByDeveloperUnderSurveillanceListings(final
            Long countWithdrawnByDeveloperUnderSurveillanceListings) {
        this.countWithdrawnByDeveloperUnderSurveillanceListings = countWithdrawnByDeveloperUnderSurveillanceListings;
    }

    public String getTransparencyAttestationUrls() {
        return transparencyAttestationUrls;
    }

    public void setTransparencyAttestationUrls(final String transparencyAttestationUrls) {
        this.transparencyAttestationUrls = transparencyAttestationUrls;
    }

    public String getAcbAttestations() {
        return acbAttestations;
    }

    public void setAcbAttestations(final String acbAttestations) {
        this.acbAttestations = acbAttestations;
    }
}
