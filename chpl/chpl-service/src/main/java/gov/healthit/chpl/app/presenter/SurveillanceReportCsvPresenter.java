package gov.healthit.chpl.app.presenter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceRequirement;

public class SurveillanceReportCsvPresenter extends SurveillanceCsvPresenter {
	private static final Logger logger = LogManager.getLogger(SurveillanceReportCsvPresenter.class);

	protected List<String> generateHeaderValues() {
		List<String> result = new ArrayList<String>();
		result.add("Developer");
		result.add("Product");
		result.add("Version");
		result.add("CHPL ID");
		result.add("Certification Status");
		result.add("Date of Last Status Change");
		result.add("Date Surveillance Began");
		result.add("Date Surveillance Ended");
		result.add("Surveillance Type");
		result.add("Non-conformity (Y/N)");
		result.add("Non-conformity Criteria");
		result.add("Date of Determination of Non-Conformity");
		result.add("Corrective Action Plan Approved Date");
		result.add("Date Corrective Action Began");
		result.add("Date Corrective Action Must Be Completed");
		result.add("Date Corrective Action Was Completed");
		result.add("Number of Days from Determination to CAP Approval");
		result.add("Number of Days from Determination to Present");
		result.add("Number of Days from CAP Approval to CAP Began");
		result.add("Number of Days from CAP Approval to Present");
		result.add("Number of Days from CAP Began to CAP Completed");
		result.add("Number of Days from CAP Began to Present");
		result.add("Difference from CAP Completed and CAP Must Be Completed");
		return result;
	}
	
