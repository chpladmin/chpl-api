package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmittedResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRequestAttestationSubmission implements Serializable, ChangeRequestDetails {
    private static final long serialVersionUID = 2150025150434933303L;

    private Long id;
    private String signature;
    private AttestationPeriod attestationPeriod;
    @Singular
    private List<AttestationSubmittedResponse> attestationResponses;
    private String signatureEmail;

    public static ChangeRequestAttestationSubmission cast(Object obj) {
        if (obj instanceof ChangeRequestAttestationSubmission) {
            return (ChangeRequestAttestationSubmission) obj;
        } else {
            throw new RuntimeException("Could not cast object as type ChangeRequestAttestationSubmission");
        }
    }

    @Override
    public boolean matches(Object obj) {
        return equals(obj);
    }
}
