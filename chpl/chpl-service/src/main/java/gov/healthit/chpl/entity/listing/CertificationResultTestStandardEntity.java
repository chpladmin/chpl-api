package gov.healthit.chpl.entity.listing;

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
import gov.healthit.chpl.entity.TestStandardEntity;
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
@Table(name = "certification_result_test_standard")
public class CertificationResultTestStandardEntity extends EntityAudit {
    private static final long serialVersionUID = -3333587092448831381L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certification_result_test_standard_id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "certification_result_id", nullable = false)
    private Long certificationResultId;

    @Column(name = "test_standard_id")
    private Long testStandardId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "test_standard_id", unique = true, nullable = true, insertable = false, updatable = false)
    private TestStandardEntity testStandard;

}
