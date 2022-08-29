package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangeRequestType implements Serializable {
    private static final long serialVersionUID = -4282000227446957351L;

    public static final String ATTESTATION_TYPE = "Developer Attestation Change Request";
    public static final String DEMOGRAPHICS_TYPE = "Developer Demographics Change Request";

    private Long id;
    private String name;

    public Boolean isDemographics() {
        return this.name.equalsIgnoreCase(DEMOGRAPHICS_TYPE);
    }

    public Boolean isAttestation() {
        return this.name.equalsIgnoreCase(ATTESTATION_TYPE);
    }
}
