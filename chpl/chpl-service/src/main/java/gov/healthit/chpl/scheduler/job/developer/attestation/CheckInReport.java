package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckInReport {
    private String developerName;
    private String developerCode;
    private Long developerId;
    private LocalDateTime submittedDate;
    private Boolean published;
    private String currentStatusName;
    private LocalDateTime lastStatusChangeDate;
    private String relevantAcbs;
    private String attestationPeriod;
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
    private Long totalSurveillances;
    private Long totalSurveillanceNonconformities;
    private Long openSurveillanceNonconformities;
    private Long totalDirectReviewNonconformities;
    private Long openDirectReviewNonconformities;
    private String assurancesValidation;
    private String realWorldTestingValidation;
    private String apiValidation;
    private String warnings;

    public List<String> toListOfStrings() {
        return List.of(developerName,
                developerCode,
                developerId.toString(),
                submittedDate != null ? submittedDate.toString() : "",
                published ? "Yes" : "No",
                currentStatusName != null ? currentStatusName : "",
                lastStatusChangeDate != null ? lastStatusChangeDate.toString() : "",
                relevantAcbs != null ? relevantAcbs : "",
                attestationPeriod != null ? attestationPeriod : "",
                informationBlockingResponse != null ? informationBlockingResponse : "",
                informationBlockingNoncompliantResponse != null ? informationBlockingNoncompliantResponse : "",
                assurancesResponse != null ? assurancesResponse : "",
                assurancesNoncompliantResponse != null ? assurancesNoncompliantResponse : "",
                communicationsResponse != null ? communicationsResponse : "",
                communicationsNoncompliantResponse != null ? communicationsNoncompliantResponse : "",
                apiResponse != null ? apiResponse : "",
                apiNoncompliantResponse != null ? apiNoncompliantResponse : "",
                rwtResponse != null ? rwtResponse : "",
                rwtNoncompliantResponse != null ? rwtNoncompliantResponse : "",
                signature != null ? signature : "",
                signatureEmail != null ? signatureEmail : "",
                totalSurveillances != null ? totalSurveillances.toString() : "0",
                totalSurveillanceNonconformities != null ? totalSurveillanceNonconformities.toString() : "0",
                openSurveillanceNonconformities != null ? openSurveillanceNonconformities.toString() : "0",
                totalDirectReviewNonconformities != null ? totalDirectReviewNonconformities.toString() : "0",
                openDirectReviewNonconformities != null ? openDirectReviewNonconformities.toString() : "0",
                assurancesValidation != null ? assurancesValidation : "",
                realWorldTestingValidation != null ? realWorldTestingValidation : "",
                apiValidation != null ? apiValidation : "");
    }

    public static List<String> getHeaders() {
        return List.of("Developer Name",
                "Developer Code",
                "Developer DBID",
                "Change Request Submitted Date",
                "Attestations Published?",
                "Change Request Current Status",
                "Change Request Last Status Change Date",
                "ONC-ACBs",
                "Attestations Period",
                "Information Blocking Response",
                "Information Blocking Optional Response",
                "Assurances Response",
                "Assurances Optional Response",
                "Communications Response",
                "Communications Optional Response",
                "Application Programming Interfaces Response",
                "Application Programming Interfaces Optional Response",
                "Real World Testing Response",
                "Real World Testing Optional Response",
                "Submitted by Name",
                "Submitted by Email",
                "Total Surveillance",
                "Total Surveillance Non-conformities",
                "Open Surveillance Non-conformities",
                "Total Direct Review Non-conformities",
                "Open Direct Review Non-conformities",
                "Has listing(s) with Assurances criteria (b)(6) or (b)(10)",
                "Has listing(s) with RWT criteria",
                "Has listing(s) with API criteria (g)(7)-(g)(10)");
    }
}
