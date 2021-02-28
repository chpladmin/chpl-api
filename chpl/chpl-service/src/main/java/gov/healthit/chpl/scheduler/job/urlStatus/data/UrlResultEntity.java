package gov.healthit.chpl.scheduler.job.urlStatus.data;

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

import lombok.Data;

@Entity
@Data
@Table(name = "url_check_result")
public class UrlResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "url")
    private String url;

    @Basic(optional = false)
    @Column(name = "url_type_id")
    private Long urlTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "url_type_id", insertable = false, updatable = false)
    private UrlTypeEntity urlType;

    @Basic(optional = false)
    @Column(name = "response_code")
    private Integer responseCode;

    @Basic(optional = false)
    @Column(name = "response_message")
    private String responseMessage;

    @Basic(optional = false)
    @Column(name = "checked_date")
    private Date lastChecked;

    @Column(name = "deleted", insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;
}
