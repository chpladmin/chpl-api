package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.domain.PublicAttestation;
import lombok.Data;

@Data
@Deprecated
public class PublicDeveloperAttestestationResults implements Serializable {
    private static final long serialVersionUID = -1242060777385042489L;

    private List<PublicAttestation> developerAttestations;

    public PublicDeveloperAttestestationResults(List<PublicAttestation> developerAttestations) {
        this.developerAttestations = developerAttestations;
    }
}
