package gov.healthit.chpl.upload.surveillance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Component("surveillanceUploadHandler2015")
public class SurveillanceUploadHandler2015 implements SurveillanceUploadHandler {
	private static final Logger logger = LogManager.getLogger(SurveillanceUploadHandler2015.class);

	private static final String DATE_FORMAT = "yyyyMMdd";
	private static final String FIRST_ROW_REGEX = "^New|Update$/i";
	private static final String SUBSEQUENT_ROW = "Subelement";
	
	@Autowired CertifiedProductDAO cpDao;
	@Autowired SurveillanceDAO survDao;
	
	protected SimpleDateFormat dateFormatter;
	private List<CSVRecord> record;
	private CSVRecord heading;
	private int lastDataIndex;
	
	public SurveillanceUploadHandler2015() {
		dateFormatter = new SimpleDateFormat(DATE_FORMAT);
	}
	
	@Transactional(readOnly = true)
	public Surveillance handle() throws InvalidArgumentsException {
		Surveillance surv = new Surveillance();
		
		//get things that are only in the first row of the surveillance
		for(CSVRecord record : getRecord()) {
			String statusStr = record.get(0).trim();
			if(!StringUtils.isEmpty(statusStr) && statusStr.matches(FIRST_ROW_REGEX)) {
				parseSurveillanceDetails(record, surv);
			}
		}
		
		//get surveilled requirements
		for(CSVRecord record : getRecord()) {
			String statusStr = record.get(0).trim();
			if(!StringUtils.isEmpty(statusStr) && 
					(statusStr.matches(FIRST_ROW_REGEX) || statusStr.equalsIgnoreCase(SUBSEQUENT_ROW))) {
				parseSurveilledRequirements(record, surv);
			}
		}
			
		//get nonconformities
		for(CSVRecord record : getRecord()) {
			String statusStr = record.get(0).trim();
			if(!StringUtils.isEmpty(statusStr) && 
					(statusStr.matches(FIRST_ROW_REGEX) || statusStr.equalsIgnoreCase(SUBSEQUENT_ROW))) {
				parseNonconformities(record, surv);
			}
		}
				
		return surv;
	}

	public void parseSurveillanceDetails(CSVRecord record, Surveillance surv) {
		int colIndex = 1;
		
		//find the chpl product this surveillance will be attached to
		String chplId = record.get(colIndex++).trim();
		if(chplId.startsWith("CHP-")) {
			try {
				CertifiedProductDTO chplProduct = cpDao.getByChplNumber(chplId);
				if(chplProduct != null) {
					CertifiedProductDetailsDTO chplProductDetails = cpDao.getDetailsById(chplProduct.getId());
					if(chplProductDetails != null) {
						surv.setCertifiedProduct(new CertifiedProduct(chplProductDetails));
					} else {
						logger.error("Found chpl product with product id '" + chplId + "' but could not find certified product with id '" + chplProduct.getId() + "'.");
					}
				} else {
					logger.error("Could not find chpl product with product id '" + chplId + "'.");
				}
			} catch(EntityRetrievalException ex) {
				logger.error("Exception looking up CHPL product details for '" + chplId + "'.");
			}
		} else {
			try {
				CertifiedProductDetailsDTO chplProductDetails = cpDao.getByChplUniqueId(chplId);
				if(chplProductDetails != null) {
					surv.setCertifiedProduct(new CertifiedProduct(chplProductDetails));
				} else {
					logger.error("Could not find chpl product with unique id '" + chplId + "'.");
				}
			} catch(EntityRetrievalException ex){
				logger.error("Exception looking up " + chplId, ex);
			}
		}
		
		if(surv.getCertifiedProduct() == null || surv.getCertifiedProduct().getId() == null) {
			surv.getErrors().add("Could not find Certified Product with unique id " + chplId);	
		}
		
		//find the surveillance id in case this is an update
		String survFriendlyId = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(survFriendlyId)) {
			try {
				if(survFriendlyId.toUpperCase().startsWith("SURV")) {
					Long survId = Long.parseLong(survFriendlyId.substring(4));
					surv.setSurveillanceIdToReplace(survId);
				} else {
					Long survId = Long.parseLong(survFriendlyId);
					surv.setSurveillanceIdToReplace(survId);
				}
			} catch(Exception ex) {
				logger.error("Could not parse surveillance id '" + survFriendlyId + "'.");
			}
		}
		
