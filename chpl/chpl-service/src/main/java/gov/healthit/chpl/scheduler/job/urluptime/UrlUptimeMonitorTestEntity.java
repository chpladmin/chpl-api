package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "url_uptime_monitor_test")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UrlUptimeMonitorTestEntity {

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
