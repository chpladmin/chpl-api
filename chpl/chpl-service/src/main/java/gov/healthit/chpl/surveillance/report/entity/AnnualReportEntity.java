package gov.healthit.chpl.surveillance.report.entity;

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

import gov.healthit.chpl.entity.CertificationBodyEntity;
import lombok.Data;

@Entity
@Data
@Table(name = "annual_report")
public class AnnualReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id", insertable = false, updatable = false)
    private CertificationBodyEntity acb;

    @Column(name = "year")
    private Integer year;

    @Column(name = "obstacle_summary")
    private String obstacleSummary;

    @Column(name = "findings_summary")
    private String findingsSummary;

    @Column(name = "deleted", insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;
}
