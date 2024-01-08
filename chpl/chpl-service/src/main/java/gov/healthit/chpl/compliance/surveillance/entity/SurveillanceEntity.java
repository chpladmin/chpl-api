package gov.healthit.chpl.compliance.surveillance.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

import org.hibernate.annotations.Where;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
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
@Table(name = "surveillance")
public class SurveillanceEntity extends EntityAudit {
    private static final long serialVersionUID = -7440112475434169829L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "friendly_id", insertable = false, updatable = false)
    private String friendlyId;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certified_product_id", insertable = false, updatable = false)
    private CertifiedProductEntity certifiedProduct;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "type_id")
    private Long surveillanceTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    private SurveillanceTypeEntity surveillanceType;

    @Column(name = "randomized_sites_used")
    private Integer numRandomizedSites;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "surveillanceId")
    @Basic(optional = false)
    @Column(name = "surveillance_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<SurveillanceRequirementEntity> surveilledRequirements = new HashSet<SurveillanceRequirementEntity>();

    public Surveillance toDomain(CertifiedProductDAO certifiedProductDAO, CertificationCriterionService certificationCriterionService) {
        try {
            return Surveillance.builder()
                    .id(this.getId())
                    .friendlyId(this.getFriendlyId())
                    .startDay(this.getStartDate())
                    .endDay(this.getEndDate())
                    .randomizedSitesUsed(this.getNumRandomizedSites())
                    .lastModifiedDate(this.getLastModifiedDate())
                    .type(SurveillanceType.builder()
                            .id(this.getSurveillanceType().getId())
                            .name(this.getSurveillanceType().getName())
                            .build())
                    .certifiedProduct(new CertifiedProduct(certifiedProductDAO.getDetailsById(this.getCertifiedProductId())))
                    .requirements(this.getSurveilledRequirements().stream()
                            .map(e -> e.toDomain(certificationCriterionService))
                            .collect(Collectors.toCollection(LinkedHashSet::new)))
                    .build();
        } catch (EntityRetrievalException e) {
            return null;
        }
    }
}
