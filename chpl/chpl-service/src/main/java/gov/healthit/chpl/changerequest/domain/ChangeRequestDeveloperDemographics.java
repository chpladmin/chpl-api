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
public class ChangeRequestDeveloperDemographics implements Serializable, ChangeRequestDetails {
    private static final long serialVersionUID = -5572794875421124955L;

    @EqualsAndHashCode.Exclude
    private Long id;

    private Boolean selfDeveloper;
    private Address address;
    private PointOfContact contact;
    private String website;

    public static ChangeRequestDeveloperDemographics cast(Object obj) {
        if (obj instanceof ChangeRequestDeveloperDemographics) {
            return (ChangeRequestDeveloperDemographics) obj;
        } else {
            throw new RuntimeException("Could not cast object as type ChangeRequestDeveloperDemographics");
        }
    }

    @Override
    public boolean matches(Object obj) {
        return equals(obj);
    }
}
