package gov.healthit.chpl.entity.developer;

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

import gov.healthit.chpl.domain.DeveloperStatusEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "vendor_status_history")
public class DeveloperStatusEventEntity implements Serializable {
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

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

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
