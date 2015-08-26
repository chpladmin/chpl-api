package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductSearchDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;

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
	private CertifiedProductSearchManager certifiedProductSearchManager;
	
	@Autowired
	private CertificationBodyManager certificationBodyManager;
	
	@Autowired
	private CertifiedProductSearchDetailsManager certifiedProductSearchDetailsManager;
	
	
	@RequestMapping(value="/certified_product_details", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetails getCertifiedProductDetails(@RequestParam("productId") Long id) throws EntityRetrievalException{
		
		CertifiedProductSearchDetails product = certifiedProductSearchDetailsManager.getCertifiedProductDetails(id);
		return product;
	}
	
	@RequestMapping(value="/list_certified_products", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<CertifiedProductSearchResult> listCertifiedProducts(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize) throws EntityRetrievalException {
		return certifiedProductSearchDetailsManager.getCertifiedProducts(pageNum, pageSize);
	}
	
	@RequestMapping(value="/simple_search", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<CertifiedProductSearchResult> simpleSearch(
			@RequestParam("searchTerm") String searchTerm, 
			@RequestParam("pageNum") Integer pageNum, 
			@RequestParam("pageSize") Integer pageSize,
			@RequestParam(value="orderBy", required = false) String orderBy
			) throws EntityRetrievalException {
		if (orderBy != null){
			return certifiedProductSearchDetailsManager.simpleSearch(searchTerm, pageNum, pageSize, orderBy);
		} else {
			return certifiedProductSearchDetailsManager.simpleSearch(searchTerm, pageNum, pageSize);
		}
		
	}
	
	
	
	@RequestMapping(value="/advanced_search", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<CertifiedProductSearchResult> advancedSearch(
			@RequestBody SearchRequest searchFilters, 
			@RequestParam("pageNum") Integer pageNum, 
			@RequestParam("pageSize") Integer pageSize) {
		return certifiedProductSearchDetailsManager.multiFilterSearch(searchFilters, pageNum, pageSize);
	}
	
	
	//TODO: Experimental
	@RequestMapping(value="/advanced_search", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<CertifiedProductSearchResult> advancedSearch(
			@RequestParam("pageNum") Integer pageNum, 
			@RequestParam("pageSize") Integer pageSize,
			@RequestParam("vendor") String vendor,
			@RequestParam("product") String product,
			@RequestParam("version") String version,
			@RequestParam("certificationCriteria") List<String> certificationCriteria,
			@RequestParam("cqms") List<String> cqms,
			@RequestParam("certificationEdition") String certificationEdition,
			@RequestParam("productClassification") String productClassification,
			@RequestParam("practiceType") String practiceType
			
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
		
		return certifiedProductSearchDetailsManager.multiFilterSearch(searchFilters, pageNum, pageSize);
	}
	
	
	@RequestMapping(value="/list_certs", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<CertificationResult> getCertifications() {
		return certifiedProductSearchManager.getCertifications();
	}
	
	@RequestMapping(value="/list_cqms", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<CQMResultDetails> getCQMResults() {
		return certifiedProductSearchManager.getCQMResults();
	}
	
	@RequestMapping(value="/list_classification_names", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<String> getClassificationNames() {
		return certifiedProductSearchManager.getClassificationNames();
	}
	
	@RequestMapping(value="/list_edition_names", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<String> getEditionNames() {
		return certifiedProductSearchManager.getEditionNames();
	}
	
	@RequestMapping(value="/list_practice_types", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<String> getPracticeTypeNames() {
		return certifiedProductSearchManager.getPracticeTypeNames();
	}
	
	@RequestMapping(value="/list_product_names", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<String> getProductNames() {
		return certifiedProductSearchManager.getProductNames();
	}
	
	@RequestMapping(value="/list_vendor_names", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<String> getVendorNames() {
		return certifiedProductSearchManager.getVendorNames();
	}
	
	@RequestMapping(value="/list_certification_body_names", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<String> getCertBodyNames() {
		return certifiedProductSearchManager.getCertBodyNames();
	}
	
	public CertificationBodyManager getCertificationBodyManager() {
		return certificationBodyManager;
	}

	public void setCertificationBodyManager(
			CertificationBodyManager certificationBodyManager) {
		this.certificationBodyManager = certificationBodyManager;
	}
	
}
