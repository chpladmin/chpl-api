package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.complaint.Complaint;

public class ComplaintResults implements Serializable{
    private static final long serialVersionUID = 570551290486119131L;
   
    private List<Complaint> results = new ArrayList<Complaint>();
    
    public List<Complaint> getResults() {
        return results;
    }
    
    public void setResults(List<Complaint> results) {
        this.results = results;
    }
}
