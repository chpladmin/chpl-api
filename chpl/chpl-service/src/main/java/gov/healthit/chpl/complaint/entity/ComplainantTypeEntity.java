package gov.healthit.chpl.complaint.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.complaint.domain.ComplainantType;
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
@Table(name = "complainant_type")
public class ComplainantTypeEntity extends EntityAudit {
    private static final long serialVersionUID = 9116199957815515032L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "complainant_type_id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    public ComplainantType buildComplainantType() {
        return ComplainantType.builder()
            .id(this.getId())
            .name(this.getName())
            .build();
    }
}
