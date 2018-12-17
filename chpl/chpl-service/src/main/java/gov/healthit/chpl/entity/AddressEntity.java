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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import gov.healthit.chpl.util.Util;

@Entity
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

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @NotNull()
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @NotNull()
    @Column(nullable = false)
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long long1) {
        this.id = long1;
    }

    public String getStreetLineOne() {
        return streetLineOne;
    }

    public void setStreetLineOne(final String streetLineOne) {
        this.streetLineOne = streetLineOne;
    }

    public String getStreetLineTwo() {
        return streetLineTwo;
    }

    public void setStreetLineTwo(final String streetLineTwo) {
        this.streetLineTwo = streetLineTwo;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(final String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

}
