package gov.healthit.chpl.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Table(name = "ehr_certification_id_product_map")
public class CertificationIdProductMapEntity extends EntityAudit {
    private static final long serialVersionUID = 6077443445177465105L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ehr_certification_id_product_map_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "ehr_certification_id_id", nullable = false)
    private Long certificationIdId;

    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long certifiedProductId;
}
