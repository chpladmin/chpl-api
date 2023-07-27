package gov.healthit.chpl.entity;

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

import gov.healthit.chpl.domain.TestStandard;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "test_standard")
public class TestStandardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "test_standard_id")
    private Long id;

    @Column(name = "number", nullable = false)
    private String name;

    @Column(name = "name", nullable = false)
    private String description;

    @Deprecated
    @Column(name = "certification_edition_id")
    private Long certificationEditionId;

    @Deprecated
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_edition_id", insertable = false, updatable = false)
    private CertificationEditionEntity certificationEdition;

    @Column(name = "deleted", nullable = false)
    protected Boolean deleted;

    @Column(name = "last_modified_user", nullable = false)
    protected Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public TestStandard toDomain() {
        return TestStandard.builder()
                .description(getDescription())
                .id(getId())
                .name(getName())
                .year(getCertificationEdition().getYear())
                .build();
    }
}
