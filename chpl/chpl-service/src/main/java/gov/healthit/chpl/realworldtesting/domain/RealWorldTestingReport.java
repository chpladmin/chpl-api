package gov.healthit.chpl.realworldtesting.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RealWorldTestingReport {
    private String acbName;
    private String chplProductNumber;
    private String productName;
    private Long productId;
    private String developerName;
    private Long developerId;
    private String rwtPlansUrl;
    private LocalDate rwtPlansCheckDate;
    private String rwtResultsUrl;
    private LocalDate rwtResultsCheckDate;
    private String rwtPlansMessage;
    private String rwtResultsMessage;
}
