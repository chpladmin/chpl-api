package gov.healthit.chpl.entity;

import java.io.Serializable;
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
@Table(name = "test_functionality")
@Data
public class TestFunctionalityEntity implements Serializable {
    private static final long serialVersionUID = 2662883108826795645L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "test_functionality_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "number")
    private String number;

    @Column(name = "certification_edition_id")
    private Long certificationEditionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_edition_id", insertable = false, updatable = false)
    private CertificationEditionEntity certificationEdition;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_type_id", insertable = false, updatable = false)
    private PracticeTypeEntity practiceType;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;
}
