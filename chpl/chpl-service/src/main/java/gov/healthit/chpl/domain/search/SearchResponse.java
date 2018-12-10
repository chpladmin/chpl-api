package gov.healthit.chpl.domain.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "searchResults")
public class SearchResponse implements Serializable {
    private static final long serialVersionUID = 5130424471766571329L;
    private Integer recordCount;
    private Integer pageSize;
    private Integer pageNumber;
    private Collection<CertifiedProductBasicSearchResult> results;

    public SearchResponse() {
        results = new ArrayList<CertifiedProductBasicSearchResult>();
    }

    public SearchResponse(Integer recordCount, Collection<CertifiedProductBasicSearchResult> results) {
        this.recordCount = recordCount;
        this.results = results;
    }

    public SearchResponse(Integer recordCount, Collection<CertifiedProductBasicSearchResult> results, Integer pageSize,
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

    public Collection<CertifiedProductBasicSearchResult> getResults() {
        return results;
    }

    public void setResults(final Collection<CertifiedProductBasicSearchResult> results) {
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
