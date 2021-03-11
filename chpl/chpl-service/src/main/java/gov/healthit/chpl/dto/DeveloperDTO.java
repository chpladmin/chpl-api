package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.Statuses;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DeveloperDTO implements Serializable {

    private static final long serialVersionUID = -2492373079266782228L;
    private String developerCode;
    private Long id;
    private AddressDTO address;
    private ContactDTO contact;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private String name;
    private String website;
    private Boolean selfDeveloper;
    private List<DeveloperStatusEventDTO> statusEvents;
    private List<DeveloperACBMapDTO> transparencyAttestationMappings;
    private Statuses statuses;

    public DeveloperDTO() {
        this.transparencyAttestationMappings = new ArrayList<DeveloperACBMapDTO>();
        this.statusEvents = new ArrayList<DeveloperStatusEventDTO>();
    }

    public DeveloperStatusEventDTO getStatus() {
        DeveloperStatusEventDTO mostRecentStatus = null;

        if (getStatusEvents() != null && getStatusEvents().size() > 0) {
            for (DeveloperStatusEventDTO currStatusHistory : getStatusEvents()) {
                if (mostRecentStatus == null) {
                    mostRecentStatus = currStatusHistory;
                } else {
                    if (currStatusHistory.getStatusDate().getTime() > mostRecentStatus.getStatusDate().getTime()) {
                        mostRecentStatus = currStatusHistory;
                    }
                }
            }
        }
        return mostRecentStatus;
    }

    // Not all attributes have been included. The attributes being used were selected so the DeveloperManager could
    // determine equality when updating a Developer
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((contact == null) ? 0 : contact.hashCode());
        result = prime * result + ((developerCode == null) ? 0 : developerCode.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((selfDeveloper == null) ? 0 : selfDeveloper.hashCode());
        result = prime * result + ((statusEvents == null) ? 0 : statusEvents.hashCode());
        result = prime * result + ((transparencyAttestationMappings == null) ? 0 : transparencyAttestationMappings.hashCode());
        result = prime * result + ((website == null) ? 0 : website.hashCode());
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
        DeveloperDTO other = (DeveloperDTO) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        if (contact == null) {
            if (other.contact != null) {
                return false;
            }
        } else if (!contact.equals(other.contact)) {
            return false;
        }
        if (developerCode == null) {
            if (other.developerCode != null) {
                return false;
            }
        } else if (!developerCode.equals(other.developerCode)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (selfDeveloper == null) {
            if (other.selfDeveloper != null) {
                return false;
            }
        } else if (!selfDeveloper.equals(other.selfDeveloper)) {
            return false;
        }
        if (statusEvents == null) {
            if (other.statusEvents != null) {
                return false;
            }
        } else if (!isStatusEventListEqual(other.statusEvents)) {
            return false;
        }
        if (transparencyAttestationMappings == null) {
            if (other.transparencyAttestationMappings != null) {
                return false;
            }
        } else if (!isTransparencyAttestationMappingsEqual(other.transparencyAttestationMappings)) {
            return false;
        }
        if (website == null) {
            if (other.website != null) {
                return false;
            }
        } else if (!website.equals(other.website)) {
            return false;
        }
        return true;
    }

    private boolean isStatusEventListEqual(List<DeveloperStatusEventDTO> other) {
        if (statusEvents.size() != other.size()) {
            return false;
        } else {
            // Make copies of both lists and order them
            List<DeveloperStatusEventDTO> clonedThis = statusEvents.stream()
                    .sorted(Comparator.comparing(DeveloperStatusEventDTO::getStatusDate))
                    .collect(Collectors.toList());
            List<DeveloperStatusEventDTO> clonedOther = other.stream()
                    .sorted(Comparator.comparing(DeveloperStatusEventDTO::getStatusDate))
                    .collect(Collectors.toList());
            return clonedThis.equals(clonedOther);
        }
    }

    private boolean isTransparencyAttestationMappingsEqual(List<DeveloperACBMapDTO> other) {
        if (transparencyAttestationMappings.size() != other.size()) {
            return false;
        } else {
            // Make copies of both lists and order them
            List<DeveloperACBMapDTO> clonedThis = transparencyAttestationMappings.stream()
                    .sorted(Comparator.comparing(DeveloperACBMapDTO::getAcbName))
                    .collect(Collectors.toList());
            List<DeveloperACBMapDTO> clonedOther = other.stream()
                    .sorted(Comparator.comparing(DeveloperACBMapDTO::getAcbName))
                    .collect(Collectors.toList());
            return clonedThis.equals(clonedOther);
        }
    }
}
