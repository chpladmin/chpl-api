package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertificationCriterion;

public class CertificationCriterionResults implements Serializable{
    private static final long serialVersionUID = -9145928293024042648L;

    private List<CertificationCriterion> criteria;

    public CertificationCriterionResults() {
        criteria = new ArrayList<CertificationCriterion>();
    }

    public List<CertificationCriterion> getCriteria() {
        return criteria;
    }

    public void setCriteria(final List<CertificationCriterion> criteria) {
        this.criteria = criteria;
    }

}
