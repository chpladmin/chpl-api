package gov.healthit.chpl.attestation.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationForm;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;

@Service
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
}
