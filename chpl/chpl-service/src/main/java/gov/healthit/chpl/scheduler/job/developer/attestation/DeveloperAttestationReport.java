package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import gov.healthit.chpl.domain.CertificationBody;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeveloperAttestationReport {
    private String developerName;
    private String developerCode;
    private Long developerId;
    private String pointOfContactName;
    private String pointOfContactEmail;
    private List<String> developerUsers;
    private String attestationStatus;
    private LocalDate attestationPublishDate;
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
    private String submitterName;
    private String submitterEmail;
    private Long totalSurveillances;
    private Long totalSurveillanceNonconformities;
    private Long openSurveillanceNonconformities;
    private Long totalDirectReviewNonconformities;
    private Long openDirectReviewNonconformities;
    private String assurancesValidation;
    private String realWorldTestingValidation;
    private String apiValidation;
    private Map<Pair<Long, Long>, Boolean> developerAcbMap; // Key: developerId, acbId
    private List<CertificationBody> activeAcbs;

    public List<String> toListOfStrings() {
        List<String> developerAttestationData =  Arrays.asList(
                developerName,
                developerCode,
                developerId.toString(),
                pointOfContactName,
                pointOfContactEmail,
                !CollectionUtils.isEmpty(developerUsers) ? developerUsers.stream().collect(Collectors.joining("; ")) : "",
                attestationPeriod,
                attestationStatus,
                attestationPublishDate != null ? attestationPublishDate.toString() : "",
                informationBlockingResponse,
                informationBlockingNoncompliantResponse,
                assurancesResponse,
                assurancesNoncompliantResponse,
                communicationsResponse,
                communicationsNoncompliantResponse,
                apiResponse,
                apiNoncompliantResponse,
                rwtResponse,
                rwtNoncompliantResponse,
                submitterName,
                submitterEmail,
                totalSurveillances != null ? totalSurveillances.toString() : "",
                totalSurveillanceNonconformities != null ? totalSurveillanceNonconformities.toString() : "",
                openSurveillanceNonconformities != null ? openSurveillanceNonconformities.toString() : "",
                totalDirectReviewNonconformities != null ? totalDirectReviewNonconformities.toString() : "",
                openDirectReviewNonconformities != null ? openDirectReviewNonconformities.toString() : "");

        List<String> acbData = activeAcbs.stream()
                .map(acb -> developerAcbMap.containsKey(Pair.of(developerId, acb.getId()))
                        ? developerAcbMap.get(Pair.of(developerId, acb.getId())) ? "Applicable" : "Not Applicable"
                        : "Not Applicable")
                .toList();

        List<String> attestationValidationData =  Arrays.asList(
                assurancesValidation,
                realWorldTestingValidation,
                apiValidation);

        List<String> data = new ArrayList<String>();
        data.addAll(developerAttestationData);
        data.addAll(acbData);
        data.addAll(attestationValidationData);

        return data;
    }

    public List<String> getHeaders() {
        List<String> developerAttestationHeaders =  Arrays.asList(
                "Developer Name",
                "Developer Code",
                "Developer DBID",
                "Developer Point of Contact Name",
                "Developer Point of Contact Email",
                "Developer Users",
                "Attestations Period",
                "Attestations Status",
                "Last Activity",
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
                "Submitter Name",
                "Submitter Email",
                "Total Surveillance",
                "Total Surveillance Non-conformities",
                "Open Surveillance Non-conformities",
                "Total Direct Review Non-conformities",
                "Open Direct Review Non-conformities");

        List<String> acbHeaders = activeAcbs.stream()
                .map(acb -> acb.getName())
                .toList();

        List<String> attestationValidationData =  Arrays.asList(
                "Assurances",
                "Real World Testing",
                "API");

        List<String> headers = new ArrayList<String>();
        headers.addAll(developerAttestationHeaders);
        headers.addAll(acbHeaders);
        headers.addAll(attestationValidationData);

        return headers;
    }
}
