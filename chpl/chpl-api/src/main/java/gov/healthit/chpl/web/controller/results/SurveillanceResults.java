package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.surveillance.Surveillance;

public class SurveillanceResults implements Serializable {
    private static final long serialVersionUID = 6767406136348327093L;
    private List<Surveillance> pendingSurveillance;

    public SurveillanceResults() {
        pendingSurveillance = new ArrayList<Surveillance>();
    }

    public List<Surveillance> getPendingSurveillance() {
        return pendingSurveillance;
    }

    public void setPendingSurveillance(final List<Surveillance> pendingSurveillance) {
        this.pendingSurveillance = pendingSurveillance;
    }

}
