package gov.healthit.chpl.entity.listing;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.domain.CertifiedProductChplProductNumberHistory;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.util.DateUtil;
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
@Table(name = "certified_product_chpl_product_number_history")
public class CertifiedProductChplProductNumberHistoryEntity extends EntityAudit {
    private static final long serialVersionUID = -3216566032065336746L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "certified_product_id", nullable = false)
    private Long certifiedProductId;

    @Column(name = "chpl_product_number", nullable = false)
    private String chplProductNumber;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    public CertifiedProductChplProductNumberHistory toDomain() {
        return CertifiedProductChplProductNumberHistory.builder()
                .id(this.getId())
                .chplProductNumber(this.getChplProductNumber())
                .endDateTime(DateUtil.toLocalDateTime(this.getEndDate().getTime()))
                .build();
    }
}
