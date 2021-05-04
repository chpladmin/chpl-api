package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.domain.complaint.ComplainantType;
import lombok.Data;

@Entity
@Table(name = "complainant_type")
@Data
public class ComplainantTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "complainant_type_id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    public ComplainantType buildComplainantType() {
        return ComplainantType.builder()
            .id(this.getId())
            .name(this.getName())
            .build();
    }
}
