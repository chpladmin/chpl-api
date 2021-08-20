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
@Table(name = "quarterly_report")
public class QuarterlyReportEntity {

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

    @Column(name = "quarter_id")
    private Long quarterId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id", insertable = false, updatable = false)
    private QuarterEntity quarter;

    @Column(name = "activities_and_outcomes_summary")
    private String activitiesOutcomesSummary;

    @Column(name = "reactive_surveillance_summary")
    private String reactiveSurveillanceSummary;

    @Column(name = "prioritized_element_summary")
    private String prioritizedElementSummary;

    @Column(name = "disclosure_requirements_summary")
    private String disclosureRequirementsSummary;

    @Column(name = "deleted", insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;
}
