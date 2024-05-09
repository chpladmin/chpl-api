package gov.healthit.chpl.entity.developer;

import java.util.Date;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import gov.healthit.chpl.domain.DeveloperStatusEvent;
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
@Table(name = "vendor_status_history")
public class DeveloperStatusEventEntity extends EntityAudit {
    private static final long serialVersionUID = 1730728043307135377L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "vendor_status_history_id", nullable = false)
    private Long id;

    @Column(name = "vendor_id")
    private Long developerId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", insertable = false, updatable = false)
    private DeveloperEntity developer;

    @Column(name = "vendor_status_id")
    private Long developerStatusId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_status_id", insertable = false, updatable = false)
    private DeveloperStatusEntity developerStatus;

    @Column(name = "reason")
    private String reason;

    @Column(name = "status_date")
    private Date statusDate;

    public DeveloperStatusEvent toDomain() {
        return DeveloperStatusEvent.builder()
                .developerId(this.getDeveloperId())
                .id(this.getId())
                .reason(this.getReason())
                .status(this.getDeveloperStatus() != null ? this.getDeveloperStatus().toDomain() : null)
                .statusDate(this.getStatusDate())
                .build();
    }
}
