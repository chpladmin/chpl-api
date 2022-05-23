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
@Table(name = "change_request_developer_demographic")
public class ChangeRequestDeveloperDemographicsEntity {

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

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
}
