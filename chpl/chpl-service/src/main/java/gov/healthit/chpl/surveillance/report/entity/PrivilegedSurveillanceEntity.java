package gov.healthit.chpl.surveillance.report.entity;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.surveillance.SurveillanceTypeEntity;
import lombok.Data;

@Entity
@Data
@Immutable
@Table(name = "surveillance_basic")
public class PrivilegedSurveillanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "friendly_id", insertable = false, updatable = false)
    private String friendlyId;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "type_id")
    private Long surveillanceTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    private SurveillanceTypeEntity surveillanceType;

    @Column(name = "randomized_sites_used")
    private Integer numRandomizedSites;

    @Column(name = "open_nonconformity_count")
    private Integer numOpenNonconformities;

    @Column(name = "closed_nonconformity_count")
    private Integer numClosedNonconformities;

    @Column(name = "user_permission_id")
    private Long userPermissionId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "surveillanceId")
    @Basic(optional = false)
    @Column(name = "surveillance_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<QuarterlyReportSurveillanceMapEntity> privSurvMap = new HashSet<QuarterlyReportSurveillanceMapEntity>();

    @Column(name = "deleted")
    private Boolean deleted;
}
