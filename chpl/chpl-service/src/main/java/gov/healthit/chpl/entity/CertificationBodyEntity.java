package gov.healthit.chpl.entity;

import java.io.Serializable;
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

import org.hibernate.annotations.Where;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import lombok.Data;

/**
 * Certification body mapping to database.
 *
 * @author kekey
 *
 */
@Entity
@Table(name = "certification_body")
@Data
public class CertificationBodyEntity implements Serializable {
    private static final long serialVersionUID = -4603773689327950041L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certification_body_id", nullable = false)
    private Long id;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", unique = true, nullable = true)
    @Where(clause = "deleted <> 'true'")
    private AddressEntity address;

    @Column(name = "acb_code")
    private String acbCode;

    @Column(name = "name")
    private String name;

    @Basic(optional = true)
    @Column(name = "website", nullable = true)
    private String website;

    @Column(name = "retired", nullable = false)
    private Boolean retired;

    @Column(name = "retirement_date", nullable = true)
    private Date retirementDate;

    @Basic(optional = false)
    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false, insertable = false)
    private Boolean deleted;

    public CertificationBody buildCertificationBody() {
        return CertificationBody.builder()
                .acbCode(this.getAcbCode())
                .address(this.getAddress() == null ? null
                        : this.getAddress().toDomain())
                .id(this.getId())
                .name(this.getName())
                .retired(this.getRetired())
                .retirementDate(this.getRetirementDate())
                .website(this.getWebsite())
                .build();
    }

    public static CertificationBodyEntity getNewAcbEntity(CertificationBody acb) {
        CertificationBodyEntity entity = new CertificationBodyEntity();
        entity.setId(acb.getId());
        entity.setAcbCode(acb.getAcbCode());
        entity.setName(acb.getName());
        entity.setWebsite(acb.getWebsite());
        entity.setRetired(acb.isRetired());
        entity.setRetirementDate(acb.getRetirementDate());
        return entity;
    }

    public static CertificationBodyEntity getNewAcbEntity(CertificationBodyDTO acb) {
        CertificationBodyEntity entity = new CertificationBodyEntity();
        entity.setId(acb.getId());
        entity.setAcbCode(acb.getAcbCode());
        entity.setName(acb.getName());
        entity.setWebsite(acb.getWebsite());
        entity.setRetired(acb.isRetired());
        entity.setRetirementDate(acb.getRetirementDate());
        return entity;
    }
}
