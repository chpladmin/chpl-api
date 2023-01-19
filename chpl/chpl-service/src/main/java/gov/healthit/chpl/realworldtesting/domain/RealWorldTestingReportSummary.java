package gov.healthit.chpl.realworldtesting.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RealWorldTestingReportSummary {
   private Integer rwtEligibilityYear;
   private Long totalListings;
   private Long totalWithdrawn;
   private Long totalActive;
   private Long totalWithPlansUrl;
   private Long totalWithPlansUrlValidated;
   private Long totalWithResultsUrl;
   private Long totalWithResultsUrlValidated;
}
