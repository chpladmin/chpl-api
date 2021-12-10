package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsExclude;

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

    @EqualsExclude
    private Long id;

    private String attestation;

    @Override
    public boolean isEqual(Object obj) {
        return equals(obj);
    }
}
