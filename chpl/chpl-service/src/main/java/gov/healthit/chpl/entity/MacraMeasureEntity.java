package gov.healthit.chpl.entity;

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

import lombok.Data;

@Entity
@Data
@Table(name = "macra_criteria_map")
public class MacraMeasureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "criteria_id")
    private Long certificationCriterionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", insertable = false, updatable = false)
    private CertificationCriterionEntity certificationCriterion;

    @Column(name = "value")
    private String value;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "removed")
    private Boolean removed;

    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    protected Date creationDate;

    @Column(nullable = false)
    protected Boolean deleted;

    @Column(name = "last_modified_date", nullable = false, updatable = false, insertable = false)
    protected Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    protected Long lastModifiedUser;
}