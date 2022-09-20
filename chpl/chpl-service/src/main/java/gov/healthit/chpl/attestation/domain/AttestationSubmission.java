package gov.healthit.chpl.attestation.domain;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @JsonIgnore
    private LocalDate datePublished;
}
