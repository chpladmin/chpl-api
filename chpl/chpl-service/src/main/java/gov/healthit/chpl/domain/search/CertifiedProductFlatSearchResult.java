package gov.healthit.chpl.domain.search;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * Class containing fields that are flattened using an emoji delimiter. This includes CQMs, certification criteria,
 * previous developers, and an array of API Documentation as necessary.
 * @author alarned
 *
 */
public class CertifiedProductFlatSearchResult extends CertifiedProductSearchResult {
    private static final long serialVersionUID = -2547390525592841044L;

    @JsonView({
            SearchViews.Default.class
    })
    private String previousDevelopers; // unicode-char delimited string of
                                       // developer names that owned the product

    @JsonView({
            SearchViews.Default.class
    })
    private String criteriaMet; // unicode-char delimited string of criteria
                                // numbers

    @JsonView({
            SearchViews.Default.class
    })
    private String cqmsMet; // unicode-char delimited string of cmqs that were
                            // met (any version)

    //unicode-char delimited string of surveillance start and end dates
    //format is start&end<char>start&
    private String surveillanceDates;

    private String apiDocumentation;

    /**
     * Default constructor.
     */
    public CertifiedProductFlatSearchResult() {

    }

    /**
     * Constructed from other flat search result.
     * @param other the other one
     */
    public CertifiedProductFlatSearchResult(final CertifiedProductFlatSearchResult other) {
        super(other);
        this.previousDevelopers = other.getPreviousDevelopers();
        this.criteriaMet = other.getCriteriaMet();
        this.cqmsMet = other.getCqmsMet();
        this.surveillanceDates = other.getSurveillanceDates();
        this.apiDocumentation = other.getApiDocumentation();
    }

    public String getPreviousDevelopers() {
        return previousDevelopers;
    }

    public void setPreviousDevelopers(final String previousDevelopers) {
        this.previousDevelopers = previousDevelopers;
    }

    public String getCriteriaMet() {
        return criteriaMet;
    }

    public void setCriteriaMet(final String criteriaMet) {
        this.criteriaMet = criteriaMet;
    }

    public String getCqmsMet() {
        return cqmsMet;
    }

    public void setCqmsMet(final String cqmsMet) {
        this.cqmsMet = cqmsMet;
    }

    public String getApiDocumentation() {
        return apiDocumentation;
    }

    public void setApiDocumentation(final String apiDocumentation) {
        this.apiDocumentation = apiDocumentation;
    }


    public String getSurveillanceDates() {
        return surveillanceDates;
    }

    public void setSurveillanceDates(final String surveillanceDates) {
        this.surveillanceDates = surveillanceDates;
    }
}
