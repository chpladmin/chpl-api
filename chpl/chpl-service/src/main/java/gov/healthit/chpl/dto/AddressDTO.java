package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.AddressEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
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

    // Not all attributes have been included. The attributes being used were selected so the DeveloperManager could
    // determine equality when updating a Developer
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((city == null) ? 0 : city.hashCode());
        result = prime * result + ((country == null) ? 0 : country.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((streetLineOne == null) ? 0 : streetLineOne.hashCode());
        result = prime * result + ((streetLineTwo == null) ? 0 : streetLineTwo.hashCode());
        result = prime * result + ((zipcode == null) ? 0 : zipcode.hashCode());
        return result;
    }

    // Not all attributes have been included. The attributes being used were selected so the DeveloperManager could
    // determine equality when updating a Developer
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AddressDTO other = (AddressDTO) obj;
        if (city == null) {
            if (other.city != null) {
                return false;
            }
        } else if (!city.equals(other.city)) {
            return false;
        }
        if (country == null) {
            if (other.country != null) {
                return false;
            }
        } else if (!country.equals(other.country)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (!state.equals(other.state)) {
            return false;
        }
        if (streetLineOne == null) {
            if (other.streetLineOne != null) {
                return false;
            }
        } else if (!streetLineOne.equals(other.streetLineOne)) {
            return false;
        }
        if (streetLineTwo == null) {
            if (other.streetLineTwo != null) {
                return false;
            }
        } else if (!streetLineTwo.equals(other.streetLineTwo)) {
            return false;
        }
        if (zipcode == null) {
            if (other.zipcode != null) {
                return false;
            }
        } else if (!zipcode.equals(other.zipcode)) {
            return false;
        }
        return true;
    }
}
