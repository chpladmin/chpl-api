package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum PublicAttestationStatus implements Serializable {
    ATTESTATIONS_SUBMITTED("Attestations submitted"),
    NO_ATTESTATIONS_SUBMITTED("No Attestations submitted");

    private String name;

    PublicAttestationStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
