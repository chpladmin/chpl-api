package gov.healthit.chpl.svap.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import gov.healthit.chpl.entity.CertificationCriterionEntity;
import lombok.Data;

@Entity
@Table(name = "svap")
@Data
public class SvapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long svapId;

    @Column(name = "regulatory_text_citation", nullable = false)
    private String regulatoryTextCitation;

    @Column(name = "approved_standard_version", nullable = false)
    private String approvedStandardVersion;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "svap_criteria_map",
        joinColumns = {@JoinColumn(name = "svap_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "criteria_id", referencedColumnName = "certification_criterion_id")})
    private Set<CertificationCriterionEntity> criteria;


    @Column(name = "replaced", nullable = false)
    private Boolean replaced;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

}
