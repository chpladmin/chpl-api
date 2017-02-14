package gov.healthit.chpl.app.surveillance.presenter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceRequirement;

/**
 * writes out all surveillance for all products in the system
 * @author kekey
 *
 */
@Component("surveillanceCsvPresenter")
public class SurveillanceCsvPresenter {
	private static final Logger logger = LogManager.getLogger(SurveillanceCsvPresenter.class);
	protected DateTimeFormatter dateFormatter;
	
	public SurveillanceCsvPresenter() {
		dateFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd");
	}
	
	public void presentAsFile(File file, CertifiedProductDownloadResponse cpList) {
		FileWriter writer = null;
		CSVPrinter csvPrinter = null;
		try {
			writer = new FileWriter(file);
			csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
			csvPrinter.printRecord(generateHeaderValues());
			
			for(CertifiedProductSearchDetails cp : cpList.getProducts()) {
				if(cp.getSurveillance() != null && cp.getSurveillance().size() > 0) {
					for(Surveillance currSurveillance : cp.getSurveillance()) {
						List<List<String>> rowValues = generateMultiRowValue(cp, currSurveillance);
						for(List<String> rowValue : rowValues) {
							csvPrinter.printRecord(rowValue);
						}
					}
				}
			}
		} catch(IOException ex) {
			logger.error("Could not write file " + file.getName(), ex);
		} finally {
			try {
				writer.flush();
				writer.close();
				csvPrinter.flush();
				csvPrinter.close();
			} catch(Exception ignore) {}
		}
	}

	protected List<String> generateHeaderValues() {
		List<String> result = new ArrayList<String>();
		result.add("RECORD_STATUS__C");
		result.add("UNIQUE_CHPL_ID__C");
		result.add("ACB_NAME");
		result.add("DEVELOPER_NAME");
		result.add("PRODUCT_NAME");
		result.add("PRODUCT_VERSION");
		result.add("SURVEILLANCE_ID");
		result.add("SURVEILLANCE_BEGAN");
		result.add("SURVEILLANCE_ENDED");
		result.add("SURVEILLANCE_TYPE");
		result.add("RANDOMIZED_SITES_USED");
		result.add("SURVEILLED_REQUIREMENT_TYPE");
		result.add("SURVEILLED_REQUIREMENT");
		result.add("SURVEILLANCE_RESULT");
		result.add("NON_CONFORMITY_TYPE");
		result.add("NON_CONFORMITY_STATUS");
		result.add("DATE_OF_DETERMINATION");
		result.add("CAP_APPROVAL_DATE");
		result.add("ACTION_BEGAN_DATE");
		result.add("MUST_COMPLETE_DATE");
		result.add("WAS_COMPLETE_DATE");
		result.add("NON_CONFORMITY_SUMMARY");
		result.add("NON_CONFORMITY_FINDINGS");
		result.add("SITES_PASSED");
		result.add("TOTAL_SITES");
		result.add("DEVELOPER_EXPLANATION");
		result.add("RESOLUTION_DESCRIPTION");
		return result;
	}
	
	protected List<List<String>> generateMultiRowValue(CertifiedProductSearchDetails data, Surveillance surv) {
		List<List<String>> result = new ArrayList<List<String>>();
		
		List<String> firstRow = new ArrayList<String>();
		firstRow.add("Update");
		firstRow.add(data.getChplProductNumber());
		firstRow.add(data.getCertifyingBody().get("name").toString());
		firstRow.add(data.getDeveloper().getName());
		firstRow.add(data.getProduct().getName());
		firstRow.add(data.getVersion().getVersion());
		firstRow.add(surv.getFriendlyId());
		if(surv.getStartDate() != null) {
			LocalDateTime survStartDate = LocalDateTime.ofInstant(
					Instant.ofEpochMilli(surv.getStartDate().getTime()), 
				    ZoneId.systemDefault());
			firstRow.add(dateFormatter.format(survStartDate));
		} else {
			firstRow.add("");
		}
		if(surv.getEndDate() != null) {
			LocalDateTime survEndDate = LocalDateTime.ofInstant(
					Instant.ofEpochMilli(surv.getEndDate().getTime()), 
				    ZoneId.systemDefault());
			firstRow.add(dateFormatter.format(survEndDate));
		} else {
			firstRow.add("");
		}
		firstRow.add(surv.getType().getName());
		if(surv.getRandomizedSitesUsed() != null) {
			firstRow.add(surv.getRandomizedSitesUsed().toString());
		} else {
			firstRow.add("");
		}
		result.add(firstRow);

		if(surv.getRequirements() != null && surv.getRequirements().size() > 0) {
			boolean isFirstSurvRow = true;
			for(SurveillanceRequirement req : surv.getRequirements()) {
				List<String> reqValues = generateSurveilledRequirementRowValues(req);
				List<String> reqRow = null;
				
				if(isFirstSurvRow) {
					//put data in firstRow
					firstRow.addAll(reqValues);
				} else {
					//make a new row
					reqRow = new ArrayList<String>();
					reqRow.add("Subelement");
					reqRow.add(data.getChplProductNumber());
					reqRow.add("");
					reqRow.add("");
					reqRow.add("");
					reqRow.add("");
					reqRow.add("");
					reqRow.add("");
					reqRow.add("");
					reqRow.add("");
					reqRow.add("");
					reqRow.addAll(reqValues);
					result.add(reqRow);
				}
				
				if(req.getNonconformities() != null && req.getNonconformities().size() > 0) {
					boolean isFirstReqRow = true;
					for(SurveillanceNonconformity nc : req.getNonconformities()) {
						List<String> ncValues = generateNonconformityRowValues(nc);
						if(isFirstSurvRow) {
							isFirstSurvRow = false;
							firstRow.addAll(ncValues);
						} else if(reqRow != null && isFirstReqRow) {
							isFirstReqRow = false;
							reqRow.addAll(ncValues);
						} else {
							List<String> ncRow = new ArrayList<String>();
							ncRow.add("Subelement");
							ncRow.add(data.getChplProductNumber());
							ncRow.add("");
							ncRow.add("");
							ncRow.add("");
							ncRow.add("");
							ncRow.add("");
							ncRow.add("");
							ncRow.add("");
							ncRow.add("");
							ncRow.add("");
							ncRow.addAll(reqValues);
							ncRow.addAll(ncValues);
							result.add(ncRow);
						}
					}
				}
				
				isFirstSurvRow = false;
			}
		}
		return result;
	}
	
