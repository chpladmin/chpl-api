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
    private Boolean published;
    private String currentStatusName;
    private LocalDateTime lastStatusChangeDate;
    private String relevantAcbs;
    private String informationBlockingResponse;
    private String assurancesResponse;
    private String communicationsResponse;
    private String rwtResponse;
    private String apiResponse;
    private String signature;
    private String signatureEmail;

    public List<String> toListOfStrings() {
        return List.of(developerName,
                submittedDate != null ? submittedDate.toString() : "",
                published ? "Yes" : "No",
                currentStatusName != null ? currentStatusName : "",
                lastStatusChangeDate != null ? lastStatusChangeDate.toString() : "",
                relevantAcbs != null ? relevantAcbs : "",
                informationBlockingResponse != null ? informationBlockingResponse : "",
                assurancesResponse != null ? assurancesResponse : "",
                communicationsResponse != null ? communicationsResponse : "",
                rwtResponse != null ? rwtResponse : "",
                apiResponse != null ? apiResponse : "",
                signature != null ? signature : "",
                signatureEmail != null ? signatureEmail : "");
    }

    public static List<String> getHeaders() {
        return List.of("Developer Name",
                "Change Request Submitted Date",
                "Attestations Published?",
                "Change Request Current Status",
                "Change Request Last Status Change Date",
                "ONC-ACBs",
                "Information Blocking Response",
                "Assurances Response",
                "Communications Response",
                "Real World Testing Response",
                "Application Programming Interfaces Response",
                "Submitted by Name",
                "Submitted by Email");
    }
}