		//surveillance begin date
		String beginDateStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(beginDateStr)) {
			try {
				Date beginDate = dateFormatter.parse(beginDateStr);
				surv.setStartDate(beginDate);
			} catch(ParseException pex) {
				logger.error("Could not parse begin date '" + beginDateStr + "'.");
			}
		}
		
		//surveillance end date
		String endDateStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(endDateStr)) {
			try {
				Date endDate = dateFormatter.parse(endDateStr);
				surv.setEndDate(endDate);
			} catch(ParseException pex) {
				logger.error("Could not parse end date '" + endDateStr + "'.");
			}
		}
		
		//surveillance type
		String survTypeStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(survTypeStr)) {
			SurveillanceType type = survDao.findSurveillanceType(survTypeStr);
			surv.setType(type);
		}
		
		//randomized sites num used
		String randomizedSitesUsedStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(randomizedSitesUsedStr)) {
			try {
				Integer randomizedSitesUsed = Integer.parseInt(randomizedSitesUsedStr);
				surv.setRandomizedSitesUsed(randomizedSitesUsed);
			} catch(Exception ex) {
				logger.error("Could not parse '" + randomizedSitesUsedStr + "' as an integer.");
			}
		}
	}
	
	public void parseSurveilledRequirements(CSVRecord record, Surveillance surv) {
		SurveillanceRequirement req = new SurveillanceRequirement();
		
		int colIndex = 7;
		//requirement type
		String requirementTypeStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(requirementTypeStr)) {
			SurveillanceRequirementType requirementType = survDao.findSurveillanceRequirementType(requirementTypeStr);
			req.setType(requirementType);
		}
		
		//the requirement
		String requirementStr = record.get(colIndex++).trim();
		req.setRequirement(requirementStr);
		
		//surveillance result
		String resultTypeStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(resultTypeStr)) {
			SurveillanceResultType resultType = survDao.findSurveillanceResultType(resultTypeStr);
			req.setResult(resultType);
		}
		surv.getRequirements().add(req);
	}
	
	public void parseNonconformities(CSVRecord record, Surveillance surv) {
		int colIndex = 8;
		
		//the requirement - to be used later for matching
		SurveillanceRequirement req = null;
		String requirementStr = record.get(colIndex).trim();
		if(!StringUtils.isEmpty(requirementStr)) {
			for(int i = 0; i < surv.getRequirements().size() && req == null; i++) {
				SurveillanceRequirement currReq = surv.getRequirements().get(i);
				if(currReq.getRequirement() != null && 
						currReq.getRequirement().equalsIgnoreCase(requirementStr)) {
					req = currReq;
				}
			}
		}
		
		colIndex = 10;
		SurveillanceNonconformity nc = new SurveillanceNonconformity();
		
		//nonconformity type
		String ncTypeStr = record.get(colIndex++).trim();
		nc.setNonconformityType(ncTypeStr);
		
		//nonconformity status
		String ncStatusStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(ncStatusStr)) {
			SurveillanceNonconformityStatus ncstatus = survDao.findSurveillanceNonconformityStatusType(ncStatusStr);
			nc.setStatus(ncstatus);
		}
		
		//date of determination
		String determinationDateStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(determinationDateStr)) {
			try {
				Date determinationDate = dateFormatter.parse(determinationDateStr);
				nc.setDateOfDetermination(determinationDate);
			} catch(ParseException pex) {
				logger.error("Could not parse determination date '" + determinationDateStr + "'.");
			}
		}
		
		//CAP approval date
		String capApprovalDateStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(capApprovalDateStr)) {
			try {
				Date capApprovalDate = dateFormatter.parse(capApprovalDateStr);
				nc.setCapApprovalDate(capApprovalDate);
			} catch(ParseException pex) {
				logger.error("Could not parse CAP approval date '" + capApprovalDateStr + "'.");
			}
		}		
		
		//action begin date
		String actionBeginDateStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(actionBeginDateStr)) {
			try {
				Date actionBeginDate = dateFormatter.parse(actionBeginDateStr);
				nc.setCapStartDate(actionBeginDate);
			} catch(ParseException pex) {
				logger.error("Could not parse action begin date '" + actionBeginDateStr + "'.");
			}
		}
		
		//must complete date
		String mustCompleteDateStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(mustCompleteDateStr)) {
			try {
				Date mustCompleteDate = dateFormatter.parse(mustCompleteDateStr);
				nc.setCapMustCompleteDate(mustCompleteDate);
			} catch(ParseException pex) {
				logger.error("Could not parse must complete date '" + mustCompleteDateStr + "'.");
			}
		}
		
		//was complete date
		String wasCompleteDateStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(wasCompleteDateStr)) {
			try {
				Date wasCompleteDate = dateFormatter.parse(wasCompleteDateStr);
				nc.setCapEndDate(wasCompleteDate);
			} catch(ParseException pex) {
				logger.error("Could not parse was complete date '" + wasCompleteDateStr + "'.");
			}
		}
		
		//summary
		String summary = record.get(colIndex++).trim();
		nc.setSummary(summary);
		
		//findings
		String findings = record.get(colIndex++).trim();
		nc.setFindings(findings);
		
		//sites passed
		String sitesPassedStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(sitesPassedStr)) {
			try {
				Integer sitesPassed = Integer.parseInt(sitesPassedStr);
				nc.setSitesPassed(sitesPassed);
			} catch(Exception ex) {
				logger.error("Could not parse '" + sitesPassedStr + "' as an integer.");
			}
		}
		
		//total sites used
		String totalSitesUsedStr = record.get(colIndex++).trim();
		if(!StringUtils.isEmpty(totalSitesUsedStr)) {
			try {
				Integer totalSitesUsed = Integer.parseInt(totalSitesUsedStr);
				nc.setTotalSites(totalSitesUsed);
			} catch(Exception ex) {
				logger.error("Could not parse '" + totalSitesUsedStr + "' as an integer.");
			}
		}
		
		//developer explanation
		String devExplanationStr = record.get(colIndex++).trim();
		nc.setDeveloperExplanation(devExplanationStr);
				
		//resolution
		String resolutionStr = record.get(colIndex++).trim();
		nc.setResolution(resolutionStr);
		
		if(req != null) {
			req.getNonconformities().add(nc);
		}
	}
	
	public List<CSVRecord> getRecord() {
		return record;
	}
	public void setRecord(List<CSVRecord> record) {
		this.record = record;
	}
	public CSVRecord getHeading() {
		return heading;
	}
	public void setHeading(CSVRecord heading) {
		this.heading = heading;
	}
	public int getLastDataIndex() {
		return lastDataIndex;
	}
	public void setLastDataIndex(int lastDataIndex) {
		this.lastDataIndex = lastDataIndex;
	}
}
