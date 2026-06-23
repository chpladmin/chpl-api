package gov.healthit.chpl.report.criteriauptodate;

import java.time.LocalDate;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@Builder
@Schema(description = "Gives a listing and a criteria that it attests to which is not up-to-date for that listing")
public class ListingNotUpToDateReport {

    private CertificationCriterion criterion;
    private Long certifiedProductId;
    private String listingDetailsUrl;
    private String chplProductNumber;
    private String developerName;
    private String developerDetailsUrl;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate requiredDay;
}
