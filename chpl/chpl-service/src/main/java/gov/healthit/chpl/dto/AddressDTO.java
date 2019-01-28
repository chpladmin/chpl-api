package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.util.Util;

public class AddressDTO implements Serializable {
    private static final long serialVersionUID = -5340045233309684152L;
    private Long id;
    private String streetLineOne;
    private String streetLineTwo;
    private String city;
    private String state;
    private String zipcode;
    private String country;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public AddressDTO() {
    }

    public AddressDTO(AddressEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.streetLineOne = entity.getStreetLineOne();
            this.streetLineTwo = entity.getStreetLineTwo();
            this.city = entity.getCity();
            this.state = entity.getState();
            this.zipcode = entity.getZipcode();
            this.country = entity.getCountry();
            this.creationDate = entity.getCreationDate();
            this.deleted = entity.getDeleted();
            this.lastModifiedDate = entity.getLastModifiedDate();
            this.lastModifiedUser = entity.getLastModifiedUser();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
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

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(final String zipcode) {
        this.zipcode = zipcode;
    }
}
