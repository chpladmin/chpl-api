package gov.healthit.chpl.accessibilityStandard;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
