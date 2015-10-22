package gov.healthit.chpl.web.controller;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.certifiedProduct.upload.CertifiedProductUploadHandler;
import gov.healthit.chpl.certifiedProduct.upload.CertifiedProductUploadHandlerFactory;
import gov.healthit.chpl.certifiedProduct.upload.CertifiedProductUploadType;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.AdditionalSoftware;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.web.controller.results.PendingCertifiedProductResults;

@RestController
@RequestMapping("/certified_products")
public class CertifiedProductController {
	
	private static final Logger logger = LogManager.getLogger(CertifiedProductController.class);

	@Autowired CertifiedProductUploadHandlerFactory uploadHandlerFactory;
	@Autowired CertifiedProductDetailsManager cpdManager;
	@Autowired CertifiedProductManager cpManager;
	@Autowired PendingCertifiedProductManager pcpManager;
	@Autowired CertificationBodyManager acbManager;
	
	@RequestMapping(value="/", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<CertifiedProduct> getCertifiedProductsByVersion(@RequestParam(required=false) Long versionId) {
		List<CertifiedProductDTO> certifiedProductList = null;
		
		if(versionId != null && versionId > 0) {
			certifiedProductList = cpManager.getByVersion(versionId);
		} else {
			certifiedProductList = cpManager.getAll();
		}
		
		List<CertifiedProduct> products= new ArrayList<CertifiedProduct>();
		if(certifiedProductList != null && certifiedProductList.size() > 0) {
			for(CertifiedProductDTO dto : certifiedProductList) {
				CertifiedProduct result = new CertifiedProduct(dto);
				products.add(result);
			}
		}
		return products;
	}
	
	@RequestMapping(value="/{certifiedProductId}/details", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetails getCertifiedProductById(@PathVariable("certifiedProductId") Long certifiedProductId) throws EntityRetrievalException {
		CertifiedProductSearchDetails certifiedProduct = cpdManager.getCertifiedProductDetails(certifiedProductId);
		
		return certifiedProduct;
	}
	
	@RequestMapping(value="/update", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetails updateCertifiedProduct(@RequestBody(required=true) CertifiedProductSearchDetails updateRequest) 
		throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		CertifiedProductDTO existingProduct = cpManager.getById(updateRequest.getId());
		Long acbId = existingProduct.getCertificationBodyId();
		Long newAcbId = new Long(updateRequest.getCertifyingBody().get("id").toString());
		
		if(acbId != newAcbId) {
			cpManager.changeOwnership(updateRequest.getId(), newAcbId);
		}
		
		CertifiedProductDTO toUpdate = new CertifiedProductDTO();
		toUpdate.setId(updateRequest.getId());
		toUpdate.setTestingLabId(updateRequest.getTestingLabId());
		toUpdate.setCertificationBodyId(newAcbId);
		toUpdate.setPracticeTypeId(new Long(updateRequest.getPracticeType().get("id").toString()));
		toUpdate.setProductClassificationTypeId(new Long(updateRequest.getClassificationType().get("id").toString()));
		toUpdate.setCertificationStatusId(new Long(updateRequest.getCertificationStatus().get("id").toString()));
		toUpdate.setChplProductNumber(updateRequest.getChplProductNumber());
		toUpdate.setReportFileLocation(updateRequest.getReportFileLocation());
		toUpdate.setQualityManagementSystemAtt(updateRequest.getQualityManagementSystemAtt());
		toUpdate.setAcbCertificationId(updateRequest.getAcbCertificationId());
		toUpdate.setOtherAcb(updateRequest.getOtherAcb());
		toUpdate.setVisibleOnChpl(updateRequest.getVisibleOnChpl());
		toUpdate = cpManager.update(acbId, toUpdate);
		
		//update additional software
		List<AdditionalSoftwareDTO> softwareDtos = new ArrayList<AdditionalSoftwareDTO>();
		for(AdditionalSoftware software : updateRequest.getAdditionalSoftware()) {
			AdditionalSoftwareDTO softwareDto = new AdditionalSoftwareDTO();
			softwareDto.setCertifiedProductId(toUpdate.getId());
			softwareDto.setJustification(software.getJustification());
			softwareDto.setName(software.getName());
			softwareDto.setVersion(software.getVersion());
			softwareDtos.add(softwareDto);
		}
		cpManager.replaceAdditionalSoftware(acbId, toUpdate, softwareDtos);
		
		//update product certifications
		Map<CertificationCriterionDTO, Boolean> newCerts = new HashMap<CertificationCriterionDTO, Boolean>();
		for(CertificationResult certResult : updateRequest.getCertificationResults()) {
			CertificationCriterionDTO newCert = new CertificationCriterionDTO();
			newCert.setNumber(certResult.getNumber());
			newCert.setTitle(certResult.getTitle());
			newCerts.put(newCert, certResult.isSuccess());
		}
		cpManager.replaceCertifications(acbId, toUpdate, newCerts);
		
		//update product cqms
		Map<CQMCriterionDTO, Boolean> cqmDtos = new HashMap<CQMCriterionDTO, Boolean>();
		for(CQMResultDetails cqm : updateRequest.getCqmResults()) {
			CQMCriterionDTO cqmDto = new CQMCriterionDTO();
			cqmDto.setCqmVersion(cqm.getVersion());
			cqmDto.setNumber(cqm.getNumber());
			cqmDto.setTitle(cqm.getTitle());
			cqmDtos.put(cqmDto, cqm.isSuccess());
		}
		cpManager.replaceCqms(acbId, toUpdate, cqmDtos);
		
		//search for the product by id to get it with all the updates
		return cpdManager.getCertifiedProductDetails(toUpdate.getId());
	}
	
	@RequestMapping(value="/pending", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody PendingCertifiedProductResults getPendingCertifiedProducts() throws EntityRetrievalException {
		List<PendingCertifiedProductDetails> products = new ArrayList<PendingCertifiedProductDetails>();
		
		List<PendingCertifiedProductDTO> productDtos = pcpManager.getPending();
		for(PendingCertifiedProductDTO dto : productDtos) {
			PendingCertifiedProductDetails details = new PendingCertifiedProductDetails(dto);
			details.setApplicableCqmCriteria(pcpManager.getApplicableCriteria(dto));
			products.add(details);
		}		
		
		PendingCertifiedProductResults results = new PendingCertifiedProductResults();
		results.getPendingCertifiedProducts().addAll(products);
		return results;
	}
	
	@RequestMapping(value="/pending/{pcpId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody PendingCertifiedProductDetails getPendingCertifiedProductById(@PathVariable("pcpId") Long pcpId) throws EntityRetrievalException {
		List<PendingCertifiedProductDetails> products = new ArrayList<PendingCertifiedProductDetails>();

		PendingCertifiedProductDTO dto = pcpManager.getById(pcpId);
		PendingCertifiedProductDetails details = new PendingCertifiedProductDetails(dto);
		details.setApplicableCqmCriteria(pcpManager.getApplicableCriteria(dto));
		
		return details;
	}
	
	@RequestMapping(value="/pending/{pcpId}/reject", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody String rejectPendingCertifiedProducts(@PathVariable("pcpId") Long id) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		pcpManager.reject(id);
		return "{\"success\" : true }";
	}
	
	@RequestMapping(value="/pending/confirm", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetails confirmPendingCertifiedProduct(@RequestBody(required = true) PendingCertifiedProductDetails pendingCp) 
		throws InvalidArgumentsException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
		String acbIdStr = pendingCp.getCertifyingBody().get("id").toString();
		if(StringUtils.isEmpty(acbIdStr)) {
			throw new InvalidArgumentsException("An ACB ID must be supplied in the request body");
		}
		Long acbId = new Long(acbIdStr);
		CertifiedProductDTO createdProduct = cpManager.createFromPending(acbId, pendingCp);
		pcpManager.confirm(pendingCp.getId());
		
		CertifiedProductSearchDetails result = cpdManager.getCertifiedProductDetails(createdProduct.getId());
		return result;
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
						PendingCertifiedProductDTO pendingCpDto = null;
						
						CertifiedProductUploadType uploadType = CertifiedProductUploadType.valueOf(pendingCp.getRecordStatus().toUpperCase());
						if(uploadType == CertifiedProductUploadType.NEW) { 
							if(pendingCp.getCertificationBodyId() == null) {
								throw new IllegalArgumentException("Could not find certifying body with name " + pendingCp.getCertificationBodyName() + ". Aborting upload.");
							}
							pendingCpDto = pcpManager.create(pendingCp.getCertificationBodyId(), pendingCp);
						} else {
							pendingCpDto = new PendingCertifiedProductDTO(pendingCp);
						}
						
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