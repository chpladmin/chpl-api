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
	
	@Override
	public CertifiedProductValidator getValidator(PendingCertifiedProductDTO product) {
		//TODO: 2015 validation
		/*g4 and g5 are required
		g3 is a two-way binding with column L (if you are certified to g3 you must have one or more from column L; if you are certified for anything from L you must have g3)
		g1 and g2 - if you are certified for either g1 or g2 you must have at least one cert from column L
		no restriction about having both g1 and g2
*/
		
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

	@Override
	public CertifiedProductValidator getValidator(CertifiedProductSearchDetails product) {
		String productCertificationEdition = product.getCertificationEdition().get("name").toString();
		if(productCertificationEdition != null && productCertificationEdition.equals("2014")) {
			String practiceTypeName = product.getPracticeType().get("name").toString();
			String productClassificationName = product.getClassificationType().get("name").toString();
			
			if(StringUtils.isEmpty(practiceTypeName) || StringUtils.isEmpty(productClassificationName)) {
				logger.error("Cannot validate a product that doesn't have practice type and/or classification specified.");
				return null;
			}
			
			if(practiceTypeName.equalsIgnoreCase(PRACTICE_TYPE_AMBULATORY)) {
				if(productClassificationName.equalsIgnoreCase(PRODUCT_CLASSIFICATION_MODULAR)) {
					return ambulatoryModular2014Validator;
				} else if(productClassificationName.equalsIgnoreCase(PRODUCT_CLASSIFICATION_COMPLETE)) {
					return ambulatoryComplete2014Validator;
				} else {
					logger.error("Cannot find validator for 2014, ambulatory, and classification '" + productClassificationName + "'.");
				}
			} else if(practiceTypeName.equalsIgnoreCase(PRACTICE_TYPE_INPATIENT)) {
				if(productClassificationName.equalsIgnoreCase(PRODUCT_CLASSIFICATION_MODULAR)) {
					return inpatientModular2014Validator;
				} else if(productClassificationName.equalsIgnoreCase(PRODUCT_CLASSIFICATION_COMPLETE)) {
					return inpatientComplete2014Validator;
				} else {
					logger.error("Cannot find validator for 2014, inpatient, and classification '" + productClassificationName + "'.");
				}
			} else {
				logger.error("Cannot find validator for practice type '" + product.getPracticeType() + "'");
			}
		} else {
			logger.info("Cannot find validator for certification edition '" + product.getCertificationEdition() + "'.");
		}
		return null;
	}
}
