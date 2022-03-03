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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.contact.PointOfContact;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Immutable
@NoArgsConstructor
@Getter
@Setter
@Table(name = "vendor")
public class DeveloperEntitySimple implements Serializable {

    private static final long serialVersionUID = -1396979119499564864L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "vendor_id", nullable = false)
    private Long id;

    @Column(name = "vendor_code", insertable = false, updatable = false)
    private String developerCode;

    @Column(name = "name")
    private String name;

    @Basic(optional = true)
    @Column(length = 300, nullable = true)
    private String website;

    @Column(name="self_developer")
    private Boolean selfDeveloper;

    @Basic(optional = true)
    @Column(name = "contact_id")
    private Long contactId;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @NotNull
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public Developer toDomain() {
        return Developer.builder()
                .id(getId())
                .developerId(getId())
                .name(getName())
                .developerCode(getDeveloperCode())
                .website(getWebsite())
                .selfDeveloper(getSelfDeveloper())
                .lastModifiedDate(getLastModifiedDate().getTime() + "")
                .deleted(getDeleted())
                .contact(PointOfContact.builder()
                        .contactId(getContactId())
                        .build())
                .build();
    }

}
