package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.OrganizationDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Organization implements Serializable {
    private static final long serialVersionUID = -5910873076481736684L;

    private Long id;
    private String name;

    public Organization(final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Organization(final OrganizationDTO org) {
        this.id = org.getId();
        this.name = org.getName();
    }
}
