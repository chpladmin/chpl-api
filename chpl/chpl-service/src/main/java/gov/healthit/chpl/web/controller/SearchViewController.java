package gov.healthit.chpl.web.controller;

import java.util.List;

import gov.healthit.chpl.acb.CertificationBodyManager;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.entity.CertificationBody;
import gov.healthit.chpl.json.CertificationJSONObject;
import gov.healthit.chpl.json.CertifiedProductSearchDetailsJSONObject;

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
	private CertificationBodyManager certificationBodyManager;
	
	
	@RequestMapping(value="/certified_product", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetailsJSONObject getCertifiedProduct(@RequestParam("productId") Long id) throws UserRetrievalException {
		
		CertifiedProductSearchDetailsJSONObject product = new CertifiedProductSearchDetailsJSONObject();
		
		return null;
	}
	
	@RequestMapping(value="/certified_products", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<CertifiedProductSearchDetailsJSONObject> getCertifiedProducts() throws UserRetrievalException {
		
		CertifiedProductSearchDetailsJSONObject product = new CertifiedProductSearchDetailsJSONObject();
		
		return null;
	}
	
	public CertificationBodyManager getCertificationBodyManager() {
		return certificationBodyManager;
	}


	public void setCertificationBodyManager(
			CertificationBodyManager certificationBodyManager) {
		this.certificationBodyManager = certificationBodyManager;
	}
	
}
