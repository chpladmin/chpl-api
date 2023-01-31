package gov.healthit.chpl.functionalityTested;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.PracticeTypeEntity;
import lombok.Data;

@Entity
@Table(name = "functionality_tested")
@Data
public class FunctionalityTestedEntity implements Serializable {
    private static final long serialVersionUID = 2662883108826795645L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "number")
    private String number;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_type_id", insertable = false, updatable = false)
    private PracticeTypeEntity practiceType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "functionalityTestedId")
    @Basic(optional = false)
    @Column(name = "functionality_tested_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<FunctionalityTestedCriteriaMapEntity> mappedCriteria = new HashSet<FunctionalityTestedCriteriaMapEntity>();

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

    public FunctionalityTested toDomain() {
        return FunctionalityTested.builder()
                .id(this.getId())
                .description(this.getName())
                .name(this.getNumber())
                .practiceType(this.getPracticeType() != null ? this.getPracticeType().toDomain() : null)
                .criteria(this.getMappedCriteria() != null ? this.getMappedCriteria().stream()
                        .map(mappedCriterion -> mappedCriterion.getCriterion().toDomain())
                        .collect(Collectors.toList()) : null)
                .build();
    }
}
