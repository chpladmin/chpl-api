package gov.healthit.chpl.web.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedHashSet;
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

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.MeaningfulUseAccurateAsOf;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.dto.MeaningfulUseAccurateAsOfDTO;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.MeaningfulUseManager;
import gov.healthit.chpl.web.controller.results.MeaningfulUseUserResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="meaningful-use")
@RestController
@RequestMapping("/meaningful_use")
public class MeaningfulUseController {
	private static final Logger logger = LogManager.getLogger(MeaningfulUseController.class);
	
	@Autowired CertifiedProductManager cpManager;
	@Autowired MeaningfulUseManager muManager;
	
	@ApiOperation(value="Upload a file to update the number of meaningful use users for each CHPL Product Number", 
			notes="Accepts a CSV file with chpl_product_number and num_meaningful_use_users to update the number of meaningful use users for each CHPL Product Number."
					+ " The user uploading the file must have ROLE_ADMIN or ROLE_ONC_STAFF ")
	@RequestMapping(value="/upload", method=RequestMethod.POST,
			produces="application/json; charset=utf-8") 
	public @ResponseBody MeaningfulUseUserResults uploadMeaningfulUseUsers(@RequestParam("file") MultipartFile file) throws ValidationException, MaxUploadSizeExceededException {
		if (file.isEmpty()) {
			throw new ValidationException("You cannot upload an empty file!");
		}
		
		if(!file.getContentType().equalsIgnoreCase("text/csv") &&
				!file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
			throw new ValidationException("File must be a CSV document.");
		}
		MeaningfulUseUserResults meaningfulUseUserResults = new MeaningfulUseUserResults();
		Set<MeaningfulUseUser> muusToUpdate = new LinkedHashSet<MeaningfulUseUser>();
		Set<String> uniqueMuusFromFile = new LinkedHashSet<String>();
		
		BufferedReader reader = null;
		CSVParser parser = null;
		try {
			reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			parser = new CSVParser(reader, CSVFormat.EXCEL);
			
			List<CSVRecord> records = parser.getRecords();
			if(records.size() <= 1) {
				throw new ValidationException("The file appears to have a header line with no other information. Please make sure there are at least two rows in the CSV file.");
			}
			
			CSVRecord heading = null;
			
			for(int i = 1; i <= records.size(); i++){
				CSVRecord currRecord = records.get(i-1);
				MeaningfulUseUser muu = new MeaningfulUseUser();
				
				// add header if something similar to "chpl_product_number" and "num_meaningful_use" exists
				if(heading == null && i == 1 && !StringUtils.isEmpty(currRecord.get(0).trim()) && currRecord.get(0).trim().contains("product")
						&& !StringUtils.isEmpty(currRecord.get(1).trim()) && currRecord.get(1).trim().contains("meaning")) {
					heading = currRecord;
				}
				// populate MeaningfulUseUserResults
				else {
					String chplProductNumber = currRecord.get(0).trim();
					Long numMeaningfulUseUsers = null;
					try{
						numMeaningfulUseUsers = Long.parseLong(currRecord.get(1).trim());
						muu.setProductNumber(chplProductNumber);
						muu.setNumberOfUsers(numMeaningfulUseUsers);
						muu.setCsvLineNumber(i);
						// check if product number has already been updated
						if(uniqueMuusFromFile.contains(muu.getProductNumber())){
							throw new IOException();
						}
						muusToUpdate.add(muu);
						uniqueMuusFromFile.add(muu.getProductNumber());
					} catch (NumberFormatException e){
						muu.setProductNumber(chplProductNumber);
						muu.setCsvLineNumber(i);
						muu.setError("Line " + muu.getCsvLineNumber() + ": Field \"num_meaningful_use\" with value \"" + currRecord.get(1).trim() + "\" is invalid. "
								+ "Value in field \"num_meaningful_use\" must be an integer.");
						muusToUpdate.add(muu);
						uniqueMuusFromFile.add(muu.getProductNumber());
					}
					catch (IOException e){
						muu.setProductNumber(chplProductNumber);
						muu.setCsvLineNumber(i);
						Integer dupLineNumber = null;
						// get line number with duplicate chpl_product_number
						for (MeaningfulUseUser entry: muusToUpdate) {
						     if (entry.getProductNumber().equals(muu.getProductNumber())){
						    	 dupLineNumber = entry.getCsvLineNumber();
						     }
						   }
						muu.setError("Line " + muu.getCsvLineNumber() + ": Field \"chpl_product_number\" with value \"" + muu.getProductNumber() + "\" is invalid. "
								+ "Duplicate \"chpl_product_number\" at line " + dupLineNumber);
						muusToUpdate.add(muu);
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
		
		try {
			meaningfulUseUserResults = cpManager.updateMeaningfulUseUsers(muusToUpdate);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (EntityCreationException e) {
			e.printStackTrace();
		} catch (EntityRetrievalException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return meaningfulUseUserResults;
	}
	
	@ApiOperation(value="Get a single date value to indicate when the last meaningful use user file data is good as of.", 
			notes="Value can be edited by ROLE_ADMIN and ROLE_CMS_STAFF using the POST version of this request.")
	@RequestMapping(value="/accurate_as_of", method=RequestMethod.GET,
			produces="application/json; charset=utf-8") 
	public @ResponseBody MeaningfulUseAccurateAsOf getAccurateAsOfDate() {
		MeaningfulUseAccurateAsOfDTO dto = muManager.getMeaningfulUseAccurateAsOf();
		MeaningfulUseAccurateAsOf muuAccurate = new MeaningfulUseAccurateAsOf(dto);
		return muuAccurate;
	}
	
	@ApiOperation(value="Update the Meaningful Use Accurate As Of date.", 
			notes="This is a single system-wide value that indicates when the last meaningful use user file data is good as of.")
	@RequestMapping(value="/accurate_as_of", method=RequestMethod.POST,
			produces="application/json; charset=utf-8") 
	public @ResponseBody void updateMeaningfulUseAccurateAsOf(@RequestParam(required=true) MeaningfulUseAccurateAsOf meaningfulUseAccurateAsOf) {
		MeaningfulUseAccurateAsOfDTO dto = new MeaningfulUseAccurateAsOfDTO(meaningfulUseAccurateAsOf);
		muManager.updateMeaningfulUseAccurateAsOf(dto);
	}
}
