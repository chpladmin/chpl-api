package gov.healthit.chpl.ucdProcess;

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
