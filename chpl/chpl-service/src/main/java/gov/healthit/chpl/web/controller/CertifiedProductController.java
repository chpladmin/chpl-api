package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.MultipartConfig;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.UpdateCertifiedProductRequest;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.web.controller.results.PendingCertifiedProductResults;

@RestController
@RequestMapping("/certified_product")
public class CertifiedProductController {
	
	private static final Logger logger = LogManager.getLogger(CertifiedProductController.class);

	@Autowired CertifiedProductDetailsManager cpdManager;
	@Autowired CertifiedProductManager cpManager;
	@Autowired PendingCertifiedProductManager pcpManager;
	@Autowired CertificationBodyManager acbManager;
	
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
	public @ResponseBody CertifiedProductSearchDetails updateCertifiedProduct(@RequestBody(required=true) UpdateCertifiedProductRequest updateRequest) 
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
		toUpdate.setVisibleOnChpl(updateRequest.getVisibleOnChpl());
		
		toUpdate = cpManager.update(toUpdate);
		
		//search for the product by id to get it with all the updates
		return cpdManager.getCertifiedProductDetails(toUpdate.getId());
	}
	
	
	//TODO: need another call to update 2011 cqms, 2014 cmqs, and certifications
	
	@RequestMapping(value="/get_pending", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody PendingCertifiedProductResults getPendingCertifiedProducts(@RequestParam(required=false) Long certifiedProductId) throws EntityRetrievalException {
		List<PendingCertifiedProductDetails> products = null;
		
		if(certifiedProductId == null || certifiedProductId < 0) {
			products = pcpManager.getAllDetails();
		} else {
			PendingCertifiedProductDetails product = pcpManager.getDetailsById(certifiedProductId);
			products = new ArrayList<PendingCertifiedProductDetails>();
			products.add(product);
		}
		
		PendingCertifiedProductResults results = new PendingCertifiedProductResults();
		results.getPendingCertifiedProducts().addAll(products);
		return results;
	}
	
	@RequestMapping(value="/upload", method=RequestMethod.POST,
			produces="application/json; charset=utf-8") 
	public @ResponseBody PendingCertifiedProductResults upload(@RequestParam("file") MultipartFile file) throws 
			InvalidArgumentsException, MaxUploadSizeExceededException, Exception {
		if (file.isEmpty()) {
			throw new InvalidArgumentsException("You cannot upload an empty file!");
		}
		
		if(!file.getContentType().equalsIgnoreCase("text/csv") &&
				!file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
			throw new InvalidArgumentsException("File must be a CSV or Excel document.");
		}
		
		List<PendingCertifiedProductDetails> uploadedProducts = pcpManager.upload(file);
		
		PendingCertifiedProductResults results = new PendingCertifiedProductResults();
		results.getPendingCertifiedProducts().addAll(uploadedProducts);
		return results;
	}
}
