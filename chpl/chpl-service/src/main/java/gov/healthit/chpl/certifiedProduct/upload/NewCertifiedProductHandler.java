package gov.healthit.chpl.certifiedProduct.upload;

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.ProductClassificationTypeDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.PendingCertificationCriterionEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Component("newCertifiedProductHandler")
public abstract class NewCertifiedProductHandler extends CertifiedProductUploadHandlerImpl {
	private static final Logger logger = LogManager.getLogger(NewCertifiedProductHandler.class);
	
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
		
		//vendor, product, version
		String vendor = getRecord().get(colIndex++);
		String product = getRecord().get(colIndex++);
		String productVersion = getRecord().get(colIndex++);
		pendingCertifiedProduct.setVendorName(vendor);
		pendingCertifiedProduct.setProductName(product);
		pendingCertifiedProduct.setProductVersion(productVersion);
		
		VendorDTO foundVendor = vendorDao.getByName(vendor);
		if(foundVendor != null) {
			pendingCertifiedProduct.setVendorId(foundVendor.getId());
			
			//product
			ProductDTO foundProduct = productDao.getByVendorAndName(foundVendor.getId(), product);
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
		
		//vendor address info
		String vendorStreetAddress = getRecord().get(colIndex++);
		String vendorState = getRecord().get(colIndex++);
		String vendorCity = getRecord().get(colIndex++);
		String vendorZipcode = getRecord().get(colIndex++);
		String vendorWebsite = getRecord().get(colIndex++);
		String vendorEmail = getRecord().get(colIndex++);
		pendingCertifiedProduct.setVendorStreetAddress(vendorStreetAddress);
		pendingCertifiedProduct.setVendorCity(vendorCity);
		pendingCertifiedProduct.setVendorState(vendorState);
		pendingCertifiedProduct.setVendorZipCode(vendorZipcode);
		pendingCertifiedProduct.setVendorWebsite(vendorWebsite);
		pendingCertifiedProduct.setVendorEmail(vendorEmail);
		
		AddressDTO toFind = new AddressDTO();
		toFind.setStreetLineOne(vendorStreetAddress);
		toFind.setCity(vendorCity);
		toFind.setRegion(vendorState);
		//TODO: what about zip code? ... it's pretty unlikely we'll match any existing addresses
		//since the columns are different
		AddressDTO foundAddress = addressDao.getByValues(toFind);
		if(foundAddress != null) {
			AddressEntity addressEntity = null;
			try {
				addressEntity = addressDao.getEntityById(foundAddress.getId());
			} catch(EntityRetrievalException ex) {
				addressEntity = null;
			}
			pendingCertifiedProduct.setVendorAddress(addressEntity);
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
		
		//cqm criterion
		//starts at BO
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0001(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}			
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0002(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0004(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0012(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0013(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0014(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0018(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0024(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0027(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0031(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0032(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0033(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0034(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0036(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0038(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0041(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0043(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0047(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0052(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0055(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0056(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0059(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0061(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0062(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0064(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0067(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0068(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0070(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0073(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0074(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0075(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0081(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0083(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0084(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0086(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0088(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0089(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0105(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0371(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0372(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0373(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0374(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0375(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0376(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0385(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0387(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0389(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0421(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0435(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0436(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0437(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0438(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0439(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0440(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0441(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0495(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0497(I)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmNqfCriterion("NQF 0575(A)", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		
		//more certification crierion
		//starts at column DV
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(4)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(5)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(6)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(7)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(8)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(9)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(10)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(11)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(12)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(13)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(14)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(15)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(16)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(17)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(18)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(19)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(a)(20)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(b)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(b)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(b)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(b)(4)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(b)(5)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(b)(6)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(b)(7)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(b)(8)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(b)(9)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(c)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(c)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(c)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(d)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(d)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(d)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(d)(4)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(d)(5)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(d)(6)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(d)(7)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(d)(8)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(d)(9)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(e)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(e)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(e)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(f)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(f)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(f)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(f)(4)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(f)(5)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(f)(6)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(f)(7)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(g)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(g)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(g)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(g)(4)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(h)(1)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(h)(2)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			pendingCertifiedProduct.getCertificationCriterion().add(handleCertificationCriterion("170.314(h)(3)", colIndex++));
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		//ends on GA
		
		//CMS criterion
		//begins on GB
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS2", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS9V1", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}		
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS22", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}		
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS26V1", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}		
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS30", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}		
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS31", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}		
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS32", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}		
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS50", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS52", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS53", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS55", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}		
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS56", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}	
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS60", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS61", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS62", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS64", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS65", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS66", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS68", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS69", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS71", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS72", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS73", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS74", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS75", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS77", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS78", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS90", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS91", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}		
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS100", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS102", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS104", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS105", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS107", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS108", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS109", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS110", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS111", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS113", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS114", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS117", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS122", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS123", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS124", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS125", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS126", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS127", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS128", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS129", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS130", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS131", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS132", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS133", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS134", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS135", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS136", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS137", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS138", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS139", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS140", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS141", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS142", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS143", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS144", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS145", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS146", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS147", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS148", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS149", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS153", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS154", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS155", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS156", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS157", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS158", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS159", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS160", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS161", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS163", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS164", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS165", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS166", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS167", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS169", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS171", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS172", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS177", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS178", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS179", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS182", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS185", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS188", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		try {
			PendingCqmCriterionEntity entity = handleCqmCmsCriterion("CMS190", colIndex++);
			if(entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
				pendingCertifiedProduct.getCqmCriterion().add(entity);
			}
		} catch(InvalidArgumentsException ex) { logger.error(ex.getMessage()); }
		
		return pendingCertifiedProduct;
	}
	
	public Long getDefaultStatusId() {
		CertificationStatusDTO statusDto = statusDao.getByStatusName("Pending");
		if(statusDto != null) {
			return statusDto.getId();
		}
		return null;
	}
	
	/**
	 * look up the certification criteria by name and throw an error if we can't find it
	 * @param criterionName
	 * @param column
	 * @return
	 * @throws InvalidArgumentsException
	 */
	private PendingCertificationCriterionEntity handleCertificationCriterion(String criterionName, int column) throws InvalidArgumentsException {
		CertificationCriterionEntity certEntity = certDao.getEntityByName(criterionName);
		if(certEntity == null) {
			throw new InvalidArgumentsException("Could not find a certification criterion matching " + criterionName);
		}
		
		PendingCertificationCriterionEntity result = new PendingCertificationCriterionEntity();		
		result.setMappedCriterion(certEntity);
		boolean meetsCriteria = false;
		if(!StringUtils.isEmpty(getRecord().get(column))) {
			int value = new Integer(getRecord().get(column)).intValue();
			if(value > 0) {
				meetsCriteria = true;
			}
		}
		result.setMeetsCriteria(meetsCriteria);		
		return result;
	}
	
	public abstract PendingCqmCriterionEntity handleCqmNqfCriterion(String criterionNum, int column) throws InvalidArgumentsException;
	public abstract PendingCqmCriterionEntity handleCqmCmsCriterion(String criterionNum, int column) throws InvalidArgumentsException;
}