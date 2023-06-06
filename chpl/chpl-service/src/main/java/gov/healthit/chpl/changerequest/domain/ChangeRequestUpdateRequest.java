package gov.healthit.chpl.changerequest.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRequestUpdateRequest {
    private ChangeRequest changeRequest;
    private boolean acknowledgeWarnings;
}
