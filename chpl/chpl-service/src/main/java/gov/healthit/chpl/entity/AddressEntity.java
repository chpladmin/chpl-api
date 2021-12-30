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
import javax.validation.constraints.Size;

import gov.healthit.chpl.domain.Address;
import lombok.Data;

@Entity
@Data
@Table(name = "address")
public class AddressEntity implements Serializable {
    private static final long serialVersionUID = 762431901700320834L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "address_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "street_line_1")
    private String streetLineOne;

    @Column(name = "street_line_2")
    private String streetLineTwo;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "city")
    private String city;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "state")
    private String state;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "zipcode")
    private String zipcode;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "country")
    private String country;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted")
    private Boolean deleted;

    public Address toDomain() {
        return Address.builder()
        .addressId(this.getId())
        .city(this.getCity())
        .country(this.getCountry())
        .line1(this.getStreetLineOne())
        .line2(this.getStreetLineTwo())
        .state(this.getState())
        .zipcode(this.getZipcode())
        .build();
    }
}
