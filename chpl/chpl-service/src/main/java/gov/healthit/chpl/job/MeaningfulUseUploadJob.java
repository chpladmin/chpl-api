package gov.healthit.chpl.job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.entity.job.JobStatusType;
import gov.healthit.chpl.manager.CertifiedProductManager;

@Component
@Scope("prototype") //tells spring to make a new instance of this class every time it is needed 
public class MeaningfulUseUploadJob extends RunnableJob {
	private static final Logger logger = LogManager.getLogger(MeaningfulUseUploadJob.class);
	
	@Autowired CertifiedProductManager cpManager;
	@Autowired CertifiedProductDAO cpDao;

	public MeaningfulUseUploadJob() {
		logger.debug("Created new MUUJob");
	}
	public MeaningfulUseUploadJob(JobDTO job) {
		logger.debug("Created new MUUJob");
		this.job = job;
	}
	
	public void run() {
		super.run();
		
		double jobPercentComplete = 0;
		Set<MeaningfulUseUser> muusToUpdate = new LinkedHashSet<MeaningfulUseUser>();
		Set<String> uniqueMuusFromFile = new LinkedHashSet<String>();
		
		BufferedReader reader = null;
		CSVParser parser = null;
		try {
			reader = new BufferedReader(new StringReader(job.getData()));
			parser = new CSVParser(reader, CSVFormat.EXCEL);
			
			List<CSVRecord> records = parser.getRecords();
			if(records.size() <= 1) {
				String msg = "The file appears to have a header line with no other information. Please make sure there are at least two rows in the CSV file.";
				logger.error(msg);	
				addJobMessage(msg);
				updateStatus(100, JobStatusType.Error);
				try { parser.close(); } catch(Exception ignore) {}
				try { reader.close(); } catch(Exception ignore) {}
			} else {
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
							String error = "Line " + muu.getCsvLineNumber() + ": Field \"num_meaningful_use\" with value \"" + currRecord.get(1).trim() + "\" is invalid. "
									+ "Value in field \"num_meaningful_use\" must be an integer.";
							muu.setError(error);
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
							
							String error = "Line " + muu.getCsvLineNumber() + ": Field \"chpl_product_number\" with value \"" + muu.getProductNumber() + "\" is invalid. "
									+ "Duplicate \"chpl_product_number\" at line " + dupLineNumber;
							muu.setError(error);
							muusToUpdate.add(muu);
						}
					}
				}
			}
			
			//finished parsing the file which is pretty quick, say that's 10% of the job done
			jobPercentComplete = 10;
			updateStatus(jobPercentComplete, JobStatusType.In_Progress);
		} catch(IOException ioEx) {
			String msg = "Could not get input stream for job data string for job with ID " + job.getId();
			logger.error(msg);
			addJobMessage(msg);
			updateStatus(100, JobStatusType.Error);
			try { parser.close(); } catch(Exception ignore) {}
			try { reader.close(); } catch(Exception ignore) {}
		}
		
		//now load everything that was parsed	
		for(MeaningfulUseUser muu : muusToUpdate){
			if(StringUtils.isEmpty(muu.getError())) {
				try{
					// If bad input, add error for this MeaningfulUseUser and continue
					if((muu.getProductNumber() == null || muu.getProductNumber().isEmpty())){
						addJobMessage("Line " + muu.getCsvLineNumber() + ": Field \"chpl_product_number\" has invalid value: \"" + muu.getProductNumber() + "\".");
					}
					else if(muu.getNumberOfUsers() == null){
						addJobMessage("Line " + muu.getCsvLineNumber() + ": Field \"num_meaningful_users\" has invalid value: \"" + muu.getNumberOfUsers() + "\".");
					}
					else{
						CertifiedProductDTO dto = new CertifiedProductDTO();
						// check if 2014 edition CHPL Product Number exists
						if(cpDao.getByChplNumber(muu.getProductNumber()) != null) {
							dto.setChplProductNumber(muu.getProductNumber());
							dto.setMeaningfulUseUsers(muu.getNumberOfUsers());
						}
						// check if 2015 edition CHPL Product Number exists
						else if(cpDao.getByChplUniqueId(muu.getProductNumber()) != null) {
							dto.setChplProductNumber(muu.getProductNumber());
							dto.setMeaningfulUseUsers(muu.getNumberOfUsers());
						} 
						// If neither exist, add error
						else {
							throw new EntityRetrievalException();
						}
						
						try{
							cpDao.updateMeaningfulUseUsers(dto);
						} catch (EntityRetrievalException e){
							addJobMessage("Line " + muu.getCsvLineNumber() + ": Field \"chpl_product_number\" with value \"" + muu.getProductNumber() + "\" is invalid. "
									+ "The provided \"chpl_product_number\" does not exist.");
						}
					}
				} catch(EntityRetrievalException ex) {
					String msg = "Line " + muu.getCsvLineNumber() + ": Field \"chpl_product_number\" with value \""+ muu.getProductNumber() + "\" is invalid. "
							+ "The provided \"chpl_product_number\" does not exist.";
					addJobMessage(msg);
				} catch (Exception ex){	
					String msg = "Line " + muu.getCsvLineNumber() + ": An unexpected error occurred. " + ex.getMessage();
					logger.error(msg, ex);
					addJobMessage(msg);
					
				}
			} else {
				addJobMessage(muu.getError());
			}
			
			//update the status
			jobPercentComplete += 90.0/(double)muusToUpdate.size();
			updateStatus(jobPercentComplete, JobStatusType.In_Progress);
		}
		
		this.complete();
	}
	
	public CertifiedProductManager getCpManager() {
		return cpManager;
	}
	public void setCpManager(CertifiedProductManager cpManager) {
		this.cpManager = cpManager;
	}
	public CertifiedProductDAO getCpDao() {
		return cpDao;
	}
	public void setCpDao(CertifiedProductDAO cpDao) {
		this.cpDao = cpDao;
	}
}
