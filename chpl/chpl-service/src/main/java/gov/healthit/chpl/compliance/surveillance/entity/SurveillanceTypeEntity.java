package gov.healthit.chpl.compliance.surveillance.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import lombok.Data;

@Entity
@Data
@Table(name = "surveillance_type")
public class SurveillanceTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public SurveillanceType buildSurveillanceType() {
        SurveillanceType st = new SurveillanceType();
        st.setId(this.getId());
        st.setName(this.getName());
        return st;
    }
}
