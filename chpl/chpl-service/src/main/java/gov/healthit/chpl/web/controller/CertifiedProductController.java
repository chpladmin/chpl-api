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
import gov.healthit.chpl.certifiedProduct.validation.CertifiedProductValidator;
import gov.healthit.chpl.certifiedProduct.validation.CertifiedProductValidatorFactory;
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
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.web.controller.results.PendingCertifiedProductResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="certified-products")
@RestController
@RequestMapping("/certified_products")
public class CertifiedProductController {
	
	private static final Logger logger = LogManager.getLogger(CertifiedProductController.class);

	@Autowired CertifiedProductUploadHandlerFactory uploadHandlerFactory;
	@Autowired CertifiedProductDetailsManager cpdManager;
	@Autowired CertifiedProductManager cpManager;
	@Autowired PendingCertifiedProductManager pcpManager;
	@Autowired CertificationBodyManager acbManager;
	@Autowired CertifiedProductValidatorFactory validatorFactory;

	@ApiOperation(value="List all certified products", 
			notes="Default behavior is to return all certified products in the system. "
					+ " The optional 'versionId' parameter filters the certified products to those"
					+ " assigned to that version. The 'editable' parameter will return only those"
					+ " certified products that the logged in user has permission to edit as "
					+ " determined by ACB roles and authorities. Not all information about "
					+ " every certified product is returned. Call the /details service for more information.")
	@RequestMapping(value="/", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<CertifiedProduct> getCertifiedProductsByVersion(
			@RequestParam(required=false) Long versionId, @RequestParam(required=false, defaultValue="false") boolean editable) {
		List<CertifiedProductDetailsDTO> certifiedProductList = null;
		
		if(versionId != null && versionId > 0) {
			if(editable) {
				certifiedProductList = cpManager.getByVersionWithEditPermission(versionId);
			} else {
				certifiedProductList = cpManager.getByVersion(versionId);
			}
		} else {
			if(editable) {
				certifiedProductList = cpManager.getAllWithEditPermission();
			} else {
				certifiedProductList = cpManager.getAll();
			}
		}
		
		List<CertifiedProduct> products= new ArrayList<CertifiedProduct>();
		if(certifiedProductList != null && certifiedProductList.size() > 0) {
			for(CertifiedProductDetailsDTO dto : certifiedProductList) {
				CertifiedProduct result = new CertifiedProduct(dto);
				products.add(result);
			}
		}
		return products;
	}
	
	@ApiOperation(value="Get all details for a specified certified product.", 
			notes="Returns all information in the CHPL related to the specified certified product.")
	@RequestMapping(value="/{certifiedProductId}/details", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetails getCertifiedProductById(@PathVariable("certifiedProductId") Long certifiedProductId) throws EntityRetrievalException {
		CertifiedProductSearchDetails certifiedProduct = cpdManager.getCertifiedProductDetails(certifiedProductId);
		CertifiedProductValidator validator = validatorFactory.getValidator(certifiedProduct);
		if(validator != null) {
			validator.validate(certifiedProduct);
		}
		
		return certifiedProduct;
	}
	
	@ApiOperation(value="Update an existing certified product.", 
			notes="Updates the certified product after first validating the request. The logged in"
					+ " user must have ROLE_ADMIN or ROLE_ACB_ADMIN and have administrative "
					+ " authority on the ACB that certified the product. If a different ACB is passed in"
					+ " as part of the request, an ownership change will take place and the logged in "
					+ " user must have ROLE_ADMIN.")
	@RequestMapping(value="/update", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetails updateCertifiedProduct(@RequestBody(required=true) CertifiedProductSearchDetails updateRequest) 
		throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException, 
		JsonProcessingException, ValidationException {
		
		//make sure the ui didn't send any error or warning messages back
		updateRequest.setErrorMessages(new ArrayList<String>());
		updateRequest.setWarningMessages(new ArrayList<String>());
		//validate
		CertifiedProductValidator validator = validatorFactory.getValidator(updateRequest);
		if(validator != null) {
			validator.validate(updateRequest);
		}
		if(updateRequest.getErrorMessages() != null && updateRequest.getErrorMessages().size() > 0) {
			throw new ValidationException(updateRequest.getErrorMessages(), updateRequest.getWarningMessages());
		}
		
		CertifiedProductDTO existingProduct = cpManager.getById(updateRequest.getId());
		Long acbId = existingProduct.getCertificationBodyId();
		Long newAcbId = new Long(updateRequest.getCertifyingBody().get("id").toString());
		
		if(newAcbId != null && acbId.longValue() != newAcbId.longValue()) {
			cpManager.changeOwnership(updateRequest.getId(), newAcbId);
		}
		
		CertifiedProductDTO toUpdate = new CertifiedProductDTO();
		toUpdate.setId(updateRequest.getId());
		toUpdate.setTestingLabId(new Long(updateRequest.getTestingLab().get("id").toString()));
		toUpdate.setCertificationBodyId(newAcbId);
		toUpdate.setPracticeTypeId(new Long(updateRequest.getPracticeType().get("id").toString()));
		toUpdate.setProductClassificationTypeId(new Long(updateRequest.getClassificationType().get("id").toString()));
		toUpdate.setCertificationStatusId(new Long(updateRequest.getCertificationStatus().get("id").toString()));
		toUpdate.setReportFileLocation(updateRequest.getReportFileLocation());
		toUpdate.setAcbCertificationId(updateRequest.getAcbCertificationId());
		toUpdate.setOtherAcb(updateRequest.getOtherAcb());
		toUpdate.setVisibleOnChpl(updateRequest.getVisibleOnChpl());
		toUpdate.setPrivacyAttestation(updateRequest.getPrivacyAttestation());
		toUpdate.setApiDocumentation(updateRequest.getApiDocumentation());
		toUpdate.setTermsOfUse(updateRequest.getTermsOfUse());
		toUpdate.setIcs(updateRequest.getIcs());
		toUpdate.setSedTesting(updateRequest.getSedTesting());
		toUpdate.setQmsTestig(updateRequest.getQmsTesting());
		
		if(updateRequest.getCertificationEdition().get("name").equals("2011") ||
				updateRequest.getCertificationEdition().get("name").equals("2014")) {
			toUpdate.setChplProductNumber(updateRequest.getChplProductNumber());
			toUpdate.setProductCode(null);
			toUpdate.setVersionCode(null);
			toUpdate.setIcsCode(null);
			toUpdate.setAdditionalSoftwareCode(null);
			toUpdate.setCertifiedDateCode(null);
		} else {
			//parse out the components of the chpl id
			toUpdate.setChplProductNumber(null);
			String chplProductId = updateRequest.getChplProductNumber();
			String[] chplProductIdComponents = chplProductId.split("\\.");
			if(chplProductIdComponents == null || chplProductIdComponents.length != 8) {
				throw new InvalidArgumentsException("CHPL Product Id " + chplProductId + " is not in a format recognized by the system.");
			}
			toUpdate.setProductCode(chplProductIdComponents[3]);
			toUpdate.setVersionCode(chplProductIdComponents[4]);
			toUpdate.setIcsCode(chplProductIdComponents[5]);
			toUpdate.setAdditionalSoftwareCode(chplProductIdComponents[6]);
			toUpdate.setCertifiedDateCode(chplProductIdComponents[7]);
		}

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
		cpManager.updateAdditionalSoftware(acbId, toUpdate, softwareDtos);
		
		//update product certifications
		cpManager.updateCertifications(acbId, toUpdate, updateRequest.getCertificationResults());
		
		
		Map<CQMCriterionDTO, Boolean> cqmDtos = new HashMap<CQMCriterionDTO, Boolean>();
		for(CQMResultDetails cqm : updateRequest.getCqmResults()) {
			if(!StringUtils.isEmpty(cqm.getCmsId()) && cqm.getSuccessVersions() != null && cqm.getSuccessVersions().size() > 0) {
				for(String version : cqm.getSuccessVersions()) {
					CQMCriterionDTO cqmDto = new CQMCriterionDTO();
					cqmDto.setNqfNumber(cqm.getNqfNumber());
					cqmDto.setCmsId(cqm.getCmsId());
					cqmDto.setNumber(cqm.getNumber());
					cqmDto.setCmsId(cqm.getCmsId());
					cqmDto.setNqfNumber(cqm.getNqfNumber());
					cqmDto.setTitle(cqm.getTitle());
					cqmDto.setCqmVersion(version);
					cqmDtos.put(cqmDto, Boolean.TRUE);
				}
			} else if(StringUtils.isEmpty(cqm.getCmsId())) {
				CQMCriterionDTO cqmDto = new CQMCriterionDTO();
				cqmDto.setNqfNumber(cqm.getNqfNumber());
				cqmDto.setCmsId(cqm.getCmsId());
				cqmDto.setNumber(cqm.getNumber());
				cqmDto.setCmsId(cqm.getCmsId());
				cqmDto.setNqfNumber(cqm.getNqfNumber());
				cqmDto.setTitle(cqm.getTitle());
				cqmDtos.put(cqmDto, cqm.isSuccess());
			}
		}
		cpManager.updateCqms(acbId, toUpdate, cqmDtos);
		
		//search for the product by id to get it with all the updates
		return cpdManager.getCertifiedProductDetails(toUpdate.getId());
	}
	
	@ApiOperation(value="List pending certified products.", 
			notes="Pending certified products are created via CSV file upload and are left in the 'pending' state "
					+ " until validated and approved by an appropriate ACB administrator.")
	@RequestMapping(value="/pending", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody PendingCertifiedProductResults getPendingCertifiedProducts() throws EntityRetrievalException {		
		List<PendingCertifiedProductDTO> allProductDtos = pcpManager.getPending();
		
		List<PendingCertifiedProductDetails> result = new ArrayList<PendingCertifiedProductDetails>();
		for(PendingCertifiedProductDTO product : allProductDtos) {
			PendingCertifiedProductDetails pcpDetails = new PendingCertifiedProductDetails(product);
			pcpManager.addAllVersionsToCmsCriterion(pcpDetails);
			result.add(pcpDetails);
		}
		
		PendingCertifiedProductResults results = new PendingCertifiedProductResults();
		results.getPendingCertifiedProducts().addAll(result);
		return results;
	}
	
	@ApiOperation(value="List a specific pending certified product.", 
			notes="")
	@RequestMapping(value="/pending/{pcpId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody PendingCertifiedProductDetails getPendingCertifiedProductById(@PathVariable("pcpId") Long pcpId) throws EntityRetrievalException {
		PendingCertifiedProductDetails details = pcpManager.getById(pcpId);	
		return details;
	}
	
	@ApiOperation(value="Reject a pending certified product.", 
			notes="Essentially deletes a pending certified product. ROLE_ACB_ADMIN, ROLE_ACB_STAFF "
					+ " and administrative authority on the ACB is required.")
	@RequestMapping(value="/pending/{pcpId}/reject", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody String rejectPendingCertifiedProducts(@PathVariable("pcpId") Long id) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		pcpManager.reject(id);
		return "{\"success\" : true }";
	}
	
	@ApiOperation(value="Confirm a pending certified product.", 
			notes="Creates a new certified product in the system based on all of the information "
					+ " passed in on the request. This information may differ from what was previously "
					+ " entered for the pending certified product during upload. It will first be validated "
					+ " to check for errors, then a new certified product is created, and the old pending certified"
					+ " product will be removed. ROLE_ACB_ADMIN or ROLE_ACB_STAFF "
					+ " and administrative authority on the ACB is required.")
	@RequestMapping(value="/pending/confirm", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetails confirmPendingCertifiedProduct(@RequestBody(required = true) PendingCertifiedProductDetails pendingCp) 
		throws InvalidArgumentsException, ValidationException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		String acbIdStr = pendingCp.getCertifyingBody().get("id").toString();
		if(StringUtils.isEmpty(acbIdStr)) {
			throw new InvalidArgumentsException("An ACB ID must be supplied in the request body");
		}
		
		PendingCertifiedProductDTO pcpDto = new PendingCertifiedProductDTO(pendingCp);
		CertifiedProductValidator validator = validatorFactory.getValidator(pcpDto);
		if(validator != null) {
			validator.validate(pcpDto);
		}
		if(pcpDto.getErrorMessages() != null && pcpDto.getErrorMessages().size() > 0) {
			throw new ValidationException(pcpDto.getErrorMessages(), pcpDto.getWarningMessages());
		}
		
		Long acbId = new Long(acbIdStr);
		CertifiedProductDTO createdProduct = cpManager.createFromPending(acbId, pendingCp);
		pcpManager.confirm(pendingCp.getId());
		
		CertifiedProductSearchDetails result = cpdManager.getCertifiedProductDetails(createdProduct.getId());
		return result;
	}
	
	@ApiOperation(value="Upload a file with certified products", 
			notes="Accepts a CSV file with very specific fields to create pending certified products. "
					+ " The user uploading the file must have ROLE_ACB_ADMIN or ROLE_ACB_STAFF "
					+ " and administrative authority on the ACB(s) specified in the file.")
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
						//if(uploadType == CertifiedProductUploadType.NEW) { 
							if(pendingCp.getCertificationBodyId() == null) {
								throw new IllegalArgumentException("Could not find certifying body with name " + pendingCp.getCertificationBodyName() + ". Aborting upload.");
							}
							pendingCpDto = pcpManager.createOrReplace(pendingCp.getCertificationBodyId(), pendingCp);
						//} else {
						//	pendingCpDto = new PendingCertifiedProductDTO(pendingCp);
						//}
						
						PendingCertifiedProductDetails details = new PendingCertifiedProductDetails(pendingCpDto);
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