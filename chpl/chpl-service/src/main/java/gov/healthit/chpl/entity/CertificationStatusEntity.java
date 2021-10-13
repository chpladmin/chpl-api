package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import gov.healthit.chpl.domain.CertificationStatus;
import lombok.Data;

@Entity
@Data
@Table(name = "certification_status")
public class CertificationStatusEntity implements Serializable{

    /** Serial Version UID. */
    private static final long serialVersionUID = -2928065796550377879L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certification_status_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "certification_status", nullable = false)
    private String status;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public CertificationStatus toDomain() {
        return CertificationStatus.builder()
                .id(this.getId())
                .name(this.getStatus())
                .build();
    }
}
