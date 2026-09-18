package gov.healthit.chpl.realworldtesting.domain;

import java.time.LocalDate;
import java.util.List;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class RealWorldTestingReport {
    private String acbName;
    private String chplProductNumber;
    private String currentStatus;
    private LocalDate certificationDate;
    private String productName;
    private Long productId;
    private String developerName;
    private Long developerId;
    private List<String> developerUsers;
    private Integer rwtEligibilityYear;
    private Boolean ics;
    private String rwtPlansUrl;
    private LocalDate rwtPlansCheckDate;
    private String rwtResultsUrl;
    private LocalDate rwtResultsCheckDate;
    private String rwtPlansMessage;
    private String rwtResultsMessage;
    private List<CriterionAndSvapData> criterionAndSvapData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CriterionAndSvapData {
        private CertificationCriterion criterion;
        private boolean isAttested;
        private boolean isGCriterion;
        private boolean usesSvap;
    }
}
