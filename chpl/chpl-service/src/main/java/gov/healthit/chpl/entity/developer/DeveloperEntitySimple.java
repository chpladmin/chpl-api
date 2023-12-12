package gov.healthit.chpl.entity.developer;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.contact.PointOfContact;
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
@Immutable
@Table(name = "vendor")
public class DeveloperEntitySimple extends EntityAudit {
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

    public Developer toDomain() {
        return Developer.builder()
                .id(getId())
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
