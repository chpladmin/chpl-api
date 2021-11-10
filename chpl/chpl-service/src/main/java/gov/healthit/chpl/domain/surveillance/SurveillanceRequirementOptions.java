package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.util.Removable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
public class SurveillanceRequirementOptions implements Serializable {
    private static final long serialVersionUID = 500382005925313527L;

    @Singular(value = "criteriaOption2014")
    private List<CertificationCriterion> criteriaOptions2014;

    @Singular(value = "criteriaOption2015")
    private List<CertificationCriterion> criteriaOptions2015;

    @Singular
    private List<Removable<String>> transparencyOptions;

    @Singular
    private List<Removable<String>> realWorldTestingOptions;

    public SurveillanceRequirementOptions() {
        criteriaOptions2014 = new ArrayList<CertificationCriterion>();
        criteriaOptions2015 = new ArrayList<CertificationCriterion>();
        transparencyOptions = new ArrayList<Removable<String>>();
        realWorldTestingOptions = new ArrayList<Removable<String>>();
    }
}
