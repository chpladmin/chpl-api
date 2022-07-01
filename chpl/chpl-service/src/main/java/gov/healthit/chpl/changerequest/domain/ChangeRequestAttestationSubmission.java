package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.form.Form;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeRequestAttestationSubmission implements Serializable, ChangeRequestDetails {
    private static final long serialVersionUID = 2150025150434933303L;

    private Long id;
    private AttestationPeriod attestationPeriod;
    private Form form;
    private String signature;
    private String signatureEmail;

    public static ChangeRequestAttestationSubmission cast(Object obj) {
        if (obj instanceof ChangeRequestAttestationSubmission) {
            return (ChangeRequestAttestationSubmission) obj;
        } else {
            throw new RuntimeException("Could not cast object as type ChangeRequestAttestationSubmission");
        }
    }
}
