package gov.healthit.chpl.attestation.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationForm;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.DeveloperAttestation;
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
    public DeveloperAttestation saveDeveloperAttestation(DeveloperAttestation developerAttestation) throws EntityRetrievalException {
        attestationDAO.getDeveloperAttestationByDeveloperAndPeriod(
                developerAttestation.getDeveloper().getDeveloperId(),
                developerAttestation.getPeriod().getId())
                .stream()
                        .forEach(da -> {
                            try {
                                attestationDAO.deleteDeveloperAttestation(da.getId());
                            } catch (EntityRetrievalException e) {
                                LOGGER.catching(e);
                                throw new RuntimeException(e);
                            }
                        });
        return attestationDAO.createDeveloperAttestation(developerAttestation);
    }
}
