package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.attestation.domain.DeveloperAttestationSubmission;
import lombok.Data;

@Data
public class DeveloperAttestationSubmissionResults implements Serializable {
    private static final long serialVersionUID = -3345114923797354483L;

    private List<DeveloperAttestationSubmission> developerAttestations;

    public DeveloperAttestationSubmissionResults(List<DeveloperAttestationSubmission> developerAttestations) {
        this.developerAttestations = developerAttestations;
    }
}
