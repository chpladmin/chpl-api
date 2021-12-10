package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsExclude;

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

    @EqualsExclude
    private Long id;

    private Boolean selfDeveloper;
    private Address address;
    private PointOfContact contact;

    @Override
    public boolean isEqual(Object obj) {
        return equals(obj);
    }
}
