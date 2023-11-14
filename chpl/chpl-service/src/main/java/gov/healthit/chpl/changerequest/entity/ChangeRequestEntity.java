package gov.healthit.chpl.changerequest.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
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
@Table(name = "change_request")
public class ChangeRequestEntity extends EntityAudit {
    private static final long serialVersionUID = -8688917364094882557L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_type_id", nullable = false, insertable = true,
            updatable = false)
    private ChangeRequestTypeEntity changeRequestType;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = false, insertable = true,
            updatable = false)
    private DeveloperEntity developer;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "changeRequestId")
    @Basic(optional = false)
    @Column(name = "change_request_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<ChangeRequestStatusEntity> statuses = new LinkedHashSet<ChangeRequestStatusEntity>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "change_request_certification_body_map",
            joinColumns = @JoinColumn(name = "change_request_id"),
            inverseJoinColumns = @JoinColumn(name = "certification_body_id"))
    private Set<CertificationBodyEntity> certificationBodies;

}
