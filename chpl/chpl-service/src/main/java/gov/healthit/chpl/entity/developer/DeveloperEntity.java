package gov.healthit.chpl.entity.developer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import javax.persistence.Transient;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.attestation.entity.AttestationSubmissionEntity;
import gov.healthit.chpl.changerequest.entity.DeveloperCertificationBodyMapEntity;
import gov.healthit.chpl.developer.DeveloperStatusEventComparator;
import gov.healthit.chpl.developer.PublicAttestationComparator;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.domain.PublicAttestation;
import gov.healthit.chpl.domain.concept.PublicAttestationStatus;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.ContactEntity;
import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@DynamicUpdate
@Table(name = "vendor")
public class DeveloperEntity extends EntityAudit {
    private static final long serialVersionUID = -1396979009499564864L;
    private static final int WEBSITE_MAX_LENGTH = 300;

    @Transient
    private final DeveloperStatusEventComparator developerStatusEventComparator = new DeveloperStatusEventComparator();
    @Transient
    private final PublicAttestationComparator publicAttestationComparator = new PublicAttestationComparator();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "vendor_id", nullable = false)
    private Long id;

    @Column(name = "vendor_code", insertable = false, updatable = false)
    private String developerCode;

    @Column(name = "name")
    private String name;

    @Basic(optional = true)
    @Column(length = WEBSITE_MAX_LENGTH, nullable = true)
    private String website;

    @Column(name = "self_developer")
    private Boolean selfDeveloper;

    @Basic(optional = true)
    @Column(name = "address_id")
    private Long addressId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", unique = true, nullable = true, insertable = false, updatable = false)
    @Where(clause = "deleted <> 'true'")
    private AddressEntity address;

    @Basic(optional = true)
    @Column(name = "contact_id")
    private Long contactId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", unique = true, nullable = true, insertable = false, updatable = false)
    private ContactEntity contact;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "developerId")
    @Basic(optional = false)
    @Column(name = "vendor_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<DeveloperStatusEventEntity> statusEvents = new LinkedHashSet<DeveloperStatusEventEntity>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "developerId")
    @Basic(optional = false)
    @Column(name = "developer_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<AttestationSubmissionEntity> attestations = new LinkedHashSet<AttestationSubmissionEntity>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "developerId")
    @Basic(optional = false)
    @Column(name = "vendor_id", nullable = false)
    private Set<DeveloperCertificationBodyMapEntity> certificationBodyMaps = new LinkedHashSet<DeveloperCertificationBodyMapEntity>();

    @Transient
    @Builder.Default
    private List<AttestationPeriodEntity> periods = new ArrayList<AttestationPeriodEntity>();

    public Developer toDomain() {
        return Developer.builder()
                .id(this.getId())
                .address(this.getAddress() != null ? this.getAddress().toDomain() : null)
                .contact(this.getContact() != null ? this.getContact().toDomain() : null)
                .deleted(this.getDeleted())
                .developerCode(this.getDeveloperCode())
                .name(this.getName())
                .selfDeveloper(this.getSelfDeveloper())
                .statusEvents(toStatusEventDomains())
                .lastModifiedDate(this.getLastModifiedDate().getTime() + "")
                .website(this.getWebsite())
                .attestations(toPublicAttestationDomains())
                .build();
    }

    private List<DeveloperStatusEvent> toStatusEventDomains() {
        if (CollectionUtils.isEmpty(this.getStatusEvents())) {
            return new ArrayList<DeveloperStatusEvent>();
        }
        return this.statusEvents.stream()
                .map(statusEvent -> statusEvent.toDomain())
                .sorted(developerStatusEventComparator)
                .collect(Collectors.toList());
    }

    private List<PublicAttestation> toPublicAttestationDomains() {
        if (CollectionUtils.isEmpty(this.getPeriods())) {
            return new ArrayList<PublicAttestation>();
        }
        return getPeriods().stream()
                .filter(period -> period.getPeriodEnd().isBefore(LocalDate.now()))
                .map(period -> {
                    AttestationSubmissionEntity attestation = getAttestationForPeriod(period.toDomain());
                    return PublicAttestation.builder()
                            .id(attestation != null ? attestation.getId() : null)
                            .attestationPeriod(period.toDomain())
                            .status(attestation != null ? PublicAttestationStatus.ATTESTATIONS_SUBMITTED : PublicAttestationStatus.NO_ATTESTATIONS_SUBMITTED)
                            .build();

                })
                .sorted(publicAttestationComparator)
                .toList();
    }

    private AttestationSubmissionEntity getAttestationForPeriod(AttestationPeriod period) {
        return attestations.stream()
                .filter(att -> att.getAttestationPeriod().getId().equals(period.getId()))
                .findAny()
                .orElse(null);
    }
}
