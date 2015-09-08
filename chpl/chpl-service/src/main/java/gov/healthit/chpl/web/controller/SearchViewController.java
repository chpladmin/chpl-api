package gov.healthit.chpl.web.controller;

import java.util.List;
import java.util.Set;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.manager.CertificationBodyManager;
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
	
	
	@RequestMapping(value="/certified_product_details", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetails getCertifiedProductDetails(@RequestParam("productId") Long id) throws EntityRetrievalException{
		
		CertifiedProductSearchDetails product = certifiedProductSearchManager.getCertifiedProductDetails(id);
		return product;
	}
	
	@RequestMapping(value="/simple_search", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchResponse simpleSearch(
			@RequestParam("searchTerm") String searchTerm, 
			@RequestParam(value = "pageNum", required = false) Integer pageNum, 
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "orderBy", required = false) String orderBy,
			@RequestParam(value = "sortDescending", required = false) Boolean sortDescending
			) throws EntityRetrievalException {
		
		
		if (pageNum == null){
			pageNum = 0;
		}
		
		if (pageSize == null){
			pageSize = 20;
		}
		
		if (orderBy != null){
			
			if (sortDescending != null){
				return certifiedProductSearchManager.simpleSearch(searchTerm, pageNum, pageSize, orderBy, sortDescending);
			} else {
				return certifiedProductSearchManager.simpleSearch(searchTerm, pageNum, pageSize, orderBy, false);
			}
			
		} else {
			return certifiedProductSearchManager.simpleSearch(searchTerm, pageNum, pageSize);
		}
		
	}
	
	
	@RequestMapping(value="/advanced_search", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchResponse advancedSearch(
			@RequestBody SearchRequest searchFilters, 
			@RequestParam("pageNum") Integer pageNum, 
			@RequestParam("pageSize") Integer pageSize) {
		return certifiedProductSearchManager.multiFilterSearch(searchFilters, pageNum, pageSize);
	}
	
	
	//TODO: Experimental
	@RequestMapping(value="/advanced_search", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchResponse advancedSearch(
			@RequestParam(value = "pageNum") Integer pageNum, 
			@RequestParam(value = "pageSize") Integer pageSize,
			@RequestParam(value = "vendor", required = false) String vendor,
			@RequestParam(value = "product", required = false) String product,
			@RequestParam(value = "version", required = false) String version,
			@RequestParam(value = "certificationCriteria", required = false) List<String> certificationCriteria,
			@RequestParam(value = "cqms", required = false) List<String> cqms,
			@RequestParam(value = "certificationEdition", required = false) String certificationEdition,
			@RequestParam(value = "productClassification", required = false) String productClassification,
			@RequestParam(value = "practiceType", required = false) String practiceType
			
			) throws EntityRetrievalException {
		
		SearchRequest searchFilters = new SearchRequest();
		searchFilters.setVendor(vendor);
		searchFilters.setProduct(product);
		searchFilters.setVersion(version);
		searchFilters.setCertificationCriteria(certificationCriteria);
		searchFilters.setCqms(cqms);
		searchFilters.setCertificationEdition(certificationEdition);
		searchFilters.setProductClassification(productClassification);
		searchFilters.setPracticeType(practiceType);
		
		return certifiedProductSearchManager.multiFilterSearch(searchFilters, pageNum, pageSize);
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
