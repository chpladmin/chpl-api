package gov.healthit.chpl.entity.auth;

import java.util.Date;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
@Table(name = "contact")
public class UserContactEntity extends EntityAudit {
    private static final long serialVersionUID = 4455611185793875304L;

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

}
