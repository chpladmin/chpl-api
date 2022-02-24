package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.Arrays;
import java.util.List;

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
    private String attestationPeriod;
    private String informationBlocking;
    private String assurances;
    private String communications;
    private String applicationProgrammingInterfaces;
    private String realWorldTesting;
    private String submitterName;
    private String submitterEmail;
    private Long totalSurveillanceNonconformities;
    private Long openSurveillanceNonconformities;
    private Long totalDirectReviewNonconformities;
    private Long openDirectReviewNonconformities;

    public List<String> toListOfStrings() {
        return Arrays.asList(
                developerName,
                developerCode,
                developerId.toString(),
                pointOfContactName,
                pointOfContactEmail,
                attestationPeriod,
                informationBlocking,
                assurances,
                communications,
                applicationProgrammingInterfaces,
                realWorldTesting,
                submitterName,
                submitterEmail,
                totalSurveillanceNonconformities != null ? totalSurveillanceNonconformities.toString() : "",
                openSurveillanceNonconformities != null ? openSurveillanceNonconformities.toString() : "",
                totalDirectReviewNonconformities != null ? totalDirectReviewNonconformities.toString() : "",
                openDirectReviewNonconformities != null ? openDirectReviewNonconformities.toString() : ""
                );
    }

    public static List<String> getHeaders() {
        return Arrays.asList(
                "Developer Name",
                "Developer Code",
                "Developer DBID",
                "Developer Point Of Contact Name",
                "Developer Point of Contact Email",
                "Attestation Period");
    }
}
