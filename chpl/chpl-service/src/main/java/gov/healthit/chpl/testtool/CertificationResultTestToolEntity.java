package gov.healthit.chpl.testtool;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.listing.CertificationResultEntity;
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
@Table(name = "certification_result_test_tool")
public class CertificationResultTestToolEntity extends EntityAudit {
    private static final long serialVersionUID = -4001381872451124331L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certification_result_test_tool_id")
    private Long id;

    @Basic(optional = true)
    @Column(name = "certification_result_id", nullable = false)
    private Long certificationResultId;

    @Column(name = "version")
    private String version;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "test_tool_id", insertable = true, updatable = false)
    private TestToolEntity testTool;

    @Basic(optional = true)
    @ManyToOne(targetEntity = CertificationResultEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_result_id", nullable = false, insertable = false, updatable = false)
    private CertificationResultEntity certificationResult;

    public CertificationResultTestTool toDomain() {
        return CertificationResultTestTool.builder()
                .id(id)
                .certificationResultId(certificationResultId)
                .testToolId(testTool.getId())
                .testToolName(testTool.getValue())
                .testToolVersion(version)
                .testTool(testTool.toDomain())
                .version(version)
                .build();
    }
}
