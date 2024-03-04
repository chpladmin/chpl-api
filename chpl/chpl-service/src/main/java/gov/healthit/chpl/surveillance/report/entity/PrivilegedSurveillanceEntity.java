package gov.healthit.chpl.surveillance.report.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.compliance.surveillance.entity.SurveillanceBasicEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Immutable
public class PrivilegedSurveillanceEntity extends SurveillanceBasicEntity {
    private static final long serialVersionUID = 7159533755185964491L;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "surveillanceId")
    @Basic(optional = false)
    @Column(name = "surveillance_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<QuarterlyReportSurveillanceMapEntity> privSurvMap = new HashSet<QuarterlyReportSurveillanceMapEntity>();
}
