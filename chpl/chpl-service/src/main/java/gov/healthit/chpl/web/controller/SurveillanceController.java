package gov.healthit.chpl.web.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
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

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformityDocument;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.upload.surveillance.SurveillanceUploadHandler;
import gov.healthit.chpl.upload.surveillance.SurveillanceUploadHandlerFactory;
import gov.healthit.chpl.web.controller.results.SurveillanceResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="surveillance")
@RestController
@RequestMapping("/surveillance")
public class SurveillanceController {
	
	private static final Logger logger = LogManager.getLogger(SurveillanceController.class);
	private static final String HEADING_CELL_INDICATOR = "RECORD_STATUS__C";
	private static final String NEW_SURVEILLANCE_BEGIN_INDICATOR = "New";
	private static final String UPDATE_SURVEILLANCE_BEGIN_INDICATOR = "Update";
	private static final String SUBELEMENT_INDICATOR = "Subelement";
	
	@Autowired Environment env;
	@Autowired
	private SurveillanceUploadHandlerFactory uploadHandlerFactory;
	@Autowired
	private SurveillanceManager survManager;
	@Autowired
	private CertifiedProductManager cpManager;
	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private CertifiedProductDetailsManager cpdetailsManager;
	@Autowired
	private CertificationBodyManager acbManager;

