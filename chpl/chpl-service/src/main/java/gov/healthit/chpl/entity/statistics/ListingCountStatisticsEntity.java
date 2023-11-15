package gov.healthit.chpl.entity.statistics;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import gov.healthit.chpl.entity.CertificationEditionEntity;
import gov.healthit.chpl.entity.CertificationStatusEntity;
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
@Table(name = "listing_count_statistics")
public class ListingCountStatisticsEntity extends EntityAudit {
    private static final long serialVersionUID = 1313677047965534572L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "developer_count", nullable = false)
    private Long developerCount;

    @Basic(optional = false)
    @Column(name = "product_count", nullable = false)
    private Long productCount;

    @Basic(optional = false)
    @Column(name = "certification_edition_id", nullable = false)
    private Long certificationEditionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_edition_id", insertable = false, updatable = false)
    private CertificationEditionEntity certificationEdition;

    @Basic(optional = false)
    @Column(name = "certification_status_id", nullable = false)
    private Long certificationStatusId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_status_id", insertable = false, updatable = false)
    private CertificationStatusEntity certificationStatus;

    public ListingCountStatisticsEntity(final Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return ListingCountStatisticsEntity.class;
    }
}
