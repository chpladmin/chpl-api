package gov.healthit.chpl.entity;

import java.time.LocalDate;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "testing_lab")
public class TestingLabEntity extends EntityAudit {
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
