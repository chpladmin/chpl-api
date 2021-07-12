package gov.healthit.chpl.domain.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "searchResults")
@Deprecated
public class SearchResponseLegacy implements Serializable {
    private static final long serialVersionUID = 5130424471766571329L;
    private Integer recordCount;
    private Integer pageSize;
    private Integer pageNumber;
    private Collection<CertifiedProductBasicSearchResultLegacy> results;

    public SearchResponseLegacy() {
        results = new ArrayList<CertifiedProductBasicSearchResultLegacy>();
    }

    public SearchResponseLegacy(Integer recordCount, Collection<CertifiedProductBasicSearchResultLegacy> results) {
        this.recordCount = recordCount;
        this.results = results;
    }

    public SearchResponseLegacy(Integer recordCount, Collection<CertifiedProductBasicSearchResultLegacy> results, Integer pageSize,
            Integer pageNumber) {
        this.recordCount = recordCount;
        this.results = results;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(final Integer recordCount) {
        this.recordCount = recordCount;
    }

    public Collection<CertifiedProductBasicSearchResultLegacy> getResults() {
        return results;
    }

    public void setResults(final Collection<CertifiedProductBasicSearchResultLegacy> results) {
        this.results = results;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(final Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(final Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

}