	@ApiOperation(value = "Get the listing of all pending surveillance items that this user has access to.")
	@RequestMapping(value = "/pending", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public @ResponseBody SurveillanceResults getAllPendingSurveillanceForAcbUser() {
		List<CertificationBodyDTO> acbs = acbManager.getAllForUser(false);
		List<Surveillance> pendingSurvs = new ArrayList<Surveillance>();

		if (acbs != null) {
			for (CertificationBodyDTO acb : acbs) {
				try {
					List<Surveillance> survsOnAcb = survManager.getPendingByAcb(acb.getId());
					pendingSurvs.addAll(survsOnAcb);
				} catch (AccessDeniedException denied) {
					logger.warn("Access denied to pending surveillance for acb " + acb.getName() + " and user "
							+ Util.getUsername());
				}
			}
		}

		SurveillanceResults results = new SurveillanceResults();
		results.setPendingSurveillance(pendingSurvs);
		return results;
	}
	
	@ApiOperation(value="Download nonconformity supporting documentation.", 
			notes="Download a specific file that was previously uploaded to a surveillance nonconformity.")
	@RequestMapping(value="/document/{documentId}", method=RequestMethod.GET)
	public void streamDocumentContents(@PathVariable("documentId") Long documentId,
			HttpServletResponse response) throws EntityRetrievalException, IOException {
		SurveillanceNonconformityDocument doc = survManager.getDocumentById(documentId, true);
		
		if(doc != null && doc.getFileContents() != null && doc.getFileContents().length > 0) {
	        ByteArrayInputStream inputStream = new ByteArrayInputStream(doc.getFileContents());
	        // get MIME type of the file
	        String mimeType = doc.getFileType();
	        if (mimeType == null) {
	            // set to binary type if MIME mapping not found
	            mimeType = "application/octet-stream";
	        }
	        // set content attributes for the response
	        response.setContentType(mimeType);
	        response.setContentLength((int) doc.getFileContents().length);
	 
	        // set headers for the response
	        String headerKey = "Content-Disposition";
	        String headerValue = String.format("attachment; filename=\"%s\"",
	                doc.getFileName());
	        response.setHeader(headerKey, headerValue);
	 
	        // get output stream of the response
	        OutputStream outStream = response.getOutputStream();
	 
	        byte[] buffer = new byte[1024];
	        int bytesRead = -1;
	 
	        // write bytes read from the input stream into the output stream
	        while ((bytesRead = inputStream.read(buffer)) != -1) {
	            outStream.write(buffer, 0, bytesRead);
	        }
	 
	        inputStream.close();
	        outStream.close();	
		}   
	}
	
	@ApiOperation(value="Create a new surveillance activity for a certified product.", 
			notes="Creates a new surveillance activity, surveilled requirements, and any applicable non-conformities "
					+ "in the system and associates them with the certified product indicated in the "
					+ "request body. The surveillance passed into this request will first be validated "
					+ " to check for errors. "
					+ "ROLE_ACB_ADMIN or ROLE_ACB_STAFF "
					+ " and administrative authority on the ACB associated with the certified product is required.")
	@RequestMapping(value="/create", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public synchronized @ResponseBody Surveillance createSurveillance(
			@RequestBody(required = true) Surveillance survToInsert) 
		throws InvalidArgumentsException, ValidationException, EntityCreationException, 
		EntityRetrievalException, CertificationBodyAccessException, JsonProcessingException {
		survToInsert.getErrorMessages().clear();
		
		//validate first. this ensures we have all the info filled in 
		//that we need to continue
		survManager.validate(survToInsert);

		if(survToInsert.getErrorMessages() != null && survToInsert.getErrorMessages().size() > 0) {
			throw new ValidationException(survToInsert.getErrorMessages(), null);
		}
		
		//look up the ACB
		CertifiedProductSearchDetails beforeCp = cpdetailsManager.getCertifiedProductDetails(survToInsert.getCertifiedProduct().getId());
		CertificationBodyDTO owningAcb = null;
		try {
			owningAcb = acbManager.getById(new Long(beforeCp.getCertifyingBody().get("id").toString()));
		} catch(AccessDeniedException ex) {
			throw new CertificationBodyAccessException("User does not have permission to add surveillance to a certified product under ACB " + beforeCp.getCertifyingBody().get("name"));
		} catch(EntityRetrievalException ex) {
			logger.error("Error looking up ACB associated with surveillance.", ex);
			throw new EntityRetrievalException("Error looking up ACB associated with surveillance.");
		}
		
		//insert the surveillance
		Long insertedSurv = survManager.createSurveillance(owningAcb.getId(), survToInsert);
		if(insertedSurv == null) {
			throw new EntityCreationException("Error creating new surveillance.");
		}
		
		CertifiedProductSearchDetails afterCp = cpdetailsManager.getCertifiedProductDetails(survToInsert.getCertifiedProduct().getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, afterCp.getId(), 
				"Surveillance was added to certified product " + afterCp.getChplProductNumber(), beforeCp, afterCp);

		//query the inserted surveillance
		return survManager.getById(insertedSurv);
	}
	
	@ApiOperation(value="Add documentation to an existing nonconformity.", 
			notes="Upload a file of any kind (current size limit 5MB) as supporting "
					+ " documentation to an existing nonconformity. The logged in user uploading the file "
					+ " must have either ROLE_ADMIN or ROLE_ACB_ADMIN and administrative "
					+ " authority on the associated ACB.")
	@RequestMapping(value="/{surveillanceId}/nonconformity/{nonconformityId}/document/create", method=RequestMethod.POST,
			produces="application/json; charset=utf-8") 
	public @ResponseBody String uploadNonconformityDocument(@PathVariable("surveillanceId") Long surveillanceId,
			@PathVariable("nonconformityId") Long nonconformityId,
			@RequestParam("file") MultipartFile file) throws 
			InvalidArgumentsException, MaxUploadSizeExceededException, Exception {
		if (file.isEmpty()) {
			throw new InvalidArgumentsException("You cannot upload an empty file!");
		}
		
		Surveillance surv = survManager.getById(surveillanceId);
		if(surv == null) {
			throw new InvalidArgumentsException("Cannot find surveillance with id " + surveillanceId + " to delete.");
		}

		CertifiedProductSearchDetails beforeCp = cpdetailsManager.getCertifiedProductDetails(surv.getCertifiedProduct().getId());
		
		SurveillanceNonconformityDocument toInsert = new SurveillanceNonconformityDocument();
		toInsert.setFileContents(file.getBytes());
		toInsert.setFileName(file.getOriginalFilename());
		toInsert.setFileType(file.getContentType());
		
		CertificationBodyDTO owningAcb = null;
		try {
			owningAcb = acbManager.getById(new Long(beforeCp.getCertifyingBody().get("id").toString()));
		} catch(Exception ex) {
			logger.error("Error looking up ACB associated with surveillance.", ex);
			throw new EntityRetrievalException("Error looking up ACB associated with surveillance.");
		}
		
		Long insertedDocId = survManager.addDocumentToNonconformity(owningAcb.getId(), nonconformityId, toInsert);
		if(insertedDocId == null) {
			throw new EntityCreationException("Error adding a document to nonconformity with id " + nonconformityId);
		}
		
		CertifiedProductSearchDetails afterCp = cpdetailsManager.getCertifiedProductDetails(surv.getCertifiedProduct().getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, beforeCp.getId(), 
				"Documentation " + toInsert.getFileName() + " was added to a nonconformity for certified product " + afterCp.getChplProductNumber(), 
				beforeCp, afterCp);
		return "{\"success\": \"true\"}";
	}
	
	@ApiOperation(value="Update a surveillance activity for a certified product.", 
			notes="Updates an existing surveillance activity, surveilled requirements, and any applicable non-conformities "
					+ "in the system. The surveillance passed into this request will first be validated "
					+ " to check for errors. "
					+ "ROLE_ACB_ADMIN or ROLE_ACB_STAFF "
					+ " and administrative authority on the ACB associated with the certified product is required.")
	@RequestMapping(value="/update", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public synchronized @ResponseBody Surveillance updateSurveillance(
			@RequestBody(required = true) Surveillance survToUpdate) 
		throws InvalidArgumentsException, ValidationException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
		survToUpdate.getErrorMessages().clear();
		
		//validate first. this ensures we have all the info filled in 
		//that we need to continue
		survManager.validate(survToUpdate);

		if(survToUpdate.getErrorMessages() != null && survToUpdate.getErrorMessages().size() > 0) {
			throw new ValidationException(survToUpdate.getErrorMessages(), null);
		}
		
		//look up the ACB
		CertifiedProductSearchDetails beforeCp = cpdetailsManager.getCertifiedProductDetails(survToUpdate.getCertifiedProduct().getId());
		CertificationBodyDTO owningAcb = null;
		try {
			owningAcb = acbManager.getById(new Long(beforeCp.getCertifyingBody().get("id").toString()));
		} catch(Exception ex) {
			logger.error("Error looking up ACB associated with surveillance.", ex);
			throw new EntityRetrievalException("Error looking up ACB associated with surveillance.");
		}
		
		//update the surveillance
		try {
			survManager.updateSurveillance(owningAcb.getId(), survToUpdate);
		} catch(Exception ex) {
			logger.error("Error updating surveillance with id " + survToUpdate.getId());
		}
		
		CertifiedProductSearchDetails afterCp = cpdetailsManager.getCertifiedProductDetails(survToUpdate.getCertifiedProduct().getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, afterCp.getId(), 
				"Surveillance was updated on certified product " + afterCp.getChplProductNumber(), beforeCp, afterCp);

		//query the inserted surveillance
		return survManager.getById(survToUpdate.getId());
	}
	
	@ApiOperation(value="Delete a surveillance activity for a certified product.", 
			notes="Deletes an existing surveillance activity, surveilled requirements, and any applicable non-conformities "
					+ "in the system. "
					+ "ROLE_ACB_ADMIN or ROLE_ACB_STAFF "
					+ " and administrative authority on the ACB associated with the certified product is required.")
	@RequestMapping(value="/{surveillanceId}/delete", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public synchronized @ResponseBody String deleteSurveillance(
			@PathVariable(value = "surveillanceId") Long surveillanceId) 
		throws InvalidArgumentsException, ValidationException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
		Surveillance survToDelete = survManager.getById(surveillanceId);
		
		if(survToDelete == null) {
			throw new InvalidArgumentsException("Cannot find surveillance with id " + surveillanceId + " to delete.");
		}

		CertifiedProductSearchDetails beforeCp = cpdetailsManager.getCertifiedProductDetails(survToDelete.getCertifiedProduct().getId());
		CertificationBodyDTO owningAcb = null;
		try {
			owningAcb = acbManager.getById(new Long(beforeCp.getCertifyingBody().get("id").toString()));
		} catch(Exception ex) {
			logger.error("Error looking up ACB associated with surveillance.", ex);
			throw new EntityRetrievalException("Error looking up ACB associated with surveillance.");
		}
		
		//delete it
		try {
			survManager.deleteSurveillance(owningAcb.getId(), survToDelete.getId());
			survManager.sendSuspiciousActivityEmail(survToDelete);
		} catch(Exception ex) {
			logger.error("Error deleting surveillance with id " + survToDelete.getId() + " during an update.");
		}
		
		CertifiedProductSearchDetails afterCp = cpdetailsManager.getCertifiedProductDetails(survToDelete.getCertifiedProduct().getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, afterCp.getId(), 
				"Surveillance was delete from certified product " + afterCp.getChplProductNumber(), beforeCp, afterCp);

		return "{\"success\" : true }";
	}
	
	@ApiOperation(value="Remove documentation from a nonconformity.", 
			notes="The logged in user"
					+ " must have either ROLE_ADMIN or ROLE_ACB_ADMIN and administrative "
					+ " authority on the associated ACB.")
	@RequestMapping(value="/{surveillanceId}/nonconformity/{nonconformityId}/document/{docId}/delete", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public String deleteNonconformityDocument(@PathVariable("surveillanceId") Long surveillanceId,
			@PathVariable("nonconformityId") Long nonconformityId,
			@PathVariable("docId") Long docId) 
			throws JsonProcessingException, EntityCreationException, EntityRetrievalException,
				InvalidArgumentsException {
		
		Surveillance surv = survManager.getById(surveillanceId);
		if(surv == null) {
			throw new InvalidArgumentsException("Cannot find surveillance with id " + surveillanceId + " to delete.");
		}

		CertifiedProductSearchDetails beforeCp = cpdetailsManager.getCertifiedProductDetails(surv.getCertifiedProduct().getId());
		CertificationBodyDTO owningAcb = null;
		try {
			owningAcb = acbManager.getById(new Long(beforeCp.getCertifyingBody().get("id").toString()));
		} catch(Exception ex) {
			logger.error("Error looking up ACB associated with surveillance.", ex);
			throw new EntityRetrievalException("Error looking up ACB associated with surveillance.");
		}
		
		try {
			survManager.deleteNonconformityDocument(owningAcb.getId(), docId);
		} catch(Exception ex) {
			throw ex;
		}
		
		CertifiedProductSearchDetails afterCp = cpdetailsManager.getCertifiedProductDetails(surv.getCertifiedProduct().getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, beforeCp.getId(), 
				"A document was removed from a nonconformity for certified product " + afterCp.getChplProductNumber(), 
				beforeCp, afterCp);
		return "{\"success\": \"true\"}";
	}
	
	@ApiOperation(value="Reject (effectively delete) a pending surveillance item.")
	@RequestMapping(value="/pending/{pendingSurvId}/reject", method=RequestMethod.POST,
			produces = "application/json; charset=utf-8")
	public @ResponseBody String deletePendingSurveillance(@PathVariable("pendingSurvId") Long id) {
		List<CertificationBodyDTO> acbs = acbManager.getAllForUser(false);
		survManager.deletePendingSurveillance(acbs, id);
		return "{\"success\" : true }";
	}
	
	@ApiOperation(value="Confirm a pending surveillance activity.", 
			notes="Creates a new surveillance activity, surveilled requirements, and any applicable non-conformities "
					+ "in the system and associates them with the certified product indicated in the "
					+ "request body. If the surveillance is an update of an existing surveillance activity "
					+ "as indicated by the 'surveillanceIdToReplace' field, that existing surveillance "
					+ "activity will be marked as deleted and the surveillance in this request body will "
					+ "be inserted. The surveillance passed into this request will first be validated "
					+ " to check for errors and the related pending surveillance will be removed. "
					+ "ROLE_ACB_ADMIN or ROLE_ACB_STAFF "
					+ " and administrative authority on the ACB associated with the certified product is required.")
	@RequestMapping(value="/pending/confirm", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public synchronized @ResponseBody Surveillance confirmPendingSurveillance(
			@RequestBody(required = true) Surveillance survToInsert) 
		throws InvalidArgumentsException, ValidationException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
		if(survToInsert == null || survToInsert.getId() == null) {
			throw new ValidationException("An id must be provided in the request body.");
		}
		Long pendingSurvToDelete = survToInsert.getId();
		
		survToInsert.getErrorMessages().clear();
		
		//validate first. this ensures we have all the info filled in 
		//that we need to continue
		survManager.validate(survToInsert);

		if(survToInsert.getErrorMessages() != null && survToInsert.getErrorMessages().size() > 0) {
			throw new ValidationException(survToInsert.getErrorMessages(), null);
		}
		
		CertifiedProductSearchDetails beforeCp = cpdetailsManager.getCertifiedProductDetails(survToInsert.getCertifiedProduct().getId());
		CertificationBodyDTO owningAcb = null;
		try {
			owningAcb = acbManager.getById(new Long(beforeCp.getCertifyingBody().get("id").toString()));
		} catch(Exception ex) {
			logger.error("Error looking up ACB associated with surveillance.", ex);
			throw new EntityRetrievalException("Error looking up ACB associated with surveillance.");
		}
		
		//insert or update the surveillance
		Long insertedSurv = survManager.createSurveillance(owningAcb.getId(), survToInsert);
		if(insertedSurv == null) {
			throw new EntityCreationException("Error creating new surveillance.");
		}
		
		//delete the pending surveillance item if this one was successfully inserted
		try {
			survManager.deletePendingSurveillance(owningAcb.getId(), pendingSurvToDelete);
		} catch(Exception ex) {
			logger.error("Error deleting pending surveillance with id " + pendingSurvToDelete, ex);
		}
		
		try {
			//if a surveillance was getting replaced, delete it
			if(!StringUtils.isEmpty(survToInsert.getSurveillanceIdToReplace())) {
				Surveillance survToReplace = survManager.getByFriendlyIdAndProduct(
						survToInsert.getCertifiedProduct().getId(),
						survToInsert.getSurveillanceIdToReplace());
				CertifiedProductDTO survToReplaceOwningCp = cpManager.getById(survToReplace.getCertifiedProduct().getId());
				survManager.deleteSurveillance(survToReplaceOwningCp.getCertificationBodyId(), survToReplace.getId());
			}
		} catch(Exception ex) {
			logger.error("Deleting surveillance with id " + survToInsert.getSurveillanceIdToReplace() + " as part of the replace operation failed", ex);
		}
		
		
		CertifiedProductSearchDetails afterCp = cpdetailsManager.getCertifiedProductDetails(survToInsert.getCertifiedProduct().getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, afterCp.getId(), 
				"Surveillance upload was confirmed for certified product " + afterCp.getChplProductNumber(), beforeCp, afterCp);

		
		//query the inserted surveillance
		return survManager.getById(insertedSurv);
	}
	
	@ApiOperation(value="Download surveillance as CSV.", 
			notes="Once per day, all surveillance and nonconformities are written out to CSV "
					+ "files on the CHPL servers. This method allows any user to download those files.")
	@RequestMapping(value="/download", method=RequestMethod.GET,
			produces="text/csv")
	public void download(@RequestParam(value="type", required=false, defaultValue="") String type,
			HttpServletRequest request, HttpServletResponse response) throws IOException {	
		String downloadFileLocation = env.getProperty("downloadFolderPath");
		String filenameToDownload = "surveillance-with-nonconformities.csv";
		if(type.equalsIgnoreCase("all")) {
			filenameToDownload = "surveillance-all.csv";
		} else if(type.equalsIgnoreCase("basic")) {
			filenameToDownload = "surveillance-basic-report.csv";
		}
		
		File downloadFile = new File(downloadFileLocation + File.separator + filenameToDownload);
		if(!downloadFile.exists() || !downloadFile.canRead()) {
			response.getWriter().write("Cannot read download file at " + downloadFileLocation + ". File does not exist or cannot be read.");
			return;
		}
		
		logger.info("Downloading " + downloadFile.getName());
		
		FileInputStream inputStream = new FileInputStream(downloadFile);

		// set content attributes for the response
		response.setContentType("text/csv");
		response.setContentLength((int) downloadFile.length());
	 
		// set headers for the response
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
		response.setHeader(headerKey, headerValue);
	 
		// get output stream of the response
		OutputStream outStream = response.getOutputStream();
		byte[] buffer = new byte[1024];
		int bytesRead = -1;
	 
		// write bytes read from the input stream into the output stream
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}
		inputStream.close();
		outStream.close();
	}
	
	@ApiOperation(value="Upload a file with surveillance and nonconformities for certified products.", 
			notes="Accepts a CSV file with very specific fields to create pending surveillance items. "
					+ " The user uploading the file must have ROLE_ACB_ADMIN or ROLE_ACB_STAFF "
					+ " and administrative authority on the ACB(s) responsible for the product(s) in the file.")
	@RequestMapping(value="/upload", method=RequestMethod.POST,
			produces="application/json; charset=utf-8") 
	public @ResponseBody SurveillanceResults upload(@RequestParam("file") MultipartFile file) throws 
			ValidationException, MaxUploadSizeExceededException {
		if (file.isEmpty()) {
			throw new ValidationException("You cannot upload an empty file!");
		}
		
		if(!file.getContentType().equalsIgnoreCase("text/csv") &&
				!file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
			throw new ValidationException("File must be a CSV document.");
		}
		
		List<Surveillance> uploadedSurveillance = new ArrayList<Surveillance>();
		
		BufferedReader reader = null;
		CSVParser parser = null;
		try {
			reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			parser = new CSVParser(reader, CSVFormat.EXCEL);
			
			List<CSVRecord> records = parser.getRecords();
			if(records.size() <= 1) {
				throw new ValidationException("The file appears to have a header line with no other information. Please make sure there are at least two rows in the CSV file.");
			}
			
			Set<String> handlerErrors = new HashSet<String>();
			List<Surveillance> pendingSurvs = new ArrayList<Surveillance>();
			
			//parse the entire file into groups of records, one group per surveillance item
			CSVRecord heading = null; 
			List<CSVRecord> rows = new ArrayList<CSVRecord>();
			for(int i = 0; i < records.size(); i++) {
				CSVRecord currRecord = records.get(i);
				
				if(heading == null && !StringUtils.isEmpty(currRecord.get(1)) 
						&& currRecord.get(0).equals(HEADING_CELL_INDICATOR)) {
					//have to find the heading first
					heading = currRecord;
				} else if(heading != null) {
					if(!StringUtils.isEmpty(currRecord.get(0).trim())) {
						String currRecordStatus = currRecord.get(0).trim();
						
						if(currRecordStatus.equalsIgnoreCase(NEW_SURVEILLANCE_BEGIN_INDICATOR) || 
								currRecordStatus.equalsIgnoreCase(UPDATE_SURVEILLANCE_BEGIN_INDICATOR)) {
							//parse the previous recordset because we hit a new surveillance item
							//if this is the last recordset, we'll handle that later
							if(rows.size() > 0) {
								try {
									SurveillanceUploadHandler handler = uploadHandlerFactory.getHandler(heading, rows);
									Surveillance pendingSurv = handler.handle();
									checkUploadedSurveillanceOwnership(pendingSurv);
									pendingSurvs.add(pendingSurv);
								}
								catch(InvalidArgumentsException ex) {
									handlerErrors.add(ex.getMessage());
								}
							}
							rows.clear();
							rows.add(currRecord);
						} else if(currRecordStatus.equalsIgnoreCase(SUBELEMENT_INDICATOR)) {
							rows.add(currRecord);
						} // ignore blank rows
					}
				}
				
				//add the last object
				if(i == records.size()-1 && !rows.isEmpty()) {
					try {
						SurveillanceUploadHandler handler = uploadHandlerFactory.getHandler(heading, rows);
						Surveillance pendingSurv = handler.handle();
						checkUploadedSurveillanceOwnership(pendingSurv);
						pendingSurvs.add(pendingSurv);
					}
					catch(InvalidArgumentsException ex) {
						handlerErrors.add(ex.getMessage());
					}
				}
			}
			if(heading == null) {
				handlerErrors.add("Could not find heading row in the uploaded file.");
			}
			
			//if we couldn't parse the files (bad format or something), stop here with the errors
			if(handlerErrors.size() > 0) {
				throw new ValidationException(handlerErrors, null);
			}
			
			//we parsed the files but maybe some of the data in them has errors
			//that are too severe to continue putting them in the database
			Set<String> allErrors = new HashSet<String>();
			for(Surveillance surv : pendingSurvs) {
				if(surv.getErrorMessages() != null && surv.getErrorMessages().size() > 0) {
					allErrors.addAll(surv.getErrorMessages());
				}
			}
			
			if(allErrors.size() > 0) {
				throw new ValidationException(allErrors, null);
			} else {
				//Certified product is guaranteed to be filled in at this point.
				//If it hadn't been found during upload an error would have been thrown above.
				for(Surveillance surv : pendingSurvs) {
					CertifiedProductDTO owningCp = null; 
					try {
						owningCp = cpManager.getById(surv.getCertifiedProduct().getId());
						Long pendingId = survManager.createPendingSurveillance(owningCp.getCertificationBodyId(), surv);
						Surveillance uploaded = survManager.getPendingById(owningCp.getCertificationBodyId(), pendingId);
						uploadedSurveillance.add(uploaded);
					} catch(AccessDeniedException denied) {
						logger.error("User " + Util.getCurrentUser().getSubjectName() + 
								" does not have access to add surveillance" + 
								(owningCp != null ? " to ACB with ID '" + owningCp.getCertificationBodyId() + "'." : "."));
					} catch(Exception ex) {
						logger.error("Error adding a new pending surveillance. Please make sure all required fields are present.", ex);
					}
				}	
			}
		} catch(IOException ioEx) {
			logger.error("Could not get input stream for uploaded file " + file.getName());			
			throw new ValidationException("Could not get input stream for uploaded file " + file.getName());
		} finally {
			 try { parser.close(); } catch(Exception ignore) {}
			try { reader.close(); } catch(Exception ignore) {}
		}
		
		SurveillanceResults results = new SurveillanceResults();
		results.getPendingSurveillance().addAll(uploadedSurveillance);
		return results;
	}
	
	private void checkUploadedSurveillanceOwnership(Surveillance pendingSurv) {
		//perform additional checks if there are no errors in the uploaded surveillance already
		if(pendingSurv.getErrorMessages() == null || pendingSurv.getErrorMessages().size() == 0) {
			//check this pendingSurv to confirm the user has ACB permissions on the 
			//appropriate ACB for the CHPL ID specified
			CertifiedProductDTO surveilledProduct =  null;
			try {
				surveilledProduct = cpManager.getById(pendingSurv.getCertifiedProduct().getId());
			} catch(EntityRetrievalException ex) {
				pendingSurv.getErrorMessages().add("Unexpected error looking up certified product with id " + pendingSurv.getCertifiedProduct().getId());
				logger.error("Could not look up certified product by id " + pendingSurv.getCertifiedProduct().getId());
			} 
			
			if(surveilledProduct != null) {
				CertificationBodyDTO owningAcb = null;
				try {
					owningAcb = acbManager.getById(surveilledProduct.getCertificationBodyId());
				} catch(EntityRetrievalException ex) {
					pendingSurv.getErrorMessages().add("Unexpected error looking up certification body with id " + surveilledProduct.getCertificationBodyId());
					logger.error("Could not look up ACB by id " + surveilledProduct.getCertificationBodyId());
				} catch(AccessDeniedException denied) {
					pendingSurv.getErrorMessages().add("User does not have permission to add surveillance to " + pendingSurv.getCertifiedProduct().getChplProductNumber());
					logger.error("User " + Util.getCurrentUser().getSubjectName() + 
							" does not have access to the ACB with id " + 
							surveilledProduct.getCertificationBodyId());
				}
			} 
		}
	}
}
