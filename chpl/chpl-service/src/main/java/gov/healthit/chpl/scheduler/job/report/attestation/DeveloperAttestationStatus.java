package gov.healthit.chpl.scheduler.job.report.attestation;

import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.domain.Developer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeveloperAttestationStatus {
    private Developer developer;
    private ChangeRequestStatus changeRequestStatus;
}
