package gov.healthit.chpl.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.developer.DeveloperACBMapEntity;
import gov.healthit.chpl.entity.developer.DeveloperACBTransparencyMapEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeveloperACBMapDTO implements Serializable {
    private static final long serialVersionUID = -1860729017532925654L;
    private Long id;
    private Long developerId;
    private Long acbId;
    private String acbName;
    private TransparencyAttestationDTO transparencyAttestation;

    public DeveloperACBMapDTO() {
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(Long developerId) {
        this.developerId = developerId;
    }

    public Long getAcbId() {
        return acbId;
    }

    public void setAcbId(final Long acbId) {
        this.acbId = acbId;
    }

    public TransparencyAttestationDTO getTransparencyAttestation() {
        return transparencyAttestation;
    }

    public void setTransparencyAttestation(TransparencyAttestationDTO transparencyAttestation) {
        this.transparencyAttestation = transparencyAttestation;
    }

    public String getAcbName() {
        return acbName;
    }

    public void setAcbName(String acbName) {
        this.acbName = acbName;
    }
}
