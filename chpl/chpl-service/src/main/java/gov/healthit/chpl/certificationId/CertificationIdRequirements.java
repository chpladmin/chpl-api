package gov.healthit.chpl.certificationId;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CertificationIdRequirements {

    private int criteriaRequired = 0;
    private int criteriaRequiredMet = 0;
    private int criteriaCpoeRequired = 0;
    private int criteriaCpoeRequiredMet = 0;
    private int criteriaTocRequired = 0;
    private int criteriaTocRequiredMet = 0;
    private int criteriaDpRequired = 0;
    private int criteriaDpRequiredMet = 0;
    private int criteriaDsRequired = 0;
    private int criteriaDsRequiredMet = 0;
    private int criteriaUpToDateRequired = 0;
    private int criteriaUpToDateMet = 0;
    private int cqmsInpatientRequired = 0;
    private int cqmsInpatientRequiredMet = 0;
    private int cqmsAmbulatoryRequired = 0;
    private int cqmsAmbulatoryRequiredMet = 0;
    private int cqmsAmbulatoryCoreRequired = 0;
    private int cqmsAmbulatoryCoreRequiredMet = 0;
    private int domainsRequired = 0;
    private int domainsRequiredMet = 0;
}
