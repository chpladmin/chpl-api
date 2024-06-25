package gov.healthit.chpl.testtool;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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
                .testTool(testTool.toDomain())
                .version(version)
                .build();
    }
}
