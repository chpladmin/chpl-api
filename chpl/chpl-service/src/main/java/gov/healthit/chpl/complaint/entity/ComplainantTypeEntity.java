package gov.healthit.chpl.complaint.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
