package gov.healthit.chpl.certifiedProduct.upload;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.ProductClassificationTypeDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.CQMCriterionEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Component("certifiedProductHandler2015")
public class CertifiedProductHandler2015 extends CertifiedProductHandler {
	
	private static final Logger logger = LogManager.getLogger(CertifiedProductHandler2015.class);
	
	public PendingCertifiedProductEntity handle() {
		PendingCertifiedProductEntity pendingCertifiedProduct = new PendingCertifiedProductEntity();
		pendingCertifiedProduct.setStatus(getDefaultStatusId());
		
		int colIndex = 0;
		//blank row
		String uniqueId = getRecord().get(colIndex++);
		if(StringUtils.isEmpty(uniqueId)) {
			return null;
		}
		pendingCertifiedProduct.setUniqueId(uniqueId);
		//TODO: parse this apart by the "."s to get the individual components
		
		String recordStatus = getRecord().get(colIndex++);
		pendingCertifiedProduct.setRecordStatus(recordStatus);
		
		//practice type
		String practiceType = getRecord().get(colIndex++);
		pendingCertifiedProduct.setPracticeType(practiceType);
		PracticeTypeDTO foundPracticeType = practiceTypeDao.getByName(practiceType);
		if(foundPracticeType != null) {
			pendingCertifiedProduct.setPracticeTypeId(foundPracticeType.getId());
		}
		
		//developer, product, version
		String developer = getRecord().get(colIndex++);
		String product = getRecord().get(colIndex++);
		String productVersion = getRecord().get(colIndex++);
		pendingCertifiedProduct.setDeveloperName(developer);
		pendingCertifiedProduct.setProductName(product);
		pendingCertifiedProduct.setProductVersion(productVersion);
		
		DeveloperDTO foundDeveloper = developerDao.getByName(developer);
		if(foundDeveloper != null) {
			pendingCertifiedProduct.setDeveloperId(foundDeveloper.getId());
			
			//product
			ProductDTO foundProduct = productDao.getByDeveloperAndName(foundDeveloper.getId(), product);
			if(foundProduct != null) {
				pendingCertifiedProduct.setProductId(foundProduct.getId());
				
				//version
				ProductVersionDTO foundVersion = versionDao.getByProductAndVersion(foundProduct.getId(), productVersion);
				if(foundVersion != null) {
					pendingCertifiedProduct.setProductVersionId(foundVersion.getId());
				}
			}
		}		
		
		//certification year
		String certificaitonYear = getRecord().get(colIndex++);
		pendingCertifiedProduct.setCertificationEdition(certificaitonYear);
		CertificationEditionDTO foundEdition = editionDao.getByYear(certificaitonYear);
		if(foundEdition != null) {
			pendingCertifiedProduct.setCertificationEditionId(new Long(foundEdition.getId()));
		}
		
		//acb certification id
		pendingCertifiedProduct.setAcbCertificationId(getRecord().get(colIndex++));
		
		//certification body
		String acbName = getRecord().get(colIndex++);
		pendingCertifiedProduct.setCertificationBodyName(acbName);
		CertificationBodyDTO foundAcb = acbDao.getByName(acbName);
		if(foundAcb != null) {
			pendingCertifiedProduct.setCertificationBodyId(foundAcb.getId());
		}
		
		//product classification
		String classification = getRecord().get(colIndex++);
		pendingCertifiedProduct.setProductClassificationName(classification);
		ProductClassificationTypeDTO foundClassification = classificationDao.getByName(classification);
		if(foundClassification != null) {
			pendingCertifiedProduct.setProductClassificationId(foundClassification.getId());
		}
		
		//TODO: column 10 is some sort of "module", what is that?
		String module = getRecord().get(colIndex++);
		pendingCertifiedProduct.setProductClassificationModule(module);
		
		//certification date
		String dateStr = getRecord().get(colIndex++);
		try {
			Date certificationDate = dateFormatter.parse(dateStr);
			pendingCertifiedProduct.setCertificationDate(certificationDate);
		} catch(ParseException ex) {
			pendingCertifiedProduct.setCertificationDate(null);
		}
		
		//developer address info
		String developerStreetAddress = getRecord().get(colIndex++);
		String developerState = getRecord().get(colIndex++);
		String developerCity = getRecord().get(colIndex++);
		String developerZipcode = getRecord().get(colIndex++);
		String developerWebsite = getRecord().get(colIndex++);
		String developerEmail = getRecord().get(colIndex++);
		pendingCertifiedProduct.setDeveloperStreetAddress(developerStreetAddress);
		pendingCertifiedProduct.setDeveloperCity(developerCity);
		pendingCertifiedProduct.setDeveloperState(developerState);
		pendingCertifiedProduct.setDeveloperZipCode(developerZipcode);
		pendingCertifiedProduct.setDeveloperWebsite(developerWebsite);
		pendingCertifiedProduct.setDeveloperEmail(developerEmail);
		
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
		
		//additional software
		String additionalSoftware = getRecord().get(colIndex++);
		//not querying the database for this because each row is unique to a certified product,
		//so we will have to insert a row for this no matter what if it gets confirmed and moved 
		//into the certified_product table
		pendingCertifiedProduct.setAdditionalSoftware(additionalSoftware);
		
		//notes (field 19) - only for record status of update or delete
		String notes = getRecord().get(colIndex++);
		pendingCertifiedProduct.setUploadNotes(notes);
		
		//report file location
		pendingCertifiedProduct.setReportFileLocation(getRecord().get(colIndex++));
		
		//the three new (optional) fields
		if(getHeading().size() == CertifiedProductUploadHandlerFactoryImpl.NUM_FIELDS_2014_EXTENDED) {
			pendingCertifiedProduct.setIcs(getRecord().get(colIndex++));
			
			String sedData = getRecord().get(colIndex++);
			if(!StringUtils.isEmpty(sedData)) {
				sedData = sedData.trim();
				if(sedData.equalsIgnoreCase("TRUE") || sedData.equals("1")) {
					pendingCertifiedProduct.setSedTesting(Boolean.TRUE);
				}
			}
			if(pendingCertifiedProduct.getSedTesting() == null) {
				pendingCertifiedProduct.setSedTesting(Boolean.FALSE);
			}
			
			String qmsData = getRecord().get(colIndex++);
			if(!StringUtils.isEmpty(qmsData)) {
				qmsData = qmsData.trim();
				if(qmsData.equalsIgnoreCase("TRUE") || qmsData.equals("1")) {
					pendingCertifiedProduct.setQmsTesting(Boolean.TRUE);
				}
			}
			if(pendingCertifiedProduct.getQmsTesting() == null) {
				pendingCertifiedProduct.setQmsTesting(Boolean.FALSE);
			}
		}
		
		//skip 2011 certifications
		colIndex += 45;
		
		//skip NQF criterion for 2014
		colIndex += 59;

		//more certification criterion
		//starts at column DV
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(4)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(5)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(6)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(7)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(8)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(9)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(10)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(11)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(12)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(13)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(14)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(15)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(16)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(17)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(18)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(19)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (a)(20)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (b)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (b)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (b)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (b)(4)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			if(pendingCertifiedProduct.getPracticeType().equalsIgnoreCase(PRACTICE_TYPE_AMBULATORY)) {
				pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (b)(5)(A)", colIndex++));
			} else if(pendingCertifiedProduct.getPracticeType().equalsIgnoreCase(PRACTICE_TYPE_INPATIENT)) {
				pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (b)(5)(B)", colIndex++));
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (b)(6)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (b)(7)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (b)(8)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (b)(9)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (c)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (c)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (c)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (d)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (d)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (d)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (d)(4)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (d)(5)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (d)(6)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (d)(7)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (d)(8)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (d)(9)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (e)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (e)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (e)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (f)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (f)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (f)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (f)(4)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (f)(5)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (f)(6)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (f)(7)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (g)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (g)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (g)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (g)(4)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (h)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (h)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314 (h)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		//ends on GA
		
		//CMS criterion
		//begins on GB
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS2", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS9V1", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS22", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS26V1", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}		
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS30", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}		
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS31", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}		
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS32", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS50", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS52", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS53", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS55", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}		
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS56", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS60", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS61", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS62", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS64", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS65", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS66", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS68", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS69", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS71", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS72", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS73", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS74", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS75", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS77", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS78", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS90", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS91", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}		
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS100", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS102", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS104", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS105", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS107", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS108", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS109", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS110", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS111", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS113", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS114", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS117", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS122", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS123", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS124", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS125", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS126", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS127", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS128", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS129", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS130", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS131", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS132", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS133", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS134", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS135", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS136", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS137", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS138", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS139", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS140", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS141", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS142", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS143", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS144", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS145", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS146", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS147", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS148", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS149", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS153", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS154", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS155", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS156", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS157", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS158", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS159", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS160", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS161", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS163", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS164", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS165", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS166", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS167", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS169", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS171", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS172", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS177", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS178", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS179", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS182", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS185", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS188", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion("CMS190", colIndex++);
			for(PendingCqmCriterionEntity entity : criterion) {
				if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
					pendingCertifiedProduct.getCqmCriterion().add(entity);
				}
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		
		return pendingCertifiedProduct;
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
	protected List<PendingCqmCriterionEntity> handleCqmCmsCriterion(String criterionNum, int column) throws InvalidArgumentsException {
		String version = getRecord().get(column);
		if(version != null) {
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
				
				CQMCriterionEntity cqmEntity = cqmDao.getCMSEntityByNumberAndVersion(criterionNum, currVersion);
				if(cqmEntity == null) {
					throw new InvalidArgumentsException("Could not find a CQM CMS criterion matching " + criterionNum + " and version " + currVersion);
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
