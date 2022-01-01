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

import gov.healthit.chpl.domain.contact.PointOfContact;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "contact")
public class ContactEntity implements Serializable {
    private static final long serialVersionUID = 1586086005459839264L;
    private static final int EMAIL_LENGTH = 250;
    private static final int FULL_NAME_LENGTH = 500;
    private static final int PHONE_LENGTH = 50;
    private static final int TITLE_LENGTH = 250;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "contact_id", nullable = false)
    private Long id;

    @Basic(optional = true)
    @Column(length = EMAIL_LENGTH)
    private String email;

    @Basic(optional = false)
    @Column(name = "full_name", nullable = false, length = FULL_NAME_LENGTH)
    private String fullName;

    @Basic(optional = true)
    @Column(name = "phone_number", length = PHONE_LENGTH)
    private String phoneNumber;

    @Basic(optional = true)
    @Column(name = "signature_date")
    private Date signatureDate;

    @Basic(optional = true)
    @Column(length = TITLE_LENGTH)
    private String title;

    @Column(name = "deleted", insertable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    public PointOfContact toDomain() {
        return PointOfContact.builder()
                .contactId(this.getId())
                .email(this.getEmail())
                .fullName(this.getFullName())
                .phoneNumber(this.getPhoneNumber())
                .title(this.getTitle())
        .build();
    }
}
