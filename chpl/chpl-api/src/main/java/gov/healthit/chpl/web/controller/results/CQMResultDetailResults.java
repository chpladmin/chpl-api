package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.domain.CQMResultDetails;

/**
 * Represents a list of CQMResultDetails domain objects.
 * @author TYoung
 *
 */
public class CQMResultDetailResults implements Serializable {
    private static final long serialVersionUID = 6855235090829140243L;

    private List<CQMResultDetails> cqmResultDetailResults;

    /**
     * Basic empty constructor.
     */
    public CQMResultDetailResults() {
        //Empty constructor
    }

    /**
     * Constructor that will populate the object based on the list of CQMResultDetails
     * objects passed in as a parameter.
     * @param cqmResultDetailResults
     */
    public CQMResultDetailResults(final List<CQMResultDetails> cqmResultDetailResults) {
        this.cqmResultDetailResults = cqmResultDetailResults;
    }

    /**
     * @return the cqmResultDetailResults
     */
    public List<CQMResultDetails> getCqmResultDetailResults() {
        return cqmResultDetailResults;
    }

    /**
     * @param cqmResultDetailResults the cqmResultDetailResults to set
     */
    public void setCqmResultDetailResults(final List<CQMResultDetails> cqmResultDetailResults) {
        this.cqmResultDetailResults = cqmResultDetailResults;
    }
}
