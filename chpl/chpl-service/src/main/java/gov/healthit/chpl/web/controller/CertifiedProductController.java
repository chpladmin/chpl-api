package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.manager.CertifiedProductManager;

@RestController
@RequestMapping("/certified_product")
public class CertifiedProductController {
	
	@Autowired
	CertifiedProductManager cpManager;
	
	@RequestMapping(value="/get_certified_product", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProduct getCertifiedProductById(@RequestParam(required=true) Long certifiedProductId) throws EntityRetrievalException {
		CertifiedProductDTO certifiedProduct = cpManager.getById(certifiedProductId);
		
		CertifiedProduct result = null;
		if(certifiedProduct != null) {
			result = new CertifiedProduct(certifiedProduct);
		}
		return result;
	}
	
	@RequestMapping(value="/list_certified_product_by_product_version", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<CertifiedProduct> getCertifiedProductsByProductVersion(@RequestParam(required=true) Long productVersionId) {
		List<CertifiedProductDTO> certifiedProductList = cpManager.getByProductVersion(productVersionId);		
		
		List<CertifiedProduct> certifiedProducts = new ArrayList<CertifiedProduct>();
		if(certifiedProductList != null && certifiedProductList.size() > 0) {
			for(CertifiedProductDTO dto : certifiedProductList) {
				CertifiedProduct result = new CertifiedProduct(dto);
				certifiedProducts.add(result);
			}
		}
		return certifiedProducts;
	}
	/*
	@RequestMapping(value="/update_version", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public ProductVersion updateVersion(@RequestBody(required=true) UpdateVersionRequest versionInfo) throws 
		EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
		ProductVersionDTO result = null;
		
		if(versionInfo == null || versionInfo.getVersionId() == null) {
			throw new InvalidArgumentsException("versionId must be provided in the request body.");
		}
		
		ProductVersionDTO toUpdate = new ProductVersionDTO();
		toUpdate.setId(versionInfo.getVersionId());
		toUpdate.setVersion(versionInfo.getVersion());
		result = pvManager.update(toUpdate);

		if(result == null) {
			throw new EntityCreationException("There was an error inserting or updating the version information.");
		}
		return new ProductVersion(result);
		
	}
	*/
}
