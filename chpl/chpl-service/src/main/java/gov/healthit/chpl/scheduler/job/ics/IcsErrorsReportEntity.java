package gov.healthit.chpl.scheduler.job.ics;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

    @Basic(optional = true)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id", nullable = false, insertable = true, updatable = true)
    private CertificationBodyEntity certificationBody;

    @Basic(optional = false)
    @Column(name = "reason")
    private String reason;

    public IcsErrorsReportItem toDomain() {
        return IcsErrorsReportItem.builder()
                .id(this.getId())
                .listingId(this.getCertifiedProductId())
                .certificationBody(this.getCertificationBody().toDomain())
                .reason(this.getReason())
                .build();
    }
}
