package gov.healthit.chpl.changerequest.entity;

import java.util.Date;

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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "change_request_developer_demogrpahics")
public class ChangeRequestDeveloperDemographicsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_id", nullable = false, insertable = true,
            updatable = false)
    private ChangeRequestEntity changeRequest;

    @Basic(optional = true)
    @Column(name = "self_developer", nullable = false)
    private Boolean selfDeveloper;

    @Basic(optional = true)
    @Column(name = "website", nullable = false)
    private String website;

    @Basic(optional = true)
    @Column(name = "street_line_1", nullable = false)
    private String streetLine1;

    @Basic(optional = true)
    @Column(name = "street_line_2", nullable = false)
    private String streetLine2;

    @Basic(optional = true)
    @Column(name = "city", nullable = false)
    private String city;

    @Basic(optional = true)
    @Column(name = "state", nullable = false)
    private String state;

    @Basic(optional = true)
    @Column(name = "zipcode", nullable = false)
    private String zipcode;

    @Basic(optional = true)
    @Column(name = "country", nullable = false)
    private String country;

    @Basic(optional = true)
    @Column(name = "full_name", nullable = false)
    private String contactFullName;

    @Basic(optional = true)
    @Column(name = "email", nullable = false)
    private String contactEmail;

    @Basic(optional = true)
    @Column(name = "phone_number", nullable = false)
    private String contactPhoneNumber;

    @Basic(optional = true)
    @Column(name = "title", nullable = false)
    private String contactTitle;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
}
