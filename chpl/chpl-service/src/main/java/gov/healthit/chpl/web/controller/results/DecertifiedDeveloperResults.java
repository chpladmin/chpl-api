package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.domain.DecertifiedDeveloperResult;

public class DecertifiedDeveloperResults implements Serializable {
    private static final long serialVersionUID = 899826444547274174L;
    private List<DecertifiedDeveloperResult> decertifiedDeveloperResults;

    public DecertifiedDeveloperResults() {
    }

    public List<DecertifiedDeveloperResult> getDecertifiedDeveloperResults() {
        return decertifiedDeveloperResults;
    }

    public void setDecertifiedDeveloperResults(List<DecertifiedDeveloperResult> decertifiedDeveloperResults) {
        this.decertifiedDeveloperResults = decertifiedDeveloperResults;
    };

}
