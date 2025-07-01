package gov.healthit.chpl.report.criteriauptodate;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Gives a listing and a criteria that it attests to which is not up-to-date for that listing")
public class ListingNotUpToDateReport {

    private CertificationCriterion criterion;
    private Long certifiedProductId;
    private String chplProductNumber;
}
