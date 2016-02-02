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

@Component("certifiedProductHandler2011")
public class CertifiedProductHandler2011 extends CertifiedProductHandler {
	
	private static final Logger logger = LogManager.getLogger(CertifiedProductHandler2011.class);

	
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
		
		//certificaiton criterion 
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(a)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(b)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(c)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(d)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(e)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(f)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(f)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(f)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(g)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(h)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(i)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(j)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(k)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(l)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(m)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(n)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(o)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(p)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(q)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(r)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(s)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(t)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(u)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(v)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.302(w)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.304(a)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.304(b)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.304(c)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.304(d)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.304(e)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.304(f)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.304(g)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.304(h)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.304(i)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.304(j)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.306(a)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.306(b)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.306(c)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.306(d)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.306(d)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.306(e)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.306(f)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.306(g)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.306(h)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.306(i)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		
//		//cqm criterion
//		//starts at BO
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0001", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}			
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0002", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0004", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0012", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0013", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0014", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0018", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0024", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0027", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0028", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }		
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0031", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0032", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0033", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0034", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0036", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0038", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0041", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0043", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0047", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0052", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0055", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0056", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0059", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0061", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0062", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0064", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0067", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0068", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0070", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0073", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0074", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0075", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0081", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0083", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0084", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0086", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0088", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0089", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0105", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0371", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0372", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0373", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0374", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0375", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0376", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0385", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0387", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0389", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0421", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0435", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0436", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0437", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0438", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0439", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0440", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0441", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0495", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0497", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("0575", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		
		//skip 2014 certifications
		
		//skip 2014 CQM criterion
		
		return pendingCertifiedProduct;
	}
	
	public List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms) {
		List<CQMCriterion> criteria = new ArrayList<CQMCriterion>();
		for (CQMCriterion criterion : allCqms) {
			if (StringUtils.isEmpty(criterion.getCmsId())) {
				criteria.add(criterion);
			}
		}
		return criteria;
	}
	
	/**
	 * look up an NQF type of CQM by name/number. throw an error if we can't find it
	 * @param criterionNum
	 * @param column
	 * @return
	 * @throws InvalidArgumentsException
	 */
	protected PendingCqmCriterionEntity handleCqmNqfCriterion(String criterionNum, int column) throws InvalidArgumentsException {
		CQMCriterionEntity cqmEntity = cqmDao.getNQFEntityByNumber(criterionNum);
		if(cqmEntity == null) {
			throw new InvalidArgumentsException("Could not find a CQM NQF criterion matching " + criterionNum);
		}
		
		PendingCqmCriterionEntity result = new PendingCqmCriterionEntity();
		result.setMappedCriterion(cqmEntity);
		result.setMeetsCriteria(asBoolean(getRecord().get(column)));		
		return result;
	}
}
