package gov.healthit.chpl.changerequest.entity;

import java.util.Date;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import gov.healthit.chpl.entity.CertificationBodyEntity;
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
    @Column(name = "user_group_name", nullable = false)
    private String userGroupName;

    @Basic(optional = false)
    @Column(name = "status_change_date", nullable = false)
    private Date statusChangeDate;

    @Basic(optional = true)
    @Column(name = "comment", nullable = true)
    private String comment;

}
