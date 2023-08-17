package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.domain.PracticeType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "practice_type", schema = "openchpl")
public class PracticeTypeEntity implements Serializable {

    /** Serial Version UID. */
    private static final long serialVersionUID = -512191905822294896L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "practice_type_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(nullable = false, length = 250)
    private String description;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    public PracticeType toDomain() {
        return PracticeType.builder()
                .id(this.getId())
                //.creationDate(this.getCreationDate())
                //.deleted(this.getDeleted())
                .description(this.getDescription())
                //.lastModifiedDate(this.getLastModifiedDate())
                //.lastModifiedUser(this.getLastModifiedUser())
                .name(this.getName())
                .build();
    }

}
