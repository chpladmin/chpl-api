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
import gov.healthit.chpl.entity.auth.UserPermissionEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "change_request_status")
@Getter
@Setter
@ToString
public class ChangeRequestStatusEntity {

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

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
}
