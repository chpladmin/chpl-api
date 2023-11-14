package gov.healthit.chpl.changerequest.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "change_request_developer_demographics")
public class ChangeRequestDeveloperDemographicsEntity extends EntityAudit {
    private static final long serialVersionUID = 8335139406667596689L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_id", nullable = false, insertable = true,
            updatable = false)
    private ChangeRequestEntity changeRequest;

    @Column(name = "self_developer", nullable = false)
    private Boolean selfDeveloper;

    @Column(name = "website", nullable = false)
    private String website;

    @Column(name = "street_line_1", nullable = false)
    private String streetLine1;

    @Column(name = "street_line_2", nullable = false)
    private String streetLine2;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "zipcode", nullable = false)
    private String zipcode;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "full_name", nullable = false)
    private String contactFullName;

    @Column(name = "email", nullable = false)
    private String contactEmail;

    @Column(name = "phone_number", nullable = false)
    private String contactPhoneNumber;

    @Column(name = "title", nullable = false)
    private String contactTitle;

}
