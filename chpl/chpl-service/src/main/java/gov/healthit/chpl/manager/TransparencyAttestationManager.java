package gov.healthit.chpl.manager;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.TransparencyAttestationDTO;

@Component
public class TransparencyAttestationManager {

    private DeveloperDAO developerDAO;
    private CertificationBodyDAO certificationBodyDAO;
    private FF4j ff4j;

    @Autowired
    public TransparencyAttestationManager(DeveloperDAO developerDAO, CertificationBodyDAO certificationBodyDAO, FF4j ff4j) {
        this.developerDAO = developerDAO;
        this.certificationBodyDAO = certificationBodyDAO;
        this.ff4j = ff4j;
    }

    public void save(DeveloperDTO developer) {
        developer.getTransparencyAttestationMappings().stream()
                .forEach(devAcbMap -> save(devAcbMap, developer.getId()));
    }

    public void save(DeveloperACBMapDTO developerACBMapDTO, Long developerId) {
        Optional<CertificationBodyDTO> acb = getAcbByName(developerACBMapDTO.getAcbName());
        if (acb.isPresent()) {
            Optional<DeveloperACBMapDTO> existingDeveloperAcbMap = getDeveloperAcbMap(developerId, acb.get().getId());
            if (existingDeveloperAcbMap.isPresent()) {
                existingDeveloperAcbMap.get().setTransparencyAttestation(developerACBMapDTO.getTransparencyAttestation());
                developerDAO.updateTransparencyMapping(existingDeveloperAcbMap.get());
            } else if (doesTransparenctAttestationExist(developerACBMapDTO)) {
                DeveloperACBMapDTO developerMapACBMap = DeveloperACBMapDTO.builder()
                        .acbId(acb.get().getId())
                        .acbName(acb.get().getName())
                        .developerId(developerId)
                        .transparencyAttestation(TransparencyAttestationDTO.builder()
                                .removed(getDerivedTransparencyAttestationRemovedAttribute())
                                .transparencyAttestation(
                                        developerACBMapDTO.getTransparencyAttestation().getTransparencyAttestation())
                                .build())
                        .build();
                developerDAO.createTransparencyMapping(developerMapACBMap);
            }
        }
    }

    private boolean getDerivedTransparencyAttestationRemovedAttribute() {
        return ff4j.check(FeatureList.EFFECTIVE_RULE_DATE);
    }

    private boolean doesTransparenctAttestationExist(DeveloperACBMapDTO dto) {
        return Objects.nonNull(dto.getTransparencyAttestation())
                && !StringUtils.isEmpty(dto.getTransparencyAttestation().getTransparencyAttestation());
    }

    private Optional<CertificationBodyDTO> getAcbByName(String acbName) {
        return Optional.ofNullable(certificationBodyDAO.getByName(acbName));
    }

    private Optional<DeveloperACBMapDTO> getDeveloperAcbMap(Long developerId, Long acbId) {
        return Optional.ofNullable(developerDAO.getTransparencyMapping(developerId, acbId));
    }
}
