package gov.healthit.chpl.certificationId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationIdMetPercentages {

    private int criteriaMet;
    private int criteriaUpToDate;
    private int cqmDomains;
    private int cqmsInpatient;
    private int cqmsAmbulatory;
}
