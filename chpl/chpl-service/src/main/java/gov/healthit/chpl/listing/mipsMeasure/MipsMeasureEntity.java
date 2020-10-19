package gov.healthit.chpl.listing.mipsMeasure;

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
@Table(name = "mips_measure")
public class MipsMeasureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "mips_domain_id", unique = true, nullable = true)
    private MipsMeasureDomain domain;

    @Column(name = "required_test_abbr")
    private String abbreviation;

    @Column(name = "required_test")
    private String requiredTest;

    @Column(name = "measure_name")
    private String name;

    @Column(name = "criteria_selection_required")
    private Boolean criteriaSelectionRequired;

    @Column(name = "removed")
    private Boolean removed;

    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    private Date creationDate;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "last_modified_date", nullable = false, updatable = false, insertable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;
}
