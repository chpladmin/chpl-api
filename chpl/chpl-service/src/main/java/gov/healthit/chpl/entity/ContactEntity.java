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

    @Basic(optional = false)
    @Column(nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;
}
