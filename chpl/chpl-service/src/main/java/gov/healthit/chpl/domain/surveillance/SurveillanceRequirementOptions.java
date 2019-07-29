package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.KeyValueModel;

public class SurveillanceRequirementOptions implements Serializable {
    private static final long serialVersionUID = 500382005925313527L;
    private List<KeyValueModel> criteriaOptions2014;
    private List<KeyValueModel> criteriaOptions2015;
    private List<String> transparencyOptions;

    public SurveillanceRequirementOptions() {
        criteriaOptions2014 = new ArrayList<KeyValueModel>();
        criteriaOptions2015 = new ArrayList<KeyValueModel>();
        transparencyOptions = new ArrayList<String>();
    }

    public List<String> getTransparencyOptions() {
        return transparencyOptions;
    }

    public void setTransparencyOptions(final List<String> transparencyOptions) {
        this.transparencyOptions = transparencyOptions;
    }

    public List<KeyValueModel> getCriteriaOptions2014() {
        return criteriaOptions2014;
    }

    public void setCriteriaOptions2014(final List<KeyValueModel> criteriaOptions2014) {
        this.criteriaOptions2014 = criteriaOptions2014;
    }

    public List<KeyValueModel> getCriteriaOptions2015() {
        return criteriaOptions2015;
    }

    public void setCriteriaOptions2015(final List<KeyValueModel> criteriaOptions2015) {
        this.criteriaOptions2015 = criteriaOptions2015;
    }
}
