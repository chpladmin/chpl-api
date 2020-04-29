package gov.healthit.chpl.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.developer.DeveloperACBMapEntity;
import gov.healthit.chpl.entity.developer.DeveloperACBTransparencyMapEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperACBMapDTO implements Serializable {
    private static final long serialVersionUID = -1860729017532925654L;
    private Long id;
    private Long developerId;
    private Long acbId;
    private String acbName;
    private TransparencyAttestationDTO transparencyAttestation;

    public DeveloperACBMapDTO(DeveloperACBMapEntity entity) {
        this.id = entity.getId();
        this.developerId = entity.getDeveloperId();
        this.acbId = entity.getCertificationBodyId();
        if (entity.getTransparencyAttestation() != null) {
            this.transparencyAttestation = new TransparencyAttestationDTO(entity.getTransparencyAttestation().toString());
        }
        if (entity.getCertificationBody() != null) {
            this.acbName = entity.getCertificationBody().getName();
        }
    }

    public DeveloperACBMapDTO(DeveloperACBTransparencyMapEntity entity) {
        this.id = entity.getId();
        this.developerId = entity.getDeveloperId();
        this.acbId = entity.getCertificationBodyId();
        if (entity.getTransparencyAttestation() != null) {
            this.transparencyAttestation = new TransparencyAttestationDTO(entity.getTransparencyAttestation().toString());
        }
        this.acbName = entity.getAcbName();
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
        DeveloperACBMapDTO other = (DeveloperACBMapDTO) obj;
        if (acbName == null) {
            if (other.acbName != null) {
                return false;
            }
        } else if (!acbName.equals(other.acbName)) {
            return false;
        }
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
        result = prime * result + ((acbName == null) ? 0 : acbName.hashCode());
        result = prime * result + ((transparencyAttestation == null) ? 0 : transparencyAttestation.hashCode());
        return result;
    }

}
