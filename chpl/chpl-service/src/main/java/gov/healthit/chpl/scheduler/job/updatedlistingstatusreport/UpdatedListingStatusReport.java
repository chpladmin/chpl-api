package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import java.time.LocalDate;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.standard.Standard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UpdatedListingStatusReport {
    private Long id;
    private LocalDate reportDay;

    private Long certifiedProductId;
    private Long certificationResultId;
    private CertificationCriterion certificationCriterion;
    private Standard standard;
    private FunctionalityTested functionalityTested;
    private CodeSet codeSet;
    private ListingNotUpToDateReason listingNotUpToDateReason;

    private String chplProductNumber;
    private String product;
    private String version;
    private String developer;
    private String certificationBody;
    private String certificationStatus;
    private Long developerId;
    private Long certificationBodyId;
    private Long certificationStatusId;

}
