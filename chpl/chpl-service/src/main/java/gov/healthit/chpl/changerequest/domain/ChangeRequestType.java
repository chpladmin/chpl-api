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

    private Long id;
    private String name;

    //TODO - This needs to be changed
    public Boolean isDemographic() {
        return !this.name.equalsIgnoreCase("Developer Attestation Change Request");
    }

    public Boolean isAttestation() {
        return this.name.equalsIgnoreCase("Developer Attestation Change Request");
    }
}
