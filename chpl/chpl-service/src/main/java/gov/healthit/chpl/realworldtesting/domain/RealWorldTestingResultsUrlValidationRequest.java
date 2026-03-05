package gov.healthit.chpl.realworldtesting.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RealWorldTestingResultsUrlValidationRequest {
    private Long listingId;
    private String url;
    private Integer year;
}
