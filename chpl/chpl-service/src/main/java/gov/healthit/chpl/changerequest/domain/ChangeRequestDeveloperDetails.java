package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Contact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public @Data class ChangeRequestDeveloperDetails implements Serializable {
    private static final long serialVersionUID = -5572794875421124955L;

    private Long id;
    private Boolean selfDeveloper;
    private Address address;
    private Contact contact;

}
