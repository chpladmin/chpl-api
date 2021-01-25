package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.svap.domain.SvapCriteriaMap;

public class SvapResults implements Serializable{
    private static final long serialVersionUID = 2250111259774528020L;
    private List<SvapCriteriaMap> results;

    public SvapResults() {
        results = new ArrayList<SvapCriteriaMap>();
    }

    public SvapResults(List<SvapCriteriaMap> results) {
        this.results = new ArrayList<SvapCriteriaMap>(results);
    }

    public List<SvapCriteriaMap> getResults() {
        return results;
    }

    public void setResults(List<SvapCriteriaMap> results) {
        this.results = results;
    }
}
