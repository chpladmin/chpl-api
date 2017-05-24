package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.AddressDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class Address implements Serializable {
	private static final long serialVersionUID = 7978604053959537664L;
	
	/**
	 * Address internal ID
	 */
	@XmlElement(required = true)
	private Long addressId;
	
	/**
	 * First line of the address
	 */
	@XmlElement(required = true)
	private String line1;

	/**
	 * Second line of the address, optional
	 */
	@XmlElement(required = false, nillable=true)
	private String line2;
	
	/**
	 * City of the address
	 */
	@XmlElement(required = true)
	private String city;
	
	/**
	 * State of the address
	 */
	@XmlElement(required = true)
	private String state;
	
	/**
	 * Zipcode of the address
	 */
	@XmlElement(required = true)
	private String zipcode;
	
	/**
	 * Country of the address
	 */
	@XmlElement(required = true)
	private String country;
	
	public Address() {}
	public Address(AddressDTO dto) {
		this.addressId = dto.getId();
		this.line1 = dto.getStreetLineOne();
		this.line2 = dto.getStreetLineTwo();
		this.city = dto.getCity();
		this.state = dto.getState();
		this.zipcode = dto.getZipcode();
		this.country = dto.getCountry();
	}
	
	public Long getAddressId() {
		return addressId;
	}
	public void setAddressId(Long addressId) {
		this.addressId = addressId;
	}
	public String getLine1() {
		return line1;
	}
	public void setLine1(String line1) {
		this.line1 = line1;
	}
	public String getLine2() {
		return line2;
	}
	public void setLine2(String line2) {
		this.line2 = line2;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getZipcode() {
		return zipcode;
	}
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	
	
}
