package gov.healthit.chpl.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import gov.healthit.chpl.domain.CertificationStatus;
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
@Table(name = "certification_status")
public class CertificationStatusEntity extends EntityAudit {
    private static final long serialVersionUID = -2928065796550377879L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certification_status_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "certification_status", nullable = false)
    private String status;

    public CertificationStatus toDomain() {
        return CertificationStatus.builder()
                .id(this.getId())
                .name(this.getStatus())
                .build();
    }
}
