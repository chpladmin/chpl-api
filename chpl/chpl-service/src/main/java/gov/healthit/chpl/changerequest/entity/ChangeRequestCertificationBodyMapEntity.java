package gov.healthit.chpl.changerequest.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "change_request_certification_body_map")
public class ChangeRequestCertificationBodyMapEntity extends EntityAudit {
    private static final long serialVersionUID = -343459298516750169L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "change_request_id", nullable = false)
    private Long changeRequestId;

    @Basic(optional = false)
    @Column(name = "certification_body_id", nullable = false)
    private Long certificationBodyId;

}
