package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.contact.PointOfContact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
public class ChangeRequestDeveloperDetails implements Serializable, ChangeRequestDetails {
    private static final long serialVersionUID = -5572794875421124955L;

    @EqualsAndHashCode.Exclude
    private Long id;

    private Boolean selfDeveloper;
    private Address address;
    private PointOfContact contact;

    public static ChangeRequestDeveloperDetails cast(Object obj) {
        if (obj instanceof ChangeRequestDeveloperDetails) {
            return (ChangeRequestDeveloperDetails) obj;
        } else {
            throw new RuntimeException("Could not cast object as type ChangeRequestDeveloperDetails");
        }
    }
}
