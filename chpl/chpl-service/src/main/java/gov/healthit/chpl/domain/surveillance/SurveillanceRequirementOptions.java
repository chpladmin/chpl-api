package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertificationCriterion;
import lombok.Data;

@Data
public class SurveillanceRequirementOptions implements Serializable {
    private static final long serialVersionUID = 500382005925313527L;
    private List<CertificationCriterion> criteriaOptions2014;
    private List<CertificationCriterion> criteriaOptions2015;
    private List<String> transparencyOptions;
    private List<String> realWorldTestingOptions;

    public SurveillanceRequirementOptions() {
        criteriaOptions2014 = new ArrayList<CertificationCriterion>();
        criteriaOptions2015 = new ArrayList<CertificationCriterion>();
        transparencyOptions = new ArrayList<String>();
        realWorldTestingOptions = new ArrayList<String>();
    }
}
