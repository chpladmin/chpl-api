package gov.healthit.chpl.entity.developer;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import gov.healthit.chpl.domain.DeveloperStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "vendor_status")
public class DeveloperStatusEntity implements Serializable {
    private static final long serialVersionUID = 1730728043307135377L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "vendor_status_id", nullable = false)
    private Long id;

    @Column(name = "name")
    @Type(type = "gov.healthit.chpl.entity.developer.PostgresDeveloperStatusType", parameters = {
            @org.hibernate.annotations.Parameter(name = "enumClassName",
                    value = "gov.healthit.chpl.entity.developer.DeveloperStatusType")
    })
    private DeveloperStatusType name;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public DeveloperStatus toDomain() {
        return DeveloperStatus.builder()
                .id(this.getId())
                .status(this.getName().getName())
                .build();
    }
}
