package gov.healthit.chpl.web.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;
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
	
	@Autowired private SurveillanceUploadHandlerFactory uploadHandlerFactory;
	@Autowired private SurveillanceManager survManager;
	@Autowired private CertifiedProductManager cpManager;
	@Autowired private CertificationBodyManager acbManager;
	
	@ApiOperation(value="Get the listing of all pending surveillance items that this user has access to.")
	@RequestMapping(value="/pending", method=RequestMethod.GET,
			produces = "application/json; charset=utf-8")
	public @ResponseBody SurveillanceResults getAllPendingSurveillanceForAcbUser() {
		List<CertificationBodyDTO> acbs = acbManager.getAllForUser(false);
		List<Surveillance> pendingSurvs = new ArrayList<Surveillance>(); 
		
		if(acbs != null) {
			for(CertificationBodyDTO acb : acbs) {
				List<Surveillance> survsOnAcb = survManager.getPendingByAcb(acb.getId());
				pendingSurvs.addAll(survsOnAcb);
			}
		}
		
		SurveillanceResults results = new SurveillanceResults();
		results.setResults(pendingSurvs);
		return results;
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
					try {
						CertifiedProductDTO owningCp = cpManager.getById(surv.getCertifiedProduct().getId());
						Long pendingId = survManager.createPendingSurveillance(owningCp.getCertificationBodyId(), surv);
						Surveillance uploaded = survManager.getPendingById(owningCp.getCertificationBodyId(), pendingId);
						uploadedSurveillance.add(uploaded);
					} catch(Exception ex) {
						logger.error("Error adding a new pending surveillance.", ex);
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
		results.getResults().addAll(uploadedSurveillance);
		return results;
	}
}
