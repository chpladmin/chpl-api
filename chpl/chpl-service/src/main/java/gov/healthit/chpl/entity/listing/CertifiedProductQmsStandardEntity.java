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
import gov.healthit.chpl.qmsStandard.QmsStandardEntity;
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
@Table(name = "certified_product_qms_standard")
public class CertifiedProductQmsStandardEntity extends EntityAudit {
    private static final long serialVersionUID = 6133501679911907363L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certified_product_qms_standard_id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long certifiedProductId;

    @Basic(optional = false)
    @Column(name = "qms_standard_id", nullable = false)
    private Long qmsStandardId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "qms_standard_id", unique = true, nullable = true, insertable = false, updatable = false)
    private QmsStandardEntity qmsStandard;

    @Basic(optional = false)
    @Column(name = "modification", nullable = false)
    private String modification;

    @Basic(optional = false)
    @Column(name = "applicable_criteria", nullable = false)
    private String applicableCriteria;

}
