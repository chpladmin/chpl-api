package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Log4j2
@AllArgsConstructor
public class Address implements Serializable {
    private static final long serialVersionUID = 7978604053959537664L;
    @JsonIgnore
    public static final String DEFAULT_COUNTRY = "US";

    @Schema(description = "Address internal ID.")
    private Long addressId;

    @Schema(description = "First line of the street address of the health IT developer for 2014 and "
            + "2015 Edition. This variable is a string variable that does not have any "
            + "restrictions on formatting or values.")
    private String line1;

    @Schema(description = "Second line of the street address of the health IT developer for 2014 and "
            + "2015 Edition. This variable is a string variable that does not have any "
            + "restrictions on formatting or values. This variable is optional.")
    private String line2;

    @Schema(description = "The city in which the health IT developer can be contacted. This variable "
            + "is for 2014 and 2015 Edition, and a string variable that does not have "
            + "any restrictions on formatting or values.")
    private String city;

    @Schema(description = "The state in which the health IT developer can be contacted. This "
            + "variable is applicable for 2014 and 2015 Edition and is a string variable "
            + "that does not have any restrictions on formatting or values. Name of "
            + "states are denoted with two-letter abbreviation.")
    private String state;

    @Schema(description = "The ZIP code at which the health IT developer can be contacted. This is a "
            + "variable applicable for 2014 and 2015 Edition, and a string variable that "
            + "does not have any restrictions on formatting or values.")
    private String zipcode;

    @Schema(description = "Country of the address")
    private String country;

    public Address() {
    }

    public Address(HashMap<String, Object> map) {
        if (map.containsKey("addressId") && map.get("addressId") != null) {
            try {
                this.addressId = Long.parseLong(map.get("addressId").toString());
            } catch (NumberFormatException ex) {
                LOGGER.warn("addressId in map = '" + map.get("addressId") + "' is not parseable into a Long");
            }
        }
        if (map.containsKey("line1") && map.get("line1") != null) {
            this.line1 = map.get("line1").toString();
        }
        if (map.containsKey("line2") && map.get("line2") != null) {
            this.line2 = map.get("line2").toString();
        }
        if (map.containsKey("city") && map.get("city") != null) {
            this.city = map.get("city").toString();
        }
        if (map.containsKey("state") && map.get("state") != null) {
            this.state = map.get("state").toString();
        }
        if (map.containsKey("zipcode") && map.get("zipcode") != null) {
            this.zipcode = map.get("zipcode").toString();
        }
        if (map.containsKey("country") && map.get("country") != null) {
            this.country = map.get("country").toString();
        }
    }

    public void normalizeSpaces() {
        this.line1 = StringUtils.normalizeSpace(this.getLine1());
        this.line2 = StringUtils.normalizeSpace(this.getLine2());
        this.city = StringUtils.normalizeSpace(this.getCity());
        this.state = StringUtils.normalizeSpace(this.getState());
        this.zipcode = StringUtils.normalizeSpace(this.getZipcode());
        this.country = StringUtils.normalizeSpace(this.getCountry());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Address)) {
            return false;
        }
        Address anotherAddress = (Address) obj;
        return (StringUtils.isAllEmpty(this.line1, anotherAddress.line1)
                    || StringUtils.equals(this.line1, anotherAddress.line1))
                && (StringUtils.isAllEmpty(this.line2, anotherAddress.line2)
                    || StringUtils.equals(this.line2, anotherAddress.line2))
                && (StringUtils.isAllEmpty(this.city, anotherAddress.city)
                    || StringUtils.equals(this.city, anotherAddress.city))
                && (StringUtils.isAllEmpty(this.state, anotherAddress.state)
                    || StringUtils.equals(this.state, anotherAddress.state))
                && (StringUtils.isAllEmpty(this.zipcode, anotherAddress.zipcode)
                    || StringUtils.equals(this.zipcode, anotherAddress.zipcode))
                && (StringUtils.isAllEmpty(this.country, anotherAddress.country)
                    || StringUtils.equals(this.country, anotherAddress.country));
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        if (!StringUtils.isEmpty(this.line1)) {
            hashCode += this.line1.hashCode();
        }
        if (!StringUtils.isEmpty(this.line2)) {
            hashCode += this.line2.hashCode();
        }
        if (!StringUtils.isEmpty(this.city)) {
            hashCode += this.city.hashCode();
        }
        if (!StringUtils.isEmpty(this.state)) {
            hashCode += this.state.hashCode();
        }
        if (!StringUtils.isEmpty(this.zipcode)) {
            hashCode += this.zipcode.hashCode();
        }
        if (!StringUtils.isEmpty(this.country)) {
            hashCode += this.country.hashCode();
        }
        return hashCode;
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
