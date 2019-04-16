package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.Filter;
import gov.healthit.chpl.dto.FilterDTO;

public class FilterResults implements Serializable{
    private static final long serialVersionUID = 1540021188714861976L;

    private List<Filter> results = new ArrayList<Filter>();
    
    public List<Filter> getResults() {
        return results;
    }
    
    public void setResults(List<Filter> results) {
        this.results = results;
    }
}
