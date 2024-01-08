package gov.healthit.chpl.qmsStandard;

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
@Table(name = "qms_standard")
public class QmsStandardEntity extends EntityAudit {
    private static final long serialVersionUID = -2592857877389323076L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qms_standard_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    public QmsStandard toDomain() {
        return QmsStandard.builder()
                .id(this.getId())
                .name(this.getName())
                .build();
    }
}
