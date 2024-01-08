package gov.healthit.chpl.changerequest.entity;

import java.util.Date;

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

import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.auth.UserPermissionEntity;
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
@Table(name = "change_request_status")
public class ChangeRequestStatusEntity extends EntityAudit {
    private static final long serialVersionUID = 2830031887773060543L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "change_request_id")
    private Long changeRequestId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_status_type_id", nullable = false, insertable = true,
            updatable = false)
    private ChangeRequestStatusTypeEntity changeRequestStatusType;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id", nullable = true, insertable = true,
            updatable = false)
    private CertificationBodyEntity certificationBody;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_permission_id", nullable = false, insertable = true, updatable = false)
    private UserPermissionEntity userPermission;

    @Basic(optional = false)
    @Column(name = "status_change_date", nullable = false)
    private Date statusChangeDate;

    @Basic(optional = true)
    @Column(name = "comment", nullable = true)
    private String comment;

}
