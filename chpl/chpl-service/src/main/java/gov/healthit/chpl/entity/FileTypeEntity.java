package gov.healthit.chpl.entity;

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
@Table(name = "file_type")
public class FileTypeEntity extends EntityAudit {
    private static final long serialVersionUID = 295190289181614799L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "file_type_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "name")
    private String name;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "description")
    private String description;
}
