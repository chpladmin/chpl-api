package gov.healthit.chpl.certifiedProduct.upload;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.CQMCriterionEntity;
import gov.healthit.chpl.entity.PendingCertificationResultAdditionalSoftwareEntity;
import gov.healthit.chpl.entity.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestDataEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestFunctionalityEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestProcedureEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestStandardEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestTaskEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestTaskParticipantEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestToolEntity;
import gov.healthit.chpl.entity.PendingCertificationResultUcdProcessEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductAccessibilityStandardEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductQmsStandardEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductTargetedUserEntity;
import gov.healthit.chpl.entity.PendingTestParticipantEntity;
import gov.healthit.chpl.entity.PendingTestTaskEntity;
import gov.healthit.chpl.entity.PendingCqmCertificationCriteriaEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Component("certifiedProductHandler2015")
public class CertifiedProductHandler2015 extends CertifiedProductHandler {
	
	private static final Logger logger = LogManager.getLogger(CertifiedProductHandler2015.class);
	private List<PendingTestParticipantEntity> participants;
	private List<PendingTestTaskEntity> tasks;
	
	public PendingCertifiedProductEntity handle() {
		participants = new ArrayList<PendingTestParticipantEntity>();
		tasks = new ArrayList<PendingTestTaskEntity>();
		
		PendingCertifiedProductEntity pendingCertifiedProduct = new PendingCertifiedProductEntity();
		pendingCertifiedProduct.setStatus(getDefaultStatusId());
		
		//get the first row of the certified product
		for(CSVRecord record : getRecord()) {
			String statusStr = record.get(1);
			if(!StringUtils.isEmpty(statusStr) && FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)) {
				parseCertifiedProductDetails(record, pendingCertifiedProduct);
			}
		}
	
		//get the targeted users for the certified product
		for(CSVRecord record : getRecord()) {
			String statusStr = record.get(1);
			if(!StringUtils.isEmpty(statusStr) && 
					(FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr) ||
					 SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
				parseTargetedUsers(record, pendingCertifiedProduct);
			}
		}
				
		//get the QMS's for the certified product
		for(CSVRecord record : getRecord()) {
			String statusStr = record.get(1);
			if(!StringUtils.isEmpty(statusStr) && 
					(FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr) ||
					 SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
				parseQms(record, pendingCertifiedProduct);
			}
		}
		
		//get the accessibility standards for the certified product
		for(CSVRecord record : getRecord()) {
			String statusStr = record.get(1);
			if(!StringUtils.isEmpty(statusStr) && 
					(FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr) ||
					 SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
				parseAccessibilityStandards(record, pendingCertifiedProduct);
			}
		}
		if(!pendingCertifiedProduct.getAccessibilityCertified() && pendingCertifiedProduct.getAccessibilityStandards().size() > 0) {
			pendingCertifiedProduct.getErrorMessages().add(pendingCertifiedProduct.getUniqueId() + " has 'false' in the Accessibility Certified column but accessibility standards were found.");
		} else if(pendingCertifiedProduct.getAccessibilityCertified() && pendingCertifiedProduct.getAccessibilityStandards().size() == 0) {
			pendingCertifiedProduct.getErrorMessages().add(pendingCertifiedProduct.getUniqueId() + " has 'true' in the Accessibility Certified column but no accessibility standards were found.");
		}
		
		//parse CQMs starts at index 27
		for(CSVRecord record : getRecord()) {
			String statusStr = record.get(1);
			if(!StringUtils.isEmpty(statusStr) && 
					(FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr) ||
					 SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
				parseCqms(record, pendingCertifiedProduct);
			}
		}
		
		//test participant starts at index 33
		for(CSVRecord record : getRecord()) {
			String statusStr = record.get(1);
			if(!StringUtils.isEmpty(statusStr) && 
					(FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr) ||
					 SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
				parseTestParticipants(record, pendingCertifiedProduct);
			}
		}
		
		//tasks start at index 42
		for(CSVRecord record : getRecord()) {
			String statusStr = record.get(1);
			if(!StringUtils.isEmpty(statusStr) && 
					(FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr) ||
					 SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
				parseTestTasks(record, pendingCertifiedProduct);
			}
		}
		
