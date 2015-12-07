package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductDownloadDetails;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.SearchMenuManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
	
	@Autowired
	private CertifiedProductManager cpManager;
	
	private static final Logger logger = LogManager.getLogger(SearchViewController.class);

	
	@RequestMapping(value="/certified_product_details", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetails getCertifiedProductDetails(@RequestParam("productId") Long id) throws EntityRetrievalException{
		
		CertifiedProductSearchDetails product = certifiedProductDetailsManager.getCertifiedProductDetails(id);
		return product;
	}

	/**
	 * don't want this to be accessible right now, but am leaving the code in here because we will need it at some point.
	 */
	@RequestMapping(value="/downloadSome", method=RequestMethod.GET,
			produces="application/xml")
	public ResponseEntity<CertifiedProductDownloadResponse> downloadSome() throws EntityRetrievalException {
		List<CertifiedProductDTO> someCertifiedProducts = new ArrayList<CertifiedProductDTO>();
		someCertifiedProducts.add(cpManager.getById(1L));
		someCertifiedProducts.add(cpManager.getById(2L));
		someCertifiedProducts.add(cpManager.getById(3L));
		someCertifiedProducts.add(cpManager.getById(4L));
		someCertifiedProducts.add(cpManager.getById(5L));
		someCertifiedProducts.add(cpManager.getById(6L));
		someCertifiedProducts.add(cpManager.getById(7L));
		someCertifiedProducts.add(cpManager.getById(8L));
		someCertifiedProducts.add(cpManager.getById(9L));
		someCertifiedProducts.add(cpManager.getById(10L));
		
		CertifiedProductDownloadResponse result = new CertifiedProductDownloadResponse();
		for(CertifiedProductDTO cp : someCertifiedProducts) {
			try {
				//CertifiedProductDownloadDetails details = certifiedProductDetailsManager.getCertifiedProductDownloadDetails(cp.getId());
				CertifiedProductDownloadDetails details = certifiedProductDetailsManager.getCertifiedProductDownloadDetails(cp.getId());
				result.getProducts().add(details);
			} catch(EntityRetrievalException ex) {
				logger.error("Could not certified product details for certified product " + cp.getId());
			}
		}
		
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentDispositionFormData("attachment", "certified_products.xml");
	    return new ResponseEntity<CertifiedProductDownloadResponse>(result, responseHeaders, HttpStatus.OK);
	}
	
	@RequestMapping(value="/search", method=RequestMethod.GET,
			produces={"application/json; charset=utf-8", "application/xml"})
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
		
		searchRequest.setSearchTerm(searchTerm);
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
	
	@RequestMapping(value="/data/classification_types", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getClassificationNames() {
		return searchMenuManager.getClassificationNames();
	}
	
	@RequestMapping(value="/data/certification_editions", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getEditionNames() {
		return searchMenuManager.getEditionNames(false);
	}
	
	@RequestMapping(value="/data/certification_statuses", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getCertificationStatuses() {
		return searchMenuManager.getCertificationStatuses();
	}
	
	@RequestMapping(value="/data/practice_types", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getPracticeTypeNames() {
		return searchMenuManager.getPracticeTypeNames();
	}
	
	@RequestMapping(value="/data/products", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getProductNames() {
		return searchMenuManager.getProductNames();
	}
	
	@RequestMapping(value="/data/vendors", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getVendorNames() {
		return searchMenuManager.getVendorNames();
	}
	
	@RequestMapping(value="/data/certification_bodies", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getCertBodyNames() {
		return searchMenuManager.getCertBodyNames();
	}
	
	@RequestMapping(value="/data/search_options", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody PopulateSearchOptions getPopulateSearchData(
			@RequestParam(value = "simple", required = false) Boolean simple
			) throws EntityRetrievalException {
		if (simple == null){
			simple = false;
		}
		return searchMenuManager.getPopulateSearchOptions(simple);
	}
	
}
