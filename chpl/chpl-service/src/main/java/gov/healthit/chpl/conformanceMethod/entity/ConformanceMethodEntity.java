package gov.healthit.chpl.conformanceMethod.entity;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import lombok.Data;

@Entity
@Data
@Table(name = "conformance_method")
public class ConformanceMethodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "name", nullable = false)
    private String name;

    @Basic(optional = true)
    @Column(name = "removal_date", nullable = true)
    private LocalDate removalDate;

    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    private Date creationDate;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "last_modified_date", nullable = false, updatable = false, insertable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public ConformanceMethod toDomain() {
        return ConformanceMethod.builder()
                .id(this.getId())
                .name(this.getName())
                .removalDate(this.getRemovalDate())
                .build();
    }
}
