package gov.healthit.chpl.form.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.form.SectionHeading;
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
@Table(name = "section_heading")
public class SectionHeadingEntity extends EntityAudit {
    private static final long serialVersionUID = -6428592182591014846L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public SectionHeading toDomain() {
        return SectionHeading.builder()
                .id(id)
                .name(name)
                .sortOrder(sortOrder)
                .build();
    }
}
