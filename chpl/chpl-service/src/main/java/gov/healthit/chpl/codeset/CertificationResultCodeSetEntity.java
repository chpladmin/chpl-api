package gov.healthit.chpl.codeset;

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
@Table(name = "certification_result_code_set")
public class CertificationResultCodeSetEntity extends EntityAudit {
    private static final long serialVersionUID = 7697779196443883288L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "certification_result_id", nullable = false)
    private Long certificationResultId;

    @Column(name = "code_set_id")
    private Long codeSetId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "code_set_id", unique = true, nullable = true, insertable = false, updatable = false)
    private CodeSetEntity codeSet;

    public CertificationResultCodeSet toDomain() {
        return CertificationResultCodeSet.builder()
                .id(id)
                .certificationResultId(certificationResultId)
                .codeSet(codeSet.toDomain())
                .creationDate(getCreationDate())
                .build();
    }
}
