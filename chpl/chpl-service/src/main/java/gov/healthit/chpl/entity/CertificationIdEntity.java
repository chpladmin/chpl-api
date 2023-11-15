package gov.healthit.chpl.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

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
@Table(name = "ehr_certification_id")
public class CertificationIdEntity extends EntityAudit {
    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ehr_certification_id_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "certification_id", length = 255, nullable = false)
    private String certificationId;

    @Basic(optional = true)
    @Column(name = "practice_type_id", nullable = true)
    private Long practiceTypeId;

    @Basic(optional = false)
    @Column(name = "year", nullable = false)
    private String year;

    public CertificationIdEntity(Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return CertificationIdEntity.class;
    }
}
