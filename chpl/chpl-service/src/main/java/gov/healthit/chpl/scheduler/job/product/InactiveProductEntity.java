package gov.healthit.chpl.scheduler.job.product;

import java.io.Serializable;
import java.time.LocalDate;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Immutable
@Entity
@Table(name = "inactive_products")
public class InactiveProductEntity implements Serializable {
    private static final long serialVersionUID = -470107210862713204L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Basic(optional = false)
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Basic(optional = false)
    @Column(name = "vendor_id", nullable = false)
    private Long developerId;

    @Basic(optional = false)
    @Column(name = "vendor_name", nullable = false)
    private String developerName;

    @Basic(optional = false)
    @Column(name = "vendor_website", nullable = false)
    private String developerWebsite;

    @Basic(optional = false)
    @Column(name = "inactive_date", nullable = false)
    private LocalDate inactiveDate;

    public InactiveProduct toDomain() {
        return InactiveProduct.builder()
                .developerId(this.getDeveloperId())
                .developerName(this.getDeveloperName())
                .productId(this.getProductId())
                .productName(this.getProductName())
                .inactiveDate(this.getInactiveDate())
                .build();
    }
}