		//parse criteria starts at index 56
		CSVRecord firstRow = null;
		for(int i = 0; i < getRecord().size() && firstRow == null; i++) {
			CSVRecord currRecord = getRecord().get(i);
			String statusStr = currRecord.get(1);
			if(!StringUtils.isEmpty(statusStr) && 
					FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)) {
				firstRow = currRecord;
			}
		}
		
		if(firstRow != null) {
			int criteriaBeginIndex = 56;
			int criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(4)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(5)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(6)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(7)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(8)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(9)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(10)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(11)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(12)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(13)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(14)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (a)(15)", firstRow, criteriaBeginIndex, criteriaEndIndex));	
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (b)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (b)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (b)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (b)(4)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (b)(5)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (b)(6)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (b)(7)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (b)(8)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (b)(9)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (c)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (c)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (c)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (c)(4)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (d)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (d)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (d)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (d)(4)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (d)(5)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (d)(6)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (d)(7)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (d)(8)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (d)(9)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (d)(10)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (d)(11)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (e)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (e)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (e)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (f)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (f)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (f)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (f)(4)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (f)(5)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (f)(6)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (f)(7)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (g)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (g)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (g)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (g)(4)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (g)(5)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (g)(6)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (g)(7)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (g)(8)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (g)(9)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (h)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.315 (h)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
		}

		return pendingCertifiedProduct;
	}
	
	private void parseCertifiedProductDetails(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
		int colIndex = 0;
		
		String uniqueId = record.get(colIndex++);
		pendingCertifiedProduct.setUniqueId(uniqueId.trim());
		
		String recordStatus = record.get(colIndex++);
		pendingCertifiedProduct.setRecordStatus(recordStatus);
		
		//developer, product, version
		String developer = record.get(colIndex++);
		String product = record.get(colIndex++);
		String productVersion = record.get(colIndex++);
		pendingCertifiedProduct.setDeveloperName(developer.trim());
		pendingCertifiedProduct.setProductName(product.trim());
		pendingCertifiedProduct.setProductVersion(productVersion.trim());

		DeveloperDTO foundDeveloper = developerDao.getByName(developer.trim());
		if(foundDeveloper != null) {
			pendingCertifiedProduct.setDeveloperId(foundDeveloper.getId());
			
			//product
			ProductDTO foundProduct = productDao.getByDeveloperAndName(foundDeveloper.getId(), product.trim());
			if(foundProduct != null) {
				pendingCertifiedProduct.setProductId(foundProduct.getId());
				
				//version
				ProductVersionDTO foundVersion = versionDao.getByProductAndVersion(foundProduct.getId(), productVersion.trim());
				if(foundVersion != null) {
					pendingCertifiedProduct.setProductVersionId(foundVersion.getId());
				}
			}
		}	
		
		//certification year
		String certificaitonYear = record.get(colIndex++);
		pendingCertifiedProduct.setCertificationEdition(certificaitonYear.trim());
		if(!pendingCertifiedProduct.getCertificationEdition().equals("2015")) {
			pendingCertifiedProduct.getErrorMessages().add("Expecting certification year 2015 but found '" + pendingCertifiedProduct.getCertificationEdition() + "' for product " + pendingCertifiedProduct.getUniqueId());
		}
		CertificationEditionDTO foundEdition = editionDao.getByYear(certificaitonYear.trim());
		if(foundEdition != null) {
			pendingCertifiedProduct.setCertificationEditionId(new Long(foundEdition.getId()));
		}
		
		//acb certification id
		pendingCertifiedProduct.setAcbCertificationId(record.get(colIndex++).trim());	
		
		//certification body
		String acbName = record.get(colIndex++);
		pendingCertifiedProduct.setCertificationBodyName(acbName.trim());
		CertificationBodyDTO foundAcb = acbDao.getByName(acbName.trim());
		if(foundAcb != null) {
			pendingCertifiedProduct.setCertificationBodyId(foundAcb.getId());
		} else {
			pendingCertifiedProduct.getErrorMessages().add("No certification body with name '" + acbName.trim() + "' could be found for product " + pendingCertifiedProduct.getUniqueId() + ".'");
		}
		
		//testing lab
		String atlName = record.get(colIndex++);
		pendingCertifiedProduct.setTestingLabName(atlName.trim());
		TestingLabDTO foundAtl = atlDao.getByName(atlName.trim());
		if(foundAtl != null) {
			pendingCertifiedProduct.setTestingLabId(foundAtl.getId());
		}	
	
		//certification date
		String dateStr = record.get(colIndex++);
		try {
			Date certificationDate = dateFormatter.parse(dateStr);
			pendingCertifiedProduct.setCertificationDate(certificationDate);
		} catch(ParseException ex) {
			pendingCertifiedProduct.setCertificationDate(null);
		}		
		
		//developer address info
		String developerStreetAddress = record.get(colIndex++).trim();
		String developerState = record.get(colIndex++).trim();
		String developerCity = record.get(colIndex++).trim();
		String developerZipcode = record.get(colIndex++).trim();
		String developerWebsite = record.get(colIndex++).trim();
		String developerEmail = record.get(colIndex++).trim();
		String developerPhone = record.get(colIndex++).trim();
		String developerContactName = record.get(colIndex++).trim();
		pendingCertifiedProduct.setDeveloperStreetAddress(developerStreetAddress);
		pendingCertifiedProduct.setDeveloperCity(developerCity);
		pendingCertifiedProduct.setDeveloperState(developerState);
		pendingCertifiedProduct.setDeveloperZipCode(developerZipcode);
		pendingCertifiedProduct.setDeveloperWebsite(developerWebsite);
		pendingCertifiedProduct.setDeveloperEmail(developerEmail);	
		pendingCertifiedProduct.setDeveloperPhoneNumber(developerPhone);
		pendingCertifiedProduct.setDeveloperContactName(developerContactName);
		
		//look for contact in db
		ContactDTO contactToFind = new ContactDTO();
		contactToFind.setLastName(developerContactName);
		contactToFind.setEmail(developerEmail);
		contactToFind.setPhoneNumber(developerPhone);
		ContactDTO foundContact = contactDao.getByValues(contactToFind);
		if(foundContact != null) {
			pendingCertifiedProduct.setDeveloperContactId(foundContact.getId());
		}
		
		AddressDTO toFind = new AddressDTO();
		toFind.setStreetLineOne(developerStreetAddress);
		toFind.setCity(developerCity);
		toFind.setState(developerState);
		toFind.setZipcode(developerZipcode);
		AddressDTO foundAddress = addressDao.getByValues(toFind);
		if(foundAddress != null) {
			AddressEntity addressEntity = null;
			try {
				addressEntity = addressDao.getEntityById(foundAddress.getId());
			} catch(EntityRetrievalException ex) {
				addressEntity = null;
			}
			pendingCertifiedProduct.setDeveloperAddress(addressEntity);
		}		
		
		//targeted users
		colIndex++;
		
		//qms
		colIndex+=3;
		
		String hasIcsStr = record.get(colIndex++);
		pendingCertifiedProduct.setIcs(asBoolean(hasIcsStr));

		//accessibility certified
		String isAccessibilityCertified = record.get(colIndex++);
		pendingCertifiedProduct.setAccessibilityCertified(asBoolean(isAccessibilityCertified));
		//accessibility standards
		colIndex++;
		
		//(k)(1)  url
		pendingCertifiedProduct.setTransparencyAttestationUrl(record.get(colIndex++).trim());
		
		//(k)(2) attestation status
		String k2AttestationStr = record.get(colIndex++);
		if(!StringUtils.isEmpty(k2AttestationStr)) {
			if("0".equals(k2AttestationStr.trim())) {
				pendingCertifiedProduct.setTransparencyAttestation(AttestationType.Negative);
			} else if("1".equals(k2AttestationStr.trim())) {
				pendingCertifiedProduct.setTransparencyAttestation(AttestationType.Affirmative);
			} else if("2".equals(k2AttestationStr.trim())) {
				pendingCertifiedProduct.setTransparencyAttestation(AttestationType.NA);
			}
		}
		
		//cqms
		colIndex+=3;
		
		pendingCertifiedProduct.setSedReportFileLocation(record.get(colIndex++));
		pendingCertifiedProduct.setSedIntendedUserDescription(record.get(colIndex++));
		String sedTestingEnd = record.get(colIndex++);
		if(!StringUtils.isEmpty(sedTestingEnd)) {
			try {
				Date sedTestingEndDate = dateFormatter.parse(sedTestingEnd);
				pendingCertifiedProduct.setSedTestingEnd(sedTestingEndDate);
			} catch(ParseException ex) {
				logger.error("Could not parse " + sedTestingEnd, ex);
				pendingCertifiedProduct.getErrorMessages().add("Product " + pendingCertifiedProduct.getUniqueId() + " has an invalid sed testing end date '" + sedTestingEnd + "'.");
			}
		}
	}
	
	private void parseQms(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
		int colIndex = 19;
		if(!StringUtils.isEmpty(record.get(colIndex))) {
			String qmsStandardName = record.get(colIndex++).toString();
			QmsStandardDTO qmsStandard = qmsDao.getByName(qmsStandardName.trim());
			String applicableCriteria = record.get(colIndex++).toString();
			String qmsMods = record.get(colIndex).toString();
			
			PendingCertifiedProductQmsStandardEntity qmsEntity = new PendingCertifiedProductQmsStandardEntity();
			qmsEntity.setMappedProduct(pendingCertifiedProduct);
			qmsEntity.setModification(qmsMods.trim());
			qmsEntity.setApplicableCriteria(applicableCriteria);
			qmsEntity.setName(qmsStandardName.trim());
			if(qmsStandard != null) {
				qmsEntity.setQmsStandardId(qmsStandard.getId());
			}
			pendingCertifiedProduct.getQmsStandards().add(qmsEntity);
		} 
	}
	
	private void parseTargetedUsers(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
		int colIndex = 18;
		if(!StringUtils.isEmpty(record.get(colIndex))) {
			String targetedUserName = record.get(colIndex).toString();
			TargetedUserDTO targetedUser = tuDao.getByName(targetedUserName.trim());
			
			PendingCertifiedProductTargetedUserEntity tuEntity = new PendingCertifiedProductTargetedUserEntity();
			tuEntity.setMappedProduct(pendingCertifiedProduct);
			tuEntity.setName(targetedUserName.trim());
			if(targetedUser != null) {
				tuEntity.setTargetedUserId(targetedUser.getId());
			}
			pendingCertifiedProduct.getTargetedUsers().add(tuEntity);
		} 
	}
	
	private void parseAccessibilityStandards(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
		int colIndex = 24;
		if(!StringUtils.isEmpty(record.get(colIndex))) {
			String accessibilityStandardName = record.get(colIndex).toString();
			AccessibilityStandardDTO std = stdDao.getByName(accessibilityStandardName.trim());
			
			PendingCertifiedProductAccessibilityStandardEntity stdEntity = new PendingCertifiedProductAccessibilityStandardEntity();
			stdEntity.setMappedProduct(pendingCertifiedProduct);
			stdEntity.setName(accessibilityStandardName.trim());
			if(std != null) {
				stdEntity.setAccessibilityStandardId(std.getId());
			}
			pendingCertifiedProduct.getAccessibilityStandards().add(stdEntity);
		} 
	}
	
	private void parseTestParticipants(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProductEntity) {
		int colIndex = 33;
		if(!StringUtils.isEmpty(record.get(colIndex))) {
			PendingTestParticipantEntity participant = new PendingTestParticipantEntity(); 
			participant.setUniqueId(record.get(colIndex++).toString());
			participant.setGender(record.get(colIndex++).toString());
			String ageStr = record.get(colIndex++).toString();
			try {
				Integer age = new Integer(ageStr);
				participant.setAge(age);
			} catch(Exception ex) {
				logger.error("Could not parse " + ageStr + " into an integer.");
			}
			String educationLevel = record.get(colIndex++).toString();
			EducationTypeDTO educationDto = educationDao.getByName(educationLevel);
			if(educationDto != null) {
				participant.setEducationTypeId(educationDto.getId());
			} else {
				logger.error("Education level '" + educationLevel + "' does not match any of the allowed options.");
			}
			participant.setOccupation(record.get(colIndex++).toString());
			String profExperienceStr = record.get(colIndex++).toString();
			try {
				Integer profExperience = new Integer(profExperienceStr);
				participant.setProfessionalExperienceMonths(profExperience);
			}catch(Exception ex) {
				logger.error("Could not parse " + profExperienceStr + " into an integer.");
			}
			
			String computerExperienceStr = record.get(colIndex++).toString();
			try {
				Integer computerExperience = new Integer(computerExperienceStr);
				participant.setComputerExperienceMonths(computerExperience);
			}catch(Exception ex) {
				logger.error("Could not parse " + computerExperienceStr + " into an integer.");
			}
			
			String productExperienceStr = record.get(colIndex++).toString();
			try {
				Integer productExperience = new Integer(productExperienceStr);
				participant.setProductExperienceMonths(productExperience);
			}catch(Exception ex) {
				logger.error("Could not parse " + productExperienceStr + " into an integer.");
			}
			
			participant.setAssistiveTechnologyNeeds(record.get(colIndex).toString());
			this.participants.add(participant);
		} 
	}
	
	private void parseTestTasks(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProductEntity) {
		int colIndex = 42;
		if(StringUtils.isEmpty(record.get(colIndex))) {
			return;
		}
		
		PendingTestTaskEntity task = new PendingTestTaskEntity();
		task.setUniqueId(record.get(colIndex++).toString());
		task.setDescription(record.get(colIndex++).toString());
		String successAvgStr = record.get(colIndex++).toString();
		try {
			Float successAvg = new Float(successAvgStr);
			task.setTaskSuccessAverage(successAvg);
		} catch(Exception ex) {
			logger.error("Cannot convert " + successAvgStr + " to a Float.");
		}
		String successStddevStr = record.get(colIndex++).toString();
		try {
			Float successStddev = new Float(successStddevStr);
			task.setTaskSuccessStddev(successStddev);
		} catch(Exception ex) {
			logger.error("Cannot convert " + successStddevStr + " to a Float.");
		}
		String taskPathDeviationObsStr = record.get(colIndex++).toString();
		try {
			Integer taskPathDeviationObs = new Integer(taskPathDeviationObsStr);
			task.setTaskPathDeviationObserved(taskPathDeviationObs);
		} catch(Exception ex) {
			logger.error("Cannot convert " + taskPathDeviationObsStr + " to a Integer.");
		}
		String taskPathDeviationOptStr = record.get(colIndex++).toString();
		try {
			Integer taskPathDeviationOpt = new Integer(taskPathDeviationOptStr);
			task.setTaskPathDeviationOptimal(taskPathDeviationOpt);
		} catch(Exception ex) {
			logger.error("Cannot convert " + taskPathDeviationOptStr + " to a Integer.");
		}
		String taskTimeAvgStr = record.get(colIndex++).toString();
		try {
			Long taskTimeAvg = new Long(taskTimeAvgStr);
			task.setTaskTimeAvg(taskTimeAvg);
		} catch(Exception ex) {
			logger.error("Cannot convert " + taskTimeAvgStr + " to a Integer.");
		}
		String taskTimeStddevStr = record.get(colIndex++).toString();
		try {
			Integer taskTimeStddev = new Integer(taskTimeStddevStr);
			task.setTaskTimeStddev(taskTimeStddev);
		} catch(Exception ex) {
			logger.error("Cannot convert " + taskTimeStddevStr + " to a Integer.");
		}
		String taskTimeDeviationAvgStr = record.get(colIndex++).toString();
		try {
			Integer taskTimeDeviationAvg = new Integer(taskTimeDeviationAvgStr);
			task.setTaskTimeDeviationObservedAvg(taskTimeDeviationAvg);
		} catch(Exception ex) {
			logger.error("Cannot convert " + taskTimeDeviationAvgStr + " to a Integer.");
		}
		String taskTimeDeviationOptimalAvgStr = record.get(colIndex++).toString();
		try {
			Integer taskTimeDeviationOptimalAvg = new Integer(taskTimeDeviationOptimalAvgStr);
			task.setTaskTimeDeviationOptimalAvg(taskTimeDeviationOptimalAvg);
		} catch(Exception ex) {
			logger.error("Cannot convert " + taskTimeDeviationOptimalAvgStr + " to a Integer.");
		}
		String taskErrorsAvgStr = record.get(colIndex++).toString();
		try {
			Float taskErrorsAvg = new Float(taskErrorsAvgStr);
			task.setTaskErrors(taskErrorsAvg);
		} catch(Exception ex) {
			logger.error("Cannot convert " + taskErrorsAvgStr + " to a Float.");
		}
		String taskErrorsStddevStr = record.get(colIndex++).toString();
		try {
			Float taskErrorsStddev = new Float(taskErrorsStddevStr);
			task.setTaskErrorsStddev(taskErrorsStddev);
		} catch(Exception ex) {
			logger.error("Cannot convert " + taskErrorsStddevStr + " to a Float.");
		}
		task.setTaskRatingScale(record.get(colIndex++).toString());
		String taskRatingStr = record.get(colIndex++).toString();
		try {
			Float taskRating = new Float(taskRatingStr);
			task.setTaskRating(taskRating);
		} catch(Exception ex) {
			logger.error("Cannot convert " + taskRatingStr + " to a Float.");
		}
		this.tasks.add(task);
	}
	
	private void parseCqms(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
		int cqmNameIndex = 27;
		int cqmVersionIndex = 28;
		int cqmCriteriaIndex = 29;
		
		String cqmName = record.get(cqmNameIndex).trim();
		String cqmVersions = record.get(cqmVersionIndex).trim();
		String cqmCriteria = record.get(cqmCriteriaIndex).trim();
		
		List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion(pendingCertifiedProduct, cqmName, cqmVersions, cqmCriteria);
		for(PendingCqmCriterionEntity entity : criterion) {
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		}
	}
	
	private int getCriteriaEndIndex(int beginIndex) {
		int criteriaBeginIndex = beginIndex;
		int criteriaEndIndex = criteriaBeginIndex+1;
		String colTitle = getHeading().get(criteriaBeginIndex).toString();
		if(colTitle.startsWith(CRITERIA_COL_HEADING_BEGIN)) {
			colTitle = getHeading().get(criteriaEndIndex).toString();
			while(criteriaEndIndex <= getLastDataIndex() && 
					!colTitle.startsWith(CRITERIA_COL_HEADING_BEGIN)) {
				criteriaEndIndex++;
				if(criteriaEndIndex <= getLastDataIndex()) {
					colTitle = getHeading().get(criteriaEndIndex).toString();
				}
			}
		} else {
			return -1;
		}
		return criteriaEndIndex-1;
	}
	
	private PendingCertificationResultEntity parseCriteria(PendingCertifiedProductEntity pendingCertifiedProduct, String criteriaNumber, CSVRecord firstRow, int beginIndex, int endIndex) {
		int currIndex = beginIndex;
		PendingCertificationResultEntity cert = null;
		try {
			cert = getCertificationResult(criteriaNumber, firstRow.get(currIndex++).toString());
			
			while(currIndex <= endIndex) {
				String colTitle = getHeading().get(currIndex).toString();
				if(!StringUtils.isEmpty(colTitle)) {
					colTitle = colTitle.trim().toUpperCase();
					switch(colTitle) {
					case "GAP":
						cert.setGap(asBoolean(firstRow.get(currIndex++).toString()));
						break;
					case "PRIVACY AND SECURITY FRAMEWORK":
						cert.setPrivacySecurityFramework(firstRow.get(currIndex++).toString());
						break;
					case "API DOCUMENTATION LINK":
						cert.setApiDocumentation(firstRow.get(currIndex++).toString());
						break;
					case "STANDARD TESTED AGAINST":
						parseTestStandards(cert, currIndex);
						currIndex++;
						break;
					case "FUNCTIONALITY TESTED":
						parseTestFunctionality(cert, currIndex);
						currIndex++;
						break;
					case "MEASURE SUCCESSFULLY TESTED FOR G1":
						cert.setG1Success(asBoolean(firstRow.get(currIndex++).toString()));
						break;
					case "MEASURE SUCCESSFULLY TESTED FOR G2":
						cert.setG2Success(asBoolean(firstRow.get(currIndex++).toString()));
						break;
					case "ADDITIONAL SOFTWARE":
						Boolean hasAdditionalSoftware = asBoolean(firstRow.get(currIndex).toString());
						cert.setHasAdditionalSoftware(hasAdditionalSoftware);
						parseAdditionalSoftware(pendingCertifiedProduct, cert, currIndex);
						currIndex += 6; 
						break;
					case "TEST TOOL NAME":
						parseTestTools(cert, currIndex);
						currIndex += 2;
					case "TEST PROCEDURE VERSION":
						parseTestProcedures(cert, currIndex);
						currIndex++;
						break;
					case "TEST DATA VERSION":
						parseTestData(pendingCertifiedProduct, cert, currIndex);
						currIndex += 3;
						break;
					case "UCD PROCESS SELECTED":
						PendingCertificationResultUcdProcessEntity ucd = new PendingCertificationResultUcdProcessEntity();
						String ucdName = firstRow.get(currIndex++).toString();
						String ucdDetails = firstRow.get(currIndex++).toString();
						
						if(!StringUtils.isEmpty(ucdName)) {
							ucd.setUcdProcessName(ucdName);
							ucd.setUcdProcessDetails(ucdDetails);
							UcdProcessDTO dto = ucdDao.getByName(ucd.getUcdProcessName());
							if(dto != null) {
								ucd.setUcdProcessId(dto.getId());
							}
							cert.getUcdProcesses().add(ucd);
						}
						break;
					case "TASK IDENTIFIER":
						parseTasksAndParticipants(pendingCertifiedProduct, cert, currIndex);
						currIndex+=2;
						break;
					default:
						logger.error("Could not handle column " + colTitle + " at index " + currIndex + ".");
					}
				}
			}						
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		return cert;
	}
	
	private void parseTestStandards(PendingCertificationResultEntity cert, int tsColumn) {
		for(CSVRecord row : getRecord()) {
			String tsValue = row.get(tsColumn).toString();
			if(!StringUtils.isEmpty(tsValue)) {
				PendingCertificationResultTestStandardEntity tsEntity = new PendingCertificationResultTestStandardEntity();
				tsEntity.setTestStandardName(tsValue);
				TestStandardDTO ts = testStandardDao.getByNumber(tsValue);
				if(ts != null) {
					tsEntity.setTestStandardId(ts.getId());
				}
				cert.getTestStandards().add(tsEntity);
			}
		}
	}
	
	private void parseTestFunctionality(PendingCertificationResultEntity cert, int tfColumn) {
		for(CSVRecord row : getRecord()) {
			String tfValue = row.get(tfColumn).toString();
			if(!StringUtils.isEmpty(tfValue)) {
				PendingCertificationResultTestFunctionalityEntity tfEntity = new PendingCertificationResultTestFunctionalityEntity();
				tfEntity.setTestFunctionalityNumber(tfValue);
				TestFunctionalityDTO tf = testFunctionalityDao.getByNumber(tfValue);
				if(tf != null) {
					tfEntity.setTestFunctionalityId(tf.getId());
				}
				cert.getTestFunctionality().add(tfEntity);
			}
		}
	}
	
	private void parseAdditionalSoftware(PendingCertifiedProductEntity product, PendingCertificationResultEntity cert, int asColumnBegin) {
		int cpSourceColumn = asColumnBegin+1;
		int nonCpSourceColumn = asColumnBegin+3;
		
		for(CSVRecord row : getRecord()) {
			String cpSourceValue = row.get(cpSourceColumn).toString().trim();
			if(!StringUtils.isEmpty(cpSourceValue)) {
				PendingCertificationResultAdditionalSoftwareEntity asEntity = new PendingCertificationResultAdditionalSoftwareEntity();
				asEntity.setChplId(cpSourceValue);
				if(cpSourceValue.startsWith("CHP-")) {
					CertifiedProductDTO cp = certifiedProductDao.getByChplNumber(cpSourceValue);
					if(cp != null) {
						asEntity.setCertifiedProductId(cp.getId());
					}
				} else {
					try {
						CertifiedProductDetailsDTO cpd = certifiedProductDao.getByChplUniqueId(cpSourceValue);
						if(cpd != null) {
							asEntity.setCertifiedProductId(cpd.getId());
						}
					} catch(EntityRetrievalException ex) {
						logger.error(ex.getMessage(), ex);
					}
				}
				asEntity.setGrouping(row.get(cpSourceColumn+1).toString().trim());
				cert.getAdditionalSoftware().add(asEntity);
			} 
			String nonCpSourceValue = row.get(nonCpSourceColumn).toString().trim();
			if(!StringUtils.isEmpty(nonCpSourceValue)) {
				PendingCertificationResultAdditionalSoftwareEntity asEntity = new PendingCertificationResultAdditionalSoftwareEntity();
				asEntity.setSoftwareName(nonCpSourceValue);
				asEntity.setSoftwareVersion(row.get(nonCpSourceColumn+1).toString().trim());
				asEntity.setGrouping(row.get(nonCpSourceColumn+2).toString().trim());
				cert.getAdditionalSoftware().add(asEntity);
			}
		}
		
		if(cert.isHasAdditionalSoftware() && cert.getAdditionalSoftware().size() == 0) {
			product.getErrorMessages().add("Certification " + cert.getMappedCriterion().getNumber() + " for product " + product.getUniqueId() + " indicates additional software should be present but none was found.");
		} else if(!cert.isHasAdditionalSoftware() && cert.getAdditionalSoftware().size() > 0) {
			product.getErrorMessages().add("Certification " + cert.getMappedCriterion().getNumber() + " for product " + product.getUniqueId() + " indicates additional software should not be present but some was found.");
		}
	}
	
	private void parseTestProcedures(PendingCertificationResultEntity cert, int tpColumn) {
		for(CSVRecord row : getRecord()) {
			String tpValue = row.get(tpColumn).toString();
			if(!StringUtils.isEmpty(tpValue)) {
				PendingCertificationResultTestProcedureEntity tpEntity = new PendingCertificationResultTestProcedureEntity();
				tpEntity.setTestProcedureVersion(tpValue);
				TestProcedureDTO tp = testProcedureDao.getByName(tpValue);
				if(tp != null) {
					tpEntity.setTestProcedureId(tp.getId());
				}
				cert.getTestProcedures().add(tpEntity);
			}
		}
	}
	
	private void parseTestData(PendingCertifiedProductEntity product, PendingCertificationResultEntity cert, int tdColumnBegin) {
		for(CSVRecord row : getRecord()) {
			String tdVersionValue = row.get(tdColumnBegin).toString();
			if(!StringUtils.isEmpty(tdVersionValue)) {
				PendingCertificationResultTestDataEntity tdEntity = new PendingCertificationResultTestDataEntity();
				tdEntity.setVersion(tdVersionValue);
				Boolean hasAlteration = asBoolean(row.get(tdColumnBegin+1).toString());
				tdEntity.setHasAlteration(hasAlteration);
				String alterationStr = row.get(tdColumnBegin+2).toString();
				if(tdEntity.isHasAlteration() && StringUtils.isEmpty(alterationStr)) {
					product.getErrorMessages().add("Certification " + cert.getMappedCriterion().getNumber() + " for product " + product.getUniqueId() + " indicates test data was altered however no test data alteration was found."); 
				} else if(!tdEntity.isHasAlteration() && !StringUtils.isEmpty(alterationStr)) {
					product.getErrorMessages().add("Certification " + cert.getMappedCriterion().getNumber() + " for product " + product.getUniqueId() + " indicates test data was not altered however a test data alteration was found."); 
				}
				tdEntity.setAlteration(row.get(tdColumnBegin+2).toString());
				cert.getTestData().add(tdEntity);
			}
		}
	}
	
	private void parseTestTools(PendingCertificationResultEntity cert, int toolColumnBegin) {
		for(CSVRecord row : getRecord()) {
			String testToolName = row.get(toolColumnBegin).toString();
			String testToolVersion = row.get(toolColumnBegin+1).toString();
			if(!StringUtils.isEmpty(testToolName)) {
				PendingCertificationResultTestToolEntity ttEntity = new PendingCertificationResultTestToolEntity();
				ttEntity.setTestToolName(testToolName);
				ttEntity.setTestToolVersion(testToolVersion);
				TestToolDTO testTool = testToolDao.getByName(testToolName);
				if(testTool != null) {
					ttEntity.setTestToolId(testTool.getId());
				}
				cert.getTestTools().add(ttEntity);
			}
		}
	}
	
	private void parseTasksAndParticipants(PendingCertifiedProductEntity product, PendingCertificationResultEntity cert, int taskColumnBegin) {
		for(CSVRecord row : getRecord()) {
			String taskUniqueId = row.get(taskColumnBegin).toString();
			if(!StringUtils.isEmpty(taskUniqueId)) {
				PendingTestTaskEntity taskEntity = null;
				for(PendingTestTaskEntity task : this.tasks) {
					if(task.getUniqueId().equals(taskUniqueId)) {
						taskEntity = task;
					}
				}
				if(taskEntity == null) {
					product.getErrorMessages().add("Certification " + cert.getMappedCriterion().getNumber() + " for product " + product.getUniqueId() + " has no task with unique id " + taskUniqueId + " defined in the file.");
				} else {
					PendingCertificationResultTestTaskEntity certTask = new PendingCertificationResultTestTaskEntity();
					certTask.setTestTask(taskEntity);
					
					String participantUniqueIdStr = row.get(taskColumnBegin+1).toString();
					String[] participantUniqueIds = participantUniqueIdStr.split(";");
					for(int i = 0; i < participantUniqueIds.length; i++) {
						PendingTestParticipantEntity participantEntity = null;
						for(PendingTestParticipantEntity participant : this.participants) {
							if(participant.getUniqueId().equals(participantUniqueIds[i])) {
								participantEntity = participant;
							}
						}
						if(participantEntity == null) {
							product.getErrorMessages().add("Certification " + cert.getMappedCriterion().getNumber() + " for product " + product.getUniqueId() + " has no participant with unique id '" + participantUniqueIds[i] + "'  defined in the file.");
						} else {
							PendingCertificationResultTestTaskParticipantEntity ttPartEntity = new PendingCertificationResultTestTaskParticipantEntity();
							ttPartEntity.setCertTestTask(certTask);
							ttPartEntity.setTestParticipant(participantEntity);
							certTask.getTestParticipants().add(ttPartEntity);
						}
					}
					cert.getTestTasks().add(certTask);
				}
			}
		}
	}
	
	public List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms) {
		List<CQMCriterion> criteria = new ArrayList<CQMCriterion>();
		for (CQMCriterion criterion : allCqms) {
			if (!StringUtils.isEmpty(criterion.getCmsId()) && criterion.getCmsId().startsWith("CMS")) {
				criteria.add(criterion);
			}
		}
		return criteria;
	}

	
	/**
	 * look up a CQM CMS criteria by number and version. throw an error if we can't find it
	 * @param criterionNum
	 * @param column
	 * @return
	 * @throws InvalidArgumentsException
	 */
	protected List<PendingCqmCriterionEntity> handleCqmCmsCriterion(PendingCertifiedProductEntity product, String criterionNum, String version, String mappedCriteria) {
		if(!StringUtils.isEmpty(version)) {
			version = version.trim();
		}
		
		List<PendingCqmCriterionEntity> result = new ArrayList<PendingCqmCriterionEntity>();

		if(!StringUtils.isEmpty(version) && !"0".equals(version)) {
			//split on ;
			String[] versionList = version.split(";");
			if(versionList.length == 1) {
				//also try splitting on ,
				versionList= version.split(",");
			}
			
			for(int i = 0; i < versionList.length; i++) {
				String currVersion = versionList[i];
				if(!criterionNum.startsWith("CMS")) {
					criterionNum = "CMS" + criterionNum;
				}
				CQMCriterionEntity cqmEntity = cqmDao.getCMSEntityByNumberAndVersion(criterionNum, currVersion);
				if(cqmEntity == null) {
					product.getErrorMessages().add("Could not find a CQM CMS criterion matching " + criterionNum + " and version " + currVersion + " for product " + product.getUniqueId());
				}
				
				PendingCqmCriterionEntity currResult = new PendingCqmCriterionEntity();
				currResult.setMappedCriterion(cqmEntity);
				currResult.setMeetsCriteria(true);	
				
				//check on mapped criteria
				if(!StringUtils.isEmpty(mappedCriteria) && !"0".equals(mappedCriteria)) {
					//split on ;
					String[] criteriaList = mappedCriteria.split(";");
					if(criteriaList.length == 1) {
						//also try splitting on ,
						criteriaList= mappedCriteria.split(",");
					}
					
					for(int j = 0; j < criteriaList.length; j++) {
						String currCriteria = criteriaList[j];
						CertificationCriterionDTO cert = null;
						if(currCriteria.startsWith("170.315")) {
							cert = certDao.getByName(currCriteria);
						} else if(currCriteria.equals("c1")) {
							cert = certDao.getByName("170.315 (c)(1)");
						} else if(currCriteria.equals("c2")) {
							cert = certDao.getByName("170.315 (c)(2)");
						}
						if(cert != null) {
							PendingCqmCertificationCriteriaEntity certEntity = new PendingCqmCertificationCriteriaEntity();
							certEntity.setCertificationId(cert.getId());
							currResult.getCertifications().add(certEntity);
						} else {
							product.getErrorMessages().add("Could not find a certification criteria matching " + currCriteria + " for product " + product.getUniqueId());
						}
					}
				}
				result.add(currResult);
			}
		}
		
		return result;
	}
}
