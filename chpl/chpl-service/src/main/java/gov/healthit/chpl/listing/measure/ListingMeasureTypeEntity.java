package gov.healthit.chpl.listing.measure;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.domain.MeasureType;
import lombok.Data;

@Entity
@Data
@Table(name = "measure_type")
public class ListingMeasureTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    private Date creationDate;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "last_modified_date", nullable = false, updatable = false, insertable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public MeasureType convert() {
        MeasureType type = new MeasureType();
        type.setId(getId());
        type.setName(getName());
        return type;
    }
}
