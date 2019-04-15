package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dto.FilterDTO;

public class FilterResults implements Serializable{
    private static final long serialVersionUID = 1540021188714861976L;

    private List<FilterDTO> results = new ArrayList<FilterDTO>();
    
    public List<FilterDTO> getResults() {
        return results;
    }
    
    public void setResults(List<FilterDTO> results) {
        this.results = results;
    }
}
