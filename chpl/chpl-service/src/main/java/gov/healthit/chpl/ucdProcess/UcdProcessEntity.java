package gov.healthit.chpl.ucdProcess;

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
@Table(name = "ucd_process")
public class UcdProcessEntity extends EntityAudit {
    private static final long serialVersionUID = -8588994880748379730L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ucd_process_id")
    private Long id;

    @Column(name = "name")
    private String name;

    public UcdProcess toDomain() {
        return UcdProcess.builder()
                .id(getId())
                .name(getName())
                .build();
    }
}
