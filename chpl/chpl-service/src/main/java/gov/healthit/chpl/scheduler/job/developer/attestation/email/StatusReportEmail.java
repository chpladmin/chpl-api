package gov.healthit.chpl.scheduler.job.developer.attestation.email;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class StatusReportEmail {
    private List<String> recipients;
    private String subject;
    private String message;
}
