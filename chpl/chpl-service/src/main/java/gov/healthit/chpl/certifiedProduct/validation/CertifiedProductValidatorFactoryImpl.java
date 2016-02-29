package gov.healthit.chpl.certifiedProduct.validation;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

@Service
public class CertifiedProductValidatorFactoryImpl implements CertifiedProductValidatorFactory {

	private static final Logger logger = LogManager.getLogger(CertifiedProductValidatorFactoryImpl.class);

	private static final String PRACTICE_TYPE_AMBULATORY = "AMBULATORY";
	private static final String PRACTICE_TYPE_INPATIENT = "INPATIENT";
	private static final String PRODUCT_CLASSIFICATION_MODULAR = "Modular EHR";
	private static final String PRODUCT_CLASSIFICATION_COMPLETE = "Complete EHR";
	
	@Autowired private AmbulatoryComplete2014Validator ambulatoryComplete2014Validator;
	@Autowired private AmbulatoryModular2014Validator ambulatoryModular2014Validator;
	@Autowired private InpatientComplete2014Validator inpatientComplete2014Validator;
	@Autowired private InpatientModular2014Validator inpatientModular2014Validator;
	@Autowired private CertifiedProduct2015Validator cp2015Validator;
	
	@Override
	public CertifiedProductValidator getValidator(PendingCertifiedProductDTO product) {		
		if(product.getCertificationEdition().equals("2014")) {
			if(product.getPracticeType().equalsIgnoreCase(PRACTICE_TYPE_AMBULATORY)) {
				if(product.getProductClassificationName().equalsIgnoreCase(PRODUCT_CLASSIFICATION_MODULAR)) {
					return ambulatoryModular2014Validator;
				} else if(product.getProductClassificationName().equalsIgnoreCase(PRODUCT_CLASSIFICATION_COMPLETE)) {
					return ambulatoryComplete2014Validator;
				} else {
					product.getErrorMessages().add("Cannot determine if the 2014 Ambulatory product is Modular or Complete so it cannot be validated.");
					logger.error("Cannot find validator for 2014, ambulatory, and classification '" + product.getProductClassificationName() + "'.");
				}
			} else if(product.getPracticeType().equalsIgnoreCase(PRACTICE_TYPE_INPATIENT)) {
				if(product.getProductClassificationName().equalsIgnoreCase(PRODUCT_CLASSIFICATION_MODULAR)) {
					return inpatientModular2014Validator;
				} else if(product.getProductClassificationName().equalsIgnoreCase(PRODUCT_CLASSIFICATION_COMPLETE)) {
					return inpatientComplete2014Validator;
				} else {
					product.getErrorMessages().add("Cannot determine if the 2014 Inpatient product is Modular or Complete so it cannot be validated.");
					logger.error("Cannot find validator for 2014, inpatient, and classification '" + product.getProductClassificationName() + "'.");
				}
			} else {
				product.getErrorMessages().add("This 2014 product is neither Ambulatory nor Inpatient so it cannot be validated.");
				logger.error("Cannot find validator for practice type '" + product.getPracticeType() + "'");
			}
		} else if(product.getCertificationEdition().equals("2015")) {
			return cp2015Validator;
		} else {
			product.getErrorMessages().add("Cannot determine if this product is from 2014 or 2015 so it cannot be validated.");
			logger.error("Cannot find validator for certificatoin edition '" + product.getCertificationEdition() + "'.");
		}
		return null;
	}

	@Override
	public CertifiedProductValidator getValidator(CertifiedProductSearchDetails product) {
		String productCertificationEdition = product.getCertificationEdition().get("name").toString();
		if(productCertificationEdition != null && productCertificationEdition.equals("2014")) {
			String practiceTypeName = product.getPracticeType().get("name").toString();
			String productClassificationName = product.getClassificationType().get("name").toString();
			
			if(StringUtils.isEmpty(practiceTypeName) || StringUtils.isEmpty(productClassificationName)) {
				product.getErrorMessages().add("Cannot determine if this product is Ambulatory or Inpatient so it cannot be validated.");
				logger.error("Cannot validate a product that doesn't have practice type and/or classification specified.");
				return null;
			}
			
			if(practiceTypeName.equalsIgnoreCase(PRACTICE_TYPE_AMBULATORY)) {
				if(productClassificationName.equalsIgnoreCase(PRODUCT_CLASSIFICATION_MODULAR)) {
					return ambulatoryModular2014Validator;
				} else if(productClassificationName.equalsIgnoreCase(PRODUCT_CLASSIFICATION_COMPLETE)) {
					return ambulatoryComplete2014Validator;
				} else {
					product.getErrorMessages().add("Cannot determine if the 2014 Ambulatory product is Modular or Complete so it cannot be validated.");
					logger.error("Cannot find validator for 2014, ambulatory, and classification '" + productClassificationName + "'.");
				}
			} else if(practiceTypeName.equalsIgnoreCase(PRACTICE_TYPE_INPATIENT)) {
				if(productClassificationName.equalsIgnoreCase(PRODUCT_CLASSIFICATION_MODULAR)) {
					return inpatientModular2014Validator;
				} else if(productClassificationName.equalsIgnoreCase(PRODUCT_CLASSIFICATION_COMPLETE)) {
					return inpatientComplete2014Validator;
				} else {
					product.getErrorMessages().add("Cannot determine if the 2014 Inpatient product is Modular or Complete so it cannot be validated.");
					logger.error("Cannot find validator for 2014, inpatient, and classification '" + productClassificationName + "'.");
				}
			} else {
				product.getErrorMessages().add("This 2014 product is not Inpatient or Ambulatory so it cannot be validated.");
				logger.error("Cannot find validator for practice type '" + product.getPracticeType() + "'");
			}
		} else if(productCertificationEdition != null && productCertificationEdition.equals("2015")) {
			return cp2015Validator;
		} else {
			product.getErrorMessages().add("The product is not listed as 2014 or 2015 so it cannot be validated.");
			logger.info("Cannot find validator for certification edition '" + product.getCertificationEdition() + "'.");
		}
		return null;
	}
}
