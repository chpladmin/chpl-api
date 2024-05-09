package gov.healthit.chpl.scheduler.job.developer.messaging;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagingReportEmail {
    private List<String> recipients;
    private String subject;
    private String message;
}
