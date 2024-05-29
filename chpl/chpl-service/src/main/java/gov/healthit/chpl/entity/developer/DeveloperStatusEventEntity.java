package gov.healthit.chpl.entity.developer;

import java.time.LocalDate;

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

import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.domain.DeveloperStatusEventDeprecated;
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

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public DeveloperStatusEvent toDomain() {
        return DeveloperStatusEvent.builder()
                .id(this.getId())
                .reason(this.getReason())
                .status(this.getDeveloperStatus() != null ? this.getDeveloperStatus().toDomain() : null)
                .startDate(this.getStartDate())
                .endDate(this.getEndDate())
                .build();
    }

    @Deprecated
    public DeveloperStatusEventDeprecated toStatusEventsDeprecated() {
        return DeveloperStatusEventDeprecated.builder()
                .developerId(this.getDeveloperId())
                .id(this.getId())
                .reason(this.getReason())
                .status(this.getDeveloperStatus() != null ? this.getDeveloperStatus().toDomain() : null)
                .statusDate(DateUtil.toDate(this.getStartDate()))
                .build();
    }
}
