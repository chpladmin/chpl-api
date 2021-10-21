package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.util.Removable;
import lombok.Data;

@Data
public class SurveillanceRequirementOptions implements Serializable {
    private static final long serialVersionUID = 500382005925313527L;
    private List<CertificationCriterion> criteriaOptions2014;
    private List<CertificationCriterion> criteriaOptions2015;
    private List<Removable<String>> transparencyOptions;
    private List<Removable<String>> realWorldTestingOptions;

    public SurveillanceRequirementOptions() {
        criteriaOptions2014 = new ArrayList<CertificationCriterion>();
        criteriaOptions2015 = new ArrayList<CertificationCriterion>();
        transparencyOptions = new ArrayList<Removable<String>>();
        realWorldTestingOptions = new ArrayList<Removable<String>>();
    }
}
