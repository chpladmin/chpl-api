package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;

public class ChangeRequestResults implements Serializable{
    private static final long serialVersionUID = 5705512902276119131L;

    private List<ChangeRequest> results = new ArrayList<ChangeRequest>();

    public List<ChangeRequest> getResults() {
        return results;
    }

    public void setResults(List<ChangeRequest> results) {
        this.results = results;
    }
}
