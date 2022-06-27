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
    private AttestationPeriodService attestationPeriodService;

    @Autowired
    public AttestationFormService(AttestationDAO attestationDAO, AttestationPeriodService attestationPeriodService) {
        this.attestationDAO = attestationDAO;
        this.attestationPeriodService = attestationPeriodService;
    }

    @Transactional(readOnly = true)
    public AttestationForm getAttestationForm(Long periodId, Long attestationFormItemId) {
        List<AttestationFormItem> formItems = getAttestationFormItems(periodId, attestationFormItemId);
        return AttestationForm.builder()
                .attestations(formItems)
                .period(attestationPeriodService.getAllPeriods().stream()
                        .filter(p -> p.getId().equals(periodId))
                        .findAny()
                        .orElse(null))
                .build();
    }

    private  List<AttestationFormItem> getAttestationFormItems(Long periodId, Long attestationFormItemId) {
        List<AttestationFormItem> formItems = attestationDAO.getAttestationFormItems(periodId, attestationFormItemId);
        formItems.forEach(fi -> {
            fi.setChildAttestations(getAttestationFormItems(periodId, fi.getId()));
        });
        return formItems;
    }
}
