package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.UpdateCertifiedProductRequest;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/certified_product")
public class CertifiedProductController {
	
	@Autowired
	CertifiedProductDetailsManager cpdManager;
	
	@Autowired
	CertifiedProductManager cpManager;
	
	@RequestMapping(value="/get_certified_product", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetails getCertifiedProductById(@RequestParam(required=true) Long certifiedProductId) throws EntityRetrievalException {
		CertifiedProductSearchDetails certifiedProduct = cpdManager.getCertifiedProductDetails(certifiedProductId);
		
		return certifiedProduct;
	}

	@RequestMapping(value="/list_certified_products_by_version", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<CertifiedProduct> getCertifiedProductsByVersions(@RequestParam(required=true) Long versionId) {
		List<CertifiedProductDTO> certifiedProductList = cpManager.getByVersion(versionId);		
		
		List<CertifiedProduct> products= new ArrayList<CertifiedProduct>();
		if(certifiedProductList != null && certifiedProductList.size() > 0) {
			for(CertifiedProductDTO dto : certifiedProductList) {
				CertifiedProduct result = new CertifiedProduct(dto);
				products.add(result);
			}
		}
		return products;
	}
	
	@RequestMapping(value="/update", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProduct updateCertifiedProduct(@RequestBody(required=true) UpdateCertifiedProductRequest updateRequest) 
		throws EntityRetrievalException {
		
		CertifiedProductDTO toUpdate = new CertifiedProductDTO();
		toUpdate.setId(updateRequest.getId());
		toUpdate.setTestingLabId(updateRequest.getTestingLabId());
		toUpdate.setCertificationBodyId(updateRequest.getCertificationBodyId());
		toUpdate.setPracticeTypeId(updateRequest.getPracticeTypeId());
		toUpdate.setProductClassificationTypeId(updateRequest.getProductClassificationTypeId());
		toUpdate.setCertificationStatusId(updateRequest.getCertificationStatusId());
		toUpdate.setChplProductNumber(updateRequest.getChplProductNumber());
		toUpdate.setReportFileLocation(updateRequest.getReportFileLocation());
		toUpdate.setQualityManagementSystemAtt(updateRequest.getQualityManagementSystemAtt());
		toUpdate.setAcbCertificationId(updateRequest.getAcbCertificationId());
		toUpdate.setOtherAcb(updateRequest.getOtherAcb());
		
		//TODO
		//toUpdate.setIsChplVisible(updateRequest.getIsChplVisible());
		
		toUpdate = cpManager.update(toUpdate);
		
		return new CertifiedProduct(toUpdate);
	}
}
