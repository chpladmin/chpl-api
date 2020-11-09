package gov.healthit.chpl.entity.auth;

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
public class UserContactEntity {

    @Id
    @Column(name = "contact_id")
    @GeneratedValue(strategy  =  GenerationType.IDENTITY)
    private Long id;

    @Basic(optional = false)
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Basic(optional = true)
    @Column(name = "friendly_name", nullable = false)
    private String friendlyName;

    @Basic(optional = false)
    @Column(name = "email", nullable = false)
    private String email;

    @Basic(optional = false)
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "title")
    private String title;

    @Column(name = "signature_date")
    private Date signatureDate;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Column(name = "deleted")
    private Boolean deleted;
}
