package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chpl_uptime_monitor")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChplUptimeMonitorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "description", nullable = false)
    private String description;

    @Basic(optional = false)
    @Column(name = "url", nullable = false)
    private String url;

    @Basic(optional = false)
    @Column(name = "datadog_monitor_key", nullable = false)
    private String datadogMonitorKey;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(nullable = false, name = "deleted")
    private Boolean deleted;

    public ChplUptimeMonitor toDomain() {
        return ChplUptimeMonitor.builder()
                .id(id)
                .datadogMonitorKey(datadogMonitorKey)
                .description(description)
                .url(url)
                .build();
    }
}
