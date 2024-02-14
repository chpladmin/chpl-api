package gov.healthit.chpl.codesetdate;

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
@Table(name = "certification_result_code_set_date")
public class CertificationResultCodeSetDateEntity extends EntityAudit {
    private static final long serialVersionUID = 7697779196443883288L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "certification_result_id", nullable = false)
    private Long certificationResultId;

    @Column(name = "code_set_date_id")
    private Long codeSetDateId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "code_set_date_id", unique = true, nullable = true, insertable = false, updatable = false)
    private CodeSetDateEntity codeSetDate;

    public CertificationResultCodeSetDate toDomain() {
        return CertificationResultCodeSetDate.builder()
                .id(id)
                .certificationResultId(certificationResultId)
                .codeSetDate(codeSetDate.toDomain())
                .build();
    }
}
