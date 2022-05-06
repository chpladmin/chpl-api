package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeveloperAttestationCheckInReport {
    private String developerName;
    private LocalDateTime submittedDate;
    private String currentStatusName;
    private LocalDateTime lastStatusChangeDate;
    private String relevantAcbs;

    public List<String> toListOfStrings() {
        return List.of(developerName,
                submittedDate != null ? submittedDate.toString() : "",
                currentStatusName != null ? currentStatusName : "",
                lastStatusChangeDate != null ? lastStatusChangeDate.toString() : "",
                relevantAcbs != null ? relevantAcbs : "");
    }
}
