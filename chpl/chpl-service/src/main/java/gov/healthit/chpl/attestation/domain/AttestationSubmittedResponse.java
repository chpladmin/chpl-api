package gov.healthit.chpl.attestation.domain;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttestationSubmittedResponse {
    private Long id;
    private Attestation attestation;
    private AttestationValidResponse response;

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
        AttestationSubmittedResponse other = (AttestationSubmittedResponse) obj;
        return Objects.equals(attestation.getId(), other.attestation.getId())
                && Objects.equals(id, other.id)
                && Objects.equals(response.getId(), other.response.getId());
    }
    @Override
    public int hashCode() {
        return Objects.hash(attestation.getId(), id, response.getId());
    }
}
