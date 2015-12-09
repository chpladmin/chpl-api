package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "searchResults")
public class SearchResponse {
	
	
	private Integer recordCount;
	private Integer pageSize;
	private Integer pageNumber;
	private List<CertifiedProductSearchResult> results;
	
	public SearchResponse(){
		results = new ArrayList<CertifiedProductSearchResult>();
	}
	
	public SearchResponse(Integer recordCount, List<CertifiedProductSearchResult> results){
		this.recordCount = recordCount;
		this.results = results;
	}
	
	public SearchResponse(Integer recordCount, 
			List<CertifiedProductSearchResult> results,
			Integer pageSize,
			Integer pageNumber){
		this.recordCount = recordCount;
		this.results = results;
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
	}
	
	public Integer getRecordCount() {
		return recordCount;
	}
	public void setRecordCount(Integer recordCount) {
		this.recordCount = recordCount;
	}
	public List<CertifiedProductSearchResult> getResults() {
		return results;
	}
	public void setResults(List<CertifiedProductSearchResult> results) {
		this.results = results;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public Integer getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}
	
}
