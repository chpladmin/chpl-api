package gov.healthit.chpl.developer.search;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class DeveloperSearchResult implements Serializable {

    private static final long serialVersionUID = -2547390625364841038L;

    private Long id;
    private String code;
    private String name;
    private Address address;
    private PointOfContact contact;
    private IdNamePair status;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate decertificationDate;

    private Set<IdNamePair> associatedAcbs;

    public DeveloperSearchResult() {
        associatedAcbs = new HashSet<IdNamePair>();
    }

    @Override
    public boolean equals(Object another) {
        if (another == null) {
            return false;
        }
        if (!(another instanceof DeveloperSearchResult)) {
            return false;
        }
        DeveloperSearchResult anotherSearchResult = (DeveloperSearchResult) another;
        if (ObjectUtils.allNotNull(this, anotherSearchResult, this.getId(), anotherSearchResult.getId())) {
            return Objects.equals(this.getId(), anotherSearchResult.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return -1;
        }
        return this.getId().hashCode();
    }
}
