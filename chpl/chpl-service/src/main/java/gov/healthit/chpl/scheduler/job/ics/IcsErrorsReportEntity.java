package gov.healthit.chpl.scheduler.job.ics;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.CertificationBodyEntity;
import lombok.Data;

@Entity
@Table(name = "inheritance_errors_report")
@Data
public class IcsErrorsReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Basic(optional = false)
    @Column(name = "developer")
    private String developer;

    @Basic(optional = false)
    @Column(name = "product")
    private String product;

    @Basic(optional = false)
    @Column(name = "version")
    private String version;

    @Basic(optional = true)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id", nullable = false, insertable = true, updatable = true)
    private CertificationBodyEntity certificationBody;

    @Basic(optional = false)
    @Column(name = "url")
    private String url;

    @Basic(optional = false)
    @Column(name = "reason")
    private String reason;

    @Basic(optional = false)
    @Column(name = "deleted")
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    public IcsErrorsReport toDomain() {
        return IcsErrorsReport.builder()
                .id(this.getId())
                .chplProductNumber(this.getChplProductNumber())
                .developer(this.getDeveloper())
                .product(this.getProduct())
                .version(this.getVersion())
                .certificationBody(this.getCertificationBody().toDomain())
                .url(this.getUrl())
                .reason(this.getReason())
                .deleted(this.getDeleted())
                .build();
    }
}
