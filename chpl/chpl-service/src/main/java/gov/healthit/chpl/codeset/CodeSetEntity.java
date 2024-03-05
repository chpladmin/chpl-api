package gov.healthit.chpl.codeset;

import java.time.LocalDate;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.EntityAudit;
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
@Table(name = "code_set")
public class CodeSetEntity extends EntityAudit {
    private static final long serialVersionUID = 3619325516271435265L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "required_day")
    private LocalDate requiredDay;

    @Basic(optional = false)
    @Column(name = "start_day")
    private LocalDate startDay;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "codeSetId")
    @Basic(optional = false)
    @Column(name = "code_set_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<CodeSetCriteriaMapEntity> mappedCriteria = new HashSet<CodeSetCriteriaMapEntity>();

    public CodeSet toDomain() {
        return CodeSet.builder()
                .id(id)
                .requiredDay(requiredDay)
                .startDay(startDay)
                .build();
    }

    public CodeSet toDomainWithCriteria() {
        return CodeSet.builder()
                .id(id)
                .requiredDay(requiredDay)
                .startDay(startDay)
                .criteria(this.getMappedCriteria() != null ? this.getMappedCriteria().stream()
                        .map(mappedCriterion -> mappedCriterion.getCriterion().toDomain())
                        .collect(Collectors.toList()) : null)
                .build();
    }
}
