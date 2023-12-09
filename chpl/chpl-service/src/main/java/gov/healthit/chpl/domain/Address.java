package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Log4j2
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Address implements Serializable {
    private static final long serialVersionUID = 7978604053959537664L;
    @JsonIgnore
    public static final String DEFAULT_COUNTRY = "US";

    @Schema(description = "Address internal ID.")
    private Long addressId;

    @Schema(description = "First line of the street address of the health IT developer. "
            + "This is a string variable that does not have any restrictions on formatting or values..")
    private String line1;

    @Schema(description = "Second line of the street address of the health IT. "
            + "This is a string variable that does not have any restrictions on formatting or values. "
            + "This variable is optional.")
    private String line2;

    @Schema(description = "The city in which the health IT developer can be contacted. "
            + "This is a string variable that does not have any restrictions on formatting or values.")
    private String city;

    @Schema(description = "The state in which the health IT developer can be contacted. "
            + "This is a string variable that does not have any restrictions on formatting or values. "
            + "Name of states are denoted with two-letter abbreviation.")
    private String state;

    @Schema(description = "The ZIP code at which the health IT developer can be contacted. "
            + "This is a string variable that does not have any restrictions on formatting or values.")
    private String zipcode;

    @Schema(description = "Country of the address")
    private String country;

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
}
