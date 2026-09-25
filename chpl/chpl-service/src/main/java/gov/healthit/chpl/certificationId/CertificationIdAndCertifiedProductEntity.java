package gov.healthit.chpl.certificationId;

import java.io.Serializable;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ehr_certification_ids_and_products")
public class CertificationIdAndCertifiedProductEntity implements Serializable {
    private static final long serialVersionUID = -1L;

    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "ehr_certification_id", nullable = false)
    private Long ehrCertificationId;

    @Basic(optional = false)
    @Column(name = "ehr_certification_id_text", length = 255, nullable = false)
    private String certificationId;

    @Basic(optional = false)
    @Column(name = "ehr_certification_id_creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    public SimpleCertificationIdWithProducts toDomainWithProducts() {
        return SimpleCertificationIdWithProducts.builder()
                .certificationId(getCertificationId())
                .created(getCreationDate())
                .products(Stream.of(getChplProductNumber()).collect(Collectors.toList()))
                .build();
    }

    public SimpleCertificationId toDomain() {
        return SimpleCertificationId.builder()
                .certificationId(getCertificationId())
                .created(getCreationDate())
                .build();
    }
}
