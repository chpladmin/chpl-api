package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.ListingMeasure;
import lombok.Data;

@Data
public class MeasureResults implements Serializable {
    private static final long serialVersionUID = 6576376082508789115L;
    private List<ListingMeasure> results;

    public MeasureResults() {
        results = new ArrayList<ListingMeasure>();
    }

    public MeasureResults(List<ListingMeasure> results) {
        this.results = new ArrayList<ListingMeasure>(results);
    }
}
