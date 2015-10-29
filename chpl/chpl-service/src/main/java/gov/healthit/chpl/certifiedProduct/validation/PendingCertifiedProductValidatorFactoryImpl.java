package gov.healthit.chpl.certifiedProduct.validation;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

@Service
public class PendingCertifiedProductValidatorFactoryImpl implements PendingCertifiedProductValidatorFactory {

	private static final Logger logger = LogManager.getLogger(PendingCertifiedProductValidatorFactoryImpl.class);

	private static final String PRACTICE_TYPE_AMBULATORY = "AMBULATORY";
	private static final String PRACTICE_TYPE_INPATIENT = "INPATIENT";
	private static final String PRODUCT_CLASSIFICATION_MODULAR = "Modular EHR";
	private static final String PRODUCT_CLASSIFICATION_COMPLETE = "Complete EHR";
	
	@Autowired private AmbulatoryComplete2014Validator ambulatoryComplete2014Validator;
	@Autowired private AmbulatoryModular2014Validator ambulatoryModular2014Validator;
	@Autowired private InpatientComplete2014Validator inpatientComplete2014Validator;
	@Autowired private InpatientModular2014Validator inpatientModular2014Validator;
	
	@Override
	public PendingCertifiedProductValidator getValidator(PendingCertifiedProductDTO product) {
		if(product.getCertificationEdition().equals("2014")) {
			if(product.getPracticeType().equalsIgnoreCase(PRACTICE_TYPE_AMBULATORY)) {
				if(product.getProductClassificationName().equalsIgnoreCase(PRODUCT_CLASSIFICATION_MODULAR)) {
					return ambulatoryModular2014Validator;
				} else if(product.getProductClassificationName().equalsIgnoreCase(PRODUCT_CLASSIFICATION_COMPLETE)) {
					return ambulatoryComplete2014Validator;
				} else {
					logger.error("Cannot find validator for 2014, ambulatory, and classification '" + product.getProductClassificationName() + "'.");
				}
			} else if(product.getPracticeType().equalsIgnoreCase(PRACTICE_TYPE_INPATIENT)) {
				if(product.getProductClassificationName().equalsIgnoreCase(PRODUCT_CLASSIFICATION_MODULAR)) {
					return inpatientModular2014Validator;
				} else if(product.getProductClassificationName().equalsIgnoreCase(PRODUCT_CLASSIFICATION_COMPLETE)) {
					return inpatientComplete2014Validator;
				} else {
					logger.error("Cannot find validator for 2014, inpatient, and classification '" + product.getProductClassificationName() + "'.");
				}
			} else {
				logger.error("Cannot find validator for practice type '" + product.getPracticeType() + "'");
			}
		} else {
			logger.error("Cannot find validator for certificatoin edition '" + product.getCertificationEdition() + "'.");
		}
		return null;
	}

}
