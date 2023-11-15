package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.LocalDateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Table(name = "url_uptime_monitor_test")
public class UrlUptimeMonitorTestEntity extends EntityAudit {
    private static final long serialVersionUID = -8138873905563114846L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "url_uptime_monitor_id", nullable = false)
    private Long urlUptimeMonitorId;

    @Basic(optional = false)
    @Column(name = "datadog_test_key", nullable = false)
    private String datadogTestKey;

    @Basic(optional = false)
    @Column(name = "check_time", nullable = false)
    private LocalDateTime checkTime;

    @Basic(optional = false)
    @Column(name = "passed", nullable = false)
    private Boolean passed;

    public UrlUptimeMonitorTest toDomain() {
        return UrlUptimeMonitorTest.builder()
                .id(id)
                .urlUptimeMonitorId(urlUptimeMonitorId)
                .datadogTestKey(datadogTestKey)
                .checkTime(DateUtil.fromSystemToEastern(checkTime))
                .passed(passed)
                .build();
    }
}
