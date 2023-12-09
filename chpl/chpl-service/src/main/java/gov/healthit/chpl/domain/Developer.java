package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.contact.PointOfContact;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Data
@Builder
public class Developer implements Serializable {
    private static final long serialVersionUID = 7341544844577617247L;

    @Schema(description = "The internal ID of the developer.")
    @JsonAlias("developerId")
    private Long id;

    @Schema(description = "A four-digit code assigned to each developer when it was created.")
    private String developerCode;

    @Schema(description = "The name of the developer or vendor of the certified health IT product. "
            + "If uploading a certified product from a developer that already exists in the CHPL database, "
            + "please use the CHPL Developer management functionality to ensure that the name of the developer matches the "
            + "database record to prevent duplication.")
    private String name;

    @Schema(description = "Website of health IT developer. "
            + "Fully qualified URL which is reachable via web browser validation and verification.")
    private String website;

    @Schema(description = "Indication of whether a health IT developer is a \"self-developer\" or not.")
    private Boolean selfDeveloper;

    @Schema(description = "Developer's physical address")
    private Address address;

    @Schema(description = "Contact information for the developer.")
    private PointOfContact contact;

    private String lastModifiedDate;
    private Boolean deleted;

    @Schema(description = "Status changes that have occurred on the developer.")
    @Builder.Default
    private List<DeveloperStatusEvent> statusEvents = new ArrayList<DeveloperStatusEvent>();

    @Schema(description = "Public attestations submitted by the developer.")
    private List<PublicAttestation> attestations;

    @JsonIgnore
    private String userEnteredName;

    @JsonIgnore
    private String userEnteredWebsite;

    @JsonIgnore
    private String userEnteredSelfDeveloper;

    @JsonIgnore
    private Address userEnteredAddress;

    @JsonIgnore
    private PointOfContact userEnteredPointOfContact;

    public Developer() {
        this.statusEvents = new ArrayList<DeveloperStatusEvent>();
    }

    @Schema(description = "The status of a developer with certified Health IT. Allowable values are \"Active\", \"Suspended by ONC\", or \"Under "
            + "Certification Ban by ONC\"")
    public DeveloperStatus getStatus() {
        if (CollectionUtils.isEmpty(this.getStatusEvents())) {
            return null;
        }

        return getMostRecentStatusEvent().getStatus();
    }

    @JsonIgnore
    public DeveloperStatusEvent getMostRecentStatusEvent() {
        if (CollectionUtils.isEmpty(this.getStatusEvents())) {
            return null;
        }

        DeveloperStatusEvent newest = this.getStatusEvents().get(0);
        for (DeveloperStatusEvent event : this.getStatusEvents()) {
            if (event.getStatusDate().after(newest.getStatusDate())) {
                newest = event;
            }
        }
        return newest;
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
        Developer other = (Developer) obj;
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
        if (website == null) {
            if (other.website != null) {
                return false;
            }
        } else if (!website.equals(other.website)) {
            return false;
        }
        return true;
    }

    private boolean isStatusEventListEqual(List<DeveloperStatusEvent> other) {
        if (statusEvents.size() != other.size()) {
            return false;
        } else {
            // Make copies of both lists and order them
            List<DeveloperStatusEvent> clonedThis = statusEvents.stream()
                    .sorted(Comparator.comparing(DeveloperStatusEvent::getStatusDate))
                    .toList();
            List<DeveloperStatusEvent> clonedOther = other.stream()
                    .sorted(Comparator.comparing(DeveloperStatusEvent::getStatusDate))
                    .toList();
            return clonedThis.equals(clonedOther);
        }
    }
}
