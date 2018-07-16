package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.domain.CertificationResult;

/**
 * Represents a list of CertificationResult domain objects.
 * @author TYoung
 *
 */
public class CertificationResults implements Serializable {

    private static final long serialVersionUID = -9082268951624062959L;

    private List<CertificationResult> certificationResults;

    /**
     * Basic empty constructor.
     */
    public CertificationResults() {
        //Empty contructor
    }

    /**
     * Constructor that will populate the object based on the list of CertificationResult
     * objects passed in as a parameter.
     * @param certificationResults
     */
    public CertificationResults(final List<CertificationResult> certificationResults) {
        this.certificationResults = certificationResults;
    }

    /**
     * @return the certificationResults
     */
    public List<CertificationResult> getCertificationResults() {
        return certificationResults;
    }

    /**
     * @param certificationResults the certificationResults to set
     */
    public void setCertificationResults(final List<CertificationResult> certificationResults) {
        this.certificationResults = certificationResults;
    }

}
