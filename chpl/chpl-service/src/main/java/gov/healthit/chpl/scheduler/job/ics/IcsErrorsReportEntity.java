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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inheritance_errors_report")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IcsErrorsReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

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
    @Column(name = "reason")
    private String reason;

    @Basic(optional = false)
    @Column(name = "deleted")
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    public IcsErrorsReportItem toDomain() {
        return IcsErrorsReportItem.builder()
                .id(this.getId())
                .listingId(this.getCertifiedProductId())
                .chplProductNumber(this.getChplProductNumber())
                .developer(this.getDeveloper())
                .product(this.getProduct())
                .version(this.getVersion())
                .certificationBody(this.getCertificationBody().toDomain())
                .reason(this.getReason())
                .deleted(this.getDeleted())
                .build();
    }
}
