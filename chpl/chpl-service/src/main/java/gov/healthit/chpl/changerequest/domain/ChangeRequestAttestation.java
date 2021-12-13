package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ChangeRequestAttestation implements Serializable, ChangeRequestDetails{
    private static final long serialVersionUID = 2150025150434933303L;

    @EqualsAndHashCode.Exclude
    private Long id;

    private String attestation;

    public static ChangeRequestAttestation cast(Object obj) {
        if (obj instanceof ChangeRequestAttestation) {
            return (ChangeRequestAttestation) obj;
        } else {
            throw new RuntimeException("Could not cast object as type ChangeRequestAttestation");
        }
    }

    @Override
    public boolean isEqual(Object obj) {
        return equals(obj);
    }
}
