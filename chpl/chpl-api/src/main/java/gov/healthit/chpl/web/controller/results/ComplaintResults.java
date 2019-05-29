package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dao.ComplaintDTO;
import gov.healthit.chpl.domain.Filter;
import gov.healthit.chpl.dto.FilterDTO;

public class ComplaintResults implements Serializable{
    private static final long serialVersionUID = 570551290486119131L;
   
    private List<ComplaintDTO> results = new ArrayList<ComplaintDTO>();
    
    public List<ComplaintDTO> getResults() {
        return results;
    }
    
    public void setResults(List<ComplaintDTO> results) {
        this.results = results;
    }
}
