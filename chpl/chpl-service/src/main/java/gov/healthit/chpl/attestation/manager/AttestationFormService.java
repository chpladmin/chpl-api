package gov.healthit.chpl.attestation.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationForm;
import gov.healthit.chpl.attestation.domain.AttestationFormItem;

@Component
public class AttestationFormService {
    private AttestationDAO attestationDAO;

    @Autowired
    public AttestationFormService(AttestationDAO attestationDAO) {
        this.attestationDAO = attestationDAO;
    }

    @Transactional(readOnly = true)
    public AttestationForm getAttestationForm(Long periodId) {
        List<AttestationFormItem> formItems = attestationDAO.getAttestationFormItems(periodId);


        return AttestationForm.builder()
                .period(formItems != null && formItems.size() != 0 ? formItems.get(0).getAttestationPeriod() : null)
                .attestations(formItems.stream()
                        .map(fi -> {
                            fi.getAttestation().setDependentAttestations(attestationDAO.getDependentAttestations(fi.getId()));
                            return fi.getAttestation();
                        })
                        .toList())
                .build();
    }
}
