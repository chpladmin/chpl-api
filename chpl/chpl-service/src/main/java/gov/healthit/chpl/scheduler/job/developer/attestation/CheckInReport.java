package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.LocalDateTime;
import java.util.List;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReport.CriterionAndSvapData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private List<CriterionAndSvapData> criterionAndSvapData;

    private ChangeRequest mostRecentAttestationChangeRequest;
    private Developer developer;
    private List<CertificationBody> certificationBodies;
}
