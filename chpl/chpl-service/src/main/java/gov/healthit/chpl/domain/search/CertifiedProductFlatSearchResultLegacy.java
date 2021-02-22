package gov.healthit.chpl.domain.search;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Deprecated
public class CertifiedProductFlatSearchResultLegacy extends CertifiedProductSearchResultLegacy {
    private static final long serialVersionUID = -2547390525592841044L;
    public static final String CERTS_SPLIT_CHAR = "\u263A";

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

    public CertifiedProductFlatSearchResultLegacy(CertifiedProductFlatSearchResultLegacy other) {
        super(other);
        this.previousDevelopers = other.getPreviousDevelopers();
        this.criteriaMet = other.getCriteriaMet();
        this.cqmsMet = other.getCqmsMet();
        this.surveillanceDates = other.getSurveillanceDates();
        this.apiDocumentation = other.getApiDocumentation();
    }
}
