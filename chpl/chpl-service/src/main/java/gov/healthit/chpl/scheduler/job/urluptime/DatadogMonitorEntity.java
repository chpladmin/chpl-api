package gov.healthit.chpl.scheduler.job.urluptime;

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

import gov.healthit.chpl.entity.developer.DeveloperEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "datadog_monitor")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatadogMonitorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id")
    private DeveloperEntity developer;;

    @Basic(optional = false)
    @Column(name = "url", nullable = false)
    private String url;

    @Basic(optional = false)
    @Column(name = "datadog_public_id", nullable = false)
    private String datadogPublicId;

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

    public DatadogMonitor toDomain() {
        return DatadogMonitor.builder()
                .id(id)
                .datadogPublicId(datadogPublicId)
                .developer(developer.toDomain())
                .url(url)
                .build();
    }
}
