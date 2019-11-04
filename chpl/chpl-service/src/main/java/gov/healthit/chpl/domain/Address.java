package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.entity.AddressEntity;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class Address implements Serializable {
    private static final long serialVersionUID = 7978604053959537664L;

    /**
     * Address internal ID.
     */
    @XmlElement(required = true)
    private Long addressId;

    /**
     * First line of the street address of the health IT developer for 2014 and
     * 2015 Edition. This variable is a string variable that does not have any
     * restrictions on formatting or values.
     */
    @XmlElement(required = true)
    private String line1;

    /**
     * Second line of the street address of the health IT developer for 2014 and
     * 2015 Edition. This variable is a string variable that does not have any
     * restrictions on formatting or values. This variable is optional.
     */
    @XmlElement(required = false, nillable = true)
    private String line2;

    /**
     * The city in which the health IT developer can be contacted. This variable
     * is for 2014 and 2015 Edition, and a string variable that does not have
     * any restrictions on formatting or values.
     */
    @XmlElement(required = true)
    private String city;

    /**
     * The state in which the health IT developer can be contacted. This
     * variable is applicable for 2014 and 2015 Edition and is a string variable
     * that does not have any restrictions on formatting or values. Name of
     * states are denoted with two-letter abbreviation.
     */
    @XmlElement(required = true)
    private String state;

    /**
     * The ZIP code at which the health IT developer can be contacted. This is a
     * variable applicable for 2014 and 2015 Edition, and a string variable that
     * does not have any restrictions on formatting or values.
     */
    @XmlElement(required = true)
    private String zipcode;

    /**
     * Country of the address
     */
    @XmlElement(required = true)
    private String country;

    public Address() {
    }

    public Address(AddressDTO dto) {
        this.addressId = dto.getId();
        this.line1 = dto.getStreetLineOne();
        this.line2 = dto.getStreetLineTwo();
        this.city = dto.getCity();
        this.state = dto.getState();
        this.zipcode = dto.getZipcode();
        this.country = dto.getCountry();
    }

    public Address(AddressEntity entity) {
        this.addressId = entity.getId();
        this.line1 = entity.getStreetLineOne();
        this.line2 = entity.getStreetLineTwo();
        this.city = entity.getCity();
        this.state = entity.getState();
        this.zipcode = entity.getZipcode();
        this.country = entity.getCountry();
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(final Long addressId) {
        this.addressId = addressId;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(final String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(final String line2) {
        this.line2 = line2;
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

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(final String zipcode) {
        this.zipcode = zipcode;
    }

}
