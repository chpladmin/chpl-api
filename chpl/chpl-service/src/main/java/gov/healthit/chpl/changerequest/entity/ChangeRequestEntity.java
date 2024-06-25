package gov.healthit.chpl.changerequest.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
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
