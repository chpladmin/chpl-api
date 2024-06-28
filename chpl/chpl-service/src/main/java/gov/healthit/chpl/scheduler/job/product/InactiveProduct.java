package gov.healthit.chpl.scheduler.job.product;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class InactiveProduct {

    private Long productId;
    private String productName;
    private Long developerId;
    private String developerName;
    private String developerWebsite;
    private List<String> productAcbs;
    private LocalDate inactiveDate;
}
