package gov.healthit.chpl.attestation.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationForm;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.DeveloperAttestationSubmission;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class AttestationManager {
    private AttestationDAO attestationDAO;

    @Autowired
    public AttestationManager(AttestationDAO attestationDAO) {
        this.attestationDAO = attestationDAO;
    }

    public List<AttestationPeriod> getAllPeriods() {
        return attestationDAO.getAllPeriods();
    }

    public AttestationForm getAttestationForm() {
        return new AttestationForm(attestationDAO.getAttestationForm());
    }

    @Transactional
    public DeveloperAttestationSubmission saveDeveloperAttestation(DeveloperAttestationSubmission developerAttestation) throws EntityRetrievalException {
        attestationDAO.getDeveloperAttestationSubmissionsByDeveloperAndPeriod(
                developerAttestation.getDeveloper().getDeveloperId(),
                developerAttestation.getPeriod().getId())
                .stream()
                        .forEach(da -> {
                            try {
                                attestationDAO.deleteDeveloperAttestationSubmission(da.getId());
                            } catch (EntityRetrievalException e) {
                                LOGGER.catching(e);
                                throw new RuntimeException(e);
                            }
                        });
        return attestationDAO.createDeveloperAttestationSubmission(developerAttestation);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ATTESTATION, "
            + "T(gov.healthit.chpl.permissions.domains.AttestationDomainPermissions).GET_BY_DEVELOPER_ID, #developerId)")
    public List<DeveloperAttestationSubmission> getDeveloperAttestations(Long developerId) {
        return attestationDAO.getDeveloperAttestationSubmissionsByDeveloper(developerId);
    }
}
