package gov.healthit.chpl.attestation.domain;

import gov.healthit.chpl.form.Form;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AttestationSubmission {
    private Long id;
    private Long developerId;
    private AttestationPeriod attestationPeriod;
    private Form form;
    private String signature;
    private String signatureEmail;
}
