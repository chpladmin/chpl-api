package gov.healthit.chpl.web.controller;

import java.util.Set;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.SearchMenuManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchViewController {
	
	
	@Autowired
	private SearchMenuManager searchMenuManager;
	
	@Autowired
	private CertificationBodyManager certificationBodyManager;
	
	@Autowired
	private CertifiedProductSearchManager certifiedProductSearchManager;
	
	@Autowired
	private CertifiedProductDetailsManager certifiedProductDetailsManager;
	
	
	@RequestMapping(value="/certified_product_details", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetails getCertifiedProductDetails(@RequestParam("productId") Long id) throws EntityRetrievalException{
		
		CertifiedProductSearchDetails product = certifiedProductDetailsManager.getCertifiedProductDetails(id);
		return product;
	}
	
	@RequestMapping(value="/search", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchResponse simpleSearch(
			@RequestParam("searchTerm") String searchTerm, 
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber, 
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "orderBy", required = false) String orderBy,
			@RequestParam(value = "sortDescending", required = false) Boolean sortDescending
			) throws EntityRetrievalException {
		
		
		if (pageNumber == null){
			pageNumber = 0;
		}
		
		if (pageSize == null){
			pageSize = 20;
		}
		
		SearchRequest searchRequest = new SearchRequest();
		
		searchRequest.setPageNumber(pageNumber);
		searchRequest.setPageSize(pageSize);
		
		if (orderBy != null){
			searchRequest.setOrderBy(orderBy);
		}
		
		if (sortDescending != null){
			searchRequest.setSortDescending(sortDescending);
		}
		
		return certifiedProductSearchManager.search(searchRequest);
		
	}
	
	@RequestMapping(value="/search", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchResponse advancedSearch(
			@RequestBody SearchRequest searchFilters) {
		return certifiedProductSearchManager.search(searchFilters);
	}
	
	@RequestMapping(value="/list_classification_names", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<String> getClassificationNames() {
		return searchMenuManager.getClassificationNames();
	}
	
	@RequestMapping(value="/list_edition_names", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<String> getEditionNames() {
		return searchMenuManager.getEditionNames();
	}
	
	@RequestMapping(value="/list_practice_types", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<String> getPracticeTypeNames() {
		return searchMenuManager.getPracticeTypeNames();
	}
	
	@RequestMapping(value="/list_product_names", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<String> getProductNames() {
		return searchMenuManager.getProductNames();
	}
	
	@RequestMapping(value="/list_vendor_names", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<String> getVendorNames() {
		return searchMenuManager.getVendorNames();
	}
	
	@RequestMapping(value="/list_certification_body_names", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<String> getCertBodyNames() {
		return searchMenuManager.getCertBodyNames();
	}
	
	@RequestMapping(value="/populate_search_options", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody PopulateSearchOptions getPopulateSearchData() {
		return searchMenuManager.getPopulateSearchOptions();
	}
	
}
