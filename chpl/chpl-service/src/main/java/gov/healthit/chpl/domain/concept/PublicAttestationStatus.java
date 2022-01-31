package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

public enum PublicAttestationStatus implements Serializable {
    ATTESTATIONS_SUBMITTED("Attestations Submitted");

    private String name;

    PublicAttestationStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