	protected List<List<String>> generateMultiRowValue(CertifiedProductSearchDetails data, Surveillance surv) {
		List<List<String>> result = new ArrayList<List<String>>();
		List<String> survFields = new ArrayList<String>();
		survFields.add(data.getDeveloper().getName());
		survFields.add(data.getProduct().getName());
		survFields.add(data.getVersion().getVersion());
		survFields.add(data.getChplProductNumber());
		survFields.add(data.getCertificationStatus().get("name").toString());
		Long lastCertificationChangeMillis = ((Date)data.getCertificationStatus().get("date")).getTime();
		if(lastCertificationChangeMillis.longValue() == data.getCertificationDate().longValue()) {
			survFields.add("No status change");
		} else {
			LocalDateTime lastStatusChangeDate = LocalDateTime.ofInstant(
					Instant.ofEpochMilli(lastCertificationChangeMillis), 
				    ZoneId.systemDefault());
			survFields.add(DateTimeFormatter.BASIC_ISO_DATE.format(lastStatusChangeDate));
		}
		
		if(surv.getStartDate() != null) {
			LocalDateTime survStartDate = LocalDateTime.ofInstant(
					Instant.ofEpochMilli(surv.getStartDate().getTime()), 
				    ZoneId.systemDefault());
			survFields.add(DateTimeFormatter.BASIC_ISO_DATE.format(survStartDate));
		} else {
			survFields.add("");
		}
		if(surv.getEndDate() != null) {
			LocalDateTime survEndDate = LocalDateTime.ofInstant(
					Instant.ofEpochMilli(surv.getEndDate().getTime()), 
				    ZoneId.systemDefault());
			survFields.add(DateTimeFormatter.BASIC_ISO_DATE.format(survEndDate));
		} else {
			survFields.add("");
		}
		survFields.add(surv.getType().getName());
		
		boolean hasNonconformities = false;
		for(SurveillanceRequirement req : surv.getRequirements()) {	
			if(req.getNonconformities() != null && req.getNonconformities().size() > 0) {
				hasNonconformities = true;
			}
		}
		if(!hasNonconformities) { 
			survFields.add("N");
			result.add(survFields);
		} else {
			for(SurveillanceRequirement req : surv.getRequirements()) {	
				for(SurveillanceNonconformity nc : req.getNonconformities()) {
					List<String> ncFields = new ArrayList<String>();
					ncFields.addAll(survFields);
					ncFields.add("Y");
					ncFields.add(nc.getNonconformityType());
					LocalDateTime ncDeterminationDate = null;
					if(nc.getDateOfDetermination() != null) {
						ncDeterminationDate = LocalDateTime.ofInstant(
								Instant.ofEpochMilli(nc.getDateOfDetermination().getTime()), 
							    ZoneId.systemDefault());
						ncFields.add(DateTimeFormatter.BASIC_ISO_DATE.format(ncDeterminationDate));
					} else {
						ncFields.add("");
					}
					LocalDateTime capApprovalDate = null;
					if(nc.getCapApprovalDate() != null) {
						capApprovalDate = LocalDateTime.ofInstant(
								Instant.ofEpochMilli(nc.getCapApprovalDate().getTime()), 
							    ZoneId.systemDefault());
						ncFields.add(DateTimeFormatter.BASIC_ISO_DATE.format(capApprovalDate));
					} else {
						ncFields.add("");
					}
					LocalDateTime capStartDate = null;
					if(nc.getCapStartDate() != null) {
						capStartDate = LocalDateTime.ofInstant(
								Instant.ofEpochMilli(nc.getCapStartDate().getTime()), 
							    ZoneId.systemDefault());
						ncFields.add(DateTimeFormatter.BASIC_ISO_DATE.format(capStartDate));
					} else {
						ncFields.add("");
					}
					LocalDateTime capMustCompleteDate  = null;
					if(nc.getCapMustCompleteDate() != null) {
						capMustCompleteDate = LocalDateTime.ofInstant(
								Instant.ofEpochMilli(nc.getCapMustCompleteDate().getTime()), 
							    ZoneId.systemDefault());
						ncFields.add(DateTimeFormatter.BASIC_ISO_DATE.format(capMustCompleteDate));
					} else {
						ncFields.add("");
					}
					LocalDateTime capEndDate = null;
					if(nc.getCapEndDate() != null) {
						capEndDate = LocalDateTime.ofInstant(
								Instant.ofEpochMilli(nc.getCapEndDate().getTime()), 
							    ZoneId.systemDefault());
						ncFields.add(DateTimeFormatter.BASIC_ISO_DATE.format(capEndDate));
					} else {
						ncFields.add("");
					}
					
					if(capApprovalDate != null) {
						//calculate number of days from nc determination to cap approval date
						Duration timeBetween = Duration.between(ncDeterminationDate, capApprovalDate);
						ncFields.add(timeBetween.toDays()+"");
						ncFields.add("");
					} else {
						ncFields.add("");
						//calculate number of days between nc determination date and present
						Duration timeBetween = Duration.between(ncDeterminationDate, LocalDateTime.now());
						ncFields.add(timeBetween.toDays()+"");
					}
					
					if(capApprovalDate != null && capStartDate != null) {
						Duration timeBetween = Duration.between(capApprovalDate, capStartDate);
						ncFields.add(timeBetween.toDays()+"");
						ncFields.add("");
					} else if(capApprovalDate != null) {
						Duration timeBetween = Duration.between(capApprovalDate, LocalDateTime.now());
						ncFields.add("");
						ncFields.add(timeBetween.toDays()+"");
					} else {
						ncFields.add("");
						ncFields.add("");
					}
					
					if(capStartDate != null && capEndDate != null) {
						Duration timeBetween = Duration.between(capStartDate, capEndDate);
						ncFields.add(timeBetween.toDays()+"");
						ncFields.add("");
					} else if(capStartDate != null) {
						Duration timeBetween = Duration.between(capStartDate, LocalDateTime.now());
						ncFields.add("");
						ncFields.add(timeBetween.toDays()+"");
					} else {
						ncFields.add("");
						ncFields.add("");
					}
					
					if(capEndDate != null && capMustCompleteDate != null) {
						Duration timeBetween = Duration.between(capMustCompleteDate, capEndDate);
						ncFields.add(timeBetween.toDays()+"");
					} else {
						ncFields.add("N/A");
					}
					result.add(ncFields);
				}
			}
		}
		
		return result;
	}
}
