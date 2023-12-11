package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.time.LocalDate;
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

import org.hibernate.annotations.Where;

import gov.healthit.chpl.domain.TestingLab;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "testing_lab")
@Data
@NoArgsConstructor
public class TestingLabEntity implements Serializable {
    private static final long serialVersionUID = -5332080900089062553L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "testing_lab_id", nullable = false)
    private Long id;

    @Column(name = "testing_lab_code")
    private String testingLabCode;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", unique = true, nullable = true)
    @Where(clause = "deleted <> 'true'")
    private AddressEntity address;

    @Column(name = "name")
    private String name;

    @Column(name = "website")
    private String website;

    @Column(name = "retired", nullable = false)
    private Boolean retired;

    @Column(name = "retirement_date", nullable = true)
    private LocalDate retirementDate;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false, insertable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public TestingLab toDomain() {
        return TestingLab.builder()
                .atlCode(this.getTestingLabCode())
                .address(this.getAddress() == null ? null
                        : this.getAddress().toDomain())
                .id(this.getId())
                .name(this.getName())
                .retired(this.getRetired())
                .retirementDay(this.getRetirementDate())
                .website(this.getWebsite())
                .build();
    }
}