	protected List<String> generateSurveilledRequirementRowValues(SurveillanceRequirement req) {
		List<String> reqRow = new ArrayList<String>();
		
		if(req.getType() != null) {
			reqRow.add(req.getType().getName());
		} else {
			reqRow.add("");
		}
		if(req.getRequirement() != null) {
			reqRow.add(req.getRequirement());
		} else {
			reqRow.add("");
		}
		if(req.getResult() != null) {
			reqRow.add(req.getResult().getName());
		} else {
			reqRow.add("");
		}
		return reqRow;
	}
	
	protected List<String> generateNonconformityRowValues(SurveillanceNonconformity nc) {
		List<String> ncRow = new ArrayList<String>();
		if(nc.getNonconformityType() != null) {
			ncRow.add(nc.getNonconformityType());
		} else {
			ncRow.add("");
		}
		if(nc.getStatus() != null) {
			ncRow.add(nc.getStatus().getName());
		} else {
			ncRow.add("");
		}
		if(nc.getDateOfDetermination() != null) {
			LocalDateTime ncDeterminationDate = LocalDateTime.ofInstant(
					Instant.ofEpochMilli(nc.getDateOfDetermination().getTime()), 
				    ZoneId.systemDefault());
			ncRow.add(dateFormatter.format(ncDeterminationDate));
		} else {
			ncRow.add("");
		}
		if(nc.getCapApprovalDate() != null) {
			LocalDateTime ncApprovalDate = LocalDateTime.ofInstant(
					Instant.ofEpochMilli(nc.getCapApprovalDate().getTime()), 
				    ZoneId.systemDefault());
			ncRow.add(dateFormatter.format(ncApprovalDate));
		} else {
			ncRow.add("");
		}
		if(nc.getCapStartDate() != null) {
			LocalDateTime ncStartDate = LocalDateTime.ofInstant(
					Instant.ofEpochMilli(nc.getCapStartDate().getTime()), 
				    ZoneId.systemDefault());
			ncRow.add(dateFormatter.format(ncStartDate));
		} else {
			ncRow.add("");
		}
		if(nc.getCapMustCompleteDate() != null) {
			LocalDateTime ncMustCompleteDate = LocalDateTime.ofInstant(
					Instant.ofEpochMilli(nc.getCapMustCompleteDate().getTime()), 
				    ZoneId.systemDefault());
			ncRow.add(dateFormatter.format(ncMustCompleteDate));
		} else {
			ncRow.add("");
		}
		if(nc.getCapEndDate() != null) {
			LocalDateTime ncEndDate = LocalDateTime.ofInstant(
					Instant.ofEpochMilli(nc.getCapEndDate().getTime()), 
				    ZoneId.systemDefault());
			ncRow.add(dateFormatter.format(ncEndDate));
		} else {
			ncRow.add("");
		}
		if(nc.getSummary() != null) {
			ncRow.add(nc.getSummary());
		} else {
			ncRow.add("");
		}
		if(nc.getFindings() != null) {
			ncRow.add(nc.getFindings());
		} else {
			ncRow.add("");
		}
		if(nc.getSitesPassed() != null) {
			ncRow.add(nc.getSitesPassed().toString());
		} else {
			ncRow.add("");
		}
		if(nc.getTotalSites() != null) {
			ncRow.add(nc.getTotalSites().toString());
		} else {
			ncRow.add("");
		}
		if(nc.getDeveloperExplanation() != null) {
			ncRow.add(nc.getDeveloperExplanation());
		} else {
			ncRow.add("");
		}
		if(nc.getResolution() != null) {
			ncRow.add(nc.getResolution());
		} else {
			ncRow.add("");
		}
		return ncRow;
	}
}
