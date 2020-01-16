package gov.healthit.chpl.dto;

import java.io.Serializable;

import org.ff4j.FF4j;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.SpringContext;
import gov.healthit.chpl.domain.TransparencyAttestation;

public class TransparencyAttestationDTO implements Serializable {
    private static final long serialVersionUID = 3828311869155691347L;

    private String transparencyAttestation;
    private Boolean removed;

    public TransparencyAttestationDTO() {
        // This is a temporary bad solution, will be removed when flag is removed
        if (SpringContext.getBean(FF4j.class).check(FeatureList.EFFECTIVE_RULE_DATE)) {
            this.removed = true;
        } else {
            this.removed = false;
        }
    }

    public TransparencyAttestationDTO(String attestation) {
        this();
        this.transparencyAttestation = attestation;
    }

    public TransparencyAttestationDTO(TransparencyAttestation domain) {
        this.transparencyAttestation = domain.getTransparencyAttestation();
        this.removed = domain.getRemoved();
    }

    public String getTransparencyAttestation() {
        return transparencyAttestation;
    }

    public void setTransparencyAttestation(String transparencyAttestation) {
        this.transparencyAttestation = transparencyAttestation;
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

}
