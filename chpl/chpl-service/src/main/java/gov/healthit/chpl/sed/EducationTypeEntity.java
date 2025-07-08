package gov.healthit.chpl.sed;

import gov.healthit.chpl.entity.EntityAudit;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
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
@Table(name = "education_type")
public class EducationTypeEntity extends EntityAudit {
    private static final long serialVersionUID = 2721997797970713112L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "education_type_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "name")
    private String name;

    public EducationType toDomain() {
        return EducationType.builder()
                .id(id)
                .name(name)
                .build();
    }
}
