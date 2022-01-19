package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRequestAttestation implements Serializable, ChangeRequestDetails {
    private static final long serialVersionUID = 2150025150434933303L;

    private Long id;
    private String signature;
    private AttestationPeriod attestationPeriod;
    private List<AttestationResponse> responses;

    @Override
    public boolean matches(Object obj) {
        return equals(obj);
    }
}
