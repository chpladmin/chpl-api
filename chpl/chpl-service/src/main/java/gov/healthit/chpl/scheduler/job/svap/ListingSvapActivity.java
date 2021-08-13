package gov.healthit.chpl.scheduler.job.svap;

import java.time.LocalDate;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.svap.domain.Svap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingSvapActivity {
    private CertifiedProductSearchDetails listing;
    private LocalDate svapNoticeLastUpdated;
    private CertificationCriterion criterion;
    private Svap criterionSvap;
    private Boolean wasCriterionAttestedToBeforeSvapAdded;
    private LocalDate criterionSvapLastUpdated;
}
