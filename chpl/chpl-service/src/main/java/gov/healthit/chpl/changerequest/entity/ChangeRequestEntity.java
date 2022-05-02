package gov.healthit.chpl.changerequest.entity;

import java.util.Date;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "change_request")
@Getter
@Setter
@ToString
public class ChangeRequestEntity {

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
    private DeveloperWithCertificationBodyMapsEntity developer;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "changeRequestId")
    @Basic(optional = false)
    @Column(name = "change_request_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<ChangeRequestStatusEntity> statuses = new LinkedHashSet<ChangeRequestStatusEntity>();

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

}
