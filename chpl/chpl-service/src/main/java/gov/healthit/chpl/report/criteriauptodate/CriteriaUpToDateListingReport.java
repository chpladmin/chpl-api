package gov.healthit.chpl.report.criteriauptodate;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Gives the set of listings attesting to the criteria and their up-to-date status")
public class CriteriaUpToDateListingReport {

    private CertificationCriterion criterion;
    private String chplProductNumber;
    private String upToDateStatus;
}
