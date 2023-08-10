package gov.healthit.chpl.functionalityTested;

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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "certification_result_functionality_tested")
public class CertificationResultFunctionalityTestedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "certification_result_id", nullable = false)
    private Long certificationResultId;

    @Column(name = "functionality_tested_id")
    private Long functionalityTestedId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "functionality_tested_id", unique = true, nullable = true, insertable = false, updatable = false)
    private FunctionalityTestedEntity functionalityTested;

    @Column(name = "deleted", insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public CertificationResultFunctionalityTested toDomain() {
        return CertificationResultFunctionalityTested.builder()
                .id(this.getId())
                .certificationResultId(certificationResultId)
                .functionalityTested(this.functionalityTested.toDomain())
                //.name(this.getFunctionalityTested() != null ? this.getFunctionalityTested().getRegulatoryTextCitation() : null)
                //.description(this.getFunctionalityTested() != null ? this.getFunctionalityTested().getValue() : null)
                .build();
    }
}
