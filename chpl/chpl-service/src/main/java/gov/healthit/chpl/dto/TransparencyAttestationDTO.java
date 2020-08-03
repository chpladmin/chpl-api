package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.domain.TransparencyAttestation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TransparencyAttestationDTO implements Serializable {
    private static final long serialVersionUID = 3828311869155691347L;

    private String transparencyAttestation;
    private Boolean removed;

    public TransparencyAttestationDTO() {
        this.removed = true;
    }

    public TransparencyAttestationDTO(String attestation) {
        this();
        this.transparencyAttestation = attestation;
    }

    public TransparencyAttestationDTO(TransparencyAttestation domain) {
        this.transparencyAttestation = domain.getTransparencyAttestation();
        this.removed = domain.getRemoved();
    }

    // Not all attributes have been included. The attributes being used were selected so the DeveloperManager could
    // determine equality when updating a Developer
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TransparencyAttestationDTO other = (TransparencyAttestationDTO) obj;
        if (transparencyAttestation == null) {
            if (other.transparencyAttestation != null) {
                return false;
            }
        } else if (!transparencyAttestation.equals(other.transparencyAttestation)) {
            return false;
        }
        return true;
    }

    // Not all attributes have been included. The attributes being used were selected so the DeveloperManager could
    // determine equality when updating a Developer
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((transparencyAttestation == null) ? 0 : transparencyAttestation.hashCode());
        return result;
    }
}
