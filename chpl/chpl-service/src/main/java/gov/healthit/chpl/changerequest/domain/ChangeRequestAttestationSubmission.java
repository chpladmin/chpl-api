package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.functors.DefaultEquator;

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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChangeRequestAttestationSubmission other = (ChangeRequestAttestationSubmission) obj;

        return Objects.equals(attestationPeriod, other.attestationPeriod)
                && CollectionUtils.isEqualCollection(attestationResponses, other.getAttestationResponses(), DefaultEquator.INSTANCE)
                && Objects.equals(id, other.id)
                && Objects.equals(signature, other.signature)
                && Objects.equals(signatureEmail, other.signatureEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attestationPeriod, attestationResponses, id, signature, signatureEmail);
    }
}
