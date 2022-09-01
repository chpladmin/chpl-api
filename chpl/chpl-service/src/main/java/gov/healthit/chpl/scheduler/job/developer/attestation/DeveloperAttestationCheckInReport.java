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
    private String informationBlockingNoncompliantResponse;
    private String assurancesResponse;
    private String assurancesNoncompliantResponse;
    private String communicationsResponse;
    private String communicationsNoncompliantResponse;
    private String rwtResponse;
    private String rwtNoncompliantResponse;
    private String apiResponse;
    private String apiNoncompliantResponse;
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
                informationBlockingNoncompliantResponse != null ? informationBlockingNoncompliantResponse : "",
                assurancesResponse != null ? assurancesResponse : "",
                assurancesNoncompliantResponse != null ? assurancesNoncompliantResponse : "",
                communicationsResponse != null ? communicationsResponse : "",
                communicationsNoncompliantResponse != null ? communicationsNoncompliantResponse : "",
                rwtResponse != null ? rwtResponse : "",
                rwtNoncompliantResponse != null ? rwtNoncompliantResponse : "",
                apiResponse != null ? apiResponse : "",
                apiNoncompliantResponse != null ? apiNoncompliantResponse : "",
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
                "Information Blocking Optional Response",
                "Assurances Response",
                "Assurances Optional Response",
                "Communications Response",
                "Communications Optional Response",
                "Real World Testing Response",
                "Real World Testing Optional Response",
                "Application Programming Interfaces Response",
                "Application Programming Interfaces Optional Response",
                "Submitted by Name",
                "Submitted by Email");
    }
}
