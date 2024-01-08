package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.domain.contact.PointOfContact;
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
@Table(name = "contact")
public class ContactEntity extends EntityAudit {
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
