package gov.healthit.chpl.web.controller;

import java.util.List;

import gov.healthit.chpl.acb.CertificationBodyManager;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.entity.CertificationBody;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchViewController {
	
	
	@Autowired
	CertifiedProductSearchManager certifiedProductSearchManager;
	
	@Autowired
	private CertificationBodyManager certificationBodyManager;
	
	
	@RequestMapping(value="/certified_product_details", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetails getCertifiedProductDetails(@RequestParam("productId") Long id) throws UserRetrievalException {
		
		CertifiedProductSearchDetails product = certifiedProductSearchManager.getDetails(id);
		
		return product;
	}
	
	@RequestMapping(value="/certified_products", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<CertifiedProductSearchResult> getCertifiedProducts() throws UserRetrievalException {
		
		return certifiedProductSearchManager.getAll();
	}
	
	public CertificationBodyManager getCertificationBodyManager() {
		return certificationBodyManager;
	}


	public void setCertificationBodyManager(
			CertificationBodyManager certificationBodyManager) {
		this.certificationBodyManager = certificationBodyManager;
	}
	
}
