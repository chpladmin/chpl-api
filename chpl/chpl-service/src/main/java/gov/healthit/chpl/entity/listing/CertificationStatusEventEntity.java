package gov.healthit.chpl.entity.listing;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.entity.CertificationStatusEntity;
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
@Table(name = "certification_status_event")
public class CertificationStatusEventEntity extends EntityAudit {
    private static final long serialVersionUID = 4174889617079658144L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certification_status_event_id")
    private Long id;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @Column(name = "event_date")
    private Date eventDate;

    @Column(name = "certification_status_id")
    private Long certificationStatusId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_status_id", insertable = false, updatable = false)
    private CertificationStatusEntity certificationStatus;

    @Column(name = "reason")
    private String reason;

    public CertificationStatusEvent toDomain() {
        CertificationStatus status = null;
        if (this.getCertificationStatus() != null) {
            status = this.certificationStatus.toDomain();
        } else {
            status = CertificationStatus.builder()
                    .id(this.getCertificationStatusId())
                    .build();
        }
        return CertificationStatusEvent.builder()
                .id(this.getId())
                .eventDate(this.getEventDate().getTime())
                .status(status)
                .reason(this.getReason())
                .build();
    }
}
