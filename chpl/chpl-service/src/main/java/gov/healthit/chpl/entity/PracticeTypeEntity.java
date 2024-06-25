package gov.healthit.chpl.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import gov.healthit.chpl.domain.PracticeType;
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
@Table(name = "practice_type")
public class PracticeTypeEntity extends EntityAudit {
    private static final long serialVersionUID = -512191905822294896L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "practice_type_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(nullable = false, length = 250)
    private String description;

    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    public PracticeType toDomain() {
        return PracticeType.builder()
                .id(this.getId())
                .description(this.getDescription())
                .name(this.getName())
                .build();
    }

}
