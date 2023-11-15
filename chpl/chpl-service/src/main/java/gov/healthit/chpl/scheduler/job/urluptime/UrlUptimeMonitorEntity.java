package gov.healthit.chpl.scheduler.job.urluptime;

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

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.developer.DeveloperEntitySimple;
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
@Table(name = "url_uptime_monitor")
public class UrlUptimeMonitorEntity extends EntityAudit {
    private static final long serialVersionUID = 6887995523356585740L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id")
    private DeveloperEntitySimple developer;;

    @Basic(optional = false)
    @Column(name = "url", nullable = false)
    private String url;

    public UrlUptimeMonitor toDomain() {
        return UrlUptimeMonitor.builder()
                .id(id)
                .developer(developer.toDomain())
                .url(url)
                .build();
    }
}
