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
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.ProductClassificationTypeDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.CQMCriterionEntity;
import gov.healthit.chpl.entity.PendingCertificationResultAdditionalSoftwareEntity;
import gov.healthit.chpl.entity.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestDataEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestFunctionalityEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestProcedureEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestStandardEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestToolEntity;
import gov.healthit.chpl.entity.PendingCertificationResultUcdProcessEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductQmsStandardEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Component("certifiedProductHandler2014")
public class CertifiedProductHandler2014 extends CertifiedProductHandler {
	
	private static final Logger logger = LogManager.getLogger(CertifiedProductHandler2014.class);
	
	public PendingCertifiedProductEntity handle() {
		PendingCertifiedProductEntity pendingCertifiedProduct = new PendingCertifiedProductEntity();
		pendingCertifiedProduct.setStatus(getDefaultStatusId());
		
		//get the first row of the certified product
		for(CSVRecord record : getRecord()) {
			String statusStr = record.get(1);
			if(!StringUtils.isEmpty(statusStr) && FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)) {
				parseCertifiedProductDetails(record, pendingCertifiedProduct);
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
		if(!pendingCertifiedProduct.isHasQms() && pendingCertifiedProduct.getQmsStandards().size() > 0) {
			pendingCertifiedProduct.getErrorMessages().add(pendingCertifiedProduct.getUniqueId() + " has 'false' in the QMS column but a QMS was found.");
		} else if(pendingCertifiedProduct.isHasQms() && pendingCertifiedProduct.getQmsStandards().size() == 0) {
			pendingCertifiedProduct.getErrorMessages().add(pendingCertifiedProduct.getUniqueId() + " has 'true' in the QMS column but no QMS was found.");
		}
		
		//parse CQMs starts at index 28
		for(CSVRecord record : getRecord()) {
			String statusStr = record.get(1);
			if(!StringUtils.isEmpty(statusStr) && 
					(FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr) ||
					 SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
				parseCqms(record, pendingCertifiedProduct);
			}
		}
		
		//parse criteria starts at index 30
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
			int criteriaBeginIndex = 30;
			int criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(4)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(5)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(6)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(7)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(8)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(9)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(10)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(11)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(12)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(13)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(14)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(15)", firstRow, criteriaBeginIndex, criteriaEndIndex));	
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(16)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(17)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(18)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(19)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (a)(20)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (b)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (b)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (b)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (b)(4)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (b)(5)(A)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (b)(5)(B)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (b)(6)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (b)(7)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (b)(8)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (b)(9)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (c)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (c)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (c)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (d)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (d)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (d)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (d)(4)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (d)(5)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (d)(6)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (d)(7)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (d)(8)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (d)(9)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (e)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (e)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (e)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (f)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (f)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (f)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (f)(4)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (f)(5)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (f)(6)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (f)(7)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (g)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (g)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (g)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (g)(4)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (h)(1)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (h)(2)", firstRow, criteriaBeginIndex, criteriaEndIndex));
			criteriaBeginIndex = criteriaEndIndex+1;
			criteriaEndIndex = getCriteriaEndIndex(criteriaBeginIndex);
			pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct, "170.314 (h)(3)", firstRow, criteriaBeginIndex, criteriaEndIndex));
		}

		return pendingCertifiedProduct;
	}
	
	private void parseCertifiedProductDetails(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
		int colIndex = 0;
		
		String uniqueId = record.get(colIndex++);
		pendingCertifiedProduct.setUniqueId(uniqueId.trim());
		
		String recordStatus = record.get(colIndex++);
		pendingCertifiedProduct.setRecordStatus(recordStatus);
		
		//practice type
		String practiceType = record.get(colIndex++);
		pendingCertifiedProduct.setPracticeType(practiceType.trim());
		PracticeTypeDTO foundPracticeType = practiceTypeDao.getByName(practiceType);
		if(foundPracticeType != null) {
			pendingCertifiedProduct.setPracticeTypeId(foundPracticeType.getId());
		}
		
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
			pendingCertifiedProduct.getErrorMessages().add("No certification body with name " + acbName.trim() + " could be found.");
		}
		
		//testing lab
		String atlName = record.get(colIndex++);
		pendingCertifiedProduct.setTestingLabName(atlName.trim());
		TestingLabDTO foundAtl = atlDao.getByName(atlName.trim());
		if(foundAtl != null) {
			pendingCertifiedProduct.setTestingLabId(foundAtl.getId());
		}	
		
		//product classification
		String classification = record.get(colIndex++);
		pendingCertifiedProduct.setProductClassificationName(classification.trim());
		ProductClassificationTypeDTO foundClassification = classificationDao.getByName(classification.trim());
		if(foundClassification != null) {
			pendingCertifiedProduct.setProductClassificationId(foundClassification.getId());
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
		
		//report file location
		pendingCertifiedProduct.setReportFileLocation(record.get(colIndex++));	
		
		//sed report link
		pendingCertifiedProduct.setSedReportFileLocation(record.get(colIndex++));
		
		String hasQmsStr = record.get(colIndex++);
		Boolean hasQms = asBoolean(hasQmsStr);
		if(hasQms != null) {
			pendingCertifiedProduct.setHasQms(hasQms.booleanValue());
		}
		
		//qms standards
		colIndex++;
		//qms modification
		colIndex++;
		
		String hasIcsStr = record.get(colIndex++);
		pendingCertifiedProduct.setIcs(asBoolean(hasIcsStr));

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
	}
	
	private void parseQms(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
		int colIndex = 23;
		if(!StringUtils.isEmpty(record.get(colIndex))) {
			String qmsStandardName = record.get(colIndex++).toString();
			QmsStandardDTO qmsStandard = qmsDao.getByName(qmsStandardName.trim());
			String qmsMods = record.get(colIndex).toString();
			
			PendingCertifiedProductQmsStandardEntity qmsEntity = new PendingCertifiedProductQmsStandardEntity();
			qmsEntity.setMappedProduct(pendingCertifiedProduct);
			qmsEntity.setModification(qmsMods.trim());
			qmsEntity.setName(qmsStandardName.trim());
			if(qmsStandard != null) {
				qmsEntity.setQmsStandardId(qmsStandard.getId());
			}
			pendingCertifiedProduct.getQmsStandards().add(qmsEntity);
		} 
	}
	
	private void parseCqms(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
		int cqmNameIndex = 28;
		int cqmVersionIndex = 29;
		
		String cqmName = record.get(cqmNameIndex).trim();
		String cqmVersions = record.get(cqmVersionIndex).trim();
		
		List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion(pendingCertifiedProduct, cqmName, cqmVersions);
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
						currIndex += 4; 
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
					case "SED":
						cert.setSed(asBoolean(firstRow.get(currIndex++).toString()));
						
						PendingCertificationResultUcdProcessEntity ucd = new PendingCertificationResultUcdProcessEntity();
						String ucdProcessName = firstRow.get(currIndex++).toString();
						if(cert.getSed().equals(Boolean.TRUE) && StringUtils.isEmpty(ucdProcessName)) {
							pendingCertifiedProduct.getErrorMessages().add(pendingCertifiedProduct.getUniqueId() + " indicates SED should be present but no UCD was entered.");
						} else if(cert.getSed().equals(Boolean.FALSE) && !StringUtils.isEmpty(ucdProcessName)) {
							pendingCertifiedProduct.getErrorMessages().add(pendingCertifiedProduct.getUniqueId() + " indicates SED is not present but a UCD process was entered.");
						}
						
						ucd.setUcdProcessName(ucdProcessName);
						ucd.setUcdProcessDetails(firstRow.get(currIndex++).toString());
						UcdProcessDTO dto = ucdDao.getByName(ucd.getUcdProcessName());
						if(dto != null) {
							ucd.setUcdProcessId(dto.getId());
						}
						cert.getUcdProcesses().add(ucd);
						break;
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
				tsEntity.setTestStandardNumber(tsValue);
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
			if(!StringUtils.isEmpty(tfColumn)) {
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
		int nonCpSourceColumn = asColumnBegin+2;
		
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
				cert.getAdditionalSoftware().add(asEntity);
			} 
			String nonCpSourceValue = row.get(nonCpSourceColumn).toString();
			if(!StringUtils.isEmpty(nonCpSourceValue)) {
				PendingCertificationResultAdditionalSoftwareEntity asEntity = new PendingCertificationResultAdditionalSoftwareEntity();
				asEntity.setSoftwareName(nonCpSourceValue);
				asEntity.setSoftwareVersion(row.get(nonCpSourceColumn+1).toString());
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
	protected List<PendingCqmCriterionEntity> handleCqmCmsCriterion(PendingCertifiedProductEntity product, String criterionNum, String version) {
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
					product.getErrorMessages().add("Could not find a CQM CMS criterion matching " + criterionNum + " and version " + currVersion);
				}
				
				PendingCqmCriterionEntity currResult = new PendingCqmCriterionEntity();
				currResult.setMappedCriterion(cqmEntity);
				currResult.setMeetsCriteria(true);	
				result.add(currResult);
			}
		}

		return result;
	}
}
