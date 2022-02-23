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

    public List<String> toListOfStrings() {
        return Arrays.asList(
                developerName,
                developerCode,
                developerId.toString(),
                pointOfContactName,
                pointOfContactEmail,
                attestationPeriod);
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
