package gov.healthit.chpl.web.controller;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.MultipartConfig;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.certifiedProduct.upload.CertifiedProductUploadHandler;
import gov.healthit.chpl.certifiedProduct.upload.CertifiedProductUploadHandlerFactory;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.UpdateCertifiedProductRequest;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.web.controller.results.PendingCertifiedProductResults;

@RestController
@RequestMapping("/certified_product")
public class CertifiedProductController {
	
	private static final Logger logger = LogManager.getLogger(CertifiedProductController.class);

	@Autowired CertifiedProductUploadHandlerFactory uploadHandlerFactory;
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
		List<PendingCertifiedProductDetails> products = new ArrayList<PendingCertifiedProductDetails>();
		
		if(certifiedProductId == null || certifiedProductId < 0) {
			List<PendingCertifiedProductDTO> productDtos = pcpManager.getAll();
			for(PendingCertifiedProductDTO dto : productDtos) {
				PendingCertifiedProductDetails details = new PendingCertifiedProductDetails(dto);
				details.setApplicableCqmCriteria(pcpManager.getApplicableCriteria(dto));
				products.add(details);
			}
		} else {
			PendingCertifiedProductDTO dto = pcpManager.getById(certifiedProductId);
			PendingCertifiedProductDetails details = new PendingCertifiedProductDetails(dto);
			details.setApplicableCqmCriteria(pcpManager.getApplicableCriteria(dto));
			products.add(details);
		}
		
		PendingCertifiedProductResults results = new PendingCertifiedProductResults();
		results.getPendingCertifiedProducts().addAll(products);
		return results;
	}
	
	@RequestMapping(value="/reject_pending", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody String rejectPendingCertifiedProducts(@RequestParam(required=true) Long id) throws EntityRetrievalException {
		pcpManager.reject(id);
		return "{\"success\" : true }";
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
		
		List<PendingCertifiedProductDetails> uploadedProducts = new ArrayList<PendingCertifiedProductDetails>();
		
		BufferedReader reader = null;
		CSVParser parser = null;
		try {
			reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			parser = new CSVParser(reader, CSVFormat.EXCEL);
			
			List<CSVRecord> records = parser.getRecords();
			if(records.size() <= 1) {
				throw new InvalidArgumentsException("The file appears to have a header line with no other information. Please make sure there are at least two rows in the CSV file.");
			}
			
			CSVRecord heading = records.get(0);
			for(int i = 1; i < records.size(); i++) {
				CSVRecord record = records.get(i);
				
				//some rows may be blank, we just look at the first column to see if it's empty or not
				if(!StringUtils.isEmpty(record.get(0))) {
					CertifiedProductUploadHandler handler = uploadHandlerFactory.getHandler(heading, record);
				
					//create a certified product to pass into the handler
					try {
						PendingCertifiedProductEntity pendingCp = handler.handle();
						if(pendingCp.getCertificationBodyId() == null) {
							throw new IllegalArgumentException("Could not find certifying body with name " + pendingCp.getCertificationBodyName() + ". Aborting upload.");
						}
						
						PendingCertifiedProductDTO pendingCpDto = pcpManager.create(pendingCp.getCertificationBodyId(), pendingCp);
						PendingCertifiedProductDetails details = new PendingCertifiedProductDetails(pendingCpDto);
						//set applicable criteria
						details.setApplicableCqmCriteria(pcpManager.getApplicableCriteria(pendingCpDto));
						uploadedProducts.add(details);
					} catch(EntityCreationException ex) {
						logger.error("could not create entity at row " + i + ". Message is " + ex.getMessage());
					}
				}
			}
		} catch(IOException ioEx) {
			logger.error("Could not get input stream for uploaded file " + file.getName());
			throw new IOException("Could not get input stream for uploaded file " + file.getName());
		} finally {
			 try { parser.close(); } catch(Exception ignore) {}
			try { reader.close(); } catch(Exception ignore) {}
		}
		
		PendingCertifiedProductResults results = new PendingCertifiedProductResults();
		results.getPendingCertifiedProducts().addAll(uploadedProducts);
		return results;
	}
}
