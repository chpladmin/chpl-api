package gov.healthit.chpl.accessibilityStandard;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
@Table(name = "accessibility_standard")
public class AccessibilityStandardEntity extends EntityAudit {
    private static final long serialVersionUID = -2059681790037353206L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "accessibility_standard_id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "name", nullable = false)
    private String name;

    public AccessibilityStandard toDomain() {
        return AccessibilityStandard.builder()
                .id(getId())
                .name(getName())
                .build();
    }
}
